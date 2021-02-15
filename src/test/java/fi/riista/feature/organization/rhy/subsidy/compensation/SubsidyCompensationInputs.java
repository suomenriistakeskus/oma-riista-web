package fi.riista.feature.organization.rhy.subsidy.compensation;

import static fi.riista.test.TestUtils.currency;

public class SubsidyCompensationInputs {

    public static final SubsidyCompensationInputDTO TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT = create("001", 11, 7);
    public static final SubsidyCompensationInputDTO TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_2 = create("002", 6, 5);
    public static final SubsidyCompensationInputDTO TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_3 = create("003", 20, 10);

    public static final SubsidyCompensationInputDTO TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT = create("004", 10, 10);

    public static final SubsidyCompensationInputDTO TOTAL_SUBSIDY_BELOW_LOWER_LIMIT = create("005", 3, 5);
    public static final SubsidyCompensationInputDTO TOTAL_SUBSIDY_BELOW_LOWER_LIMIT_2 = create("006", 10, 18);

    public static final SubsidyCompensationInputDTO ALREADY_COMPENSATED = create("007", 10, 5, true);

    static SubsidyCompensationInputDTO create(final String rhyCode,
                                              final int calculatedSubsidy,
                                              final int subsidyLowerLimitBasedOnLastYear) {

        return create(rhyCode, calculatedSubsidy, subsidyLowerLimitBasedOnLastYear, false);
    }

    static SubsidyCompensationInputDTO create(final String rhyCode,
                                              final int calculatedSubsidy,
                                              final int subsidyLowerLimitBasedOnLastYear,
                                              final boolean alreadyCompensated) {

        return new SubsidyCompensationInputDTO(
                rhyCode, currency(calculatedSubsidy), currency(subsidyLowerLimitBasedOnLastYear), alreadyCompensated);
    }
}
