package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;

import javax.annotation.Nonnull;

import static fi.riista.feature.permit.PermitTypeCode.GAME_MANAGEMENT;
import static fi.riista.feature.permit.PermitTypeCode.IMPORTING;
import static java.util.Objects.requireNonNull;

class HarvestPermitSpeciesAmountOps {
    @Nonnull
    public static HarvestPermitSpeciesAmount create(final @Nonnull HarvestPermit permit, final @Nonnull PermitDecisionSpeciesAmount spa) {
        requireNonNull(permit);
        requireNonNull(spa);

        final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount;
        switch (permit.getPermitTypeCode()){
            case PermitTypeCode.NEST_REMOVAL_BASED: {
                harvestPermitSpeciesAmount =
                        HarvestPermitSpeciesAmount.createForNestRemoval(permit,
                                spa.getGameSpecies(), spa.getNestAmount(), spa.getEggAmount(), spa.getConstructionAmount(),
                                convertRestriction(spa), spa.getRestrictionAmount(),
                                spa.getBeginDate(), spa.getEndDate());
                break;
            }
            case IMPORTING:
            case GAME_MANAGEMENT: {
                harvestPermitSpeciesAmount =
                        HarvestPermitSpeciesAmount.createWithSpecimenOrEggs(permit,
                                spa.getGameSpecies(), spa.getSpecimenAmount(), spa.getEggAmount(), spa.getBeginDate(), spa.getEndDate());
                break;
            }
            default: {
                harvestPermitSpeciesAmount =
                        HarvestPermitSpeciesAmount.createForHarvest(permit,
                                spa.getGameSpecies(), spa.getSpecimenAmount(),
                                convertRestriction(spa), spa.getRestrictionAmount(),
                                spa.getBeginDate(), spa.getEndDate());
                break;
            }
        }

        harvestPermitSpeciesAmount.setBeginDate2(spa.getBeginDate2());
        harvestPermitSpeciesAmount.setEndDate2(spa.getEndDate2());

        return harvestPermitSpeciesAmount;
    }

    public static void copy(final @Nonnull PermitDecisionSpeciesAmount from, final @Nonnull HarvestPermitSpeciesAmount to) {
        requireNonNull(from);
        requireNonNull(to);

        to.setSpecimenAmount(from.getSpecimenAmount());
        to.setNestAmount(from.getNestAmount());
        to.setEggAmount(from.getEggAmount());
        to.setConstructionAmount(from.getConstructionAmount());
        to.setBeginDate(from.getBeginDate());
        to.setEndDate(from.getEndDate());
        to.setBeginDate2(from.getBeginDate2());
        to.setEndDate2(from.getEndDate2());
        to.setRestrictionAmount(from.getRestrictionAmount());
        to.setRestrictionType(convertRestriction(from));
    }

    static HarvestPermitSpeciesAmount.RestrictionType convertRestriction(final @Nonnull PermitDecisionSpeciesAmount spa) {
        requireNonNull(spa);

        if (spa.getRestrictionType() == null) {
            return null;
        }

        switch (spa.getRestrictionType()) {
            case AE:
                return HarvestPermitSpeciesAmount.RestrictionType.AE;
            case AU:
                return HarvestPermitSpeciesAmount.RestrictionType.AU;

            default:
                throw new IllegalArgumentException("Unknown restriction type: "
                        + spa.getRestrictionType());
        }
    }

    private HarvestPermitSpeciesAmountOps() {
        throw new AssertionError();
    }
}
