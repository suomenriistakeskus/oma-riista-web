package fi.riista.integration.paytrail.order;

import fi.riista.config.Constants;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

// Valid characters are 0-9, a-z, A-Z and ()[]{}*+-_,.
// As regular expression '/^[0-9a-zA-Z()\[\]{}*+\-_,. ]{1,64}$/'
public class PaytrailOrderNumber {

    public static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmss";
    private static final Pattern PATTERN = Pattern.compile("(\\S+)-(\\d{14})-(\\d+)");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(TIMESTAMP_PATTERN);

    @Nonnull
    public static PaytrailOrderNumber create(final @Nonnull Invoice invoice, final @Nonnull DateTime creationTime) {
        return new PaytrailOrderNumber(invoice.getType(), invoice.getInvoiceNumber(), creationTime);
    }

    @Nonnull
    public static PaytrailOrderNumber valueOf(final @Nonnull String asText) {
        final Matcher dm = PATTERN.matcher(asText);

        if (!dm.matches()) {
            throw new RuntimeException("Could not parse orderNumber: " + asText);
        }

        final InvoiceType invoiceType = parseInvoiceType(dm.group(1));
        final DateTime creationTime = DATE_FORMATTER.parseLocalDateTime(dm.group(2)).toDateTime(Constants.DEFAULT_TIMEZONE);
        final int invoiceNumber = Integer.parseInt(dm.group(3));

        return new PaytrailOrderNumber(invoiceType, invoiceNumber, creationTime);
    }

    private static InvoiceType parseInvoiceType(final String asText) {
        switch (asText) {
            case "PAATOS":
                return InvoiceType.PERMIT_PROCESSING;
            case "SAALIS":
                return InvoiceType.PERMIT_HARVEST;
            default:
                throw new IllegalArgumentException("Invalid invoiceType: " + asText);
        }
    }

    private static String invoiceTypeAsText(final InvoiceType invoiceType) {
        switch (invoiceType) {
            case PERMIT_PROCESSING:
                return "PAATOS";
            case PERMIT_HARVEST:
                return "SAALIS";
            default:
                throw new IllegalArgumentException("Invalid invoiceType: " + invoiceType);
        }
    }

    private InvoiceType orderType;
    private DateTime creationTime;

    private int invoiceNumber;

    public PaytrailOrderNumber(final InvoiceType orderType, final int invoiceNumber, final DateTime creationTime) {
        this.orderType = requireNonNull(orderType);
        this.invoiceNumber = invoiceNumber;
        this.creationTime = requireNonNull(creationTime);
    }

    public String formatAsText() {
        return String.format("%s-%s-%d", invoiceTypeAsText(orderType), DATE_FORMATTER.print(creationTime), invoiceNumber);
    }

    public InvoiceType getOrderType() {
        return orderType;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }
}
