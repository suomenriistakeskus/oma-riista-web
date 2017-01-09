package fi.riista.feature.huntingclub.hunting;

public class GroupHuntingStatusDTO {
    private boolean canCreateHuntingDay;
    private boolean canCreateHarvest;
    private boolean canCreateObservation;
    private boolean canEditDiaryEntry;
    private boolean canEditPermit;
    private boolean canEditHuntingDay;
    private boolean canExportData;

    public boolean isCanCreateHuntingDay() {
        return canCreateHuntingDay;
    }

    public void setCanCreateHuntingDay(final boolean canCreateHuntingDay) {
        this.canCreateHuntingDay = canCreateHuntingDay;
    }

    public boolean isCanCreateHarvest() {
        return canCreateHarvest;
    }

    public void setCanCreateHarvest(final boolean canCreateHarvest) {
        this.canCreateHarvest = canCreateHarvest;
    }

    public boolean isCanCreateObservation() {
        return canCreateObservation;
    }

    public void setCanCreateObservation(final boolean canCreateObservation) {
        this.canCreateObservation = canCreateObservation;
    }

    public boolean isCanEditDiaryEntry() {
        return canEditDiaryEntry;
    }

    public void setCanEditDiaryEntry(final boolean canEditDiaryEntry) {
        this.canEditDiaryEntry = canEditDiaryEntry;
    }

    public boolean isCanEditPermit() {
        return canEditPermit;
    }

    public void setCanEditPermit(final boolean canEditPermit) {
        this.canEditPermit = canEditPermit;
    }

    public boolean isCanEditHuntingDay() {
        return canEditHuntingDay;
    }

    public void setCanEditHuntingDay(final boolean canEditHuntingDay) {
        this.canEditHuntingDay = canEditHuntingDay;
    }

    public boolean isCanExportData() {
        return canExportData;
    }

    public void setCanExportData(final boolean canExportData) {
        this.canExportData = canExportData;
    }
}
