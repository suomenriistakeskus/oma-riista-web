package fi.riista.feature.shootingtest;

import fi.riista.util.BigDecimalComparison;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ShootingTestParticipantEntityTest {

    private ShootingTestParticipant participant;

    @Before
    public void setup() {
        participant = new ShootingTestParticipant();
    }

    @Test
    public void testIsPartiallyPaid_whenBothAmountsAreNull() {
        assertFalse(participant.isPartiallyPaid());
    }

    @Test
    public void testIsPartiallyPaid_whenPaidAmountIsNull() {
        participant.updateTotalDueAmount(0);
        assertFalse(participant.isPartiallyPaid());

        participant.updateTotalDueAmount(1);
        assertTrue(participant.isPartiallyPaid());
    }

    @Test
    public void testIsPartiallyPaid_whenBothAmountsNotNull() {
        participant.updateTotalDueAmount(0);
        participant.updatePaymentState(0, false);
        assertFalse(participant.isPartiallyPaid());

        participant.updateTotalDueAmount(2);
        assertTrue(participant.isPartiallyPaid());

        participant.updatePaymentState(1, false);
        assertTrue(participant.isPartiallyPaid());

        participant.updatePaymentState(2, false);
        assertFalse(participant.isPartiallyPaid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTotalDueAmount_withNegativeAttempts() {
        participant.updateTotalDueAmount(-1);
    }

    @Test
    public void testUpdateTotalDueAmount_withZeroAttempts() {
        participant.updateTotalDueAmount(0);

        assertNotNull(participant.getTotalDueAmount());
        assertNull(participant.getPaidAmount());

        assertTrue(BigDecimalComparison.of(participant.getTotalDueAmount()).eq(ZERO));
        assertFalse(participant.isCompleted());
    }

    @Test
    public void testUpdateTotalDueAmount_withPositiveAttempts() {
        participant.updateTotalDueAmount(3);

        assertNotNull(participant.getTotalDueAmount());
        assertNull(participant.getPaidAmount());

        assertTrue(BigDecimalComparison.of(participant.getTotalDueAmount()).eq(calculateAmount(3)));
        assertFalse(participant.isCompleted());
    }

    @Test(expected = IllegalShootingTestParticipantStateException.class)
    public void testUpdateTotalDueAmount_whenCompleted() {
        participant.setCompleted();
        participant.updateTotalDueAmount(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePaymentState_withNegativeAttempts() {
        participant.updatePaymentState(-1, false);
    }

    @Test
    public void testUpdatePaymentState_withZeroAttemptsWhenTotalDueAmountNotSet() {
        testUpdatePaymentState(0, false);
        testUpdatePaymentState(0, true);
    }

    @Test(expected = IllegalShootingTestParticipantStateException.class)
    public void testUpdatePaymentState_withPositiveAttemptsWhenTotalDueAmountNotSet() {
        participant.updatePaymentState(1, false);
    }

    @Test
    public void testUpdatePaymentState_whenPaidAmountLessThanTotalDueAmount() {
        participant.updateTotalDueAmount(3);
        testUpdatePaymentState(2, false);
        testUpdatePaymentState(2, true);
    }

    @Test
    public void testUpdatePaymentState_whenPaidAmountEqualToTotalDueAmount() {
        participant.updateTotalDueAmount(3);
        testUpdatePaymentState(3, false);
        testUpdatePaymentState(3, true);
    }

    @Test(expected = IllegalShootingTestParticipantStateException.class)
    public void testUpdatePaymentState_whenPaidAmountGreaterThanTotalDueAmount() {
        participant.updateTotalDueAmount(3);
        testUpdatePaymentState(4, false);
    }

    private void testUpdatePaymentState(final int paidAttempts, final boolean completed) {
        final BigDecimal totalDueAmountBefore = participant.getTotalDueAmount();

        participant.updatePaymentState(paidAttempts, completed);

        final BigDecimal totalDueAmountAfter = participant.getTotalDueAmount();
        assertNotNull(totalDueAmountAfter);

        if (totalDueAmountBefore == null) {
            assertEquals(BigDecimal.ZERO, totalDueAmountAfter);
        } else {
            assertTrue(BigDecimalComparison.of(totalDueAmountBefore).eq(totalDueAmountAfter));
        }

        assertNotNull(participant.getPaidAmount());
        assertTrue(BigDecimalComparison.of(participant.getPaidAmount()).eq(calculateAmount(paidAttempts)));
        assertEquals(completed, participant.isCompleted());
    }

    @Test
    public void testSetCompleted_whenPaymentAmountsNotSet() {
        participant.setCompleted();

        assertEquals(ZERO, participant.getTotalDueAmount());
        assertEquals(ZERO, participant.getPaidAmount());
        assertTrue(participant.isCompleted());
    }

    @Test
    public void testSetCompleted_whenPaidAmountNotSet() {
        participant.updateTotalDueAmount(3);
        participant.setCompleted();

        assertTrue(BigDecimalComparison.of(participant.getTotalDueAmount()).eq(calculateAmount(3)));
        assertFalse(participant.isPartiallyPaid());
        assertTrue(participant.isCompleted());
    }

    @Test
    public void testSetCompleted_whenPaymentAmountsSet() {
        participant.updateTotalDueAmount(3);
        participant.updatePaymentState(1, false);
        participant.setCompleted();

        assertTrue(BigDecimalComparison.of(participant.getTotalDueAmount()).eq(calculateAmount(3)));
        assertFalse(participant.isPartiallyPaid());
        assertTrue(participant.isCompleted());
    }

    private static BigDecimal calculateAmount(final int attempts) {
        return ShootingTestAttempt.calculatePaymentSum(attempts);
    }
}
