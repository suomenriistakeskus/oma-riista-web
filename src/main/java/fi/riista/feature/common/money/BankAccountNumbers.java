package fi.riista.feature.common.money;

public final class BankAccountNumbers {

    // For annual game management fee
    public static final String GAME_MANAGEMENT_FEE_OP_POHJOLA = "FI7850000120378442";
    public static final String GAME_MANAGEMENT_FEE_NORDEA = "FI1216603000107212";
    public static final String GAME_MANAGEMENT_FEE_DANSKE_BANK = "FI8480001300035350";

    // For payment of hunting permit
    public static final String PERMIT_DECISION_FEE_NORDEA = "FI5710253000227630";
    public static final String MOOSELIKE_HARVEST_FEE_OP_POHJOLA = "FI2950000121502875";
    public static final String MOOSELIKE_HARVEST_FEE_DANSKE = "FI1081299710011453";
    public static final String MOOSELIKE_HARVEST_FEE_NORDEA = "FI3318043000015388";

    private BankAccountNumbers() {
        throw new AssertionError();
    }
}
