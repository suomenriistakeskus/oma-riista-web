package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.MobileHarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.harvest.DeletedHarvestRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenAssertionBuilder;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.util.DateUtil.huntingYearBeginDate;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class MobileHarvestFeatureTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin, MobileHarvestDTOBuilderFactory {

    @Resource
    private MobileHarvestFeature feature;

    @Resource
    private HarvestRepository harvestRepo;

    @Resource
    private HarvestSpecimenRepository specimenRepo;

    @Resource
    private PersonRepository personRepo;

    @Resource
    private DeletedHarvestRepository deletedHarvestRepository;

    @Theory
    public void testCreateHarvest_whenHarvestSpecVersionIsNull(final HarvestSpecVersion specVersion) {
        assertThrows(NullPointerException.class, () -> {
            invokeCreateHarvest(newDTOBuilderAndFixtureForCreate(specVersion).withSpecVersion(null).build());
        });
    }

    @Theory
    public void testCreateHarvest_whenMobileClientRefIdIsNull(final HarvestSpecVersion specVersion) {
        assertThrows(ValidationException.class, () -> {
            invokeCreateHarvest(newDTOBuilderAndFixtureForCreate(specVersion).withMobileClientRefId(null).build());
        });
    }

    @Theory
    public void testCreateHarvest_whenGeoLocationSourceIsNull(final HarvestSpecVersion specVersion) {
        assertThrows(ValidationException.class, () -> {
            final MobileHarvestDTO dto = newDTOBuilderAndFixtureForCreate(specVersion).build();
            dto.getGeoLocation().setSource(null);

            invokeCreateHarvest(dto);
        });
    }

    @Theory
    public void testCreateHarvest(final HarvestSpecVersion specVersion) {
        final GameSpecies species = model().newGameSpecies();

        onSavedAndAuthenticated(createUserWithPerson(), user -> {

            final MobileHarvestDTO inputDto = create(specVersion, species).build();
            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
        });
    }

    @Theory
    public void testCreateHarvest_withSpecimens(final HarvestSpecVersion specVersion) {
        final GameSpecies species = model().newGameSpecies(true);

        onSavedAndAuthenticated(createUserWithPerson(), user -> {

            final MobileHarvestDTO inputDto = create(specVersion, species, 10).build();
            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
        });
    }

    @Theory
    public void testCreateHarvest_whenSpecimensIsNull(final HarvestSpecVersion specVersion) {
        final GameSpecies species = model().newGameSpecies();

        onSavedAndAuthenticated(createUserWithPerson(), user -> {

            final MobileHarvestDTO inputDto = create(specVersion, species, 10).withSpecimens(null).build();
            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
        });
    }

    @Theory
    public void testCreateHarvest_withMobileClientRefIdAssociatedWithExistingHarvest(final HarvestSpecVersion specVersion) {
        withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newMobileHarvest(species, author);
            final GameSpecies newSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(author), () -> {

                invokeCreateHarvest(create(specVersion, harvest, newSpecies).mutate().build());

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, species).build();

                doCreateAssertions(harvest.getId(), expectedValues, author);
            });
        });
    }

    @Theory
    public void testCreateHarvest_withMobileClientRefIdAssociatedWithExistingHarvestOfOtherPerson(final HarvestSpecVersion specVersion) {
        final GameSpecies species = model().newGameSpecies();
        final Harvest harvestOfOtherPerson = model().newMobileHarvest(model().newPerson());

        onSavedAndAuthenticated(createUserWithPerson(), user -> {

            final MobileHarvestDTO inputDto = create(specVersion, species)
                    .withMobileClientRefId(harvestOfOtherPerson.getMobileClientRefId())
                    .build();

            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
        });
    }

    @Theory
    public void testCreateHarvest_withPointOfTimeInsideSeason(final HarvestSpecVersion specVersion) {
        model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final int huntingYear = DateUtil.huntingYear();
        final LocalDate seasonBegin = DateUtil.huntingYearBeginDate(huntingYear);
        final LocalDate seasonEnd = DateUtil.huntingYearEndDate(huntingYear);
        model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd.plusDays(7));

        onSavedAndAuthenticated(createUserWithPerson(), user -> {
            final MobileHarvestDTO inputDto = create(specVersion, species)
                    .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                    .build();

            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            if (specVersion.supportsHarvestReport()) {
                inputDto.setHarvestReportRequired(true);
                inputDto.setHarvestReportDone(true);
                inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            } else {
                inputDto.setHarvestReportRequired(true);
                inputDto.setHarvestReportDone(false);
                inputDto.setHarvestReportState(null);
            }

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
        });
    }

    @Theory
    @Ignore("TODO Fix this test. Currently unsure what it is actually about to test.")
    public void testCreateHarvest_whenHarvestReportAlwaysRequiredForSpecies(final HarvestSpecVersion specVersion) {
        withRhy(rhy -> withPerson(person -> {

            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, species, 1).build();

                final HarvestSpecimenDTO specimenDto = inputDto.getSpecimens().get(0);
                specimenDto.setGender(MALE);
                specimenDto.setAge(ADULT);
                specimenDto.setWeight(100.0);

                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                if (specVersion.supportsHarvestReport()) {
                    inputDto.setHarvestReportRequired(false);
                    inputDto.setHarvestReportDone(true);
                    inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                } else {
                    inputDto.setHarvestReportRequired(true);
                }

                doCreateAssertions(outputDto.getId(), inputDto, person);
            });
        }));
    }

    @Theory
    public void testCreateHarvest_withPermit(final HarvestSpecVersion specVersion) {
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newHarvestPermit();
        model().newHarvestPermitSpeciesAmount(permit, species);

        onSavedAndAuthenticated(createUserWithPerson(), user -> {

            final MobileHarvestDTO inputDto = create(specVersion, species)
                    .withPermitNumber(permit.getPermitNumber())
                    .withPermitType(permit.getPermitType())
                    .build();

            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson(), harvest -> {
                final HarvestPermit permit2 = harvest.getHarvestPermit();
                assertNotNull(permit2);
                assertEquals(permit.getPermitNumber(), permit2.getPermitNumber());
                assertEquals(permit.getPermitType(), permit2.getPermitType());
            });
        });
    }

    @Theory
    public void testCreateHarvest_withPermitWhenRhyNotFound(final HarvestSpecVersion specVersion) {
        assertThrows(RhyNotResolvableByGeoLocationException.class, () -> {

            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            persistAndAuthenticateWithNewUser(true);

            // everything else should be fine, but RHY is not found for location
            invokeCreateHarvest(create(specVersion, species)
                    .withPermitNumber(permit.getPermitNumber())
                    .withPermitType(permit.getPermitType())
                    .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                    .build());
        });
    }

    // Test integration to HarvestSpecimenOps and deer pilot status handling.
    @Theory
    public void testCreateHarvest_newMooseAntlerFields_startingFrom2020(final HarvestSpecVersion specVersion) {
        assumeTrue(specVersion.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        testCreateHarvest_mooseAntlerFields(2020, specVersion, (entity, dto) -> {

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2020Present()
                    .mooseFields2020EqualTo(dto)
                    .verify(entity);
        });
    }

    // Test integration to HarvestSpecimenOps and deer pilot status handling.
    @Theory
    public void testCreateHarvest_mooseAntlerFields_before2020(final HarvestSpecVersion specVersion) {
        assumeTrue(specVersion.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        testCreateHarvest_mooseAntlerFields(2019, specVersion, (entity, dto) -> {

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2015Present()
                    .mooseFields2017EqualTo(dto, specVersion)
                    .verify(entity);
        });
    }


    // Test integration to HarvestSpecimenOps and deer pilot status handling.
    @Theory
    public void testCreateHarvest_mooseAntlerFields_startingFrom2020_preSpecVersion8(final HarvestSpecVersion specVersion) {
        assumeTrue(specVersion.lessThan(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        testCreateHarvest_mooseAntlerFields(2020, specVersion, (entity, dto) -> {

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2015Present()
                    .mooseFields2017EqualTo(dto, specVersion)
                    .verify(entity);
        });
    }

    private void testCreateHarvest_mooseAntlerFields(final int huntingYear,
                                                     final HarvestSpecVersion specVersion,
                                                     final BiConsumer<HarvestSpecimen, HarvestSpecimenDTO> consumer) {

        final GameSpecies species = model().newGameSpeciesMoose();

        withHuntingGroupFixture(species, fixture -> {

            final Person author = fixture.groupMember;

            onSavedAndAuthenticated(createUser(author), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, species)
                        .withPointOfTime(huntingYearBeginDate(huntingYear).toLocalDateTime(LocalTime.now()))
                        .withSpecimen(ADULT_MALE)
                        .build();

                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, harvest -> {

                    final List<HarvestSpecimen> persistedSpecimens = specimenRepo.findByHarvest(harvest);
                    assertEquals(1, persistedSpecimens.size());

                    consumer.accept(persistedSpecimens.get(0), inputDto.getSpecimens().get(0));
                });
            });
        });
    }

    @Test
    public void testCreateHarvest_withHuntingClub() {

        final HuntingClub huntingClub = model().newHuntingClub();

        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newHarvestPermit();
        model().newHarvestPermitSpeciesAmount(permit, species);
        onSavedAndAuthenticated(createUserWithPerson(), user -> {

            final MobileHarvestDTO inputDto = create(HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_HARVEST_HUNTING_CLUB, species)
                    .withPermitNumber(permit.getPermitNumber())
                    .withPermitType(permit.getPermitType())
                    .withHuntingClub(huntingClub)
                    .build();

            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson(), harvest -> {
                assertThat(outputDto.getSelectedHuntingClub().getId(), is(huntingClub.getId()));
            });
        });
    }
    @Test
    public void testCreateHarvest_withHuntingClubAndOldSpecVersion() {

        final HuntingClub huntingClub = model().newHuntingClub();

        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newHarvestPermit();
        model().newHarvestPermitSpeciesAmount(permit, species);
        onSavedAndAuthenticated(createUserWithPerson(), user -> {

            final MobileHarvestDTO inputDto = create(HarvestSpecVersion._9, species)
                    .withPermitNumber(permit.getPermitNumber())
                    .withPermitType(permit.getPermitType())
                    .withHuntingClub(huntingClub)
                    .build();

            final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

            doCreateAssertions(outputDto.getId(), inputDto, user.getPerson(), harvest -> {
                assertThat(outputDto.getSelectedHuntingClub(), is(nullValue()));
            });
        });
    }

    @Theory
    public void testUpdateHarvest_whenHarvestSpecVersionIsNull(final HarvestSpecVersion specVersion) {
        assertThrows(NullPointerException.class, () -> {
            invokeUpdateHarvest(newDTOBuilderAndFixtureForUpdate(specVersion).withSpecVersion(null).build());
        });
    }

    @Theory
    public void testUpdateHarvest_whenGeoLocationSourceIsNull(final HarvestSpecVersion specVersion) {
        assertThrows(ValidationException.class, () -> {

            final MobileHarvestDTO dto = newDTOBuilderAndFixtureForUpdate(specVersion).build();
            dto.getGeoLocation().setSource(null);

            invokeUpdateHarvest(dto);
        });
    }

    @Theory
    public void testUpdateHarvest_whenSpecimensIsNull(final HarvestSpecVersion specVersion) {
        assertThrows(ValidationException.class, () -> {
            invokeUpdateHarvest(newDTOBuilderAndFixtureForUpdate(specVersion).withSpecimens(null).build());
        });
    }

    @Theory
    public void testUpdateHarvest_whenNoChanges(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest).withSpecimensMappedFrom(specimens).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 0);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_whenHarvestReportNotDone(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .withSpecimens(5)
                        .withAmount(7) // Greater than number of specimens
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_whenHarvestReportDone(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies(true);
            final Harvest harvest = model().newMobileHarvest(species, person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());

            final GameSpecies updatedSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .withSpecimens(emptyList())
                        .withAmount(1)
                        .build();

                invokeUpdateHarvest(dto);

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, species)
                        .withHarvestReportDone(true)
                        .withSpecimensMappedFrom(specimens)
                        .withDescription(dto.getDescription())
                        .build();

                doUpdateAssertions(expectedValues, person, 1);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_mobileClientRefIdShouldNotBeUpdated(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final Long originalMobileClientRefId = harvest.getMobileClientRefId();
                final MobileHarvestDTO dto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .withMobileClientRefId(originalMobileClientRefId + 1)
                        .build();

                invokeUpdateHarvest(dto);

                dto.setMobileClientRefId(originalMobileClientRefId);

                doUpdateAssertions(dto, person, 1);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_forSpecimensBeingPreserved(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .withSpecimensMappedFrom(specimens)
                        .withAmount(specimens.size())
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_whenUpdatingSpecimensOnly(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest)
                        .withSpecimensMappedFrom(specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_forAmountChange_whenSpecimensPresent(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest)
                        .withSpecimensMappedFrom(specimens)
                        .withAmount(specimens.size() + 10)
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_forAmountChange_whenSpecimensNotPresent(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest).withAmount(10).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_withPointOfTimeInsideSeason_noReport(final HarvestSpecVersion specVersion) {
        withPerson(person -> {
            model().newRiistanhoitoyhdistys();
            final GameSpecies species = model().newGameSpecies();
            final int huntingYear = DateUtil.huntingYear();
            final LocalDate seasonBegin = DateUtil.huntingYearBeginDate(huntingYear);
            final LocalDate seasonEnd = DateUtil.huntingYearEndDate(huntingYear);
            final HarvestSeason season = model().newHarvestSeason(
                    species, seasonBegin, seasonEnd, seasonEnd.plusDays(7));

            final Harvest harvest = model().newMobileHarvest(species, person);

            onSavedAndAuthenticated(createUser(person), user -> {
                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                        .build();

                final MobileHarvestDTO outputDto = invokeUpdateHarvest(inputDto);

                if (specVersion.supportsHarvestReport()) {
                    inputDto.setHarvestReportDone(true);
                    inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                    inputDto.setHarvestReportRequired(true);
                } else {
                    inputDto.setHarvestReportDone(false);
                    inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                    inputDto.setHarvestReportRequired(true);
                }

                doUpdateAssertions(outputDto, person, 1, h -> {
                    if (specVersion.supportsHarvestReport()) {
                        assertEquals(season, h.getHarvestSeason());
                    } else {
                        assertNull(h.getHarvestSeason());
                    }
                });
            });
        });
    }

    @Theory
    public void testUpdateHarvest_forAddingPermit(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit originalPermit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(originalPermit, species);

            final Harvest harvest = model().newMobileHarvest(species, person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest)
                        .withPermitNumber(originalPermit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1, h -> {
                    final HarvestPermit permit = h.getHarvestPermit();
                    assertNotNull(permit);
                    assertEquals(originalPermit.getPermitNumber(), permit.getPermitNumber());
                    assertEquals(originalPermit.getPermitType(), permit.getPermitType());
                });
            });
        });
    }

    @Theory
    public void testUpdateHarvest_withPermitWhenRhyNotFound(final HarvestSpecVersion specVersion) {
        assertThrows(RhyNotResolvableByGeoLocationException.class, () -> {

            withPerson(person -> {

                final GameSpecies species = model().newGameSpecies();

                final HarvestPermit permit = model().newHarvestPermit();
                model().newHarvestPermitSpeciesAmount(permit, species);

                final Harvest harvest = model().newMobileHarvest(species, person);

                // Everything else should be fine, but rhy is not found for location
                onSavedAndAuthenticated(createUser(person), () -> invokeUpdateHarvest(
                        create(specVersion, harvest)
                                .withPermitNumber(permit.getPermitNumber())
                                .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                                .build()));
            });
        });
    }

    @Theory
    public void testUpdateHarvest_forRemovingPermit(final HarvestSpecVersion specVersion) {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newMobileHarvest(species, person);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest)
                        .withPointOfTime(Optional
                                .ofNullable(harvest.getPointOfTime())
                                .map(DateTime::toLocalDateTime)
                                .map(ldt -> ldt.plusMinutes(1))
                                .orElse(null))
                        .withPermitNumber(null)
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1, h -> assertNull(h.getHarvestPermit()));
            });
        });
    }

    // Test that harvest is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Theory
    public void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard(final HarvestSpecVersion specVersion) {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, DateUtil.today());
            final Harvest harvest = model().newMobileHarvest(f.species, f.groupMember, huntingDay);

            final GameSpecies newSpecies = model().newDeerSubjectToClubHunting();

            onSavedAndAuthenticated(createUser(f.groupMember), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest, newSpecies).mutate().build();
                invokeUpdateHarvest(dto);

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, f.species)
                        .withDescription(dto.getDescription())
                        .build();

                doUpdateAssertions(expectedValues, f.groupMember, 1);
            });
        });
    }

    @Theory
    @Ignore("TODO Fix this test. Currently unsure what it is actually about to test.")
    public void testUpdateHarvest_whenHarvestChangedToRequired(final HarvestSpecVersion specVersion) {
        withRhy(rhy -> withPerson(person -> {
            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
            final Harvest harvest = model().newMobileHarvest(person);
            final HarvestSpecimen harvestSpecimen = model().newHarvestSpecimen(harvest, ADULT, MALE, 100.0);

            onSavedAndAuthenticated(createUser(person), () -> {
                // Check invariant
                assertFalse(harvest.isHarvestReportRequired());

                final MobileHarvestDTO inputDto = create(specVersion, harvest, species)
                        .withSpecimensMappedFrom(singletonList(harvestSpecimen))
                        .build();

                invokeUpdateHarvest(inputDto);

                if (specVersion.supportsHarvestReport()) {
                    inputDto.setHarvestReportRequired(false);
                    inputDto.setHarvestReportDone(true);
                    inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                } else {
                    inputDto.setHarvestReportRequired(true);
                }

                doUpdateAssertions(inputDto, person, 1);
            });
        }));
    }

    @Theory
    public void testUpdateHarvest_withHarvestAcceptedToPermit(final HarvestSpecVersion specVersion) {
        doTestUpdateHarvestWithPermit(specVersion, false, false, StateAcceptedToHarvestPermit.ACCEPTED);
    }

    @Theory
    public void testUpdateHarvest_withHarvestAcceptedToPermit_asContactPerson(final HarvestSpecVersion specVersion) {
        doTestUpdateHarvestWithPermit(specVersion, true, true, StateAcceptedToHarvestPermit.ACCEPTED);
    }

    @Theory
    public void testUpdateHarvest_withHarvestProposedToPermit(final HarvestSpecVersion specVersion) {
        doTestUpdateHarvestWithPermit(specVersion, false, true, StateAcceptedToHarvestPermit.PROPOSED);
    }

    @Theory
    public void testUpdateHarvest_withHarvestRejectedToPermit(final HarvestSpecVersion specVersion) {
        doTestUpdateHarvestWithPermit(specVersion, false, true, StateAcceptedToHarvestPermit.REJECTED);
    }

    private void doTestUpdateHarvestWithPermit(final HarvestSpecVersion specVersion,
                                               final boolean setAsOriginalContactPerson,
                                               final boolean businessFieldsUpdateable,
                                               final StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit) {

        withHarvestHavingPermitState(stateAcceptedToHarvestPermit, harvest -> {

            if (setAsOriginalContactPerson) {
                harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
            }

            final String permitNumber = harvest.getHarvestPermit().getPermitNumber();

            onSavedAndAuthenticated(createUser(harvest.getAuthor()), () -> {

                // Do not change species because test fixture has been setup for original species.
                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .mutate()
                        .withPermitNumber(permitNumber)
                        .build();

                invokeUpdateHarvest(inputDto);

                final boolean expectReportDone = setAsOriginalContactPerson &&
                        stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.ACCEPTED;
                final StateAcceptedToHarvestPermit expectedStateAfterUpdate =
                        stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.REJECTED
                                ? StateAcceptedToHarvestPermit.PROPOSED
                                : stateAcceptedToHarvestPermit;

                inputDto.setHarvestReportDone(expectReportDone);
                inputDto.setHarvestReportState(expectReportDone ? HarvestReportState.SENT_FOR_APPROVAL : null);
                inputDto.setHarvestReportRequired(expectReportDone);

                final MobileHarvestDTO expectedValues = businessFieldsUpdateable
                        ? inputDto
                        : create(specVersion, harvest, harvest.getSpecies())
                        .withDescription(inputDto.getDescription())
                        .build();

                doUpdateAssertions(expectedValues, harvest.getAuthor(), 1, h -> {
                    assertEquals(permitNumber, h.getHarvestPermit().getPermitNumber());
                    assertEquals(expectedStateAfterUpdate, h.getStateAcceptedToHarvestPermit());
                });
            });
        });
    }

    private void withHarvestHavingPermitState(final StateAcceptedToHarvestPermit state,
                                              final Consumer<Harvest> consumer) {

        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = model().newHarvestPermit(rhy, true);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newMobileHarvest(species, author, new LocalDate(2022, 8, 2));
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(state);
            harvest.setRhy(rhy);

            consumer.accept(harvest);
        }));
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportRejected() {
        withPerson(author -> {
            final Harvest harvest = model().newMobileHarvest(author);
            model().newHarvestSpecimen(harvest);

            harvest.setHarvestReportState(HarvestReportState.REJECTED);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());

            onSavedAndAuthenticated(createUser(author), () -> {
                final Throwable exception =
                        assertThrows(RuntimeException.class, () -> feature.deleteHarvest(harvest.getId()));

                assertEquals("Cannot delete harvest with an associated harvest report.", exception.getMessage());
            });
        });
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportApproved() {
        withPerson(author -> {
            final Harvest harvest = model().newMobileHarvest(author);
            model().newHarvestSpecimen(harvest);
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());

            onSavedAndAuthenticated(createUser(author), () -> {
                final Throwable exception =
                        assertThrows(RuntimeException.class, () -> feature.deleteHarvest(harvest.getId()));

                assertEquals("Cannot delete harvest with an associated harvest report.", exception.getMessage());
            });
        });
    }

    @Test
    public void testDeleteHarvest_whenAttachedToHuntingDay() {
        withMooseHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, DateUtil.today());
            final Harvest harvest = model().newMobileHarvest(f.species, f.groupMember, huntingDay);

            onSavedAndAuthenticated(createUser(f.groupMember), () -> {
                try {
                    feature.deleteHarvest(harvest.getId());
                    fail("Deletion of harvest associated with a hunting day should fail");
                } catch (final RuntimeException e) {
                    // Expected
                }
                assertNotNull(harvestRepo.findById(harvest.getId()));
            });
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
            doTestDeleteHarvest(true, harvest);
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            doTestDeleteHarvest(false, harvest);
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestProposedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.PROPOSED, harvest -> {
            doTestDeleteHarvest(true, harvest);
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestRejectedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.REJECTED, harvest -> {
            doTestDeleteHarvest(true, harvest);
        });
    }

    @Test
    public void testGetDeletedHarvestIds() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        final Long personId = user.getPerson().getId();
        model().newDeletedHarvest(now, 1L, personId, personId);

        onSavedAndAuthenticated(user, () -> {
            final DateTime deletionTime = now.minusSeconds(1);
            final MobileDeletedDiaryEntriesDTO ids = feature.getDeletedHarvestIds(ldt(deletionTime));
            Asserts.assertThat(ids.getEntryIds(), hasSize(1));
            Asserts.assertThat(ids.getEntryIds().get(0), equalTo(1L));
            Asserts.assertThat(ids.getLatestEntry(), equalTo(ldt(now)));
        });
    }

    @Test
    public void testGetDeletedHarvestIds_nullDate() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        final Long personId = user.getPerson().getId();
        model().newDeletedHarvest(now, 1L, personId, personId);

        onSavedAndAuthenticated(user, () -> {
            final MobileDeletedDiaryEntriesDTO ids = feature.getDeletedHarvestIds(null);
            Asserts.assertThat(ids.getEntryIds(), hasSize(1));
            Asserts.assertThat(ids.getEntryIds().get(0), equalTo(1L));
            Asserts.assertThat(ids.getLatestEntry(), equalTo(ldt(now)));
        });
    }

    @Test
    public void testGetDeletedHarvestIds_author() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        final Long personId = user.getPerson().getId();
        model().newDeletedHarvest(now, 1L, personId, personId + 1);

        onSavedAndAuthenticated(user, () -> {
            final DateTime deletionTime = now.minusSeconds(1);
            final MobileDeletedDiaryEntriesDTO ids = feature.getDeletedHarvestIds(ldt(deletionTime));
            Asserts.assertThat(ids.getEntryIds(), hasSize(1));
            Asserts.assertThat(ids.getEntryIds().get(0), equalTo(1L));
            Asserts.assertThat(ids.getLatestEntry(), equalTo(ldt(now)));
        });
    }

    @Test
    public void testGetDeletedHarvestIds_shooter() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        final Long personId = user.getPerson().getId();
        model().newDeletedHarvest(now, 1L, personId + 1, personId);

        onSavedAndAuthenticated(user, () -> {
            final DateTime deletionTime = now.minusSeconds(1);
            final MobileDeletedDiaryEntriesDTO ids = feature.getDeletedHarvestIds(ldt(deletionTime));
            Asserts.assertThat(ids.getEntryIds(), hasSize(1));
            Asserts.assertThat(ids.getEntryIds().get(0), equalTo(1L));
            Asserts.assertThat(ids.getLatestEntry(), equalTo(ldt(now)));
        });
    }

    @Test
    public void testGetDeletedHarvestIds_otherUser() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        final Long personId = user.getPerson().getId();
        model().newDeletedHarvest(now, 1L, personId, personId);

        onSavedAndAuthenticated(createUserWithPerson("otherUser"), () -> {
            final MobileDeletedDiaryEntriesDTO ids = feature.getDeletedHarvestIds(null);
            Asserts.assertThat(ids.getEntryIds(), is(empty()));
        });
    }

    @Test
    public void testGetDeletedHarvestIds_onlyNewerDeletionTimesReturned() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        final Long personId = user.getPerson().getId();
        model().newDeletedHarvest(now, 1L, personId, personId);

        onSavedAndAuthenticated(user, () -> {
            final MobileDeletedDiaryEntriesDTO ids = feature.getDeletedHarvestIds(ldt(now));
            Asserts.assertThat(ids.getEntryIds(), hasSize(0));
            Asserts.assertThat(ids.getLatestEntry(), is(nullValue()));
        });
    }

    @Test
    public void testGetDeletedHarvestIds_onlyOwnObservationDeletionTimesReturned() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        final Long personId = user.getPerson().getId();
        model().newDeletedHarvest(now, 1L, personId + 1, personId + 1);

        onSavedAndAuthenticated(user, () -> {
            final MobileDeletedDiaryEntriesDTO ids = feature.getDeletedHarvestIds(ldt(now.minusSeconds(1)));
            Asserts.assertThat(ids.getEntryIds(), hasSize(0));
            Asserts.assertThat(ids.getLatestEntry(), is(nullValue()));
        });
    }

    @Test
    public void testGetHarvestsWhereOnlyAuthor() {
        withPerson(author-> {
            withPerson(shooter-> {
                final Harvest harvest = model().newHarvest(author, shooter);

                onSavedAndAuthenticated(createUser(author), () -> {
                    final MobileDeletedDiaryEntriesDTO dto = feature.getHarvestsWhereOnlyAuthor();
                    assertThat(dto.getEntryIds(), hasSize(1));
                    assertThat(dto.getEntryIds().get(0), equalTo(harvest.getId()));
                    assertThat(dto.getLatestEntry(), is(nullValue()));
                });
            });
        });
    }

    @Test
    public void testGetHarvestsWhereOnlyAuthor_authorAndShooter() {
        withPerson(authorAndShooter-> {
                final Harvest harvest = model().newHarvest(authorAndShooter, authorAndShooter);

                onSavedAndAuthenticated(createUser(authorAndShooter), () -> {
                    final MobileDeletedDiaryEntriesDTO dto = feature.getHarvestsWhereOnlyAuthor();
                    assertThat(dto.getEntryIds(), is(empty()));
                    assertThat(dto.getLatestEntry(), is(nullValue()));
                });
        });
    }

    private void doTestDeleteHarvest(final boolean shouldBeDeleted, final Harvest harvest) {
        doTestDeleteHarvest(shouldBeDeleted, harvest, createUser(harvest.getAuthor()));
    }

    private void doTestDeleteHarvest(final boolean shouldBeDeleted, final Harvest harvest, final SystemUser user) {
        onSavedAndAuthenticated(user, () -> {
            final long harvestId = harvest.getId();

            if (shouldBeDeleted) {
                feature.deleteHarvest(harvestId);
                assertFalse(harvestRepo.existsById(harvestId));
            } else {
                final Throwable exception =
                        assertThrows(RuntimeException.class, () -> feature.deleteHarvest(harvestId));

                assertEquals("Cannot delete harvest which is accepted to permit.", exception.getMessage());
            }
        });
    }

    private MobileHarvestDTOBuilderFactory.Builder newDTOBuilderAndFixtureForCreate(final HarvestSpecVersion specVersion) {
        final GameSpecies species = model().newGameSpecies();
        persistAndAuthenticateWithNewUser(true);
        return create(specVersion, species);
    }

    private MobileHarvestDTOBuilderFactory.Builder newDTOBuilderAndFixtureForUpdate(final HarvestSpecVersion specVersion) {
        final SystemUser user = createUserWithPerson();
        final Harvest harvest = model().newMobileHarvest(user.getPerson());
        final GameSpecies newSpecies = model().newGameSpecies(true);

        persistInNewTransaction();
        authenticate(user);

        return create(specVersion, harvest, newSpecies);
    }

    private List<HarvestSpecimen> createSpecimens(final Harvest harvest, final int numSpecimens) {
        return createList(numSpecimens, () -> model().newHarvestSpecimen(harvest));
    }

    private void doCreateAssertions(final long harvestId,
                                    final MobileHarvestDTO expectedValues,
                                    final Person expectedAuthor) {

        doCreateAssertions(harvestId, expectedValues, expectedAuthor, h -> {
        });
    }

    private void doCreateAssertions(final long harvestId,
                                    final MobileHarvestDTO expectedValues,
                                    final Person expectedAuthor,
                                    final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Optional<Harvest> harvestOpt = harvestRepo.findById(harvestId);
            assertTrue(harvestOpt.isPresent());

            final Harvest harvest = harvestOpt.get();
            assertCommonExpectations(harvest, expectedValues);
            assertVersion(harvest, 0);

            final Person author = assertValidAuthor(harvest, expectedAuthor.getId());
            assertTrue(harvest.isActor(author));

            additionalAssertions.accept(harvest);
        });
    }

    private void doUpdateAssertions(final MobileHarvestDTO expectedValues,
                                    final Person expectedAuthor,
                                    final int expectedRevision) {

        doUpdateAssertions(expectedValues, expectedAuthor, expectedRevision, h -> {
        });
    }

    private void doUpdateAssertions(final MobileHarvestDTO expectedValues,
                                    final Person expectedAuthor,
                                    final int expectedRevision,
                                    final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Optional<Harvest> harvestOpt = harvestRepo.findById(expectedValues.getId());
            assertTrue(harvestOpt.isPresent());

            final Harvest harvest = harvestOpt.get();
            assertVersion(harvest, expectedRevision);
            assertCommonExpectations(harvest, expectedValues);
            assertValidAuthor(harvest, expectedAuthor.getId());

            additionalAssertions.accept(harvest);
        });
    }

    private void assertCommonExpectations(final Harvest harvest, final MobileHarvestDTO expectedValues) {
        assertNotNull(harvest);

        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
        assertEquals(Boolean.TRUE, harvest.getFromMobile());

        assertEquals(expectedValues.getMobileClientRefId(), harvest.getMobileClientRefId());
        assertEquals(expectedValues.getGameSpeciesCode(), harvest.getSpecies().getOfficialCode());
        assertEquals(expectedValues.getPointOfTime(), harvest.getPointOfTime().toLocalDateTime());
        assertEquals(expectedValues.getGeoLocation(), harvest.getGeoLocation());
        assertEquals(expectedValues.getAmount(), harvest.getAmount());
        assertEquals(expectedValues.getDescription(), harvest.getDescription());
        assertEquals(expectedValues.getHarvestReportState(), harvest.getHarvestReportState());
        assertEquals(expectedValues.isHarvestReportDone(), harvest.isHarvestReportDone());
        assertEquals(expectedValues.isHarvestReportRequired(), harvest.isHarvestReportRequired());

        assertSpecimens(
                specimenRepo.findByHarvest(harvest),
                expectedValues.getSpecimens(),
                expectedValues.specimenOps()::equalContent);

        assertEmpty(harvest.getImages());
    }

    private Person assertValidAuthor(final Harvest harvest, final long expectedAuthorId) {
        return personRepo.findById(expectedAuthorId)
                .map(author -> {
                    assertTrue(harvest.isAuthor(author));
                    return author;
                })
                .orElseThrow(() -> new AssertionError("Author not found by ID=" + expectedAuthorId));
    }

    private static void assertSpecimens(final List<HarvestSpecimen> specimens,
                                        final List<HarvestSpecimenDTO> expectedSpecimens,
                                        final BiFunction<HarvestSpecimen, HarvestSpecimenDTO, Boolean> compareFn) {

        final int numSpecimenDTOs = Optional.ofNullable(expectedSpecimens).map(List::size).orElse(0);
        assertEquals(numSpecimenDTOs, specimens.size());

        if (numSpecimenDTOs > 0) {
            assertTrue(equalNotNull(specimens, expectedSpecimens, compareFn));
        }
    }

    private MobileHarvestDTO invokeCreateHarvest(final MobileHarvestDTO input) {
        return withVersionChecked(feature.createHarvest(input));
    }

    private MobileHarvestDTO invokeUpdateHarvest(final MobileHarvestDTO input) {
        return withVersionChecked(feature.updateHarvest(input));
    }

    private MobileHarvestDTO withVersionChecked(final MobileHarvestDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Harvest.class);
    }


    private LocalDateTime ldt(final DateTime dateTime) {
        return DateUtil.toLocalDateTimeNullSafe(dateTime);
    }
}
