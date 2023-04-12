package fi.riista.feature.permit.application.conflict;

class PalstaConflictSummaryDTO {
    private final long palstaId;
    private final boolean onlyMhConflicts;
    private final boolean onlyPrivateConflicts;
    private final Double conflictSum;
    private final Double conflictWaterSum;

    PalstaConflictSummaryDTO(final long palstaId,
                             final Integer mhCount,
                             final Integer privateCount,
                             final Double conflictSum,
                             final Double conflictWaterSum) {
        this.palstaId = palstaId;
        this.onlyMhConflicts = privateCount == null || privateCount == 0;
        this.onlyPrivateConflicts = mhCount == null || mhCount == 0;
        this.conflictSum = conflictSum;
        this.conflictWaterSum = conflictWaterSum;
    }

    public long getPalstaId() {
        return palstaId;
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
}
