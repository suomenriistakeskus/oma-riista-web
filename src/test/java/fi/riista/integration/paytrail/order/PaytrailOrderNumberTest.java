package fi.riista.integration.paytrail.order;

import fi.riista.config.Constants;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaytrailOrderNumberTest {

    @Test
    public void testParseValid() {
        final String asText = "PAATOS-201805041633-1234";
        final PaytrailOrderNumber paytrailOrderNumber = PaytrailOrderNumber.valueOf(asText);

        assertEquals(InvoiceType.PERMIT_PROCESSING, paytrailOrderNumber.getOrderType());
        assertEquals(2018, paytrailOrderNumber.getCreationTime().getYear());
        assertEquals(5, paytrailOrderNumber.getCreationTime().getMonthOfYear());
        assertEquals(4, paytrailOrderNumber.getCreationTime().getDayOfMonth());
        assertEquals(16, paytrailOrderNumber.getCreationTime().getHourOfDay());
        assertEquals(33, paytrailOrderNumber.getCreationTime().getMinuteOfHour());
        assertEquals(1234, paytrailOrderNumber.getInvoiceNumber());
        assertEquals(asText, paytrailOrderNumber.formatAsText());
    }

    @Test
    public void testFormatValid() {
        final Invoice invoice = new Invoice(true);
        invoice.setInvoiceNumber(1234);
        invoice.setType(InvoiceType.PERMIT_PROCESSING);

        final DateTime now = new LocalDate(2018, 5, 4).toDateTime(new LocalTime(16, 33), Constants.DEFAULT_TIMEZONE);
        final PaytrailOrderNumber paytrailOrderNumber = PaytrailOrderNumber.create(invoice, now);

        assertEquals(2018, paytrailOrderNumber.getCreationTime().getYear());
        assertEquals(5, paytrailOrderNumber.getCreationTime().getMonthOfYear());
        assertEquals(4, paytrailOrderNumber.getCreationTime().getDayOfMonth());
        assertEquals(16, paytrailOrderNumber.getCreationTime().getHourOfDay());
        assertEquals(33, paytrailOrderNumber.getCreationTime().getMinuteOfHour());
        assertEquals(1234, paytrailOrderNumber.getInvoiceNumber());
        assertEquals("PAATOS-201805041633-1234", paytrailOrderNumber.formatAsText());
    }
}
