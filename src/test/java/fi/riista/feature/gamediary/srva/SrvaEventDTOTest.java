package fi.riista.feature.gamediary.srva;

import org.junit.Test;

public class SrvaEventDTOTest extends SrvaEventDTOBaseTest<SrvaEventDTO> {

    @Test
    public void testIsExclusiveGameSpeciesCodeOrOtherSpeciesDescription() {
        SrvaEventDTO dto = new SrvaEventDTO();
        assertIsExclusiveGameSpeciesCodeOrOtherSpeciesDescription(dto);
    }

}
