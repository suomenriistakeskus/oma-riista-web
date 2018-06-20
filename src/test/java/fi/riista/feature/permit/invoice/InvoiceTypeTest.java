package fi.riista.feature.permit.invoice;

import org.junit.Test;

import static fi.riista.test.TestUtils.ld;
import static org.junit.Assert.assertEquals;

public class InvoiceTypeTest {

    @Test
    public void testCalculateDueDate() {
        assertEquals(ld(2018, 6, 22), InvoiceType.PERMIT_PROCESSING.calculateDueDate(ld(2018, 6, 1)));
    }
}
