package fi.riista.feature.organization.rhy.subsidy.compensation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SubsidyAllocationCompensationResultDTOTest {

    @Test
    public void testNoCompensationDone() {
       final SubsidyAllocationCompensationResultDTO dto = SubsidyAllocationCompensationResultDTO.noCompensationDone();

       assertFalse(dto.isAnyCompensationRoundExecuted());
       assertEquals(0, dto.getCompensationBases().size());
       assertEquals(0, dto.getNumberOfRounds());
       assertEquals(0, dto.getRounds().size());
    }
}
