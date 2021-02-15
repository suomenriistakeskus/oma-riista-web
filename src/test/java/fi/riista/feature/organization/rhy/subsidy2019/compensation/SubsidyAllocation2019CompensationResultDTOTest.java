package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import org.junit.Test;

public class SubsidyAllocation2019CompensationResultDTOTest {

    @Test
    public void testNoCompensationDone() {
        // should pass validation
        SubsidyAllocation2019CompensationResultDTO.noCompensationDone();
    }
}
