package fi.riista.feature.permit.application.metsahallitus;

import java.util.List;

public class MetsahallitusAreaPermitUrlsDTO {
    long applicationId;
    String verdictFileUrl;
    List<String> shooterListFileUrls;

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final long applicationId) {
        this.applicationId = applicationId;
    }

    public String getVerdictFileUrl() {
        return verdictFileUrl;
    }

    public void setVerdictFileUrl(final String verdictFileUrl) {
        this.verdictFileUrl = verdictFileUrl;
    }

    public List<String> getShooterListFileUrls() {
        return shooterListFileUrls;
    }

    public void setShooterListFileUrls(final List<String> shooterListFileUrls) {
        this.shooterListFileUrls = shooterListFileUrls;
    }
}
