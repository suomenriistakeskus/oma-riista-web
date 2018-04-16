package fi.riista.feature.huntingclub.moosedatacard;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardPage1Validation;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.Asserts;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.control.Either;
import javaslang.control.Try;
import javaslang.control.Validation;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.alreadyExistingHuntingDaysIgnored;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.harvestsIgnoredBecauseOfAlreadyExistingHuntingDays;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.missingHuntingDaysCreated;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationsIgnoredBecauseOfAlreadyExistingHuntingDays;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.clubHuntingFinishedByModeratorOverride;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.contactPersonCouldNotBeFoundByHunterNumberOrSsn;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.harvestPermitSpeciesAmountForMooseNotFound;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingClubNotFoundByCustomerNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingFinishedForPermit;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingYearForHarvestPermitCouldNotBeUnambiguouslyResolved;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.multipleHarvestPermitMooseAmountsFound;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.permitNotFoundByPermitNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.permitNotOfCorrectType;
import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.Asserts.assertSuccess;
import static fi.riista.util.DateUtil.today;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MooseDataCardImportHelperTest extends EmbeddedDatabaseTest {

    private static final String NON_EXISTENT = "nonExistent";

    private static final Function<String, String> TO_NON_EXISTENT = s -> NON_EXISTENT;

    @Resource
    private MooseDataCardImportHelper helper;

    @Resource
    private OccupationRepository occupationRepo;

    @Resource
    private GroupHuntingDayRepository huntingDayRepo;

    @Resource
    private HarvestRepository harvestRepo;

    @Resource
    private ObservationRepository observationRepo;

    private class EntityResolveFixture {

        public final GameSpecies speciesMoose;
        public final GameSpecies speciesBear;
        public final GameSpecies speciesLynx;
        public final HarvestPermit permit;
        public final HarvestPermitSpeciesAmount speciesAmount;
        public final HarvestPermitSpeciesAmount speciesAmount2;
        public final int huntingYear;
        public final Either<LHOrganisation, HuntingClub> lhOrgOrClub;
        public final Person person;

        public EntityResolveFixture(final EntitySupplier model) {
            this(model, true);
        }

        public EntityResolveFixture(final EntitySupplier model, final boolean clubInsteadOfLhOrg) {
            final Riistanhoitoyhdistys rhy = model.newRiistanhoitoyhdistys();

            this.speciesMoose = model.newGameSpeciesMoose();
            this.speciesBear = model.newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
            this.speciesLynx = model.newGameSpecies(GameSpecies.OFFICIAL_CODE_LYNX);

            this.permit = model.newMooselikePermit(rhy);

            this.huntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
            this.speciesAmount = model.newHarvestPermitSpeciesAmount(this.permit, this.speciesMoose, this.huntingYear);
            this.speciesAmount2 = model.newHarvestPermitSpeciesAmount(this.permit, this.speciesBear, this.huntingYear);

            this.lhOrgOrClub = clubInsteadOfLhOrg
                    ? Either.right(model.newHuntingClub(rhy))
                    : Either.left(model.newLHOrganisation(rhy));

            this.person = model.newPerson();
            this.person.setSsn(ssn());

            final SystemUser moderator = createNewModerator();

            persistInCurrentlyOpenTransaction();

            this.permit.getSpeciesAmounts().addAll(asList(this.speciesAmount, this.speciesAmount2));

            authenticate(moderator);
        }

        public HuntingClub getClub() {
            return lhOrgOrClub.get();
        }

        public MooseDataCardPage1Validation getValidationInput() {
            return getValidationInput(true);
        }

        public MooseDataCardPage1Validation getValidationInput(final boolean useHunterNumberInsteadOfSsn) {
            return new MooseDataCardPage1Validation(
                    useHunterNumberInsteadOfSsn ? Either.left(person.getHunterNumber()) : Either.right(person.getSsn()),
                    permit.getPermitNumber(),
                    lhOrgOrClub.fold(LHOrganisation::getOfficialCode, HuntingClub::getOfficialCode),
                    geoLocation());
        }
    }

    @Test
    @Transactional
    public void testResolveEntities_withHuntingClub() {
        testResolveEntities(true);
    }

    @Test
    @Transactional
    public void testResolveEntities_withLhOrganisation() {
        testResolveEntities(false);
    }

    private void testResolveEntities(final boolean createClubInsteadOfLhOrganisation) {
        final EntityResolveFixture f = new EntityResolveFixture(model(), createClubInsteadOfLhOrganisation);

        helper.resolveEntities(f.getValidationInput())
                .toEither()
                .peek(result -> {
                    assertEquals(f.lhOrgOrClub, result.lhOrganisationOrClub);
                    assertEquals(f.speciesAmount, result.speciesAmount);
                    assertEquals(f.huntingYear, result.huntingYear);
                    assertEquals(f.person.getId(), Long.valueOf(result.contactPersonId));
                })
                .orElseRun(err -> assertEmpty(err, "Unexpected validation errors: "));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenClubNotExistsForGivenOfficialCode() {
        testResolveEntities_whenClubOrLhOrganisationNotExistsForGivenOfficialCode(true);
    }

    @Test
    @Transactional
    public void testResolveEntities_whenLhOrganisationNotExistsForGivenOfficialCode() {
        testResolveEntities_whenClubOrLhOrganisationNotExistsForGivenOfficialCode(false);
    }

    private void testResolveEntities_whenClubOrLhOrganisationNotExistsForGivenOfficialCode(final boolean createClub) {
        final EntityResolveFixture f = new EntityResolveFixture(model(), createClub);
        final MooseDataCardPage1Validation input = f.getValidationInput().asTuple4()
                .map3(TO_NON_EXISTENT)
                .transform(MooseDataCardPage1Validation::new);

        assertValidationFailure(input, huntingClubNotFoundByCustomerNumber(NON_EXISTENT));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenPermitNotExistsForGivenNumber() {
        final EntityResolveFixture f = new EntityResolveFixture(model());
        final MooseDataCardPage1Validation input = f.getValidationInput().asTuple4()
                .map2(TO_NON_EXISTENT)
                .transform(MooseDataCardPage1Validation::new);

        assertValidationFailure(input, permitNotFoundByPermitNumber(NON_EXISTENT));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenPermitIsOfIncorrectType() {
        final EntityResolveFixture f = new EntityResolveFixture(model());
        f.permit.setPermitTypeCode("012");

        assertValidationFailure(f.getValidationInput(), permitNotOfCorrectType("012"));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenMooseHarvestReportDone() {
        final EntityResolveFixture f = new EntityResolveFixture(model());

        model().newMooseHarvestReport(f.speciesAmount);
        persistInCurrentlyOpenTransaction();

        assertValidationFailure(f.getValidationInput(), huntingFinishedForPermit(f.permit.getPermitNumber()));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenMoosePermitAmountNotAvailable() {
        final EntityResolveFixture f = new EntityResolveFixture(model());
        f.speciesAmount.setGameSpecies(f.speciesBear);

        assertValidationFailure(f.getValidationInput(), harvestPermitSpeciesAmountForMooseNotFound());
    }

    @Test
    @Transactional
    public void testResolveEntities_whenMultipleMoosePermitAmountsAvailable() {
        final EntityResolveFixture f = new EntityResolveFixture(model());
        f.speciesAmount2.setGameSpecies(f.speciesMoose);

        assertValidationFailure(f.getValidationInput(), multipleHarvestPermitMooseAmountsFound());
    }

    @Test
    @Transactional
    public void testResolveEntities_whenUnambiguousHuntingYearCouldNotBeResolved() {
        final EntityResolveFixture f = new EntityResolveFixture(model());

        final int nextHuntingYear = f.huntingYear + 1;
        f.speciesAmount.setBeginDate2(DateUtil.huntingYearBeginDate(nextHuntingYear));
        f.speciesAmount.setEndDate2(DateUtil.huntingYearEndDate(nextHuntingYear));

        assertValidationFailure(
                f.getValidationInput(),
                huntingYearForHarvestPermitCouldNotBeUnambiguouslyResolved(f.permit.getPermitNumber()));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenHuntingFinishedByModeratorOverride() {
        final EntityResolveFixture f = new EntityResolveFixture(model());
        final HuntingClub club = f.getClub();

        model().newModeratedBasicHuntingSummary(f.speciesAmount, club);
        persistInCurrentlyOpenTransaction();

        assertValidationFailure(f.getValidationInput(), clubHuntingFinishedByModeratorOverride(club.getOfficialCode()));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenContactPersonNotExistsForGivenHunterNumber() {
        testResolveEntities_whenContactPersonNotExists(true);
    }

    @Test
    @Transactional
    public void testResolveEntities_whenContactPersonNotExistsForGivenSsn() {
        testResolveEntities_whenContactPersonNotExists(false);
    }

    private void testResolveEntities_whenContactPersonNotExists(final boolean findByHunterNumber) {
        final EntityResolveFixture f = new EntityResolveFixture(model());

        final MooseDataCardPage1Validation input = f.getValidationInput(findByHunterNumber).asTuple4()
                .map1(findByHunterNumber
                        ? hunterNumberOrSsn -> hunterNumberOrSsn.mapLeft(TO_NON_EXISTENT)
                        : hunterNumberOrSsn -> hunterNumberOrSsn.map(TO_NON_EXISTENT))
                .transform(MooseDataCardPage1Validation::new);

        assertValidationFailure(input, contactPersonCouldNotBeFoundByHunterNumberOrSsn(
                findByHunterNumber ? NON_EXISTENT : null,
                findByHunterNumber ? null : NON_EXISTENT));
    }

    @Test
    @Transactional
    public void testResolveEntities_whenMultipleValidationErrorsPresent() {
        final EntityResolveFixture f = new EntityResolveFixture(model());

        final MooseDataCardPage1Validation input = f.getValidationInput().asTuple4()
                .map1(hunterNumberOrSsn -> hunterNumberOrSsn.mapLeft(TO_NON_EXISTENT))
                .map2(TO_NON_EXISTENT)
                .map3(TO_NON_EXISTENT)
                .transform(MooseDataCardPage1Validation::new);

        f.speciesAmount2.setGameSpecies(f.speciesMoose);

        assertValidationFailure(input, asList(
                huntingClubNotFoundByCustomerNumber(NON_EXISTENT),
                permitNotFoundByPermitNumber(NON_EXISTENT),
                contactPersonCouldNotBeFoundByHunterNumberOrSsn(NON_EXISTENT, null)));
    }

    private void assertValidationFailure(final MooseDataCardPage1Validation input, final String expectedMessage) {
        assertValidationFailure(input, singletonList(expectedMessage));
    }

    private void assertValidationFailure(final MooseDataCardPage1Validation input,
                                         final List<String> expectedMessages) {

        final Validation<List<String>, MooseDataCardEntitySearchResult> validation = helper.resolveEntities(input);
        assertTrue("Validation should have failed", validation.isInvalid());
        assertEquals(expectedMessages, validation.getError());
    }

    @Test
    @Transactional
    public void persistHuntingDayData() {
        final GameSpecies species = model().newGameSpecies();
        final HuntingClubGroup group = model().newHuntingClubGroup(species);
        final Person author = model().newPerson();
        final LocalDate today = today();

        // persistent hunting days and observations
        final GroupHuntingDay huntingDay1 = model().newGroupHuntingDay(group, today);
        final Harvest harvest1 = model().newHarvest(species, author, huntingDay1);
        final Observation observation1 = model().newObservation(species, author, huntingDay1);

        final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(group, today.plusDays(1));
        final Harvest harvest2 = model().newHarvest(species, author, huntingDay2);

        final GroupHuntingDay huntingDay3 = model().newGroupHuntingDay(group, today.plusDays(2));
        final Observation observation2 = model().newObservation(species, author, huntingDay3);

        final GroupHuntingDay huntingDay4 = model().newGroupHuntingDay(group, today.plusDays(3));

        persistInCurrentlyOpenTransaction();

        // transient hunting days and observations

        final GroupHuntingDay transientHuntingDay1 = model().newGroupHuntingDay(group, today.plusDays(4));
        final Harvest transientHarvest1 = model().newHarvest(species, author, transientHuntingDay1);
        final Observation transientObservation1 = model().newObservation(species, author, transientHuntingDay1);

        final GroupHuntingDay transientHuntingDay2 = model().newGroupHuntingDay(group, today.plusDays(5));
        final Harvest transientHarvest2 = model().newHarvest(species, author, transientHuntingDay2);

        final GroupHuntingDay transientHuntingDay3 = model().newGroupHuntingDay(group, today.plusDays(6));
        final Observation transientObservation2 = model().newObservation(species, author, transientHuntingDay3);

        final GroupHuntingDay transientHuntingDay4 = model().newGroupHuntingDay(group, today.plusDays(7));

        final Harvest transientHarvest3 = model().newHarvest(species, author, today.plusDays(8));
        final Observation transientObservation3 = model().newObservation(species, author, today.plusDays(8));

        final Harvest transientHarvest4 = model().newHarvest(species, author, today.plusDays(9));

        final Observation transientObservation4 = model().newObservation(species, author, today.plusDays(10));

        // Hunting day already exists for following
        final GroupHuntingDay transientHuntingDay5 = model().newGroupHuntingDay(group, huntingDay4.getStartDate());
        final Harvest transientHarvest5 = model().newHarvest(species, author, huntingDay1.getStartDate());
        final Observation transientObservation5 =
                model().newObservation(species, author, huntingDay1.getStartDate());

        final Try<Tuple2<List<GroupHuntingDay>, List<String>>> tryResult = helper.persistHuntingDayData(
                group,
                asList(transientHuntingDay1, transientHuntingDay2, transientHuntingDay3, transientHuntingDay4,
                        transientHuntingDay5),
                Stream.of(transientHarvest1, transientHarvest2, transientHarvest3, transientHarvest4, transientHarvest5)
                        .map(harvest -> Tuple.of(harvest, model().newHarvestSpecimen(harvest)))
                        .collect(toList()),
                asList(transientObservation1, transientObservation2, transientObservation3, transientObservation4,
                        transientObservation5));

        if (tryResult.isFailure()) {
            fail(tryResult.getCause().getMessage());
        }

        tryResult.peek(result -> {
            final List<String> messages = result._2;
            assertEquals(4, messages.size());

            // Assert informational messages first.

            final String expectedMissingHuntingDaysMessage =
                    missingHuntingDaysCreated(Stream.of(transientHarvest3, transientHarvest4, transientObservation4)
                            .map(GameDiaryEntry::getPointOfTimeAsLocalDate));
            assertEquals(expectedMissingHuntingDaysMessage, messages.get(0));

            final String expectedHuntingDayIgnoreMessage =
                    alreadyExistingHuntingDaysIgnored(Stream.of(transientHuntingDay5.getStartDate()));
            assertEquals(expectedHuntingDayIgnoreMessage, messages.get(1));

            final String expectedHarvestIgnoreMessage =
                    harvestsIgnoredBecauseOfAlreadyExistingHuntingDays(ImmutableMap.of(
                            transientHarvest5.getPointOfTimeAsLocalDate(), 1));
            assertEquals(expectedHarvestIgnoreMessage, messages.get(2));

            final String expectedObservationIgnoreMessage =
                    observationsIgnoredBecauseOfAlreadyExistingHuntingDays(ImmutableMap.of(
                            transientObservation5.getPointOfTimeAsLocalDate(), 1));
            assertEquals(expectedObservationIgnoreMessage, messages.get(3));

            final List<GroupHuntingDay> resultHuntingDays = result._1;

            // Assert dates of result hunting days.
            assertEquals(
                    IntStream.rangeClosed(4, 10).mapToObj(i -> today.plusDays(i)).collect(toSet()),
                    resultHuntingDays.stream().map(GroupHuntingDay::getStartDate).collect(toSet()));

            // Assert that result hunting days are persisted.
            assertEquals(
                    IntStream.rangeClosed(0, 10).mapToObj(i -> today.plusDays(i)).collect(toSet()),
                    huntingDayRepo.findByGroup(group).stream().map(GroupHuntingDay::getStartDate).collect(toSet()));

            // Assert that hunting group is set correctly for result hunting days.
            assertEquals(singleton(group), resultHuntingDays.stream().map(GroupHuntingDay::getGroup).collect(toSet()));

            final Set<Long> newHarvestIds =
                    F.getUniqueIds(transientHarvest1, transientHarvest2, transientHarvest3, transientHarvest4);
            final List<Harvest> allHarvests = harvestRepo.findAll();

            // Assert that expected transient harvests are persisted.
            assertEquals(Sets.union(F.getUniqueIds(harvest1, harvest2), newHarvestIds), F.getUniqueIds(allHarvests));

            // Assert that result harvests are correctly associated with hunting days.
            allHarvests.stream()
                    .filter(harvest -> newHarvestIds.contains(harvest.getId()))
                    .forEach(harvest -> {
                        assertTrue(resultHuntingDays.contains(harvest.getHuntingDayOfGroup()));
                        assertEquals(
                                harvest.getHuntingDayOfGroup().getStartDate(), harvest.getPointOfTimeAsLocalDate());
                    });

            final Set<Long> newObservationIds = F.getUniqueIds(
                    transientObservation1, transientObservation2, transientObservation3, transientObservation4);
            final List<Observation> allObservations = observationRepo.findAll();

            // Assert that expected transient observations are persisted.
            assertEquals(
                    Sets.union(F.getUniqueIds(observation1, observation2), newObservationIds),
                    F.getUniqueIds(allObservations));

            // Assert that result observations are correctly associated with hunting days.
            allObservations.stream()
                    .filter(observation -> newObservationIds.contains(observation.getId()))
                    .forEach(observation -> {
                        assertTrue(resultHuntingDays.contains(observation.getHuntingDayOfGroup()));
                        assertEquals(
                                observation.getHuntingDayOfGroup().getStartDate(),
                                observation.getPointOfTimeAsLocalDate());
                    });
        });
    }

    @Test
    public void testGetDatesOfMissingHuntingDays() {
        final Person author = model().newPerson();
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final LocalDate today = today();

        final GroupHuntingDay huntingDay1 = model().newGroupHuntingDay(group, today);
        final Harvest harvest1 = model().newHarvest(author, huntingDay1);
        final Observation observation1 = model().newObservation(author, huntingDay1);

        final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(group, today.plusDays(1));
        final Harvest harvest2 = model().newHarvest(author, huntingDay2);

        final GroupHuntingDay huntingDay3 = model().newGroupHuntingDay(group, today.plusDays(2));
        final Observation observation2 = model().newObservation(author, huntingDay3);

        final GroupHuntingDay huntingDay4 = model().newGroupHuntingDay(group, today.plusDays(3));

        final Harvest harvest3 = model().newHarvest(author, today.plusDays(4));
        final Observation observation3 = model().newObservation(author, today.plusDays(4));

        final Harvest harvest4 = model().newHarvest(author, today.plusDays(5));

        final Observation observation4 = model().newObservation(author, today.plusDays(6));

        final List<LocalDate> expectedDates = asList(today.plusDays(4), today.plusDays(5), today.plusDays(6));

        assertEquals(expectedDates, MooseDataCardImportHelper.getDatesOfMissingHuntingDays(
                asList(huntingDay1, huntingDay2, huntingDay3, huntingDay4),
                asList(harvest1, harvest2, harvest3, harvest4),
                asList(observation1, observation2, observation3, observation4)));
    }

    @Test
    @Transactional
    public void testAssignClubMembershipToPerson_whenPersonNotMemberOfClub_andReportingPeriodBeginInPast() {
        testAssignClubMembershipToPerson_whenPersonNotMemberOfClub(today().minusDays(1));
    }

    @Test
    @Transactional
    public void testAssignClubMembershipToPerson_whenPersonNotMemberOfClub_andReportingPeriodBeginInFuture() {
        testAssignClubMembershipToPerson_whenPersonNotMemberOfClub(today().plusMonths(1));
    }

    private void testAssignClubMembershipToPerson_whenPersonNotMemberOfClub(final LocalDate reportingPeriodBeginDate) {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();

        persistInCurrentlyOpenTransaction();

        final LocalDate expectedOccupationBeginDate =
                F.getFirstByNaturalOrderNullsFirst(reportingPeriodBeginDate, today());

        assertSuccess(helper.assignClubMembershipToPerson(person, club, reportingPeriodBeginDate), occupation -> {
            assertEquals(club, occupation.getOrganisation());
            assertEquals(person, occupation.getPerson());
            assertEquals(OccupationType.SEURAN_JASEN, occupation.getOccupationType());
            assertEquals(expectedOccupationBeginDate, occupation.getBeginDate());
            assertNull(occupation.getEndDate());
        });

        assertEquals(1, occupationRepo.findAll().size());
    }

    @Test
    @Transactional
    public void testAssignClubMembershipToPerson_whenPersonNonActiveMemberOfClub() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();

        final LocalDate reportingPeriodBeginDate = today();

        model().newOccupation(
                club, person, OccupationType.SEURAN_YHDYSHENKILO, null, reportingPeriodBeginDate.minusDays(1));

        persistInCurrentlyOpenTransaction();

        assertSuccess(helper.assignClubMembershipToPerson(person, club, reportingPeriodBeginDate), occupation -> {
            assertEquals(club, occupation.getOrganisation());
            assertEquals(person, occupation.getPerson());
            assertEquals(OccupationType.SEURAN_JASEN, occupation.getOccupationType());
            assertEquals(reportingPeriodBeginDate, occupation.getBeginDate());
            assertNull(occupation.getEndDate());
        });

        assertEquals(2, occupationRepo.findAll().size());
    }

    @Test
    @Transactional
    public void testAssignClubMembershipToPerson_whenPersonAlreadyActiveMemberOfClub() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        final LocalDate today = today();

        // Non-overlapping occupations active in the past, present or future.
        final Occupation occupation1 = model().newOccupation(
                club, person, OccupationType.SEURAN_YHDYSHENKILO, null, today.minusMonths(3).minusDays(1));
        final Occupation occupation2 = model().newOccupation(
                club, person, OccupationType.SEURAN_JASEN, today.minusMonths(3), today.minusMonths(2).minusDays(1));
        final Occupation occupation3 = model().newOccupation(
                club, person, OccupationType.SEURAN_JASEN, today.minusMonths(2), today.minusDays(1));
        final Occupation occupation4 = model().newOccupation(
                club, person, OccupationType.SEURAN_YHDYSHENKILO, today.minusMonths(1), today.minusDays(1));
        final Occupation occupation5 = model().newOccupation(
                club, person, OccupationType.SEURAN_JASEN, today, today.plusMonths(1).minusDays(1));
        final Occupation occupation6 = model().newOccupation(
                club, person, OccupationType.SEURAN_YHDYSHENKILO, today.plusMonths(2), null);

        // Deleted occupations should not be taken into account.
        final Occupation deletedOccupation1 = model().newOccupation(
                club, person, OccupationType.SEURAN_JASEN, today.minusMonths(2), today.minusMonths(1));
        deletedOccupation1.softDelete();
        final Occupation deletedOccupation2 =
                model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO, today, null);
        deletedOccupation2.softDelete();

        persistInCurrentlyOpenTransaction();

        final int totalOccupations = occupationRepo.findAll().size();

        final BiConsumer<LocalDate, Occupation> assertionPerformer = (periodBeginDate, expectedOccupation) -> {

            final LocalDate originalBeginDate = expectedOccupation.getBeginDate();
            final LocalDate originalEndDate = expectedOccupation.getEndDate();
            final OccupationType originalOccupationType = expectedOccupation.getOccupationType();

            assertSuccess(helper.assignClubMembershipToPerson(person, club, periodBeginDate), occupation -> {

                // Verify that new occupations are not created as side effect.
                assertEquals(totalOccupations, occupationRepo.findAll().size());

                // Result may be null.
                final LocalDate expectedBeginDate =
                        F.getFirstByNaturalOrderNullsFirst(originalBeginDate, periodBeginDate, today());

                assertEquals(expectedOccupation, occupation);
                assertEquals(expectedBeginDate, occupation.getBeginDate());

                // Verify that these occupation fields/associations are not mutated.
                assertEquals(club, occupation.getOrganisation());
                assertEquals(person, occupation.getPerson());
                assertEquals(originalOccupationType, occupation.getOccupationType());
                assertEquals(originalEndDate, occupation.getEndDate());

                // Restore original test fixture for next test round.
                occupation.setBeginDate(originalBeginDate);
            });
        };

        assertionPerformer.accept(null, occupation1);
        assertionPerformer.accept(occupation2.getEndDate(), occupation2);
        assertionPerformer.accept(occupation3.getBeginDate(), occupation3);
        assertionPerformer.accept(occupation4.getEndDate(), occupation3);
        assertionPerformer.accept(today, occupation5);
        assertionPerformer.accept(occupation6.getBeginDate().minusDays(1), occupation6);
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenNonImportOriginatedGroupExists() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            model().newHuntingClubGroup(club, hpsa);

            persistInCurrentlyOpenTransaction();

            assertFailure(invokeFindGroupService(club, hpsa, contactPerson),
                    MooseDataCardImportFailureReasons.huntingClubAlreadyHasGroupNotCreatedWithinMooseDataCardImport());
        }));
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenPersonAlreadyMemberOfOneGroup() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            final HuntingClubGroup group = model().newHuntingClubGroup(club, hpsa);
            group.setFromMooseDataCard(true);

            model().newOccupation(group, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            persistInCurrentlyOpenTransaction();

            assertSuccess(group, invokeFindGroupService(club, hpsa, contactPerson));
        }));
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenPersonIsMemberInMultipeGroups_butNoActiveOccupations() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            final HuntingClubGroup group1 = model().newHuntingClubGroup(club, hpsa);
            group1.setFromMooseDataCard(true);
            model().newDeletedOccupation(group1, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            final HuntingClubGroup group2 = model().newHuntingClubGroup(club, hpsa);
            group2.setFromMooseDataCard(true);
            model().newDeletedOccupation(group2, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            persistInCurrentlyOpenTransaction();

            assertFailure(invokeFindGroupService(club, hpsa, contactPerson),
                    MooseDataCardImportFailureReasons
                            .contactPersonMemberOfMultipleMooseDataCardGroupsButWithNoActiveOccupations());
        }));
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenPersonIsMemberInMultipeGroups_butActiveInOnlyOne() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            final HuntingClubGroup group1 = model().newHuntingClubGroup(club, hpsa);
            group1.setFromMooseDataCard(true);
            model().newDeletedOccupation(group1, contactPerson, OccupationType.RYHMAN_JASEN);

            final HuntingClubGroup group2 = model().newHuntingClubGroup(club, hpsa);
            group2.setFromMooseDataCard(true);
            model().newOccupation(group2, contactPerson, OccupationType.RYHMAN_JASEN);

            persistInCurrentlyOpenTransaction();

            assertSuccess(group2, invokeFindGroupService(club, hpsa, contactPerson));
        }));
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenPersonIsActiveMemberInMultipeGroups_butNotAsLeader() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            final HuntingClubGroup group1 = model().newHuntingClubGroup(club, hpsa);
            group1.setFromMooseDataCard(true);
            model().newOccupation(group1, contactPerson, OccupationType.RYHMAN_JASEN);

            final HuntingClubGroup group2 = model().newHuntingClubGroup(club, hpsa);
            group2.setFromMooseDataCard(true);
            model().newOccupation(group2, contactPerson, OccupationType.RYHMAN_JASEN);

            persistInCurrentlyOpenTransaction();

            assertFailure(invokeFindGroupService(club, hpsa, contactPerson),
                    MooseDataCardImportFailureReasons.contactPersonMemberOfMultipleMooseDataCardGroupsButNotAsLeader());
        }));
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenPersonIsActiveMemberInMultipeGroups_asLeaderInMany() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            final HuntingClubGroup group1 = model().newHuntingClubGroup(club, hpsa);
            group1.setFromMooseDataCard(true);
            model().newOccupation(group1, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            final HuntingClubGroup group2 = model().newHuntingClubGroup(club, hpsa);
            group2.setFromMooseDataCard(true);
            model().newOccupation(group2, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            persistInCurrentlyOpenTransaction();

            assertFailure(invokeFindGroupService(club, hpsa, contactPerson),
                    MooseDataCardImportFailureReasons.contactPersonIsLeaderInMultipleMooseDataCardGroups());
        }));
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenPersonIsActiveMemberInMultipeGroups_asLeaderInOne() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            final HuntingClubGroup group1 = model().newHuntingClubGroup(club, hpsa);
            group1.setFromMooseDataCard(true);
            model().newOccupation(group1, contactPerson, OccupationType.RYHMAN_JASEN);

            final HuntingClubGroup group2 = model().newHuntingClubGroup(club, hpsa);
            group2.setFromMooseDataCard(true);
            model().newOccupation(group2, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            persistInCurrentlyOpenTransaction();

            assertSuccess(group2, invokeFindGroupService(club, hpsa, contactPerson));
        }));
    }

    @Test
    @Transactional
    public void testFindOrCreateGroupForMooseDataCardImport_whenNoMooseDataGroupExistsForClubAndPermit() {
        withSpeciesAmountAndClub((hpsa, club) -> withPerson(contactPerson -> {

            final HarvestPermit anotherMoosePermit = model().newHarvestPermit(hpsa.getHarvestPermit().getRhy());
            final HarvestPermitSpeciesAmount anotherMooseAmount =
                    model().newHarvestPermitSpeciesAmount(anotherMoosePermit, hpsa.getGameSpecies());

            final HuntingClubGroup anotherMooseDataCardGroup = model().newHuntingClubGroup(club, anotherMooseAmount);
            anotherMooseDataCardGroup.setFromMooseDataCard(true);
            model().newOccupation(anotherMooseDataCardGroup, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            final HarvestPermitSpeciesAmount deerAmount =
                    model().newHarvestPermitSpeciesAmount(hpsa.getHarvestPermit(), model().newGameSpecies());
            final HuntingClubGroup deerGroup = model().newHuntingClubGroup(club, deerAmount);
            model().newOccupation(deerGroup, contactPerson, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            persistInCurrentlyOpenTransaction();

            assertSuccess(invokeFindGroupService(club, hpsa, contactPerson), group -> {
                assertFalse(group.isNew());
                assertEquals(club, HuntingClub.class.cast(group.getParentOrganisation()));
                assertEquals(hpsa.getGameSpecies(), group.getSpecies());
                assertEquals(hpsa.getHarvestPermit(), group.getHarvestPermit());
                assertEquals(hpsa.resolveHuntingYear(), group.getHuntingYear());
                assertTrue(group.getNameLocalisation().asStream()
                        .allMatch(HuntingClubGroup::isNameReservedForMooseDataCardGroups));

                final List<Occupation> allOccupations = occupationRepo.findByOrganisation(group);
                assertEquals(1, allOccupations.size());

                final Occupation occupation = allOccupations.iterator().next();
                assertFalse(occupation.isNew());
                assertEquals(contactPerson, occupation.getPerson());
                assertEquals(group, occupation.getOrganisation());
                assertEquals(OccupationType.RYHMAN_METSASTYKSENJOHTAJA, occupation.getOccupationType());
                assertEquals(Integer.valueOf(0), occupation.getCallOrder());
                assertFalse(occupation.hasBeginAndEndDate());
                assertNull(occupation.getLifecycleFields().getDeletionTime());
            });
        }));
    }

    private Try<HuntingClubGroup> invokeFindGroupService(
            final HuntingClub club, final HarvestPermitSpeciesAmount speciesAmount, final Person contactPerson) {

        final HarvestPermit permit = speciesAmount.getHarvestPermit();
        final GameSpecies species = speciesAmount.getGameSpecies();
        final int huntingYear = speciesAmount.resolveHuntingYear();

        return helper.findOrCreateGroupForMooseDataCardImport(
                club, permit, species, huntingYear, contactPerson, null, null);
    }

    @Test
    public void testGenerateNameForMooseDataCardGroupUsingHunterNumber() {
        final String permitNumber = permitNumber();
        final String hunterNumber = hunterNumber();

        assertGroupName(
                helper.generateNameForMooseDataCardGroupUsingHunterNumber(permitNumber, hunterNumber),
                String.format(" %s %s", permitNumber, hunterNumber));
    }

    @Test
    public void testGenerateNameForMooseDataCardGroupUsingSsn() {
        final String permitNumber = permitNumber();
        final String ssn = ssn();

        assertGroupName(
                helper.generateNameForMooseDataCardGroupUsingSsn(permitNumber, ssn),
                String.format(" %s %s", permitNumber, ssn.substring(0, 6)));
    }

    private static void assertGroupName(final LocalisedString groupName, final String expectedNamePostfix) {
        groupName.asStream().forEach(name -> {
            assertNotNull(name);
            assertTrue(
                    String.format("Group name does not end with \"%s\": %s", expectedNamePostfix, name),
                    name.endsWith(expectedNamePostfix));
            assertTrue(HuntingClubGroup.isNameReservedForMooseDataCardGroups(name));
        });
    }

    private static void assertFailure(final Try<?> tryObj, final String expectedMessage) {
        assertFailure(tryObj, Collections.singletonList(expectedMessage));
    }

    private static void assertFailure(final Try<?> tryObj, final List<String> expectedMessages) {
        Asserts.assertFailure(MooseDataCardImportException.class, tryObj);
        assertEquals(expectedMessages, MooseDataCardImportException.class.cast(tryObj.getCause()).getMessages());
    }

    private String permitNumber() {
        return permitNumber("001");
    }

    private void withSpeciesAmountAndClub(final BiConsumer<HarvestPermitSpeciesAmount, HuntingClub> testBody) {
        withRhy(rhy -> {
            final GameSpecies mooseSpecies = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount hpsa = model().newHarvestPermitSpeciesAmount(permit, mooseSpecies);

            testBody.accept(hpsa, model().newHuntingClub(rhy));
        });
    }

}
