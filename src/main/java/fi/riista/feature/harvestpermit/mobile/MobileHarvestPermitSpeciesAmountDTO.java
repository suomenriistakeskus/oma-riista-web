package fi.riista.feature.harvestpermit.mobile;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.List;

import static fi.riista.feature.gamediary.harvest.fields.RequiredHarvestSpecimenField.YES;
import static java.util.Objects.requireNonNull;

public class MobileHarvestPermitSpeciesAmountDTO extends Has2BeginEndDatesDTO {

    public static List<MobileHarvestPermitSpeciesAmountDTO> create(
            @Nonnull final Iterable<HarvestPermitSpeciesAmount> speciesAmounts,
            @Nonnull final HarvestSpecVersion specVersion) {

        return F.mapNonNullsToList(speciesAmounts, spa -> MobileHarvestPermitSpeciesAmountDTO.create(spa, specVersion));
    }

    @Nonnull
    public static MobileHarvestPermitSpeciesAmountDTO create(@Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                                             @Nonnull final HarvestSpecVersion specVersion) {

        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(specVersion, "specVersion must not be null");

        final int huntingYear = DateUtil.huntingYearContaining(speciesAmount.getBeginDate());
        final int gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();

        final boolean withPermit = true;

        final RequiredHarvestFields.Specimen specimenRequirements = RequiredHarvestFields.getSpecimenFields(
                huntingYear, gameSpeciesCode, null, HarvestReportingType.PERMIT, false, specVersion, withPermit);

        final MobileHarvestPermitSpeciesAmountDTO dto = new MobileHarvestPermitSpeciesAmountDTO();
        dto.setGameSpeciesCode(gameSpeciesCode);
        dto.setAmount(speciesAmount.getSpecimenAmount());
        dto.copyDatesFrom(speciesAmount);

        dto.setAgeRequired(specimenRequirements.getAge() == YES);
        dto.setGenderRequired(specimenRequirements.getGender() == YES);
        dto.setWeightRequired(specimenRequirements.getWeight() == YES);

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

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(final float amount) {
        this.amount = amount;
    }

    public boolean isAgeRequired() {
        return ageRequired;
    }

    public void setAgeRequired(final boolean ageRequired) {
        this.ageRequired = ageRequired;
    }

    public boolean isGenderRequired() {
        return genderRequired;
    }

    public void setGenderRequired(final boolean genderRequired) {
        this.genderRequired = genderRequired;
    }

    public boolean isWeightRequired() {
        return weightRequired;
    }

    public void setWeightRequired(final boolean weightRequired) {
        this.weightRequired = weightRequired;
    }
}
