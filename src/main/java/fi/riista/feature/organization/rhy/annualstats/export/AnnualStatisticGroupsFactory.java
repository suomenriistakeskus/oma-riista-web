package fi.riista.feature.organization.rhy.annualstats.export;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.BASIC_INFO;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.COMMUNICATION;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.GAME_DAMAGE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAMS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAM_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_TRAINING_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE_2018;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SUBSIDY_SUMMARY_2018;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.YOUTH_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTING_CONTROL;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.JHT_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.MAIN_SUMMARY_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.METSAHALLITUS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTER_TRAINING_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTING_RELATED;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTING_RELATED_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_PUBLIC_ADMIN_TASKS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_SUMMARY;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_TRAINING_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.PUBLIC_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.PUBLIC_EVENTS_2018;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_TESTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_DEPORTATIONS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_INJURIES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_TOTALS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SUBSIDY_SUMMARY;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.TRAINING_SUMMARY;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.TRAINING_SUMMARY_2017;
import static java.util.Arrays.asList;

public class AnnualStatisticGroupsFactory {

    public static List<AnnualStatisticGroup> getAllGroups(final int year) {
        if (year < 2018) {
            return asList(
                    BASIC_INFO,
                    MAIN_SUMMARY_2017,
                    OTHER_SUMMARY,

                    HUNTER_EXAMS,
                    SHOOTING_TESTS,
                    GAME_DAMAGE,
                    HUNTING_CONTROL,
                    OTHER_PUBLIC_ADMIN_TASKS,

                    SRVA_TOTALS,
                    SRVA_ACCIDENTS,
                    SRVA_DEPORTATIONS,
                    SRVA_INJURIES,

                    TRAINING_SUMMARY_2017,
                    HUNTER_EXAM_TRAINING,
                    JHT_TRAINING,
                    HUNTER_TRAINING_2017,
                    OTHER_HUNTER_TRAINING_2017,
                    OTHER_TRAINING_2017,

                    OTHER_HUNTING_RELATED_2017,
                    COMMUNICATION,
                    SHOOTING_RANGES,
                    LUKE_2017,
                    METSAHALLITUS);
        }

        if (year == 2018) {
            return asList(
                    BASIC_INFO,
                    SUBSIDY_SUMMARY_2018,
                    OTHER_SUMMARY,

                    HUNTER_EXAMS,
                    SHOOTING_TESTS,
                    GAME_DAMAGE,
                    HUNTING_CONTROL,
                    OTHER_PUBLIC_ADMIN_TASKS,

                    SRVA_TOTALS,
                    SRVA_ACCIDENTS,
                    SRVA_DEPORTATIONS,
                    SRVA_INJURIES,

                    TRAINING_SUMMARY,
                    HUNTER_EXAM_TRAINING,
                    JHT_TRAINING,
                    HUNTER_TRAINING,
                    YOUTH_TRAINING,
                    OTHER_HUNTER_TRAINING,
                    PUBLIC_EVENTS_2018,

                    OTHER_HUNTING_RELATED,
                    COMMUNICATION,
                    SHOOTING_RANGES,
                    LUKE_2018,
                    METSAHALLITUS);
        }

        if (year == 2019) {
            return asList(
                    BASIC_INFO,
                    SUBSIDY_SUMMARY_2018,
                    OTHER_SUMMARY,

                    HUNTER_EXAMS,
                    SHOOTING_TESTS,
                    GAME_DAMAGE,
                    HUNTING_CONTROL,
                    OTHER_PUBLIC_ADMIN_TASKS,

                    SRVA_TOTALS,
                    SRVA_ACCIDENTS,
                    SRVA_DEPORTATIONS,
                    SRVA_INJURIES,

                    TRAINING_SUMMARY,
                    HUNTER_EXAM_TRAINING,
                    JHT_TRAINING,
                    HUNTER_TRAINING,
                    YOUTH_TRAINING,
                    OTHER_HUNTER_TRAINING,

                    OTHER_HUNTING_RELATED,
                    COMMUNICATION,
                    SHOOTING_RANGES,
                    LUKE_2018,
                    METSAHALLITUS,
                    PUBLIC_EVENTS);
        }

        return asList(
                BASIC_INFO,
                SUBSIDY_SUMMARY,
                OTHER_SUMMARY,

                HUNTER_EXAMS,
                SHOOTING_TESTS,
                GAME_DAMAGE,
                HUNTING_CONTROL,
                OTHER_PUBLIC_ADMIN_TASKS,

                SRVA_TOTALS,
                SRVA_ACCIDENTS,
                SRVA_DEPORTATIONS,
                SRVA_INJURIES,

                TRAINING_SUMMARY,
                HUNTER_EXAM_TRAINING,
                JHT_TRAINING,
                HUNTER_TRAINING,
                YOUTH_TRAINING,
                OTHER_HUNTER_TRAINING,

                OTHER_HUNTING_RELATED,
                COMMUNICATION,
                SHOOTING_RANGES,
                LUKE,
                METSAHALLITUS,
                PUBLIC_EVENTS);
    }
}
