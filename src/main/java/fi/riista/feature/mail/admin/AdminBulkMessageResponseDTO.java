package fi.riista.feature.mail.admin;

public class AdminBulkMessageResponseDTO {
    private long successCount;
    private long errorCount;

    public AdminBulkMessageResponseDTO(long successCount, long errorCount) {
        this.successCount = successCount;
        this.errorCount = errorCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getErrorCount() {
        return errorCount;
    }
}
