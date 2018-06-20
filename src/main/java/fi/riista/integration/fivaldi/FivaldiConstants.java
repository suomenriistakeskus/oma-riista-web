package fi.riista.integration.fivaldi;

public final class FivaldiConstants {

    public static final int FIVALDI_COMPANY_NUMBER_PROD = 500050;
    public static final int FIVALDI_COMPANY_NUMBER_TEST = 500059;

    // Customer ID for permit decision invoices to be paid through Paytrail
    public static final String FIVALDI_CUSTOMER_ID_PAYTRAIL = "9999";

    // Customer ID for permit decision invoices sent by ordinary mail
    public static final String FIVALDI_CUSTOMER_ID_PAPER = "9998";

    public static final String FIVALDI_PAYMENT_TERMS_CODE = "2";
    public static final String FIVALDI_VAT_CODE = "1";

    public static final String ACCOUNTING_NUMBER_TILISAAMISET = "1703"; // myyntisaamiset
    public static final String ACCOUNTING_NUMBER_SALES = "3001"; // lupapäätösten käsittelymaksu
    public static final String ACCOUNTING_NUMBER_VAT_DEBT = "2939"; // arvonlisäverovelka

    public static final String ACCOUNTING_MONITORING_TARGET_1 = "1";
    public static final String ACCOUNTING_MONITORING_TARGET_2 = "500";
    public static final String ACCOUNTING_MONITORING_TARGET_4 = "5012";

    private FivaldiConstants() {
        throw new AssertionError();
    }
}
