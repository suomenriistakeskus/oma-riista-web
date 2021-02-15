package fi.riista.feature.gamediary.observation;

import fi.riista.util.F;
import static fi.riista.util.F.coalesceAsInt;

public interface HasMooselikeObservationAmounts {

    Integer getMooselikeMaleAmount();

    Integer getMooselikeFemaleAmount();

    Integer getMooselikeCalfAmount();

    Integer getMooselikeFemale1CalfAmount();

    Integer getMooselikeFemale2CalfsAmount();

    Integer getMooselikeFemale3CalfsAmount();

    Integer getMooselikeFemale4CalfsAmount();

    Integer getMooselikeUnknownSpecimenAmount();

    default int getSumOfMooselikeAmounts() {
        return coalesceAsInt(getMooselikeMaleAmount(), 0)
                + coalesceAsInt(getMooselikeFemaleAmount(), 0)
                + coalesceAsInt(getMooselikeCalfAmount(), 0)
                + 2 * coalesceAsInt(getMooselikeFemale1CalfAmount(), 0)
                + 3 * coalesceAsInt(getMooselikeFemale2CalfsAmount(), 0)
                + 4 * coalesceAsInt(getMooselikeFemale3CalfsAmount(), 0)
                + 5 * coalesceAsInt(getMooselikeFemale4CalfsAmount(), 0)
                + coalesceAsInt(getMooselikeUnknownSpecimenAmount(), 0);
    }

    default boolean isAnyMooselikeAmountPresent() {
        return F.anyNonNull(
                getMooselikeMaleAmount(), getMooselikeFemaleAmount(), getMooselikeCalfAmount(),
                getMooselikeFemale1CalfAmount(), getMooselikeFemale2CalfsAmount(), getMooselikeFemale3CalfsAmount(),
                getMooselikeFemale4CalfsAmount(), getMooselikeUnknownSpecimenAmount());
    }

    default boolean hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies() {
        // The list below includes all mandatory amount fields for moose. Other moose-like species
        // include these as well plus female-with4-calfs-amount.
        return F.allNotNull(
                getMooselikeMaleAmount(), getMooselikeFemaleAmount(), getMooselikeFemale1CalfAmount(),
                getMooselikeFemale2CalfsAmount(), getMooselikeFemale3CalfsAmount(), getMooselikeUnknownSpecimenAmount());
    }
}
