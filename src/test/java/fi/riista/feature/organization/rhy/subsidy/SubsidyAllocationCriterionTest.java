package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.util.NumberUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.streamSubsidyYears;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.getSubsidyCriteria;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.NumberUtils.MAX_PERCENTAGE_SHARE;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class SubsidyAllocationCriterionTest {


    @Test
    public void testSumOfCriterionShares() {
        streamSubsidyYears().forEach(subsidyYear -> {
            final List<SubsidyAllocationCriterion> criteria = getSubsidyCriteria(subsidyYear);
            final Function<? super SubsidyAllocationCriterion, BigDecimal> mapper =
                    (criterion) -> criterion.getPercentageShare();

            final BigDecimal sum = NumberUtils.sum(criteria, mapper);

            assertThat(sum, is(equalTo(new BigDecimal("100.00"))));
        });
    }

    @Test
    public void testAllPercentageSharesAreValid() {
        streamSubsidyYears().forEach(subsidyYear -> {
            EnumSet.allOf(SubsidyAllocationCriterion.class).forEach(criterion -> {

                final BigDecimal percentageShare = criterion.getPercentageShare();

                assertThat(percentageShare, is(greaterThan(ZERO)));
                assertThat(percentageShare, is(lessThan(MAX_PERCENTAGE_SHARE)));

                assertThat(percentageShare.scale(), is(equalTo(2)));
            });
        });
    }

    @Test
    public void testCorrectNumberOfCriterions() {
        streamSubsidyYears().forEach(subsidyYear -> {
            assertThat(getSubsidyCriteria(subsidyYear), hasSize(11));
        });
    }

    @Test
    public void testAllValuesHaveStatisticsItemMapping() {
        Stream.of(SubsidyAllocationCriterion.values()).forEach(SubsidyAllocationCriterion::getRelatedStatisticItem);
    }
}
