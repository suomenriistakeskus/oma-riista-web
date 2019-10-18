package fi.riista.feature.harvestpermit.mobile;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class MobileHarvestPermitSpeciesAmountDTO extends Has2BeginEndDatesDTO {

    public static List<MobileHarvestPermitSpeciesAmountDTO> create(
            @Nonnull final Iterable<HarvestPermitSpeciesAmount> speciesAmounts) {
        return F.mapNonNullsToList(speciesAmounts, MobileHarvestPermitSpeciesAmountDTO::create);
    }

    @Nonnull
    public static MobileHarvestPermitSpeciesAmountDTO create(@Nonnull final HarvestPermitSpeciesAmount speciesAmount) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");

        final int huntingYear = DateUtil.huntingYearContaining(speciesAmount.getBeginDate());
        final int gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        final RequiredHarvestFields.Specimen specimenRequirements = RequiredHarvestFields.getSpecimenFields(
                huntingYear, gameSpeciesCode, null, HarvestReportingType.PERMIT);

        final MobileHarvestPermitSpeciesAmountDTO dto = new MobileHarvestPermitSpeciesAmountDTO();
        dto.setGameSpeciesCode(gameSpeciesCode);
        dto.setAmount(speciesAmount.getAmount());
        dto.copyDatesFrom(speciesAmount);

        dto.setAgeRequired(specimenRequirements.getAge() == Required.YES);
        dto.setGenderRequired(specimenRequirements.getGender() == Required.YES);
        dto.setWeightRequired(specimenRequirements.getWeight() == Required.YES);

        return dto;
    }

    private int gameSpeciesCode;
    private float amount;

    private boolean ageRequired;
    private boolean genderRequired;
    private boolean weightRequired;

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public boolean isAgeRequired() {
        return ageRequired;
    }

    public void setAgeRequired(boolean ageRequired) {
        this.ageRequired = ageRequired;
    }

    public boolean isGenderRequired() {
        return genderRequired;
    }

    public void setGenderRequired(boolean genderRequired) {
        this.genderRequired = genderRequired;
    }

    public boolean isWeightRequired() {
        return weightRequired;
    }

    public void setWeightRequired(boolean weightRequired) {
        this.weightRequired = weightRequired;
    }
}
