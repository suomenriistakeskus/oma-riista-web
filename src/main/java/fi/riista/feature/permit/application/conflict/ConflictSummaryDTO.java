package fi.riista.feature.permit.application.conflict;

class ConflictSummaryDTO {
    private final long applicationId;
    private final boolean onlyMhConflicts;
    private final boolean onlyPrivateConflicts;
    private final Double conflictSum;
    private final Double conflictWaterSum;
    private final Double conflictPrivateAreaSum;
    private final Double conflictPrivateAreaWaterSum;

    ConflictSummaryDTO(final long applicationId,
                       final Integer mhCount,
                       final Integer privateCount,
                       final Double conflictSum,
                       final Double conflictWaterSum,
                       final Double conflictPrivateAreaSum,
                       final Double conflictPrivateAreaWaterSum) {
        this.applicationId = applicationId;
        this.onlyMhConflicts = privateCount == null || privateCount == 0;
        this.onlyPrivateConflicts = mhCount == null || mhCount == 0;
        this.conflictSum = conflictSum;
        this.conflictWaterSum = conflictWaterSum;
        this.conflictPrivateAreaSum = conflictPrivateAreaSum;
        this.conflictPrivateAreaWaterSum = conflictPrivateAreaWaterSum;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public boolean isOnlyMhConflicts() {
        return onlyMhConflicts;
    }

    public boolean isOnlyPrivateConflicts() {
        return onlyPrivateConflicts;
    }

    public Double getConflictSum() {
        return conflictSum;
    }

    public Double getConflictWaterSum() {
        return conflictWaterSum;
    }

    public Double getConflictPrivateAreaSum() {
        return conflictPrivateAreaSum;
    }

    public Double getConflictPrivateAreaWaterSum() {
        return conflictPrivateAreaWaterSum;
    }
}
