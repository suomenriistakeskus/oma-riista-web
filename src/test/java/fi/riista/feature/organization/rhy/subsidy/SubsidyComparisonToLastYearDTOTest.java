package fi.riista.feature.organization.rhy.subsidy;

import org.junit.Test;

import static fi.riista.test.TestUtils.currency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubsidyComparisonToLastYearDTOTest {

    // Primarily tests that lower limit based on last year is calculated correctly.
    @Test
    public void testCreate() {
        assertEquality(
                new SubsidyComparisonToLastYearDTO(currency(20), currency(15), currency(12)),
                SubsidyComparisonToLastYearDTO.create(currency(20), currency(15)));

        assertEquality(
                new SubsidyComparisonToLastYearDTO(currency(10), currency(9), currency(8)),
                SubsidyComparisonToLastYearDTO.create(currency(10), currency(9)));
    }

    @Test
    public void testEquals() {
        final SubsidyComparisonToLastYearDTO reference =
                SubsidyComparisonToLastYearDTO.create(currency(10), currency(5));

        // Identity
        assertTrue(reference.equals(reference));

        // Same parameters
        assertTrue(reference.equals(SubsidyComparisonToLastYearDTO.create(currency(10), currency(5))));

        assertFalse(reference.equals(SubsidyComparisonToLastYearDTO.create(currency(5), currency(5))));
        assertFalse(reference.equals(SubsidyComparisonToLastYearDTO.create(currency(10), currency(10))));
        assertFalse(reference.equals(SubsidyComparisonToLastYearDTO.create(currency(5), currency(5))));
        assertFalse(reference.equals(new Object()));
        assertFalse(reference.equals(null));
    }

    @Test
    public void testComputeDifferenceOfCalculatedStatisticsToLowerLimit() {
        final SubsidyComparisonToLastYearDTO dto =
                new SubsidyComparisonToLastYearDTO(currency(20), currency(15), currency(12));

        assertEquals(currency(8), dto.computeDifferenceOfCalculatedStatisticsToLowerLimit());

        final SubsidyComparisonToLastYearDTO dto2 =
                new SubsidyComparisonToLastYearDTO(currency(6), currency(9), currency(8));

        assertEquals(currency(-2), dto2.computeDifferenceOfCalculatedStatisticsToLowerLimit());
    }

    @Test
    public void testIsCalculatedSubsidyBelowLowerLimit() {
        final SubsidyComparisonToLastYearDTO dto =
                new SubsidyComparisonToLastYearDTO(currency(20), currency(15), currency(12));

        assertFalse(dto.isCalculatedSubsidyBelowLowerLimit());

        final SubsidyComparisonToLastYearDTO dto2 =
                new SubsidyComparisonToLastYearDTO(currency(6), currency(9), currency(8));

        assertTrue(dto2.isCalculatedSubsidyBelowLowerLimit());
    }

    private static void assertEquality(final SubsidyComparisonToLastYearDTO expected,
                                       final SubsidyComparisonToLastYearDTO actual) {
     
        assertEquals(expected.getSubsidyCalculatedBasedOnStatistics(), actual.getSubsidyCalculatedBasedOnStatistics());
        assertEquals(expected.getSubsidyGrantedLastYear(), actual.getSubsidyGrantedLastYear());
        assertEquals(expected.getSubsidyLowerLimitBasedOnLastYear(), actual.getSubsidyLowerLimitBasedOnLastYear());
    }
}
