package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.equalTo;

public class InvoicePaymentLineMatcher {

    public static Matcher<InvoicePaymentLine> isEqualAnnouncement(final InvoicePaymentLine paymentLine) {
        return Matchers.allOf(
                hasInvoice(paymentLine.getInvoice()),
                hasAccountTransfer(paymentLine.getAccountTransfer()),
                hasPaymentDate(paymentLine.getPaymentDate()),
                hasAmount(paymentLine.getAmount()));
    }

    public static FeatureMatcher<InvoicePaymentLine, Invoice> hasInvoice(final Invoice expected) {
        return new FeatureMatcher<InvoicePaymentLine, Invoice>(equalTo(expected), "invoice", "invoice") {
            @Override
            protected Invoice featureValueOf(final InvoicePaymentLine paymentLine) {
                return paymentLine.getInvoice();
            }
        };
    }

    public static FeatureMatcher<InvoicePaymentLine, AccountTransfer> hasAccountTransfer(final AccountTransfer expected) {
        return new FeatureMatcher<InvoicePaymentLine, AccountTransfer>(equalTo(expected), "accountTransfer", "accountTransfer") {
            @Override
            protected AccountTransfer featureValueOf(final InvoicePaymentLine expected) {
                return expected.getAccountTransfer();
            }
        };
    }

    public static FeatureMatcher<InvoicePaymentLine, LocalDate> hasPaymentDate(final LocalDate expected) {
        return new FeatureMatcher<InvoicePaymentLine, LocalDate>(equalTo(expected), "paymentDate", "paymentDate") {
            @Override
            protected LocalDate featureValueOf(final InvoicePaymentLine paymentLine) {
                return paymentLine.getPaymentDate();
            }
        };
    }

    public static FeatureMatcher<InvoicePaymentLine, BigDecimal> hasAmount(final BigDecimal expected) {
        return new FeatureMatcher<InvoicePaymentLine, BigDecimal>(equalTo(expected), "amount", "amount") {
            @Override
            protected BigDecimal featureValueOf(final InvoicePaymentLine paymentLine) {
                return paymentLine.getAmount();
            }
        };
    }

    private InvoicePaymentLineMatcher() {
        throw new AssertionError();
    }
}
