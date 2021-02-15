package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.util.F;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.FIRST_SUBSIDY_YEAR;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.HUNTING_CONTROL_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.MOOSELIKE_TAXATION_PLANNING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.RHY_MEMBERS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SRVA_ALL_MOOSELIKE_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_OTHER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUM_OF_LUKE_CALCULATIONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUM_OF_LUKE_CALCULATIONS_PRE_2021;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.TOTAL_LUKE_CARNIVORE_PERSONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.WOLF_TERRITORY_WORKGROUPS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.WOLF_TERRITORY_WORKGROUPS_PRE_2021;
import static fi.riista.test.TestUtils.currency;
import static fi.riista.util.DateUtil.currentYear;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class RhySubsidyTestHelper {

    public static IntStream streamSubsidyYears() {
        return IntStream.rangeClosed(FIRST_SUBSIDY_YEAR, currentYear());
    }

    public static AnnualStatisticsExportDTO export(final RhyAnnualStatistics statistics) {
        final Riistanhoitoyhdistys rhy = statistics.getRhy();
        final Organisation rka = rhy.getRiistakeskuksenAlue();

        return AnnualStatisticsExportDTO.create(
                OrganisationNameDTO.createWithOfficialCode(rhy),
                OrganisationNameDTO.createWithOfficialCode(rka),
                statistics);
    }

    public static List<AnnualStatisticsExportDTO> export(final RhyAnnualStatistics... stats) {
        return F.mapNonNullsToList(stats, RhySubsidyTestHelper::export);
    }

    public static RhySubsidyMergeResolver createRhySubsidyMergeResolver(final int subsidyYear,
                                                                        final Riistanhoitoyhdistys... rhys) {
        final List<RiistanhoitoyhdistysNameDTO> rhyNames = Arrays
                .stream(rhys)
                .map(rhy -> createRhyNameDTO(rhy, rhy.getParentOrganisation()))
                .collect(toList());

        return new RhySubsidyMergeResolver(subsidyYear, rhyNames);
    }

    private static RiistanhoitoyhdistysNameDTO createRhyNameDTO(final Riistanhoitoyhdistys rhy,
                                                                final Organisation rka) {
        return new RiistanhoitoyhdistysNameDTO(
                rhy.getId(), rka.getId(),
                rhy.getOfficialCode(), rka.getOfficialCode(),
                rhy.getNameFinnish(), rhy.getNameSwedish(),
                rka.getNameFinnish(), rka.getNameSwedish());
    }

    public static StatisticsBasedSubsidyShareDTO createEmptyCalculatedShares() {
        return StatisticsBasedSubsidyShareDTO
                .builder()
                .withRhyMembers(null, currency(0))
                .withHunterExamTrainingEvents(null, currency(0))
                .withOtherTrainingEvents(null, currency(0))
                .withStudentAndYouthTrainingEvents(null, currency(0))
                .withHuntingControlEvents(null, currency(0))
                .withSumOfLukeCalculations(null, currency(0))
                .withLukeCarnivoreContactPersons(null, currency(0))
                .withMooselikeTaxationPlanningEvents(null, currency(0))
                .withWolfTerritoryWorkgroups(null, currency(0))
                .withSrvaMooselikeEvents(null, currency(0))
                .withSoldMhLicenses(null, currency(0))
                .build();
    }

    public static void assertEquality(final int subsidyYear,
                                      final StatisticsBasedSubsidyShareDTO expected,
                                      final StatisticsBasedSubsidyShareDTO actual) {

        assertEquality(RHY_MEMBERS, expected, actual, StatisticsBasedSubsidyShareDTO::getRhyMembers);

        assertEquality(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS,
                expected, actual, StatisticsBasedSubsidyShareDTO::getHunterExamTrainingEvents);

        assertEquality(SUBSIDIZABLE_OTHER_TRAINING_EVENTS,
                expected, actual, StatisticsBasedSubsidyShareDTO::getOtherTrainingEvents);

        assertEquality(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS,
                expected, actual, StatisticsBasedSubsidyShareDTO::getStudentAndYouthTrainingEvents);

        assertEquality(HUNTING_CONTROL_EVENTS,
                expected, actual, StatisticsBasedSubsidyShareDTO::getHuntingControlEvents);

        final SubsidyAllocationCriterion lukeCalculations =
                subsidyYear >= 2021 ? SUM_OF_LUKE_CALCULATIONS : SUM_OF_LUKE_CALCULATIONS_PRE_2021;
        assertEquality(lukeCalculations,
                expected, actual, StatisticsBasedSubsidyShareDTO::getSumOfLukeCalculations);

        final SubsidyAllocationCriterion carnivorePersons =
                subsidyYear >= 2021 ? TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021 : TOTAL_LUKE_CARNIVORE_PERSONS;
        assertEquality(carnivorePersons,
                expected, actual, StatisticsBasedSubsidyShareDTO::getLukeCarnivoreContactPersons);

        assertEquality(MOOSELIKE_TAXATION_PLANNING_EVENTS,
                expected, actual, StatisticsBasedSubsidyShareDTO::getMooselikeTaxationPlanningEvents);

        final SubsidyAllocationCriterion wolfTerritoryWorkgroups =
                subsidyYear >= 2021 ? WOLF_TERRITORY_WORKGROUPS : WOLF_TERRITORY_WORKGROUPS_PRE_2021;
        assertEquality(wolfTerritoryWorkgroups,
                expected, actual, StatisticsBasedSubsidyShareDTO::getWolfTerritoryWorkgroups);

        assertEquality(SRVA_ALL_MOOSELIKE_EVENTS,
                expected, actual, StatisticsBasedSubsidyShareDTO::getSrvaMooselikeEvents);

        assertEquality(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS,
                expected, actual, StatisticsBasedSubsidyShareDTO::getSoldMhLicenses);
    }

    private static void assertEquality(final SubsidyAllocationCriterion criterion,
                                       final StatisticsBasedSubsidyShareDTO expected,
                                       final StatisticsBasedSubsidyShareDTO actual,
                                       final Function<StatisticsBasedSubsidyShareDTO, SubsidyProportionDTO> fn) {

        assertEquals(criterion.name() + ": ", fn.apply(expected), fn.apply(actual));
    }

    public static void assertOrganisationTransformation(final Organisation entity, final OrganisationNameDTO dto) {
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getOfficialCode(), dto.getOfficialCode());
        assertEquals(entity.getNameFinnish(), dto.getNameFI());
        assertEquals(entity.getNameSwedish(), dto.getNameSV());
    }
}
