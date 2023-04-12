package fi.riista.feature.gamediary.harvest.validation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.search.HuntingClubNameDTO;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestHuntingClubValidatorTest {

    @Test
    public void testValid() {
        //Selected HuntingClub must be null for mooselike GameSpecies
        HarvestDTO harvestDTO = new HarvestDTO();
        harvestDTO.setSelectedHuntingClub(null);
        harvestDTO.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_MOOSE);

        HarvestHuntingClubValidator validator = new HarvestHuntingClubValidator();
        assertTrue(validator.isValid(harvestDTO, null));
    }

    @Test
    public void testInvalid() {
        //Selected HuntingClub can not be set for mooselike GameSpecies
        HarvestDTO harvestDTO = new HarvestDTO();
        HuntingClubNameDTO huntingClubDTO = new HuntingClubNameDTO();
        huntingClubDTO.setId(1L);
        harvestDTO.setSelectedHuntingClub(huntingClubDTO);
        harvestDTO.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_MOOSE);

        HarvestHuntingClubValidator validator = new HarvestHuntingClubValidator();
        assertFalse(validator.isValid(harvestDTO, null));
    }
}
