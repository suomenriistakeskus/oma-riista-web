package fi.riista.feature.permit.invoice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class Invoice_SetStateReminderTest {

    @Parameters(name = "{index}: type={0}; electronicInvoicing={1}; state={2}")
    public static Iterable<Object[]> data() {
        return Stream
                .of(InvoiceType.PERMIT_PROCESSING, InvoiceType.PERMIT_HARVEST)
                .flatMap(type -> Stream
                        .of(true, false)
                        .flatMap(electronicInvoicing -> Arrays
                                .stream(InvoiceState.values())
                                .map(state -> new Object[] { type, electronicInvoicing, state })
                        ))
                .collect(toList());
    }

    @Parameter(0)
    public InvoiceType type;

    @Parameter(1)
    public boolean electronicInvoicing;

    @Parameter(2)
    public InvoiceState state;

    @Test
    public void testSetStateReminder() {
        final Invoice invoice = new Invoice(type, electronicInvoicing);
        invoice.setState(state);

        try {
            invoice.setStateReminder();

            if (electronicInvoicing) {
                fail("Should raise exception because electronic invoicing was enabled");
            }

            if (state == InvoiceState.PAID) {
                if (type == InvoiceType.PERMIT_PROCESSING) {
                    fail("Should raise exception when processing invoice is paid");
                }
            } else if (state != InvoiceState.DELIVERED && state != InvoiceState.REMINDER) {
                fail("Should raise exception because of unexpected state");
            }

            assertEquals(InvoiceState.REMINDER, invoice.getState());

        } catch (final IllegalStateException e) {
            // Invoice state should not have changed.
            assertEquals(state, invoice.getState());
        }
    }
}
