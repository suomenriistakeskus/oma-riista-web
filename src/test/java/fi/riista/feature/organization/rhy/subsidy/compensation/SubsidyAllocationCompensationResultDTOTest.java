package fi.riista.feature.organization.rhy.subsidy.compensation;

import org.junit.Test;

public class SubsidyAllocationCompensationResultDTOTest {

    @Test
    public void testNoCompensationDone() {
        // should pass validation
        SubsidyAllocationCompensationResultDTO.noCompensationDone();
    }
}
