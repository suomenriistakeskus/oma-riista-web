package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MobileHarvestPermitSpeciesAmountDTO extends Has2BeginEndDatesDTO {

    public static List<MobileHarvestPermitSpeciesAmountDTO> create(
            @Nonnull final Iterable<HarvestPermitSpeciesAmount> speciesAmounts,
            @Nonnull final Map<Long, HarvestReportFields> gameSpeciesIdToFields) {

        return F.mapNonNullsToList(speciesAmounts, amount -> {
            return MobileHarvestPermitSpeciesAmountDTO.create(
                    amount,
                    gameSpeciesIdToFields.get(amount.getGameSpecies().getId()));
        });
    }

    public static @Nonnull MobileHarvestPermitSpeciesAmountDTO create(
            @Nonnull final HarvestPermitSpeciesAmount speciesAmount,
            @Nonnull final HarvestReportFields fields) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");

        MobileHarvestPermitSpeciesAmountDTO dto = new MobileHarvestPermitSpeciesAmountDTO();
        dto.setGameSpeciesCode(speciesAmount.getGameSpecies().getOfficialCode());
        dto.setAmount(speciesAmount.getAmount());
        dto.copyDatesFrom(speciesAmount);

        dto.setAgeRequired(fields.getAge() == Required.YES);
        dto.setGenderRequired(fields.getGender() == Required.YES);
        dto.setWeightRequired(fields.getWeight() == Required.YES);
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
