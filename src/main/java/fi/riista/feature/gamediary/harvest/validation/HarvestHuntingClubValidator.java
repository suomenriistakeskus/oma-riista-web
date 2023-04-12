package fi.riista.feature.gamediary.harvest.validation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HarvestHuntingClubValidator implements ConstraintValidator<HarvestHuntingClubConstraint, HarvestDTOBase> {

    @Override
    public boolean isValid(HarvestDTOBase dto, ConstraintValidatorContext context) {
        if (GameSpecies.isMooseOrDeerRequiringPermitForHunting(dto.getGameSpeciesCode())) {
            return dto.getSelectedHuntingClub() == null || dto.getSelectedHuntingClub().getId() == null;
        }
        return true;
    }
}