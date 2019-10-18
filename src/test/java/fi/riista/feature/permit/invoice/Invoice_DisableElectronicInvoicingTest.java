package fi.riista.feature.permit.invoice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class Invoice_DisableElectronicInvoicingTest {

    @Parameters(name = "{index}: type={0}; electronicInvoicing={1}; state={2}; anyPaymentLinesPresent={3}; remainingAmountCents={4}")
    public static Iterable<Object[]> data() {
        return Stream.of(InvoiceType.PERMIT_PROCESSING, InvoiceType.PERMIT_HARVEST).flatMap(type -> {

            return Stream.of(true, false).flatMap(electronicInvoicing -> {

                return Arrays.stream(InvoiceState.values()).flatMap(state -> {

                    return Stream.of(true, false).flatMap(anyPaymentLinesPresent -> {

                        return IntStream.of(-1, 0, 1).mapToObj(remainingAmount -> {

                            return new Object[] {
                                    type, electronicInvoicing, state, anyPaymentLinesPresent, remainingAmount
                            };
                        });
                    });
                });
            });
        }).collect(toList());
    }

    @Parameter(0)
    public InvoiceType type;

    @Parameter(1)
    public boolean electronicInvoicing;

    @Parameter(2)
    public InvoiceState state;

    @Parameter(3)
    public boolean anyPaymentLinesPresent;

    @Parameter(4)
    public int remainingAmountCents;

    @Test
    public void testDisableElectronicInvoicing() {
        final Invoice invoice = spy(new Invoice(type, electronicInvoicing));
        invoice.setState(state);
        invoice.setReceivedAmount(anyPaymentLinesPresent ? new BigDecimal(1) : null);
        doReturn(new BigDecimal(remainingAmountCents).movePointLeft(2)).when(invoice).getRemainingAmount();

        try {
            invoice.disableElectronicInvoicing();

            if (!electronicInvoicing) {
                fail("Should raise exception because electronic invoicing was already disabled");
            }

            switch (type) {
                case PERMIT_PROCESSING:
                    if (state == InvoiceState.PAID) {
                        fail("Should raise exception when processing invoice is in PAID state");
                    }
                    break;

                case PERMIT_HARVEST:
                    if (!anyPaymentLinesPresent) {
                        if (state == InvoiceState.PAID) {
                            fail("Should raise exception because received amount is not updated to invoice");
                        }

                    } else {
                        if (remainingAmountCents <= 0) {
                            fail("Should raise exception because invoiced amount is less than received amount");
                        }
                        verify(invoice, times(1)).getRemainingAmount();
                    }

                    break;
            }

            assertFalse(invoice.isElectronicInvoicingEnabled());

        } catch (final IllegalStateException e) {
            // Electronic invoicing flag should not have changed.
            assertEquals(electronicInvoicing, invoice.isElectronicInvoicingEnabled());
        }
    }
}
