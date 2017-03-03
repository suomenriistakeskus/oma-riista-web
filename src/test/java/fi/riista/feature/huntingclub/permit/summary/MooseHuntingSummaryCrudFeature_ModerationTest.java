package fi.riista.feature.huntingclub.permit.summary;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReport;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportDoneException;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportRepository;
import fi.riista.feature.huntingclub.support.HuntingClubTestDataHelper;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.Asserts;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static fi.riista.util.Asserts.assertEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MooseHuntingSummaryCrudFeature_ModerationTest extends EmbeddedDatabaseTest {

    @Resource
    private MooseHuntingSummaryCrudFeature feature;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepo;

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepo;

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepo;

    private Person person;

    private GameSpecies species;
    private GameSpecies otherSpecies;
    private HuntingClub permitHolder;
    private HuntingClub permitPartner;

    private HarvestPermit permit;
    private HuntingClubGroup holderGroup;
    private HuntingClubGroup partnerGroup;
    private HarvestPermitSpeciesAmount speciesAmount;

    private HarvestPermit nextYearPermit;
    private HarvestPermitSpeciesAmount nextYearSpeciesAmount;

    private final HuntingClubTestDataHelper testDataHelper = new HuntingClubTestDataHelper() {
        @Override
        protected EntitySupplier model() {
            return MooseHuntingSummaryCrudFeature_ModerationTest.this.model();
        }
    };

    @Before
    public void before() {
        species = model().newGameSpeciesMoose();
        otherSpecies = model().newGameSpecies(1, GameCategory.GAME_MAMMAL, "muu", "annan", "other");

        final int huntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();

        withRhy(rhy -> withPerson(person -> {
            this.person = person;

            permitHolder = model().newHuntingClub(rhy);
            permitPartner = model().newHuntingClub(rhy);

            permit = createPermit(rhy);
            speciesAmount = createSpeciesAmount(permit, huntingYear);

            holderGroup = model().newHuntingClubGroup(permitHolder, speciesAmount);
            partnerGroup = model().newHuntingClubGroup(permitPartner, speciesAmount);

            model().newMooselikePrice(huntingYear, species);

            nextYearPermit = createPermit(rhy);
            nextYearSpeciesAmount = createSpeciesAmount(nextYearPermit, huntingYear + 1);
            model().newMooselikePrice(huntingYear + 1, species);

            model().newOccupation(permitPartner, person, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(person, partnerGroup);
        }));
    }

    private HarvestPermit createPermit(Riistanhoitoyhdistys rhy) {
        final HarvestPermit p = model().newMooselikePermit(rhy);
        p.setPermitHolder(permitHolder);
        Stream.of(permitHolder, permitPartner).forEach(p.getPermitPartners()::add);
        return p;
    }

    private HarvestPermitSpeciesAmount createSpeciesAmount(final HarvestPermit permit, final int huntingYear) {
        final HarvestPermitSpeciesAmount s = model().newHarvestPermitSpeciesAmount(permit, species);
        s.setCreditorReference(creditorReference());
        s.setBeginDate(DateUtil.huntingYearBeginDate(huntingYear));
        s.setEndDate(new LocalDate(huntingYear, 12, 31));
        return s;
    }

    @Test
    public void testGetHuntingSummariesForModeration_inCaseOfMooseSpecies() {
        final HuntingClub partner2 = model().newHuntingClub(permit.getRhy());
        final HuntingClub partner3 = model().newHuntingClub(permit.getRhy());
        final HuntingClub partner4 = model().newHuntingClub(permit.getRhy());
        final HuntingClub partner5 = model().newHuntingClub(permit.getRhy());
        Stream.of(partner2, partner3, partner4, partner5).forEach(permit.getPermitPartners()::add);

        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInNewTransaction();

        final MooseHuntingSummary finishedMooseSummary = model().newMooseHuntingSummary(permit, permitHolder, true);
        final BasicClubHuntingSummary moderatedSummary =
                model().newModeratedBasicHuntingSummary(speciesAmount, permitPartner);
        final MooseHuntingSummary unfinishedMooseSummary = model().newMooseHuntingSummary(permit, partner3, false);

        final MooseHuntingSummary unfinishedMooseSummary2 = model().newMooseHuntingSummary(permit, partner4, false);
        unfinishedMooseSummary2.getAreaSizeAndPopulation().setRemainingPopulationInTotalArea(null);
        unfinishedMooseSummary2.getAreaSizeAndPopulation().setRemainingPopulationInEffectiveArea(null);

        final MooseHuntingSummary unfinishedMooseSummary3 = model().newMooseHuntingSummary(permit, partner5, false);
        unfinishedMooseSummary3.getAreaSizeAndPopulation().setTotalHuntingArea(100);
        unfinishedMooseSummary3.getAreaSizeAndPopulation().setEffectiveHuntingArea(null);
        unfinishedMooseSummary3.setEffectiveHuntingAreaPercentage(80F);

        final HuntingClubGroup partner2Group = model().newHuntingClubGroup(partner2, speciesAmount);
        model().newHuntingClubGroupMember(person, partner2Group);

        final HasHarvestCountsForPermit harvestCountsForHolder = testDataHelper.generateHarvestCounts();
        testDataHelper.createHarvestsForHuntingGroup(holderGroup, person, harvestCountsForHolder);

        final HasHarvestCountsForPermit harvestCountsForPartner2 = testDataHelper.generateHarvestCounts();
        testDataHelper.createHarvestsForHuntingGroup(partner2Group, person, harvestCountsForPartner2);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final List<BasicClubHuntingSummaryDTO> summaries =
                    feature.getHuntingSummariesForModeration(permit.getId(), species.getOfficialCode()).stream()
                            .sorted(comparing(BasicClubHuntingSummaryDTO::getClubId))
                            .collect(toList());

            assertEquals(
                    asList(false, true, false, false, false, false),
                    F.mapNonNullsToList(summaries, BasicClubHuntingSummaryDTO::isModeratorOverridden));

            final List<AreaSizeAndRemainingPopulation> expectedAreasAndPopulations = asList(
                    finishedMooseSummary.getAreaSizeAndPopulation(),
                    moderatedSummary.getAreaSizeAndPopulation(),
                    new AreaSizeAndRemainingPopulation(),
                    unfinishedMooseSummary.getAreaSizeAndPopulation(),
                    unfinishedMooseSummary2.getAreaSizeAndPopulation(),
                    unfinishedMooseSummary3.getAreaSizeAndPopulation()
                            .calculateMissingValues(unfinishedMooseSummary3.getEffectiveHuntingAreaPercentage(), 0));

            Asserts.assertEqualAfterTransformation(
                    expectedAreasAndPopulations,
                    F.mapNonNullsToList(summaries, BasicClubHuntingSummaryDTO::getAreaSizeAndRemainingPopulation),
                    AreaSizeAndRemainingPopulation::asTuple);

            final List<HasHarvestCountsForPermit> expectedHarvestCounts = asList(
                    harvestCountsForHolder, moderatedSummary.getHarvestCounts(), harvestCountsForPartner2,
                    HasHarvestCountsForPermit.zeros(), HasHarvestCountsForPermit.zeros(), HasHarvestCountsForPermit.zeros());

            Asserts.assertEqualAfterTransformation(
                    expectedHarvestCounts,
                    F.mapNonNullsToList(summaries, BasicClubHuntingSummaryDTO::getHarvestCounts),
                    HasHarvestCountsForPermit::asTuple);
        });
    }

    @Test
    public void testGetHuntingSummariesForModeration_inCaseOfNonMooseSpecies() {
        species.setOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE + 1);

        final HuntingClub anotherPartner = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(anotherPartner);

        final BasicClubHuntingSummary basicSummary = model().newBasicHuntingSummary(speciesAmount, permitHolder, true);
        final BasicClubHuntingSummary moderatedSummary =
                model().newModeratedBasicHuntingSummary(speciesAmount, permitPartner);

        final HuntingClubGroup anotherPartnerGroup = model().newHuntingClubGroup(anotherPartner, speciesAmount);
        model().newHuntingClubGroupMember(person, anotherPartnerGroup);

        final HasHarvestCountsForPermit harvestCountsForHolder = testDataHelper.generateHarvestCounts();
        testDataHelper.createHarvestsForHuntingGroup(holderGroup, person, harvestCountsForHolder);

        final HasHarvestCountsForPermit harvestCountsForAnotherPartner = testDataHelper.generateHarvestCounts();
        testDataHelper.createHarvestsForHuntingGroup(anotherPartnerGroup, person, harvestCountsForAnotherPartner);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final List<BasicClubHuntingSummaryDTO> summaries =
                    feature.getHuntingSummariesForModeration(permit.getId(), species.getOfficialCode()).stream()
                            .sorted(comparing(BasicClubHuntingSummaryDTO::getClubId))
                            .collect(toList());

            assertEquals(
                    asList(false, true, false),
                    F.mapNonNullsToList(summaries, BasicClubHuntingSummaryDTO::isModeratorOverridden));

            final List<AreaSizeAndRemainingPopulation> expectedAreasAndPopulations = asList(
                    basicSummary.getAreaSizeAndPopulation(), moderatedSummary.getAreaSizeAndPopulation(),
                    new AreaSizeAndRemainingPopulation());

            Asserts.assertEqualAfterTransformation(
                    expectedAreasAndPopulations,
                    F.mapNonNullsToList(summaries, BasicClubHuntingSummaryDTO::getAreaSizeAndRemainingPopulation),
                    AreaSizeAndRemainingPopulation::asTuple);

            final List<HasHarvestCountsForPermit> expectedHarvestCounts =
                    asList(harvestCountsForHolder, moderatedSummary.getHarvestCounts(), harvestCountsForAnotherPartner);

            Asserts.assertEqualAfterTransformation(
                    expectedHarvestCounts,
                    F.mapNonNullsToList(summaries, BasicClubHuntingSummaryDTO::getHarvestCounts),
                    HasHarvestCountsForPermit::asTuple);
        });
    }

    @Test
    public void testProcessModeratorOverriddenHuntingSummaries_forChangesPersisted_inCaseOfMooseSpecies() {
        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInNewTransaction();

        final MooseHuntingSummary summary = model().newMooseHuntingSummary(permit, permitHolder, false);
        summary.setHuntingEndDate(speciesAmount.getEndDate());

        testProcessModeratorOverriddenHuntingSummaries_forChangesPersisted();
    }

    @Test
    public void testProcessModeratorOverriddenHuntingSummaries_forChangesPersisted_inCaseOfNonMooseSpecies() {
        species.setOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE + 1);

        final BasicClubHuntingSummary summary = model().newBasicHuntingSummary(speciesAmount, permitHolder, false);
        summary.setHuntingEndDate(speciesAmount.getEndDate());

        testProcessModeratorOverriddenHuntingSummaries_forChangesPersisted();
    }

    private void testProcessModeratorOverriddenHuntingSummaries_forChangesPersisted() {
        onSavedAndAuthenticated(createNewModerator(), () -> {

            final int speciesCode = species.getOfficialCode();

            final List<BasicClubHuntingSummaryDTO> inputs = prepareModeratedSummaries(speciesCode);
            assertEquals(2, inputs.size());

            // This should not be changed.
            final LocalDate huntingEndDateOfPermitHolder = inputs.get(0).getHuntingEndDate();

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);

            final List<BasicClubHuntingSummary> allBasicSummaries = basicHuntingSummaryRepo.findAll().stream()
                    .sorted(comparing(s -> s.getClub().getId()))
                    .collect(toList());
            assertEquals(2, allBasicSummaries.size());

            final List<Tuple3<Long, LocalDate, Boolean>> expectedTriples = asList(
                    Tuple.of(permitHolder.getId(), huntingEndDateOfPermitHolder, true),
                    Tuple.of(permitPartner.getId(), speciesAmount.getLastDate(), true));

            assertEquals(expectedTriples, allBasicSummaries.stream()
                    .map(s -> Tuple.of(s.getClub().getId(), s.getHuntingEndDate(), s.isModeratorOverride()))
                    .collect(toList()));

            Asserts.assertEqualAfterTransformation(
                    F.mapNonNullsToList(inputs, BasicClubHuntingSummaryDTO::getAreaSizeAndRemainingPopulation),
                    F.mapNonNullsToList(allBasicSummaries, BasicClubHuntingSummary::getAreaSizeAndPopulation),
                    AreaSizeAndRemainingPopulation::asTuple);

            Asserts.assertEqualAfterTransformation(
                    F.mapNonNullsToList(inputs, BasicClubHuntingSummaryDTO::getHarvestCounts),
                    F.mapNonNullsToList(allBasicSummaries, BasicClubHuntingSummary::getHarvestCounts),
                    HasHarvestCountsForPermit::asTuple);
        });
    }

    @Test
    public void testProcessModeratorOverriddenHuntingSummaries_unfinishedMooseSummaryFinishedButNotOverridden() {
        // Create another permit for which partner is added as a permit holder.
        final HarvestPermit anotherPermit = model().newHarvestPermit(permit.getRhy());
        anotherPermit.setPermitHolder(permitPartner);
        anotherPermit.getPermitPartners().add(permitPartner);
        model().newHarvestPermitSpeciesAmount(anotherPermit, species);

        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInNewTransaction();

        final MooseHuntingSummary summary = model().newMooseHuntingSummary(permit, permitHolder, false);
        summary.setHuntingEndDate(speciesAmount.getEndDate().minusDays(1));

        final MooseHuntingSummary summary2 = model().newMooseHuntingSummary(permit, permitPartner, false);
        summary2.setHuntingEndDate(null);

        final MooseHuntingSummary summary3 = model().newMooseHuntingSummary(anotherPermit, permitPartner, false);
        summary3.setHuntingEndDate(null);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final int speciesCode = species.getOfficialCode();

            final List<BasicClubHuntingSummaryDTO> inputs = prepareModeratedSummaries(speciesCode);
            inputs.forEach(s -> s.setModeratorOverridden(false));
            assertEquals(2, inputs.size());

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);

            runInTransaction(() -> {
                assertEmpty(basicHuntingSummaryRepo.findAll());

                final MooseHuntingSummary reloadedSummary = mooseHuntingSummaryRepo.getOne(summary.getId());
                final MooseHuntingSummary reloadedSummary2 = mooseHuntingSummaryRepo.getOne(summary2.getId());
                final MooseHuntingSummary reloadedSummary3 = mooseHuntingSummaryRepo.getOne(summary3.getId());

                assertTrue(reloadedSummary.isHuntingFinished());
                assertEquals(summary.getHuntingEndDate(), reloadedSummary.getHuntingEndDate());

                assertTrue(reloadedSummary2.isHuntingFinished());
                assertEquals(speciesAmount.getEndDate(), reloadedSummary2.getHuntingEndDate());

                // This summary should not be marked finished as it is related to different permit.
                assertFalse(reloadedSummary3.isHuntingFinished());
                assertNull(reloadedSummary3.getHuntingEndDate());
            });
        });
    }

    @Test
    public void testProcessModeratorOverriddenHuntingSummaries_unfinishedBasicSummaryFinishedButNotOverridden() {
        final int speciesCode = GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
        species.setOfficialCode(speciesCode);

        // Create another permit for which partner is added as a permit holder.
        final HarvestPermit anotherPermit = model().newHarvestPermit(permit.getRhy());
        anotherPermit.setPermitHolder(permitPartner);
        anotherPermit.getPermitPartners().add(permitPartner);
        final HarvestPermitSpeciesAmount anotherSpa = model().newHarvestPermitSpeciesAmount(anotherPermit, species);

        final BasicClubHuntingSummary summary = model().newBasicHuntingSummary(speciesAmount, permitHolder, false);
        summary.setHuntingEndDate(speciesAmount.getEndDate().minusDays(1));

        final BasicClubHuntingSummary summary2 = model().newBasicHuntingSummary(speciesAmount, permitPartner, false);
        summary2.setHuntingEndDate(null);

        final BasicClubHuntingSummary summary3 = model().newBasicHuntingSummary(anotherSpa, permitPartner, false);
        summary3.setHuntingEndDate(null);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final List<BasicClubHuntingSummaryDTO> inputs = prepareModeratedSummaries(speciesCode);
            inputs.forEach(s -> s.setModeratorOverridden(false));
            assertEquals(2, inputs.size());

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);

            runInTransaction(() -> {
                final BasicClubHuntingSummary reloadedSummary = basicHuntingSummaryRepo.getOne(summary.getId());
                final BasicClubHuntingSummary reloadedSummary2 = basicHuntingSummaryRepo.getOne(summary2.getId());
                final BasicClubHuntingSummary reloadedSummary3 = basicHuntingSummaryRepo.getOne(summary3.getId());

                assertTrue(reloadedSummary.isHuntingFinished());
                assertEquals(summary.getHuntingEndDate(), reloadedSummary.getHuntingEndDate());

                assertTrue(reloadedSummary2.isHuntingFinished());
                assertEquals(speciesAmount.getEndDate(), reloadedSummary2.getHuntingEndDate());

                // This summary should not be marked finished as it is related to different permit.
                assertFalse(reloadedSummary3.isHuntingFinished());
                assertNull(reloadedSummary3.getHuntingEndDate());
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessModeratorOverriddenHuntingSummaries_failBecauseOfHarvestPermitInconsistency() {
        onSavedAndAuthenticated(createNewModerator(), () -> {

            final int speciesCode = species.getOfficialCode();

            final List<BasicClubHuntingSummaryDTO> inputs =
                    feature.getHuntingSummariesForModeration(permit.getId(), speciesCode);
            assertTrue(inputs.size() >= 1);
            inputs.get(0).setHarvestPermitId(nextYearPermit.getId());

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessModeratorOverriddenHuntingSummaries_failBecauseOfGameSpeciesInconsistency() {
        onSavedAndAuthenticated(createNewModerator(), () -> {

            final int speciesCode = species.getOfficialCode();

            final List<BasicClubHuntingSummaryDTO> inputs =
                    feature.getHuntingSummariesForModeration(permit.getId(), speciesCode);
            assertTrue(inputs.size() >= 1);
            inputs.get(0).setGameSpeciesCode(otherSpecies.getOfficialCode());

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessModeratorOverriddenHuntingSummaries_failBecauseClubsNotSelectedToBeOverridden() {
        onSavedAndAuthenticated(createNewModerator(), () -> {

            final int speciesCode = species.getOfficialCode();

            final List<BasicClubHuntingSummaryDTO> inputs =
                    feature.getHuntingSummariesForModeration(permit.getId(), speciesCode);

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void testProcessModeratorOverriddenHuntingSummaries_failBecauseMissingDataViolatesDbConstraint() {
        onSavedAndAuthenticated(createNewModerator(), () -> {

            final int speciesCode = species.getOfficialCode();

            final List<BasicClubHuntingSummaryDTO> inputs =
                    feature.getHuntingSummariesForModeration(permit.getId(), speciesCode).stream()
                            .map(dto -> {
                                dto.setModeratorOverridden(true);
                                dto.setHuntingFinished(true);
                                return dto;
                            })
                            .collect(toList());

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessModeratorOverriddenHuntingSummaries_failBecauseOfIncompleteClubCoverage() {
        onSavedAndAuthenticated(createNewModerator(), () -> {

            final int speciesCode = species.getOfficialCode();

            final List<BasicClubHuntingSummaryDTO> inputs =
                    feature.getHuntingSummariesForModeration(permit.getId(), speciesCode).stream()
                            .map(input -> input.withAreaSizeAndRemainingPopulation(new AreaSizeAndRemainingPopulation()
                                    .withTotalHuntingArea(5000)
                                    .withEffectiveHuntingArea(4000)
                                    .withRemainingPopulationInTotalArea(3000)
                                    .withRemainingPopulationInEffectiveArea(2000)))
                            // Only permit holder club covered.
                            .limit(1)
                            .collect(toList());

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);
        });
    }

    @Test(expected = MooseHarvestReportDoneException.class)
    public void testProcessModeratorOverriddenHuntingSummaries_failBecauseOfExistingMooseHarvestReport() {
        model().newMooseHarvestReport(speciesAmount);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final int speciesCode = species.getOfficialCode();
            final List<BasicClubHuntingSummaryDTO> inputs = prepareModeratedSummaries(speciesCode);
            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);
        });
    }

    @Test
    public void testProcessModeratorOverriddenHuntingSummaries_okBecauseOfExistingMooseHarvestReportIsModeratorOverride() {
        model().newMooseHarvestReportModeratorOverride(speciesAmount);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final int speciesCode = species.getOfficialCode();
            final List<BasicClubHuntingSummaryDTO> inputs = prepareModeratedSummaries(speciesCode);
            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), speciesCode, true, inputs);
        });
    }

    private List<BasicClubHuntingSummaryDTO> prepareModeratedSummaries(int speciesCode) {
        return feature.getHuntingSummariesForModeration(permit.getId(), speciesCode).stream()
                .map(input -> {
                    input.setModeratorOverridden(true);

                    final int i = nextIntBetween(1, 499);

                    input.withAreaSizeAndRemainingPopulation(new AreaSizeAndRemainingPopulation()
                            .withTotalHuntingArea(5000 + i)
                            .withEffectiveHuntingArea(4000 + i)
                            .withRemainingPopulationInTotalArea(3000 + i)
                            .withRemainingPopulationInEffectiveArea(2000 + i));

                    input.setNumberOfAdultMales(600 + i);
                    input.setNumberOfAdultFemales(500 + i);
                    input.setNumberOfYoungMales(400 + i);
                    input.setNumberOfYoungFemales(300 + i);
                    input.setNumberOfNonEdibleAdults(200 + i);
                    input.setNumberOfNonEdibleYoungs(100 + i);

                    return input;
                })
                .sorted(comparing(s -> s.getClubId()))
                .collect(toList());
    }

    @Test
    public void testRevokeHuntingSummaryModeration_inCaseOfMooseSpecies() {
        final HuntingClub partner2 = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(partner2);

        // Create another permit for which partner is added as a permit holder.
        final HarvestPermit anotherPermit = model().newHarvestPermit(permit.getRhy());
        anotherPermit.setPermitHolder(permitPartner);
        anotherPermit.getPermitPartners().add(permitPartner);
        final HarvestPermitSpeciesAmount anotherSpa = model().newHarvestPermitSpeciesAmount(anotherPermit, species);

        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInNewTransaction();

        model().newMooseHarvestReportModeratorOverride(speciesAmount);
        model().newMooseHuntingSummary(permit, permitHolder, true);
        model().newModeratedBasicHuntingSummary(speciesAmount, permitPartner);
        model().newModeratedBasicHuntingSummary(speciesAmount, partner2);

        final BasicClubHuntingSummary moderatedSummaryForAnotherPermit =
                model().newModeratedBasicHuntingSummary(anotherSpa, permitPartner);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            feature.revokeHuntingSummaryModeration(permit.getId(), species.getOfficialCode());

            final List<BasicClubHuntingSummary> summariesAfterRevocation = basicHuntingSummaryRepo.findAll();
            assertEquals(1, summariesAfterRevocation.size());

            final BasicClubHuntingSummary remainingSummary = summariesAfterRevocation.get(0);
            assertEquals(moderatedSummaryForAnotherPermit.getId(), remainingSummary.getId());
            assertTrue(remainingSummary.isModeratorOverride());
            assertEmpty(mooseHarvestReportRepo.findAll());
        });
    }

    @Test
    public void testRevokeHuntingSummaryModeration_inCaseOfNonMooseSpecies() {
        species.setOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE + 1);

        // Create second partner for permit to extend test coverage of revocation effects.
        final HuntingClub partner2 = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(partner2);

        // Create another permit for which partner is added as a permit holder.
        final HarvestPermit anotherPermit = model().newHarvestPermit(permit.getRhy());
        anotherPermit.setPermitHolder(permitPartner);
        anotherPermit.getPermitPartners().add(permitPartner);
        final HarvestPermitSpeciesAmount anotherSpa = model().newHarvestPermitSpeciesAmount(anotherPermit, species);

        final BasicClubHuntingSummary holderSummary =
                model().newModeratedBasicHuntingSummary(speciesAmount, permitHolder);

        final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, BasicClubHuntingSummary> createModeratedSummaryFromOriginallyEmptySummary =
                (spa, club) -> {
                    final BasicClubHuntingSummary summary = model().newBasicHuntingSummary(spa, club, false);
                    summary.setHuntingEndDate(null);
                    summary.setAreaSizeAndPopulation(null);
                    summary.doModeratorOverride(
                            speciesAmount.getLastDate(),
                            new AreaSizeAndRemainingPopulation()
                                    .withTotalHuntingArea(45678)
                                    .withEffectiveHuntingArea(34567)
                                    .withRemainingPopulationInTotalArea(567)
                                    .withRemainingPopulationInEffectiveArea(234),
                            HasHarvestCountsForPermit.of(98, 87, 76, 65, 54, 43));
                    return summary;
                };

        // Although originally empty, this summary is not expected to be removed because it is not
        // originally created by moderator.
        final BasicClubHuntingSummary partnerSummary =
                createModeratedSummaryFromOriginallyEmptySummary.apply(speciesAmount, permitPartner);

        // This should not be affected by revocation because it is for different species.
        final BasicClubHuntingSummary summaryForAnotherPermit =
                model().newModeratedBasicHuntingSummary(anotherSpa, permitPartner);

        // MooseHarvestReport is expected to be removed within revocation.
        model().newMooseHarvestReportModeratorOverride(speciesAmount);

        // This should not be affected by revocation because it is for different species.
        final MooseHarvestReport reportForAnotherSpa = model().newMooseHarvestReportModeratorOverride(anotherSpa);

        final SystemUser moderator = createNewModerator();

        persistInNewTransaction();
        authenticate(moderator);

        // Moderator is set as creator of summary for partner2 due to previous authenticate call.
        // This summary is expected to be removed within revocation because it was originally
        // empty and created by moderator.
        createModeratedSummaryFromOriginallyEmptySummary.apply(speciesAmount, partner2);

        persistInNewTransaction();

        feature.revokeHuntingSummaryModeration(permit.getId(), species.getOfficialCode());

        final Set<Tuple2<Long, Boolean>> expected = ImmutableSet.of(
                Tuple.of(holderSummary.getId(), false),
                Tuple.of(partnerSummary.getId(), false),
                Tuple.of(summaryForAnotherPermit.getId(), true));

        assertEquals(expected, basicHuntingSummaryRepo.findAll().stream()
                .map(summary -> Tuple.of(summary.getId(), summary.isModeratorOverride()))
                .collect(toSet()));

        assertEquals(singleton(reportForAnotherSpa.getId()), F.getUniqueIds(mooseHarvestReportRepo.findAll()));
    }

    @Test(expected = AreaSizeAssertionHelper.TotalAreaSizeTooBigException.class)
    public void testUpdateSummary_inCaseOfTotalAreaTooBig() {
        complete(dto -> dto.setTotalHuntingArea(permit.getPermitAreaSize() + 1));
    }

    @Test(expected = AreaSizeAssertionHelper.EffectiveAreaSizeTooBigException.class)
    public void testUpdateSummary_inCaseOfEffectiveAreaTooBig() {
        complete(dto -> dto.setEffectiveHuntingArea(permit.getPermitAreaSize() + 1));
    }

    @Test(expected = AreaSizeAssertionHelper.TotalAreaSizeSmallerThanEffectiveAreaSizeException.class)
    public void testUpdateSummary_inCaseOfTotalAreaSmallerThanEffectiveArea() {
        complete(dto -> {
            dto.setTotalHuntingArea(1);
            dto.setEffectiveHuntingArea(dto.getTotalHuntingArea() + 1);
        });
    }

    private void complete(Consumer<BasicClubHuntingSummaryDTO> summaryMutator) {
        model().newBasicHuntingSummary(speciesAmount, permitHolder, false);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            List<BasicClubHuntingSummaryDTO> dtos = feature.getHuntingSummariesForModeration(permit.getId(), species.getOfficialCode());
            dtos.forEach(dto -> {
                dto.setModeratorOverridden(true);
                summaryMutator.accept(dto);
            });

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), species.getOfficialCode(), true, dtos);
        });
    }

    @Test
    public void testProcessModeratorOverriddenHuntingSummaries_notCompletingHuntingOfPermit() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<BasicClubHuntingSummaryDTO> dtos = feature
                    .getHuntingSummariesForModeration(permit.getId(), species.getOfficialCode());
            assertEquals(2, dtos.size());

            // complete hunting of the first partner
            final BasicClubHuntingSummaryDTO firstPartner = dtos.get(0);
            firstPartner.setModeratorOverridden(true);
            firstPartner.setTotalHuntingArea(permit.getPermitAreaSize());
            firstPartner.setRemainingPopulationInTotalArea(12);

            feature.processModeratorOverriddenHuntingSummaries(permit.getId(), species.getOfficialCode(), false, dtos);

            // make sure permit is not locked and only the first partner is overridden
            assertTrue(mooseHarvestReportRepo.findAll().isEmpty());

            final List<BasicClubHuntingSummaryDTO> updatedDtos = feature
                    .getHuntingSummariesForModeration(permit.getId(), species.getOfficialCode());
            assertEquals(2, updatedDtos.size());
            assertTrue(updatedDtos.get(0).isModeratorOverridden());
            assertTrue(updatedDtos.get(0).isHuntingFinished());

            assertFalse(updatedDtos.get(1).isModeratorOverridden());
            assertFalse(updatedDtos.get(1).isHuntingFinished());

            // make sure that these overrides can be deleted
            feature.revokeHuntingSummaryModeration(permit.getId(), species.getOfficialCode());
            feature.getHuntingSummariesForModeration(permit.getId(), species.getOfficialCode())
                    .forEach(dto -> assertFalse(dto.isModeratorOverridden()));
        });
    }
}
