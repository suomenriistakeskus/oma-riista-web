package fi.riista.integration.mmm.statement;

import com.google.common.collect.ImmutableList;

public class MMMConstants {

    public static final String MMM_ACCOUNT_NUMBER_DANSKE = "81299710011453";
    public static final String MMM_ACCOUNT_NUMBER_NORDEA = "18043000015388";
    public static final String MMM_ACCOUNT_NUMBER_OP = "50000121502875";

    public static final ImmutableList<String> VALID_ACCOUNT_NUMBERS =
            ImmutableList.of(MMM_ACCOUNT_NUMBER_DANSKE, MMM_ACCOUNT_NUMBER_NORDEA, MMM_ACCOUNT_NUMBER_OP);

    public static final int CURRENCY_CODE_EURO = 1;
    public static final int VALID_REVERSAL_INDICATOR = 0;

    private MMMConstants() {
        throw new AssertionError();
    }
}
