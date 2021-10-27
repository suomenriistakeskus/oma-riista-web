package fi.riista.feature.organization.rhy.annualstats;

import javax.annotation.Nullable;

import static fi.riista.util.NumberUtils.nullableIntSum;

public final class AnnualStatisticsYearDependentCalculations {

    @Nullable
    public static Integer countSubsidizableOtherTrainingEvents(final RhyAnnualStatistics stats) {
        return countSubsidizableOtherTrainingEvents(
                stats.getOrCreateJhtTraining(),
                stats.getOrCreateHunterTraining(),
                stats.getOrCreateOtherHunterTraining());
    }

    @Nullable
    public static Integer countSubsidizableOtherTrainingEvents(final JHTTrainingStatistics jhtTraining,
                                                               final HunterTrainingStatistics hunterTraining,
                                                               final OtherHunterTrainingStatistics otherHunterTraining) {
        return nullableIntSum(
                jhtTraining.countJhtTrainingEvents(),
                hunterTraining.countHunterTrainingEvents(),
                otherHunterTraining.countOtherHunterTrainingEvents());
    }

    @Nullable
    public static Integer countSubsidizableStudentAndYouthTrainingEvents(final YouthTrainingStatistics youthTraining) {
        return youthTraining.countStudentAndYouthTrainingEvents();
    }

    @Nullable
    public static Integer countHunterTrainingEvents2017(final HunterTrainingStatistics hunterTraining) {
        return hunterTraining.countHunterTrainingEvents();
    }

    @Nullable
    public static Integer countStudentTrainingEvents2017(final YouthTrainingStatistics youthTraining) {
        return youthTraining.countStudentTrainingEvents();
    }

    @Nullable
    public static Integer countLargeCarnivores2017(final SrvaEventStatistics srva) {
        return nullableIntSum(
                srva.getAccident().countLargeCarnivores(),
                srva.getDeportation().countLargeCarnivores(),
                srva.getInjury().countLargeCarnivores());
    }

    @Nullable
    public static Integer countWildBoars2017(final SrvaEventStatistics srva) {
        return nullableIntSum(
                srva.getAccident().getWildBoars(),
                srva.getDeportation().getWildBoars(),
                srva.getInjury().getWildBoars());
    }

    @Nullable
    public static Integer countSubsidizableTrainingEvents(final RhyAnnualStatistics stats) {
        if (stats.getYear() < 2018) {
            return countAllTrainingEvents2017(
                    stats.getOrCreateHunterExamTraining(),
                    stats.getOrCreateJhtTraining(),
                    stats.getOrCreateHunterTraining(),
                    stats.getOrCreateYouthTraining(),
                    stats.getOrCreateOtherHunterTraining(),
                    stats.getOrCreatePublicEvents());
        }

        return countSubsidizableTrainingEvents(
                stats.getOrCreateHunterExamTraining(),
                stats.getOrCreateJhtTraining(),
                stats.getOrCreateHunterTraining(),
                stats.getOrCreateYouthTraining(),
                stats.getOrCreateOtherHunterTraining());
    }

    @Nullable
    public static Integer countSubsidizableTrainingEvents(final HunterExamTrainingStatistics hunterExamTraining,
                                                          final JHTTrainingStatistics jhtTraining,
                                                          final HunterTrainingStatistics hunterTraining,
                                                          final YouthTrainingStatistics youthTraining,
                                                          final OtherHunterTrainingStatistics otherHunterTraining) {

        return nullableIntSum(
                hunterExamTraining.getHunterExamTrainingEvents(),
                jhtTraining.countJhtTrainingEvents(),
                hunterTraining.countHunterTrainingEvents(),
                youthTraining.countStudentAndYouthTrainingEvents(),
                otherHunterTraining.countOtherHunterTrainingEvents());
    }

    @Nullable
    public static Integer countAllNonSubsidizableTrainingEvents(final RhyAnnualStatistics stats) {

        return countNonSubsidizableTrainingEvents(
                stats.getOrCreateHunterExamTraining(),
                stats.getOrCreateJhtTraining(),
                stats.getOrCreateHunterTraining(),
                stats.getOrCreateYouthTraining(),
                stats.getOrCreateOtherHunterTraining());
    }

    @Nullable
    public static Integer countNonSubsidizableTrainingEvents(final HunterExamTrainingStatistics hunterExamTraining,
                                                 final JHTTrainingStatistics jhtTraining,
                                                 final HunterTrainingStatistics hunterTraining,
                                                 final YouthTrainingStatistics youthTraining,
                                                 final OtherHunterTrainingStatistics otherHunterTraining) {

        return nullableIntSum(
                hunterExamTraining.getNonSubsidizableHunterExamTrainingEvents(),
                jhtTraining.countJhtNonSubsidizableTrainingEvents(),
                hunterTraining.countNonSubsidizableHunterTrainingEvents(),
                youthTraining.countNonSubsidizableStudentAndYouthTrainingEvents(),
                otherHunterTraining.countNonSubsidizableOtherHunterTrainingEvents());
    }



    @Nullable
    public static Integer countAllTrainingEvents2017(final HunterExamTrainingStatistics hunterExamTraining,
                                                     final JHTTrainingStatistics jhtTraining,
                                                     final HunterTrainingStatistics hunterTraining,
                                                     final YouthTrainingStatistics youthTraining,
                                                     final OtherHunterTrainingStatistics otherHunterTraining,
                                                     final PublicEventStatistics publicEvents) {

        return nullableIntSum(
                hunterExamTraining.getHunterExamTrainingEvents(),
                jhtTraining.countJhtTrainingEvents(),
                hunterTraining.countHunterTrainingEvents(),
                youthTraining.countStudentAndYouthTrainingEvents(),
                otherHunterTraining.countOtherHunterTrainingEvents(),
                publicEvents.getPublicEvents());
    }

    @Nullable
    public static Integer countSubsidizableTrainingParticipants(final RhyAnnualStatistics stats) {
        if (stats.getYear() < 2018) {
            return countAllTrainingParticipants2017(
                    stats.getOrCreateHunterExamTraining(),
                    stats.getOrCreateJhtTraining(),
                    stats.getOrCreateHunterTraining(),
                    stats.getOrCreateYouthTraining(),
                    stats.getOrCreateOtherHunterTraining(),
                    stats.getOrCreatePublicEvents());
        }

        return countSubsidizableTrainingParticipants(
                stats.getOrCreateHunterExamTraining(),
                stats.getOrCreateJhtTraining(),
                stats.getOrCreateHunterTraining(),
                stats.getOrCreateYouthTraining(),
                stats.getOrCreateOtherHunterTraining());
    }

    @Nullable
    public static Integer countSubsidizableTrainingParticipants(final HunterExamTrainingStatistics hunterExamTraining,
                                                                final JHTTrainingStatistics jhtTraining,
                                                                final HunterTrainingStatistics hunterTraining,
                                                                final YouthTrainingStatistics youthTraining,
                                                                final OtherHunterTrainingStatistics otherHunterTraining) {

        return nullableIntSum(
                hunterExamTraining.getHunterExamTrainingParticipants(),
                jhtTraining.countJhtTrainingParticipants(),
                hunterTraining.countHunterTrainingParticipants(),
                youthTraining.countStudentAndYouthTrainingParticipants(),
                otherHunterTraining.countOtherHunterTrainingParticipants());
    }

    @Nullable
    public static Integer countNonSubsidizableAllTrainingParticipants(final RhyAnnualStatistics stats) {

        return countNonSubsidizableTrainingParticipants(
                stats.getOrCreateHunterExamTraining(),
                stats.getOrCreateJhtTraining(),
                stats.getOrCreateHunterTraining(),
                stats.getOrCreateYouthTraining(),
                stats.getOrCreateOtherHunterTraining());
    }

    @Nullable
    public static Integer countNonSubsidizableTrainingParticipants(final HunterExamTrainingStatistics hunterExamTraining,
                                                                    final JHTTrainingStatistics jhtTraining,
                                                                    final HunterTrainingStatistics hunterTraining,
                                                                    final YouthTrainingStatistics youthTraining,
                                                                    final OtherHunterTrainingStatistics otherHunterTraining) {

        return nullableIntSum(
                hunterExamTraining.getNonSubsidizableHunterExamTrainingParticipants(),
                jhtTraining.countNonSubsidizableJhtTrainingParticipants(),
                hunterTraining.countNonSubsidizableHunterTrainingParticipants(),
                youthTraining.countNonSubsidizableStudentAndYouthTrainingParticipants(),
                otherHunterTraining.countNonSubsidizableOtherHunterTrainingParticipants());
    }

    @Nullable
    public static Integer countAllTrainingParticipants2017(final HunterExamTrainingStatistics hunterExamTraining,
                                                           final JHTTrainingStatistics jhtTraining,
                                                           final HunterTrainingStatistics hunterTraining,
                                                           final YouthTrainingStatistics youthTraining,
                                                           final OtherHunterTrainingStatistics otherHunterTraining,
                                                           final PublicEventStatistics publicEvents) {

        return nullableIntSum(
                hunterExamTraining.getHunterExamTrainingParticipants(),
                jhtTraining.countJhtTrainingParticipants(),
                hunterTraining.countHunterTrainingParticipants(),
                youthTraining.countStudentAndYouthTrainingParticipants(),
                otherHunterTraining.countOtherHunterTrainingParticipants(),
                publicEvents.getPublicEventParticipants());
    }

    private AnnualStatisticsYearDependentCalculations() {
        throw new AssertionError();
    }
}
