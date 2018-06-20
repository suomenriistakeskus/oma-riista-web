package fi.riista.util;

import fi.riista.feature.common.entity.CreditorReference;
import org.apache.commons.lang.StringUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class InvoiceUtil {

    private static final DateTimeFormatter BAR_CODE_DUE_DATE_PATTERN = DateTimeFormat.forPattern("yyMMdd");

    public static String formatInvoiceReferenceForBarCode(final CreditorReference invoiceReference) {
        final String value = invoiceReference.getValue();
        return value == null ? null : StringUtils.leftPad(value.replaceAll("[^0-9]", ""), 20, '0');
    }

    public static String formatIbanForBarCode(final Iban iban) {
        checkArgument(iban.getCountryCode() == CountryCode.FI, "can only generate for FI");

        // Skip country-code
        return iban.toString().substring(2);
    }

    public static String createBarCodeMessage(final int euros, final int cents,
                                              final CreditorReference invoiceReference,
                                              final Iban iban, final LocalDate dueDate) {
        requireNonNull(invoiceReference);
        checkArgument(invoiceReference.isValid());
        requireNonNull(iban);
        checkArgument(cents >= 0 && cents < 100);
        checkArgument(euros >= 0 && euros <= 999999);

        // Create message for version 4
        return "4" +
                formatIbanForBarCode(iban) +
                String.format("%06d", euros) +
                String.format("%02d", cents) +
                // Reserved fixed value
                "000" +
                formatInvoiceReferenceForBarCode(invoiceReference) +
                (dueDate != null ? BAR_CODE_DUE_DATE_PATTERN.print(dueDate) : "000000");
    }

    private InvoiceUtil() {
        throw new AssertionError();
    }
}
