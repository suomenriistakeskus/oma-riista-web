package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfHuntingDayException;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfPermittedDatesException;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@RunWith(Theories.class)
public class MobileGroupHarvestFeatureTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin, MobileGroupHarvestDTOBuilderFactory {

    @Resource
    private MobileGroupHarvestFeature feature;

    @Resource
    private HarvestRepository harvestRepo;

    @Resource
    private HarvestSpecimenRepository specimenRepo;

    @Resource
    private PersonRepository personRepo;

    // TODO Add ANTLERS_LOST back when deer pilot 2020 is over.
    @DataPoints("specimenTypes")
    public static final HarvestSpecimenType[] SPECIMEN_TYPES = {
            ADULT_MALE, ADULT_FEMALE,/* ANTLERS_LOST,*/ YOUNG_MALE, YOUNG_FEMALE
    };

    // TODO Switch to `CURRENTLY_SUPPORTED` when deer pilot 2020 is over.
    public static final HarvestSpecVersion SPEC_VERSION = HarvestSpecVersion._7;

    @Theory
    public void testCreateHarvest_linkToHuntingDay(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            final Person author = f.groupLeader;
            final Person actor = f.groupMember;

            onSavedAndAuthenticated(createUser(author), () -> {

                final MobileGroupHarvestDTO inputDto = create(SPEC_VERSION, f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .linkToHuntingDay(huntingDay)
                        .build();

                final MobileGroupHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto);
            });
        });
    }

    @Theory
    public void testCreateHarvestForHuntingDay_whenHuntingFinished(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            // Intermediary flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated required for foreign key constraint.
            persistInNewTransaction();

            // Set club hunting finished.
            model().newMooseHuntingSummary(f.permit, f.club, true);

            final Person author = f.clubContact;

            onSavedAndAuthenticated(createUser(author), () -> {

                final MobileGroupHarvestDTO dto = create(SPEC_VERSION, f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .build();

                assertCreateThrows(ClubHuntingFinishedException.class, dto);
            });
        });
    }

    @Theory
    public void testCreateHarvestForHuntingDay_whenPointOfTimeOutsideOfHuntingDay(final HarvestSpecimenType specimenType) {
        // Month check done in order to have this test working in August generally because beginDate of speciesAmount
        // is set to 1.9.
        final int currentMonth = today().getMonthOfYear();
        final int huntingYear = currentMonth >= 9 ? huntingYear() : huntingYear() - 1;

        final HarvestPermitSpeciesAmount speciesAmount =
                model().newHarvestPermitSpeciesAmount(
                        model().newMooselikePermit(model().newRiistanhoitoyhdistys()),
                        model().newGameSpeciesMoose());
        speciesAmount.setBeginDate(ld(huntingYear, 9, 1));
        speciesAmount.setEndDate(ld(huntingYear, 12, 31));

        withHuntingGroupFixture(speciesAmount, f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, speciesAmount.getBeginDate());
            final Person author = f.clubContact;

            onSavedAndAuthenticated(createUser(author), () -> {

                final MobileGroupHarvestDTO dto = create(SPEC_VERSION, f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .withPointOfTime(huntingDay.getStartAsLocalDateTime().minusHours(4))
                        .build();

                assertCreateThrows(PointOfTimeOutsideOfHuntingDayException.class, dto);
            });
        });
    }

    @Theory
    public void testCreateHarvestForHuntingDay_whenPointOfTimeOutsideOfPermitted(final HarvestSpecimenType specimenType) {
        // Month check done in order to have this test working in August generally because beginDate of speciesAmount
        // is set to 1.9.
        final int currentMonth = today().getMonthOfYear();
        final int huntingYear = currentMonth >= 9 ? huntingYear() : huntingYear() - 1;

        final HarvestPermitSpeciesAmount speciesAmount =
                model().newHarvestPermitSpeciesAmount(
                        model().newMooselikePermit(model().newRiistanhoitoyhdistys()),
                        model().newGameSpeciesMoose());
        speciesAmount.setBeginDate(ld(huntingYear, 9, 1));
        speciesAmount.setEndDate(ld(huntingYear, 12, 31));

        withHuntingGroupFixture(speciesAmount, f -> {

            final LocalDate date = f.speciesAmount.getBeginDate().minusDays(1);
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, date);

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {

                final MobileGroupHarvestDTO dto = create(SPEC_VERSION, f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(f.groupMember)
                        .linkToHuntingDay(huntingDay)
                        .build();

                assertCreateThrows(PointOfTimeOutsideOfPermittedDatesException.class, dto);
            });
        });
    }

    @Theory
    public void testUpdateHarvest_linkToHuntingDay(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Person author = fixture.groupMember;
            final Person acceptor = fixture.groupLeader;

            final Harvest harvest = model().newHarvest(fixture.species, author);
            harvest.setGeoLocation(fixture.zoneCentroid);
            harvest.setAmount(1);

            final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, specimenType, SPEC_VERSION);

            onSavedAndAuthenticated(createUser(acceptor), () -> {

                final MobileGroupHarvestDTO dto = create(SPEC_VERSION, harvest)
                        .withSpecimensMappedFrom(asList(specimen))
                        .linkToHuntingDay(huntingDay)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(dto.getId());
                    assertVersion(updated, 1);

                    assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                    assertHuntingDay(updated, F.getId(huntingDay), F.getId(acceptor));
                });
            });
        });
    }

    // Test that harvest is not mutated (except for description/images) when hunting is finished.
    @Theory
    public void testUpdateHarvest_whenHuntingFinished(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(f -> {

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            final Person originalAuthor = f.clubContact;
            final Person newAuthor = f.groupMember;

            final GameSpecies originalSpecies = f.species;
            final Harvest harvest = model().newHarvest(originalSpecies, originalAuthor, huntingDay);
            final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, specimenType, SPEC_VERSION);

            // Intermediary flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated required for foreign key constraint.
            persistInNewTransaction();

            // Set club hunting finished.
            model().newMooseHuntingSummary(f.permit, f.club, true);

            final GameSpecies newSpecies = model().newDeerSubjectToClubHunting();

            onSavedAndAuthenticated(createUser(originalAuthor), () -> {

                final MobileGroupHarvestDTO dto = create(SPEC_VERSION, harvest, newSpecies)
                        .withSpecimensMappedFrom(asList(specimen))
                        .mutate()
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newAuthor)
                        .linkToHuntingDay(huntingDay2)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(dto.getId());
                    assertVersion(updated, 1);

                    // Assert that description is changed.
                    assertThat(updated.getDescription(), is(dto.getDescription()));

                    // Assert that other harvest fields are NOT changed.

                    assertThat(updated.getSpecies().getOfficialCode(), is(originalSpecies.getOfficialCode()));
                    assertThat(updated.getGeoLocation(), is(harvest.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(harvest.getPointOfTime()));

                    assertAuthorAndActor(updated, F.getId(originalAuthor), F.getId(originalAuthor));

                    // Hunting day should be unchanged.
                    assertHuntingDay(updated, F.getId(huntingDay), null);
                });
            });
        });
    }

    private MobileGroupHarvestDTO invokeCreateHarvest(final MobileGroupHarvestDTO input) {
        return withVersionChecked(feature.createHarvest(input));
    }

    private MobileGroupHarvestDTO withVersionChecked(final MobileGroupHarvestDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Harvest.class);
    }

    private void doCreateAssertions(final long harvestId,
                                    final MobileGroupHarvestDTO expectedValues) {

        doCreateAssertions(harvestId, expectedValues, h -> {});
    }

    private void doCreateAssertions(final long harvestId,
                                    final MobileGroupHarvestDTO expectedValues,
                                    final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Optional<Harvest> harvestOpt = harvestRepo.findById(harvestId);
            assertThat(harvestOpt.isPresent(), is(true));

            final Harvest harvest = harvestOpt.get();
            assertCommonExpectations(harvest, expectedValues);
            assertVersion(harvest, 0);

            assertAuthorAndActor(harvest, expectedValues.getAuthorInfo().getId(), expectedValues.getActorInfo().getId());

            assertHuntingDay(harvest, expectedValues.getHuntingDayId(), expectedValues.getAuthorInfo().getId());

            additionalAssertions.accept(harvest);
        });
    }

    private void assertCommonExpectations(final Harvest harvest, final MobileGroupHarvestDTO expectedValues) {
        assertThat(harvest, is(notNullValue()));

        assertThat(harvest.getGeoLocation().getSource(), is(equalTo(GeoLocation.Source.GPS_DEVICE)));
        assertThat(harvest.getFromMobile(), is(true));

        assertThat(harvest.getMobileClientRefId(), is(equalTo(expectedValues.getMobileClientRefId())));
        assertThat(harvest.getSpecies().getOfficialCode(), is(equalTo(expectedValues.getGameSpeciesCode())));
        assertThat(harvest.getPointOfTime().toLocalDateTime(), is(equalTo(expectedValues.getPointOfTime())));
        assertThat(harvest.getGeoLocation(), is(equalTo(expectedValues.getGeoLocation())));
        assertThat(harvest.getAmount(), is(equalTo(expectedValues.getAmount())));
        assertThat(harvest.getDescription(), is(equalTo(expectedValues.getDescription())));
        assertThat(harvest.getHarvestReportState(), is(equalTo(expectedValues.getHarvestReportState())));
        assertThat(harvest.isHarvestReportDone(), is(expectedValues.isHarvestReportDone()));
        assertThat(harvest.isHarvestReportRequired(), is(equalTo(expectedValues.isHarvestReportRequired())));

        assertSpecimens(
                specimenRepo.findByHarvest(harvest),
                expectedValues.getSpecimens(),
                expectedValues.specimenOps()::equalContent);

        assertThat(harvest.getImages(), is(empty()));
    }

    private void assertAuthorAndActor(final Harvest harvest, final Long expectedAuthorId, final Long expectedActorId) {
        assertThat(harvest.getAuthor().getId(), is(equalTo(expectedAuthorId)));
        assertThat(harvest.getActor().getId(), is(equalTo(expectedActorId)));
    }

    private void assertHuntingDay(final Harvest harvest, final Long expectedHuntingDayId, final Long expectedAcceptorId) {
        assertThat(F.getId(harvest.getHuntingDayOfGroup()), is(equalTo(expectedHuntingDayId)));
        assertThat(harvest.getPointOfTimeApprovedToHuntingDay(), is(notNullValue()));
        assertThat(F.getId(harvest.getApproverToHuntingDay()), is(equalTo(expectedAcceptorId)));
    }

    private static void assertSpecimens(final List<HarvestSpecimen> specimens,
                                        final List<HarvestSpecimenDTO> expectedSpecimens,
                                        final BiFunction<HarvestSpecimen, HarvestSpecimenDTO, Boolean> compareFn) {

        final int numSpecimenDTOs = Optional.ofNullable(expectedSpecimens).map(List::size).orElse(0);
        assertThat(specimens, hasSize(numSpecimenDTOs));

        if (numSpecimenDTOs > 0) {
            assertThat(equalNotNull(specimens, expectedSpecimens, compareFn), is(equalTo(true)));
        }
    }

    private void assertCreateThrows(final Class<? extends Throwable> exceptionClass, final MobileGroupHarvestDTO dto) {
        assertThrows(exceptionClass, () -> invokeCreateHarvest(dto));

        assertThat(harvestRepo.findAll(), is(empty()));
    }

    private MobileGroupHarvestDTO invokeUpdateHarvest(final MobileGroupHarvestDTO input) {
        return withVersionChecked(feature.updateHarvest(input));
    }
}
