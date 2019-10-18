package fi.riista.feature.harvestpermit;

import javax.annotation.Nonnull;

// This DTO contains additional amounts collected from amendment permits
public class HarvestPermitSpeciesAmountWithAmendmentDTO extends HarvestPermitSpeciesAmountDTO {

    @Nonnull
    public static HarvestPermitSpeciesAmountWithAmendmentDTO create(@Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                                                    float amendmentAmount) {
        return new HarvestPermitSpeciesAmountWithAmendmentDTO(speciesAmount, amendmentAmount);
    }

    private float amendmentAmount;

    public HarvestPermitSpeciesAmountWithAmendmentDTO(@Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                                      float amendmentAmount) {
        super(speciesAmount, speciesAmount.getGameSpecies());
        this.amendmentAmount = amendmentAmount;
    }

    public float getAmendmentAmount() {
        return amendmentAmount;
    }

    public void setAmendmentAmount(final float amendmentAmount) {
        this.amendmentAmount = amendmentAmount;
    }
}
