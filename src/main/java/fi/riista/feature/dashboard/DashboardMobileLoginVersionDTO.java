package fi.riista.feature.dashboard;

public class DashboardMobileLoginVersionDTO {
    private String version;
    private long count;

    public DashboardMobileLoginVersionDTO(final String version, final long count) {
        this.version = version;
        this.count = count;
    }

    public String getVersion() {
        return version;
    }

    public long getCount() {
        return count;
    }
}
