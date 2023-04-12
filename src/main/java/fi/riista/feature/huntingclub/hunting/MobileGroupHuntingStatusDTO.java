package fi.riista.feature.huntingclub.hunting;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class MobileGroupHuntingStatusDTO {
    private final boolean canCreateHuntingDay;
    private final boolean canCreateHarvest;
    private final boolean canCreateObservation;
    private final boolean canEditDiaryEntry;
    private final boolean canEditHuntingDay;
    /** To ease implementation of MJ views in mobile */
    private final boolean canEditHarvest;
    private final boolean canEditObservation;
    private final boolean huntingFinished;

    public static MobileGroupHuntingStatusDTO from(final @Nonnull GroupHuntingStatusDTO dto, final boolean huntingFinished) {
        requireNonNull(dto);
        if (huntingFinished) {
            return new MobileGroupHuntingStatusDTO(false, false, false, false, false, true);
        }
        return new MobileGroupHuntingStatusDTO(dto.isCanCreateHuntingDay(), dto.isCanCreateHarvest(), dto.isCanCreateObservation(),
                                               dto.isCanEditDiaryEntry(), dto.isCanEditHuntingDay(), false);
    }

    private MobileGroupHuntingStatusDTO(final boolean canCreateHuntingDay,
                                        final boolean canCreateHarvest,
                                        final boolean canCreateObservation,
                                        final boolean canEditDiaryEntry,
                                        final boolean canEditHuntingDay,
                                        final boolean huntingFinished) {
        this.canCreateHuntingDay = canCreateHuntingDay;
        this.canCreateHarvest = canCreateHarvest;
        this.canCreateObservation = canCreateObservation;
        this.canEditDiaryEntry = canEditDiaryEntry;
        this.canEditHuntingDay = canEditHuntingDay;
        this.canEditHarvest = canEditDiaryEntry && canCreateHarvest;
        this.canEditObservation = canEditDiaryEntry && canCreateObservation;
        this.huntingFinished = huntingFinished;
    }

    public boolean isCanCreateHuntingDay() {
        return canCreateHuntingDay;
    }

    public boolean isCanCreateHarvest() {
        return canCreateHarvest;
    }

    public boolean isCanCreateObservation() {
        return canCreateObservation;
    }

    public boolean isCanEditDiaryEntry() {
        return canEditDiaryEntry;
    }

    public boolean isCanEditHuntingDay() {
        return canEditHuntingDay;
    }

    public boolean isCanEditHarvest() {
        return canEditHarvest;
    }

    public boolean isCanEditObservation() {
        return canEditObservation;
    }

    public boolean isHuntingFinished() {
        return huntingFinished;
    }
}
