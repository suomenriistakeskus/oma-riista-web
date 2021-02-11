package fi.riista.feature.gamediary.observation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObservationDTOBaseTest {

    @Test
    public void testGetSumOfMooselikeAmountFields() {
        final ObservationDTOBase dto = new ObservationDTO();
        dto.setMooselikeMaleAmount(3);
        dto.setMooselikeFemaleAmount(5);
        dto.setMooselikeCalfAmount(7);
        dto.setMooselikeFemale1CalfAmount(11);
        dto.setMooselikeFemale2CalfsAmount(13);
        dto.setMooselikeFemale3CalfsAmount(17);
        dto.setMooselikeFemale4CalfsAmount(19);
        dto.setMooselikeUnknownSpecimenAmount(23);

        assertEquals(3 + 5 + 7 + 2 * 11 + 3 * 13 + 4 * 17 + 5 * 19 + 23, dto.getSumOfMooselikeAmounts());
    }
}
