package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019InputDTO;
import static fi.riista.test.TestUtils.currency;

public class SubsidyCompensation2019Inputs {

    public static final SubsidyCompensation2019InputDTO TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT = create("001", 11, 5, 7);
    public static final SubsidyCompensation2019InputDTO TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_2 = create("002", 6, 3, 5);
    public static final SubsidyCompensation2019InputDTO TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_3 = create("003", 20, 5, 10);

    public static final SubsidyCompensation2019InputDTO TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT = create("004", 10, 5, 10);

    public static final SubsidyCompensation2019InputDTO TOTAL_SUBSIDY_BELOW_LOWER_LIMIT = create("005", 3, 3, 5);
    public static final SubsidyCompensation2019InputDTO TOTAL_SUBSIDY_BELOW_LOWER_LIMIT_2 = create("006", 10, 5, 18);

    public static final SubsidyCompensation2019InputDTO NEGATIVE_SECOND_BATCH = create("007", 5, 7, 3);

    public static final SubsidyCompensation2019InputDTO NEGATIVE_SECOND_BATCH_AND_TOTAL_SUBSIDY_BELOW_LOWER_LIMIT = create(
            "008", 7, 13, 11);

    public static final SubsidyCompensation2019InputDTO ALREADY_COMPENSATED = create("009", 10, 10, 5, true);

    static SubsidyCompensation2019InputDTO create(final String rhyCode,
                                                  final int calculatedSubsidy,
                                                  final int subsidyGrantedInFirstBatch,
                                                  final int subsidyLowerLimitBasedOnLastYear) {

        return create(rhyCode, calculatedSubsidy, subsidyGrantedInFirstBatch, subsidyLowerLimitBasedOnLastYear, false);
    }

    static SubsidyCompensation2019InputDTO create(final String rhyCode,
                                                  final int calculatedSubsidy,
                                                  final int subsidyGrantedInFirstBatch,
                                                  final int subsidyLowerLimitBasedOnLastYear,
                                                  final boolean alreadyCompensated) {

        return new SubsidyCompensation2019InputDTO(
                rhyCode,
                currency(calculatedSubsidy),
                currency(subsidyGrantedInFirstBatch),
                currency(subsidyLowerLimitBasedOnLastYear),
                alreadyCompensated);
    }
}
