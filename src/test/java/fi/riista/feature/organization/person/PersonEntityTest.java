package fi.riista.feature.organization.person;

import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Optional;

import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class PersonEntityTest {

    @Test
    public void testMaskSSN() {
        assertEquals("123456*****", Person.maskSsn("123456A0123"));
    }

    @Test
    public void testMaskSSNTooShort() {
        assertEquals("1234*****", Person.maskSsn("1234"));
    }

    @Test
    public void testMaskSSNNull() {
        assertEquals("", Person.maskSsn(null));
    }

    @Test
    public void testIsHuntingCardValidNow_PaymentValid() {
        doTestHuntingCard(today().minusMonths(6), today().plusMonths(6), true);
    }

    @Test
    public void testIsHuntingCardValidNow_PaymentValid2() {
        doTestHuntingCard(today(), today().plusMonths(6), true);
    }

    @Test
    public void testIsHuntingCardValidNow_PaymentValid3() {
        doTestHuntingCard(today().minusMonths(6), today(), true);
    }

    @Test
    public void testIsHuntingCardValidNow_NotValidPayment() {
        doTestHuntingCard(today().plusDays(1), today().plusMonths(6), false);
    }

    @Test
    public void testIsHuntingCardValidNow_NotValidPayment2() {
        doTestHuntingCard(today().minusMonths(6), today().minusDays(1), false);
    }

    private static void doTestHuntingCard(LocalDate begin, LocalDate end, boolean isPaid) {
        Person p = new Person();
        p.setHuntingCardStart(begin);
        p.setHuntingCardEnd(end);

        assertEquals(isPaid, p.isHuntingCardValidNow());
    }

    @Test
    public void testIsHunterExamValidNow() {
        final LocalDate today = today();

        doTestHunterExam(null, null, false);

        doTestHunterExam(today, null, true);
        doTestHunterExam(today.plusDays(1), null, false);
        doTestHunterExam(today.minusDays(1), null, true);

        doTestHunterExam(null, today, true);
        doTestHunterExam(null, today.plusDays(1), true);
        doTestHunterExam(null, today.minusDays(1), false);

        doTestHunterExam(today, today, true);
        doTestHunterExam(today, today.plusDays(1), true);

        doTestHunterExam(today.plusDays(1), today.plusDays(1), false);

        doTestHunterExam(today.minusDays(1), today, true);
        doTestHunterExam(today.minusDays(1), today.plusDays(1), true);
        doTestHunterExam(today.minusDays(1), today.minusDays(1), false);
    }

    private static void doTestHunterExam(LocalDate begin, LocalDate end, boolean isValid) {
        Person p = new Person();
        p.setHunterExamDate(begin);
        p.setHunterExamExpirationDate(end);

        assertEquals(isValid, p.isHunterExamValidNow());
    }

    @Test
    public void isBanActive_no() {
        doTestBan(null, null, false);
    }

    @Test
    public void isBanActive_no2() {
        doTestBan(today(), null, false);
    }

    @Test
    public void isBanActive_no3() {
        doTestBan(null, today(), false);
    }

    @Test
    public void isBanActive_banned() {
        doTestBan(today(), today().plusDays(1), true);
    }

    @Test
    public void isBanActive_banned2() {
        doTestBan(today().minusDays(1), today(), true);
    }

    private static void doTestBan(LocalDate begin, LocalDate end, boolean isBan) {
        Person p = new Person();
        p.setHuntingBanStart(begin);
        p.setHuntingBanEnd(end);

        assertEquals(isBan, p.isHuntingBanActiveNow());
    }

    @Test
    public void testPaymentDate() {
        final LocalDate paymentOneDay = ld(2015, 6, 9);

        final Person p = new Person();
        p.setHuntingPaymentOneDay(paymentOneDay);
        p.setHuntingPaymentOneYear(paymentOneDay.getYear());

        assertEquals(Optional.of(paymentOneDay), p.getHuntingPaymentDateForNextOrCurrentSeason(2015));
        assertEquals(Optional.empty(), p.getHuntingPaymentDateForNextOrCurrentSeason(2016));
    }

    @Test
    public void testPaymentDate_PreferNextSeason() {
        final LocalDate paymentOneDay = ld(2015, 6, 9);
        final LocalDate paymentTwoDay = paymentOneDay.plusYears(1);

        final Person p = new Person();
        p.setHuntingPaymentOneDay(paymentOneDay);
        p.setHuntingPaymentOneYear(paymentOneDay.getYear());
        p.setHuntingPaymentTwoDay(paymentTwoDay);
        p.setHuntingPaymentTwoYear(paymentTwoDay.getYear());

        assertEquals(Optional.of(paymentOneDay), p.getHuntingPaymentDateForNextOrCurrentSeason(2014));
        assertEquals(Optional.of(paymentTwoDay), p.getHuntingPaymentDateForNextOrCurrentSeason(2015));
        assertEquals(Optional.of(paymentTwoDay), p.getHuntingPaymentDateForNextOrCurrentSeason(2016));
    }
}
