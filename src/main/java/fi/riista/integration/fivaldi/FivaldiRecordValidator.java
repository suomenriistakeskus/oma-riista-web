package fi.riista.integration.fivaldi;

import fi.riista.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fi.riista.integration.fivaldi.FivaldiConstants.ACCOUNTING_MONITORING_TARGET_1;
import static fi.riista.integration.fivaldi.FivaldiConstants.ACCOUNTING_MONITORING_TARGET_2;
import static fi.riista.integration.fivaldi.FivaldiConstants.ACCOUNTING_MONITORING_TARGET_4;
import static fi.riista.integration.fivaldi.FivaldiConstants.ACCOUNTING_NUMBER_SALES;
import static fi.riista.integration.fivaldi.FivaldiConstants.ACCOUNTING_NUMBER_TILISAAMISET;
import static fi.riista.integration.fivaldi.FivaldiConstants.ACCOUNTING_NUMBER_VAT_DEBT;
import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_COMPANY_NUMBER_PROD;
import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_COMPANY_NUMBER_TEST;
import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_CUSTOMER_ID_PAPER;
import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_CUSTOMER_ID_PAYTRAIL;
import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_PAYMENT_TERMS_CODE;
import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_VAT_CODE;
import static fi.riista.integration.fivaldi.FivaldiHelper.asString;
import static fi.riista.integration.fivaldi.FivaldiHelper.scaleMonetaryAmount;
import static fi.riista.integration.fivaldi.FivaldiReslasLine.RESLAS_RECORD_TYPE;
import static fi.riista.integration.fivaldi.FivaldiRestapLine.RESTAP_RECORD_TYPE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FivaldiRecordValidator {

    private static final BigDecimal ZERO_SUM = scaleMonetaryAmount(BigDecimal.ZERO);

    public static void validate(@Nonnull final FivaldiRecord record) {
        validate(record, true);
    }

    public static void validate(@Nonnull final FivaldiRecord record,
                                final boolean expectPermitDecisionPaymentCustomizations) {

        requireNonNull(record);

        if (expectPermitDecisionPaymentCustomizations) {
            record.assertHasSingleRestapLine();
        }

        final FivaldiReslasLine reslas = record.getReslas();
        final List<FivaldiRestapLine> restapLines = record.getRestap();

        validateReslas(reslas, expectPermitDecisionPaymentCustomizations);

        for (final FivaldiRestapLine restap : restapLines) {

            mustMatch("company number", reslas.getCompanyNumber(), restap.getCompanyNumber());
            mustEqual("RESTAP: type", RESTAP_RECORD_TYPE, restap.getType());
            mustMatch("customer id", reslas.getCustomerId(), restap.getCustomerId());
            mustMatch("invoice number", reslas.getInvoiceNumber(), restap.getInvoiceNumber());
            mustNotBeBlank("RESTAP: sales accounting number", restap.getSalesAccountingNumber());
            mustNotBeNull("RESTAP: sum", restap.getSum());
            mustNotBeNull("RESTAP: vat sum", restap.getVatSum());
            mustNotBeNull("RESTAP: vat currency sum", restap.getVatCurrencySum());
            mustNotBeBlank("RESTAP: VAT debt accounting number", restap.getVatDebtAccountingNumber());

            if (expectPermitDecisionPaymentCustomizations) {
                mustEqual("RESTAP: sales accounting number", ACCOUNTING_NUMBER_SALES, restap.getSalesAccountingNumber());
                mustEqual("RESTAP: VAT debt accounting number", ACCOUNTING_NUMBER_VAT_DEBT, restap.getVatDebtAccountingNumber());
                mustEqual("RESTAP: VAT code", FIVALDI_VAT_CODE, restap.getVatCode());
                mustEqual("RESTAP: vat sum", ZERO_SUM, restap.getVatSum());
                mustEqual("RESTAP: vat currency sum", ZERO_SUM, restap.getVatCurrencySum());
                mustEqual("RESTAP: accounting monitoring target 1", ACCOUNTING_MONITORING_TARGET_1, restap.getAccountingMonitoringTarget1());
                mustEqual("RESTAP: accounting monitoring target 2", ACCOUNTING_MONITORING_TARGET_2, restap.getAccountingMonitoringTarget2());
                mustEqual("RESTAP: accounting monitoring target 4", ACCOUNTING_MONITORING_TARGET_4, restap.getAccountingMonitoringTarget4());
            }
        }

        final BigDecimal reslasSum = reslas.getInvoiceSum();
        final BigDecimal reslasCurrencySum = Optional.ofNullable(reslas.getCurrencySum()).orElse(ZERO_SUM);

        final BigDecimal restapTotalSum = NumberUtils.sum(restapLines, FivaldiRestapLine::getTotalSum);
        final BigDecimal restapTotalCurrencySum = NumberUtils.sum(restapLines, FivaldiRestapLine::getTotalCurrencySum);

        mustMatch("total sum", reslasSum, scaleMonetaryAmount(restapTotalSum));
        mustMatch("total currency sum", reslasCurrencySum, scaleMonetaryAmount(restapTotalCurrencySum));
    }

    private static void validateReslas(final FivaldiReslasLine reslas,
                                       final boolean expectPermitDecisionPaymentCustomizations) {

        final LocalDate invoiceDate = reslas.getInvoiceDate();
        final LocalDate netDueDate = reslas.getNetDueDate();

        mustNotBeNull("RESLAS: company number", reslas.getCompanyNumber());
        mustEqual("RESLAS: type", RESLAS_RECORD_TYPE, reslas.getType());
        mustNotBeNull("RESLAS: customer id", reslas.getCustomerId());
        mustNotBeNull("RESLAS: invoice number", reslas.getInvoiceNumber());
        mustNotBeNull("RESLAS: season", reslas.getSeason());
        mustNotBeNull("RESLAS: invoice date", invoiceDate);
        mustNotBeNull("RESLAS: net due date", netDueDate);
        mustNotBeNull("RESLAS: invoice sum", reslas.getInvoiceSum());
        mustNotBeBlank("RESLAS: tilisaamistili", reslas.getTiliSaamisetAccountingNumber());
        mustNotBeNull("RESLAS: creditor reference", reslas.getCreditorReference());

        mustHold("RESLAS: net due date must not be before invoice date", !netDueDate.isBefore(invoiceDate));

        if (expectPermitDecisionPaymentCustomizations) {
            mustHold("RESLAS: company number is unknown", isValidCompanyNumber(reslas.getCompanyNumber()));
            mustHold("RESLAS: customer ID is unknown", isValidCustomerId(reslas.getCustomerId()));
            mustEqual("RESLAS: payment terms code", FIVALDI_PAYMENT_TERMS_CODE, reslas.getPaymentTermsCode());
            mustEqual("RESLAS: tilisaamistili", ACCOUNTING_NUMBER_TILISAAMISET, reslas.getTiliSaamisetAccountingNumber());
        }
    }

    private static void mustNotBeNull(final String fieldName, final Object value) {
        if (value == null) {
            throw new FivaldiRecordValidationException(format("%s: must not be null", fieldName));
        }
    }

    private static void mustNotBeBlank(final String fieldName, final String string) {
        if (StringUtils.isBlank(string)) {
            throw new FivaldiRecordValidationException(format("%s: must not be blank", fieldName));
        }
    }

    private static void mustEqual(final String fieldName, final Object expected, final Object value) {
        if (!expected.equals(value)) {
            throw new FivaldiRecordValidationException(format("%s: illegal value: %s", fieldName, asString(value)));
        }
    }

    private static void mustHold(final String failureMessage, final boolean condition) {
        if (!condition) {
            throw new FivaldiRecordValidationException(failureMessage);
        }
    }

    private static void mustMatch(final String fieldName, final Object fromSummary, final Object fromItem) {
        if (!Objects.equals(fromSummary, fromItem)) {

            throw new FivaldiRecordValidationException(format(
                    "%s differs between RESLAS and RESTAP: %s <> %s",
                    fieldName, asString(fromSummary), asString(fromItem)));
        }
    }

    private static boolean isValidCompanyNumber(final int companyNumber) {
        return FIVALDI_COMPANY_NUMBER_PROD == companyNumber || FIVALDI_COMPANY_NUMBER_TEST == companyNumber;
    }

    private static boolean isValidCustomerId(final String customerId) {
        return FIVALDI_CUSTOMER_ID_PAYTRAIL.equals(customerId) || FIVALDI_CUSTOMER_ID_PAPER.equals(customerId);
    }
}
