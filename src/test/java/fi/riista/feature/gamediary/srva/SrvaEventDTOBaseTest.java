package fi.riista.feature.gamediary.srva;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class SrvaEventDTOBaseTest<DTO extends SrvaEventDTOBase> {

    protected void assertIsExclusiveGameSpeciesCodeOrOtherSpeciesDescription(DTO dto) {
        assertFalse(dto.isExclusiveGameSpeciesCodeOrOtherSpeciesDescription());

        dto.setGameSpeciesCode(1);
        assertTrue(dto.isExclusiveGameSpeciesCodeOrOtherSpeciesDescription());

        dto.setGameSpeciesCode(null);
        dto.setOtherSpeciesDescription("other");
        assertTrue(dto.isExclusiveGameSpeciesCodeOrOtherSpeciesDescription());

        dto.setGameSpeciesCode(1);
        dto.setOtherSpeciesDescription("other");
        assertFalse(dto.isExclusiveGameSpeciesCodeOrOtherSpeciesDescription());
    }

}
