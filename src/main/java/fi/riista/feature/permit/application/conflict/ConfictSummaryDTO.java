package fi.riista.feature.permit.application.conflict;

class ConfictSummaryDTO {
    private final long applicationId;
    private final boolean onlyMhConflicts;
    private final boolean onlyPrivateConflicts;
    private final Double conflictSum;

    ConfictSummaryDTO(final long applicationId,
                      final Integer mhCount,
                      final Integer privateCount,
                      final Double conflictSum) {
        this.applicationId = applicationId;
        this.onlyMhConflicts = privateCount == null || privateCount == 0;
        this.onlyPrivateConflicts = mhCount == null || mhCount == 0;
        this.conflictSum = conflictSum;
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
}
