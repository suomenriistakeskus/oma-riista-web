package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.srva.SrvaEventDTOBaseTest;
import org.junit.Test;

public class MobileSrvaEventDTOTest extends SrvaEventDTOBaseTest<MobileSrvaEventDTO> {

    @Test
    public void testIsExclusiveGameSpeciesCodeOrOtherSpeciesDescription() {
        MobileSrvaEventDTO dto = new MobileSrvaEventDTO();
        assertIsExclusiveGameSpeciesCodeOrOtherSpeciesDescription(dto);
    }

}
