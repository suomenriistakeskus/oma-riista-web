package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.util.LocalisedEnum;

import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsCategory.MISC;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsCategory.OVERVIEW;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsCategory.PUBLIC_ADMINISTRATION_TASKS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsCategory.SRVA;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsCategory.TRAININGS;
import static java.util.Objects.requireNonNull;

public enum AnnualStatisticItemGroupId implements LocalisedEnum {

    BASIC_INFO(OVERVIEW, 1),
    STATE_AID_SUMMARY(OVERVIEW, 1),
    OTHER_SUMMARY(OVERVIEW, 1),
    HUNTER_EXAMS(PUBLIC_ADMINISTRATION_TASKS, 2),
    SHOOTING_TESTS(PUBLIC_ADMINISTRATION_TASKS, 2),
    GAME_DAMAGE(PUBLIC_ADMINISTRATION_TASKS, 2),
    HUNTING_CONTROL(PUBLIC_ADMINISTRATION_TASKS, 2),
    OTHER_PUBLIC_ADMIN_TASKS(PUBLIC_ADMINISTRATION_TASKS, 2),
    SRVA_TOTALS(SRVA, 3),
    SRVA_ACCIDENTS(SRVA, 3),
    SRVA_DEPORTATIONS(SRVA, 3),
    SRVA_INJURIES(SRVA, 3),
    TRAINING_SUMMARY(TRAININGS, 4),
    HUNTER_EXAM_TRAINING(TRAININGS, 4),
    JHT_TRAINING(TRAININGS, 4),
    STATE_AID_TRAINING(TRAININGS, 4),
    OTHER_HUNTER_TRAINING(TRAININGS, 5),
    OTHER_TRAINING(TRAININGS, 5),
    OTHER_HUNTING_RELATED(MISC, 6),
    COMMUNICATION(MISC, 6),
    SHOOTING_RANGES(MISC, 6),
    LUKE(MISC, 6),
    METSAHALLITUS(MISC, 6);

    private final AnnualStatisticsCategory category;
    private final int printoutPageNumber;

    AnnualStatisticItemGroupId(final AnnualStatisticsCategory category, final int pageNumber) {
        this.category = requireNonNull(category);
        this.printoutPageNumber = pageNumber;
    }

    public AnnualStatisticsCategory getCategory() {
        return category;
    }

    public int getPrintoutPageNumber() {
        return printoutPageNumber;
    }
}
