package fi.riista.integration.fivaldi;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/** Fivaldi accounts receivable record ("myyntireskontratietue" in Finnish) */
public class FivaldiRecord {

    private final FivaldiReslasLine reslas;
    private final List<FivaldiRestapLine> restap;

    public FivaldiRecord(final boolean usePermitDecisionPaymentDefaults) {
        this.reslas = new FivaldiReslasLine();
        this.restap = singletonList(new FivaldiRestapLine());

        if (usePermitDecisionPaymentDefaults) {
            setPermitDecisionPaymentDefaults();
        }
    }

    public FivaldiRecord(@Nonnull final FivaldiReslasLine reslas, @Nonnull final List<FivaldiRestapLine> restap) {
        this.reslas = Objects.requireNonNull(reslas, "reslas is null");
        this.restap = unmodifiableList(Objects.requireNonNull(restap, "restap is null"));

        restap.forEach(Objects::requireNonNull);
    }

    public FivaldiRecord(@Nonnull final FivaldiReslasLine reslas, @Nonnull final FivaldiRestapLine restap) {
        this(reslas, singletonList(restap));
    }

    public void setPermitDecisionPaymentDefaults() {
        assertHasSingleRestapLine();

        reslas.setPaymentTermsCode(FivaldiConstants.FIVALDI_PAYMENT_TERMS_CODE);
        reslas.setTiliSaamisetAccountingNumber(FivaldiConstants.ACCOUNTING_NUMBER_TILISAAMISET);

        final FivaldiRestapLine restapItem = restap.get(0);
        restapItem.setSalesAccountingNumber(FivaldiConstants.ACCOUNTING_NUMBER_SALES);
        restapItem.setVatDebtAccountingNumber(FivaldiConstants.ACCOUNTING_NUMBER_VAT_DEBT);

        restapItem.setVatCode(FivaldiConstants.FIVALDI_VAT_CODE);
        restapItem.setVatSum(BigDecimal.ZERO);
        restapItem.setVatCurrencySum(BigDecimal.ZERO);

        restapItem.setAccountingMonitoringTarget1(FivaldiConstants.ACCOUNTING_MONITORING_TARGET_1);
        restapItem.setAccountingMonitoringTarget2(FivaldiConstants.ACCOUNTING_MONITORING_TARGET_2);
        restapItem.setAccountingMonitoringTarget4(FivaldiConstants.ACCOUNTING_MONITORING_TARGET_4);
    }

    public FivaldiReslasLine getReslas() {
        return reslas;
    }

    public List<FivaldiRestapLine> getRestap() {
        return restap;
    }

    public void setCompanyNumber(final Integer companyNumber) {
        reslas.setCompanyNumber(companyNumber);
        restap.forEach(item -> item.setCompanyNumber(companyNumber));
    }

    public void setCustomerId(final String customerId) {
        reslas.setCustomerId(customerId);
        restap.forEach(item -> item.setCustomerId(customerId));
    }

    public void setInvoiceNumber(final Integer invoiceNumber) {
        reslas.setInvoiceNumber(invoiceNumber);
        restap.forEach(item -> item.setInvoiceNumber(invoiceNumber));
    }

    public void setInvoiceSum(final BigDecimal sum) {
        assertHasSingleRestapLine();
        reslas.setInvoiceSum(sum);
        restap.get(0).setSum(sum);
    }

    public void assertHasSingleRestapLine() {
        if (restap.size() != 1) {
            throw new IllegalStateException("Fivaldi record expected to have exactly one RESTAP line");
        }
    }
}
