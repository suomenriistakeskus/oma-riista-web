package fi.riista.feature.organization.rhy.subsidy.compensation;

import fi.riista.util.F;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationPartition.partitionByCompensationNeed;
import static fi.riista.test.TestUtils.bd;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class SubsidyCompensationPartitionTest {

    @Test
    public void testPartitionByCompensationNeed() {
        final SubsidyCompensationPartition partition = partitionByCompensationNeed(createDefaultInput());

        assertEquals(
                newHashSet("001", "002"),
                F.mapNonNullsToSet(partition.getDownscaled(), SubsidyCompensationInputDTO::getRhyCode));

        assertEquals(
                newHashSet("004", "009"),
                F.mapNonNullsToSet(partition.getKeptUnchanged(), SubsidyCompensationInputDTO::getRhyCode));

        assertEquals(
                newHashSet("005", "007", "008"),
                F.mapNonNullsToSet(partition.getNeedingCompensation(), SubsidyCompensationInputDTO::getRhyCode));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPartitionByCompensationNeed_withEmptyList() {
        partitionByCompensationNeed(emptyList());
    }

    @Test
    public void testCalculateCompensationBasis() {
        final SubsidyAllocationCompensationBasis basis =
                partitionByCompensationNeed(createDefaultInput()).calculateCompensationBasis();

        assertEquals(currency(2 + 2 + 6), basis.getTotalCompensationNeed());
        assertEquals(currency(11 + 6), basis.getSumOfSubsidiesAboveLowerLimit());
        assertEquals(bd("0.5882352941"), basis.getDecrementCoefficient());
    }

    private static List<SubsidyCompensationInputDTO> createDefaultInput() {
        return asList(
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_2,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT,
                SubsidyCompensationInputs.NEGATIVE_SECOND_BATCH,
                SubsidyCompensationInputs.NEGATIVE_SECOND_BATCH_AND_TOTAL_SUBSIDY_BELOW_LOWER_LIMIT,
                SubsidyCompensationInputs.ALREADY_COMPENSATED);
    }
}
