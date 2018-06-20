package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.MobileHarvestDTOBuilderFactory;
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
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.VersionedTestExecutionSupport;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_REQUIRING_AMOUNT;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_REQUIRING_LOCATION_SOURCE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_PERMIT_STATE;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.test.TestUtils.expectValidationException;
import static fi.riista.test.TestUtils.wrapExceptionExpectation;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class MobileHarvestFeatureTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin, MobileHarvestDTOBuilderFactory,
        VersionedTestExecutionSupport<HarvestSpecVersion> {

    @Resource
    private MobileGameDiaryFeature mobileGameDiaryFeature;

    @Resource
    protected HarvestRepository harvestRepo;

    @Resource
    protected HarvestSpecimenRepository specimenRepo;

    @Resource
    protected PersonRepository personRepo;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected abstract int getApiVersion();

    @Override
    public void onAfterVersionedTestExecution() {
        reset();
    }

    @Test
    public void testCreateHarvest_whenHarvestSpecVersionIsNull() {
        forEachVersion(expectValidationException(specVersion -> {

            invokeCreateHarvest(newDTOBuilderAndFixtureForCreate(specVersion).withSpecVersion(null).build());
        }));
    }

    @Test
    public void testCreateHarvest_whenAmountIsNull() {
        forEachVersionStartingFrom(LOWEST_VERSION_REQUIRING_AMOUNT, expectValidationException(specVersion -> {

            invokeCreateHarvest(newDTOBuilderAndFixtureForCreate(specVersion).withAmount(null).build());
        }));
    }

    @Test
    public void testCreateHarvest() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies();

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species).build();
                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_withSpecimens() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species, 10).build();
                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_whenSpecimensIsNull() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies();

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species, 10).withSpecimens(null).build();
                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_withMobileClientRefIdAssociatedWithExistingHarvest() {
        forEachVersion(specVersion -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newMobileHarvest(species, author);
            final GameSpecies newSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(author), () -> {

                invokeCreateHarvest(create(specVersion, harvest, newSpecies).mutate().build());

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, species).build();

                doCreateAssertions(harvest.getId(), expectedValues, author);
            });
        }));
    }

    @Test
    public void testCreateHarvest_withMobileClientRefIdAssociatedWithExistingHarvestOfOtherPerson() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvestOfOtherPerson = model().newMobileHarvest(model().newPerson());

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species)
                        .withMobileClientRefId(harvestOfOtherPerson.getMobileClientRefId())
                        .build();

                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_withPointOfTimeInsideSeason() {
        forEachVersion(specVersion -> {
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
        });
    }

    @Test
    public void testCreateHarvest_whenHarvestReportAlwaysRequiredForSpecies() {
        forEachVersion(specVersion -> withRhy(rhy -> withPerson(person -> {
            final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileHarvestDTO inputDto = create(specVersion, species, 1).build();
                final HarvestSpecimenDTO specimenDto = inputDto.getSpecimens().get(0);
                specimenDto.setGender(GameGender.MALE);
                specimenDto.setAge(GameAge.ADULT);
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
        })));
    }

    @Test
    public void testCreateHarvest_withPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {

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
        });
    }

    @Test
    public void testCreateHarvest_withPermitWhenRhyNotFound() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, wrapExceptionExpectation(
                RhyNotResolvableByGeoLocationException.class, specVersion -> {

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
                }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestSpecVersionIsNull() {
        forEachVersion(expectValidationException(specVersion -> {

            invokeUpdateHarvest(newDTOBuilderAndFixtureForUpdate(specVersion).withSpecVersion(null).build());
        }));
    }

    @Test
    public void testUpdateHarvest_whenGeoLocationSourceIsNull() {
        forEachVersionStartingFrom(LOWEST_VERSION_REQUIRING_LOCATION_SOURCE, expectValidationException(specVersion -> {

            final MobileHarvestDTO dto = newDTOBuilderAndFixtureForUpdate(specVersion).build();
            dto.getGeoLocation().setSource(null);

            invokeUpdateHarvest(dto);
        }));
    }

    @Test
    public void testUpdateHarvest_whenAmountIsNull() {
        forEachVersionStartingFrom(LOWEST_VERSION_REQUIRING_AMOUNT, expectValidationException(specVersion -> {

            invokeUpdateHarvest(newDTOBuilderAndFixtureForUpdate(specVersion).withAmount(null).build());
        }));
    }

    @Test
    public void testUpdateHarvest_whenNoChanges() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest).populateSpecimensWith(specimens).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 0);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportNotDone() {
        forEachVersion(specVersion -> withPerson(person -> {

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
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportDone() {
        forEachVersion(specVersion -> withPerson(person -> {

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
                        .withSpecimens(Collections.emptyList())
                        .withAmount(1)
                        .build();

                invokeUpdateHarvest(dto);

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, species)
                        .withHarvestReportDone(true)
                        .populateSpecimensWith(specimens)
                        .withDescription(dto.getDescription())
                        .build();

                doUpdateAssertions(expectedValues, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_mobileClientRefIdShouldNotBeUpdated() {
        forEachVersion(specVersion -> withPerson(person -> {

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
        }));
    }

    @Test
    public void testUpdateHarvest_forSpecimensBeingPreserved() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .populateSpecimensWith(specimens)
                        .withAmount(specimens.size())
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenUpdatingSpecimensOnly() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest)
                        .populateSpecimensWith(specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensPresent() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest)
                        .populateSpecimensWith(specimens)
                        .withAmount(specimens.size() + 10)
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensNotPresent() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest).withAmount(10).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_withPointOfTimeInsideSeason_noReport() {
        forEachVersion(specVersion -> withPerson(person -> {
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
        }));
    }

    @Test
    public void testUpdateHarvest_forAddingPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> withPerson(person -> {

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
        }));
    }

    @Test
    public void testUpdateHarvest_withPermitWhenRhyNotFound() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, wrapExceptionExpectation(
                RhyNotResolvableByGeoLocationException.class, specVersion -> withPerson(person -> {

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
                })));
    }

    @Test
    public void testUpdateHarvest_forRemovingPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> withPerson(person -> {

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
                                .ofNullable(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()))
                                .map(ldt -> ldt.plusMinutes(1))
                                .orElse(null))
                        .withPermitNumber(null)
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1, h -> assertNull(h.getHarvestPermit()));
            });
        }));
    }

    // Test that harvest is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Test
    public void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard() {
        forEachVersion(specVersion -> withMooseHuntingGroupFixture(f -> {
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
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestChangedToRequired() {
        forEachVersion(specVersion -> withRhy(rhy -> withPerson(person -> {
            final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
            final Harvest harvest = model().newMobileHarvest(person);
            final HarvestSpecimen harvestSpecimen = model().newHarvestSpecimen(harvest, GameAge.ADULT, GameGender.MALE, 100.0);

            onSavedAndAuthenticated(createUser(person), () -> {
                // Check invariant
                assertFalse(harvest.isHarvestReportRequired());

                final MobileHarvestDTO inputDto = create(specVersion, harvest, species)
                        .populateSpecimensWith(singletonList(harvestSpecimen))
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
        })));
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {
            doTestUpdateHarvestWithPermit(specVersion, false, false, StateAcceptedToHarvestPermit.ACCEPTED);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {
            doTestUpdateHarvestWithPermit(specVersion, true, true, StateAcceptedToHarvestPermit.ACCEPTED);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestProposedToPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {
            doTestUpdateHarvestWithPermit(specVersion, false, true, StateAcceptedToHarvestPermit.PROPOSED);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestRejectedToPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {
            doTestUpdateHarvestWithPermit(specVersion, false, true, StateAcceptedToHarvestPermit.REJECTED);
        });
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

            final Harvest harvest = model().newMobileHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(state);
            harvest.setRhy(rhy);

            consumer.accept(harvest);
        }));
    }

    @Test
    public void testUpdateHarvest_forIntegrationWithHarvestSpecimenOps() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpeciesMoose(), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest).mutate().withSpecimens(1).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, 1, h -> {

                    final HarvestSpecimenDTO specimenDTO = dto.getSpecimens().get(0);

                    final HarvestSpecimenAssertionBuilder assertionBuilder = HarvestSpecimenAssertionBuilder.builder()
                            .withAgeAndGender(specimenDTO.getAge(), specimenDTO.getGender());

                    if (dto.specimenOps().supportsExtendedMooseFields()) {
                        assertionBuilder.allMooseFieldsPresent(specVersion);
                    } else {
                        assertionBuilder.mooseFieldsAbsentExceptEstimatedWeight();
                    }

                    assertionBuilder.verify(specimenRepo.findByHarvest(h).get(0));
                });
            });
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

            thrown.expect(RuntimeException.class);
            thrown.expectMessage("Cannot delete harvest with an associated harvest report.");

            onSavedAndAuthenticated(createUser(author), () -> {
                mobileGameDiaryFeature.deleteHarvest(harvest.getId());
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

            thrown.expect(RuntimeException.class);
            thrown.expectMessage("Cannot delete harvest with an associated harvest report.");

            onSavedAndAuthenticated(createUser(author), () -> {
                mobileGameDiaryFeature.deleteHarvest(harvest.getId());
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
                    mobileGameDiaryFeature.deleteHarvest(harvest.getId());
                    fail("Deletion of harvest associated with a hunting day should fail");
                } catch (final RuntimeException e) {
                    // Expected
                }
                assertNotNull(harvestRepo.findOne(harvest.getId()));
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

    private void doTestDeleteHarvest(final boolean shouldBeDeleted, final Harvest harvest) {
        doTestDeleteHarvest(shouldBeDeleted, harvest, createUser(harvest.getAuthor()));
    }

    private void doTestDeleteHarvest(final boolean shouldBeDeleted, final Harvest harvest, final SystemUser user) {
        onSavedAndAuthenticated(user, () -> {
            if (!shouldBeDeleted) {
                thrown.expect(RuntimeException.class);
                thrown.expectMessage("Cannot delete harvest which is accepted to permit");
            }
            mobileGameDiaryFeature.deleteHarvest(harvest.getId());
        });
    }

    protected MobileHarvestDTOBuilderFactory.Builder newDTOBuilderAndFixtureForCreate(
            final HarvestSpecVersion specVersion) {

        final GameSpecies species = model().newGameSpecies();
        persistAndAuthenticateWithNewUser(true);
        return create(specVersion, species);
    }

    protected MobileHarvestDTOBuilderFactory.Builder newDTOBuilderAndFixtureForUpdate(
            final HarvestSpecVersion specVersion) {

        final SystemUser user = createUserWithPerson();
        final Harvest harvest = model().newMobileHarvest(user.getPerson());
        final GameSpecies newSpecies = model().newGameSpecies(true);

        persistInNewTransaction();
        authenticate(user);

        return create(specVersion, harvest, newSpecies);
    }

    protected List<HarvestSpecimen> createSpecimens(final Harvest harvest, final int numSpecimens) {
        return createList(numSpecimens, () -> model().newHarvestSpecimen(harvest));
    }

    protected void doCreateAssertions(
            final long harvestId, final MobileHarvestDTO expectedValues, final Person expectedAuthor) {

        doCreateAssertions(harvestId, expectedValues, expectedAuthor, h -> {
        });
    }

    protected void doCreateAssertions(final long harvestId,
                                      final MobileHarvestDTO expectedValues,
                                      final Person expectedAuthor,
                                      final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(harvestId);
            assertCommonExpectations(harvest, expectedValues);
            assertVersion(harvest, 0);

            final Person author = assertValidAuthor(harvest, expectedAuthor.getId());
            assertTrue(harvest.isActor(author));

            additionalAssertions.accept(harvest);
        });
    }

    protected void doUpdateAssertions(final MobileHarvestDTO expectedValues,
                                      final Person expectedAuthor,
                                      final int expectedRevision) {

        doUpdateAssertions(expectedValues, expectedAuthor, expectedRevision, h -> {
        });
    }

    protected void doUpdateAssertions(final MobileHarvestDTO expectedValues,
                                      final Person expectedAuthor,
                                      final int expectedRevision,
                                      final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(expectedValues.getId());
            assertVersion(harvest, expectedRevision);
            assertCommonExpectations(harvest, expectedValues);
            assertValidAuthor(harvest, expectedAuthor.getId());

            additionalAssertions.accept(harvest);
        });
    }

    protected void assertCommonExpectations(final Harvest harvest, final MobileHarvestDTO expectedValues) {
        assertNotNull(harvest);

        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
        assertEquals(Boolean.TRUE, harvest.getFromMobile());

        assertTrue(Objects.equals(expectedValues.getMobileClientRefId(), harvest.getMobileClientRefId()));
        assertEquals(expectedValues.getGameSpeciesCode(), harvest.getSpecies().getOfficialCode());
        assertEquals(expectedValues.getPointOfTime(), DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()));
        assertEquals(expectedValues.getGeoLocation(), harvest.getGeoLocation());
        assertEquals(expectedValues.getDescription(), harvest.getDescription());
        assertEquals(expectedValues.getHarvestReportState(), harvest.getHarvestReportState());
        assertEquals(expectedValues.isHarvestReportDone(), harvest.isHarvestReportDone());
        assertEquals(expectedValues.isHarvestReportRequired(), harvest.isHarvestReportRequired());

        final int expectedAmount = Optional.ofNullable(expectedValues.getAmount()).orElse(1);
        assertEquals(expectedAmount, harvest.getAmount());

        assertSpecimens(
                specimenRepo.findByHarvest(harvest),
                expectedValues.getSpecimens(),
                expectedValues.specimenOps()::equalContent);

        assertEmpty(harvest.getImages());
    }

    protected Person assertValidAuthor(final Harvest harvest, final long expectedAuthorId) {
        final Person author = personRepo.findOne(expectedAuthorId);
        assertNotNull(author);
        assertTrue(harvest.isAuthor(author));
        return author;
    }

    protected void assertSpecimens(final List<HarvestSpecimen> specimens,
                                   final List<HarvestSpecimenDTO> expectedSpecimens,
                                   final BiFunction<HarvestSpecimen, HarvestSpecimenDTO, Boolean> compareFn) {

        final int numSpecimenDTOs = Optional.ofNullable(expectedSpecimens).map(List::size).orElse(0);
        assertEquals(numSpecimenDTOs, specimens.size());

        if (numSpecimenDTOs > 0) {
            assertTrue(equalNotNull(specimens, expectedSpecimens, compareFn));
        }
    }

    protected MobileHarvestDTO invokeCreateHarvest(final MobileHarvestDTO input) {
        return withVersionChecked(mobileGameDiaryFeature.createHarvest(input, getApiVersion()));
    }

    protected MobileHarvestDTO invokeUpdateHarvest(final MobileHarvestDTO input) {
        return withVersionChecked(mobileGameDiaryFeature.updateHarvest(input, getApiVersion()));
    }

    private MobileHarvestDTO withVersionChecked(final MobileHarvestDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Harvest.class);
    }
}
