package fi.riista.feature.huntingclub.hunting;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class MobileGroupHuntingStatusDTO {
    private final boolean canCreateHuntingDay;
    private final boolean canCreateHarvest;
    private final boolean canCreateObservation;
    private final boolean canEditDiaryEntry;
    private final boolean canEditHuntingDay;

    public static MobileGroupHuntingStatusDTO from(final @Nonnull GroupHuntingStatusDTO dto) {
        requireNonNull(dto);
        return new MobileGroupHuntingStatusDTO(dto.isCanCreateHuntingDay(), dto.isCanCreateHarvest(), dto.isCanCreateObservation(), dto.isCanEditDiaryEntry(), dto.isCanEditHuntingDay());
    }

    private MobileGroupHuntingStatusDTO(final boolean canCreateHuntingDay,
                                        final boolean canCreateHarvest,
                                        final boolean canCreateObservation,
                                        final boolean canEditDiaryEntry,
                                        final boolean canEditHuntingDay) {
        this.canCreateHuntingDay = canCreateHuntingDay;
        this.canCreateHarvest = canCreateHarvest;
        this.canCreateObservation = canCreateObservation;
        this.canEditDiaryEntry = canEditDiaryEntry;
        this.canEditHuntingDay = canEditHuntingDay;
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

}
