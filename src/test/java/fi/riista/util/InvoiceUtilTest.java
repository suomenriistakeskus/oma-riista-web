package fi.riista.util;

import fi.riista.feature.common.entity.CreditorReference;
import org.iban4j.Iban;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InvoiceUtilTest {

    private static final CreditorReference TEST_REF_NUM = CreditorReference.fromNullable("14507700161");

    @Test
    public void testFormatInvoiceReferenceForBarCode() {
        assertEquals("00000000014507700161", InvoiceUtil.formatInvoiceReferenceForBarCode(TEST_REF_NUM));
    }

    @Test
    public void testFormatBarCodeMessage() {
        assertEquals("478500001203784420000330000000000000014507700161000000",
                InvoiceUtil.createBarCodeMessage(33, 0, TEST_REF_NUM, Iban.valueOf("FI7850000120378442"), null));
    }
}
