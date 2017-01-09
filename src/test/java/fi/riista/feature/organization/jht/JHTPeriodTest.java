package fi.riista.feature.organization.jht;

import fi.riista.feature.organization.jht.JHTPeriod;
import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.util.TestUtils.ld;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JHTPeriodTest {

    @Test
    public void testTodayBeforeHuntingYearStartAndYear2016() {
        final LocalDate today = ld(2016, 7, 31);

        final JHTPeriod period = new JHTPeriod(today);
        assertEquals(ld(2016, 8, 1), period.getBeginDate());
        assertEquals(ld(2021, 7, 31), period.getEndDate());
    }

    @Test
    public void testTodayBeforeHuntingYearStart() {
        final LocalDate today = ld(2017, 1, 1);

        final JHTPeriod period = new JHTPeriod(today);
        assertEquals(ld(2017, 1, 1), period.getBeginDate());
        assertEquals(ld(2021, 7, 31), period.getEndDate());
    }

    @Test
    public void testTodayEqualToHuntingYearStart() {
        // Jos nimityspäivä 1.8.2017 - 31.7.2018, nimitys päättyy aina 31.7.2022.
        final LocalDate today = ld(2017, 8, 1);

        final JHTPeriod period = new JHTPeriod(today);
        assertEquals(ld(2017, 8, 1), period.getBeginDate());
        assertEquals(ld(2022, 7, 31), period.getEndDate());
    }

    @Test
    public void testTodayAfterHuntingYearStart() {
        // jos nimitys esim. 10.8.2016 - 31.7.2017 välisenä aikana niin nimitys päättyy aina 31.7.2021.
        final LocalDate today = ld(2016, 8, 10);

        final JHTPeriod period = new JHTPeriod(today);
        assertEquals(ld(2016, 8, 10), period.getBeginDate());
        assertEquals(ld(2021, 7, 31), period.getEndDate());
    }

    @Test
    public void testValidPeriodLength() {
        assertTrue(new JHTPeriod(
                ld(2016, 8, 1),
                ld(2021, 7, 31)).validateBeforeAndEnd());

        assertTrue(new JHTPeriod(
                ld(2017, 1, 1),
                ld(2021, 7, 31)).validateBeforeAndEnd());

        assertTrue(new JHTPeriod(
                ld(2017, 8, 1),
                ld(2022, 7, 31)).validateBeforeAndEnd());

        assertTrue(new JHTPeriod(
                ld(2016, 8, 10),
                ld(2021, 7, 31)).validateBeforeAndEnd());
    }

    @Test
    public void testInvalidPeriodLength_TooLong() {
        assertFalse(new JHTPeriod(
                ld(2010, 8, 1),
                ld(2020, 7, 31)).validateBeforeAndEnd());
    }

    @Test
    public void testInvalidPeriodLength_TooLong2() {
        assertFalse(new JHTPeriod(
                ld(2016, 7, 30),
                ld(2021, 7, 31)).validateBeforeAndEnd());
    }

    @Test
    public void testInvalidPeriodLength_TooShort() {
        assertFalse(new JHTPeriod(
                ld(2020, 8, 1),
                ld(2021, 7, 31)).validateBeforeAndEnd());
    }

    @Test
    public void testInvalidPeriodLength_TooShort2() {
        assertFalse(new JHTPeriod(
                ld(2017, 8, 1),
                ld(2021, 7, 31)).validateBeforeAndEnd());
    }

    @Test
    public void testEndDate() {
        assertTrue(new JHTPeriod(
                ld(2016, 8, 1),
                ld(2021, 7, 31)).validateEndDate());

        assertFalse(new JHTPeriod(
                ld(2016, 8, 1),
                ld(2021, 7, 30)).validateEndDate());
    }
}
