package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.util.NumberUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.streamSubsidyYears;
import static fi.riista.test.TestUtils.currency;
import static fi.riista.util.NumberUtils.isPositive;
import static fi.riista.util.NumberUtils.sum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubsidyAllocationCriterionTest {

    @Test
    public void testAllPercentageSharesAreValid() {
        EnumSet.allOf(SubsidyAllocationCriterion.class).forEach(criterion -> {

            final BigDecimal percentageShare = criterion.getPercentageShare();

            assertTrue(isPositive(percentageShare));
            assertTrue(NumberUtils.MAX_PERCENTAGE_SHARE.compareTo(percentageShare) >= 0);

            assertEquals(2, percentageShare.scale());
        });
    }

    @Test
    public void testSumOfPercentageShares() {
        streamSubsidyYears().forEach(this::testSumOfPercentageShares);
    }

    private void testSumOfPercentageShares(final int subsidyYear) {
        final List<SubsidyAllocationCriterion> criteria = SubsidyAllocationCriterion.getSubsidyCriteria(subsidyYear); 
        final BigDecimal sumOfPercentageShares = sum(criteria, SubsidyAllocationCriterion::getPercentageShare);

        assertEquals("Failed for year " + subsidyYear + ", ", currency(100), sumOfPercentageShares);
    }
}
