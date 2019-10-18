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

import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class Invoice_IsOverdueTest {

    @Parameters(name = "{index}: type={0}; dueDateInPast={1}; state={2}; remainingAmountCents={3}")
    public static Iterable<Object[]> data() {
        return Stream.of(InvoiceType.PERMIT_PROCESSING, InvoiceType.PERMIT_HARVEST).flatMap(invoiceType -> {

            return Stream.of(true, false).flatMap(dueDateInPast -> {

                return Arrays.stream(InvoiceState.values()).flatMap(invoiceState -> {

                    return IntStream.of(-1, 0, 1).mapToObj(remainingAmount -> {

                        return new Object[] {
                                invoiceType, dueDateInPast, invoiceState, remainingAmount
                        };
                    });
                });
            });
        }).collect(toList());
    }

    @Parameter(0)
    public InvoiceType type;

    @Parameter(1)
    public boolean dueDateInPast;

    @Parameter(2)
    public InvoiceState state;

    @Parameter(3)
    public int remainingAmountCents;

    @Test
    public void testIsOverdue() {
        final Invoice invoice = spy(new Invoice(type, true));
        invoice.setDueDate(dueDateInPast ? today().minusDays(1) : today());
        invoice.setState(state);
        doReturn(new BigDecimal(remainingAmountCents).movePointLeft(2)).when(invoice).getRemainingAmount();

        final boolean isOverdue = invoice.isOverdue();

        if (!dueDateInPast) {
            assertFalse(isOverdue);

        } else {
            switch (type) {
                case PERMIT_PROCESSING:
                    assertEquals(state == InvoiceState.DELIVERED || state == InvoiceState.REMINDER, isOverdue);
                    break;

                case PERMIT_HARVEST:
                    if (state == InvoiceState.VOID) {
                        assertFalse(isOverdue);
                    } else {
                        assertEquals(remainingAmountCents > 0, isOverdue);
                        verify(invoice, times(1)).getRemainingAmount();
                    }
                    break;
            }
        }
    }
}
