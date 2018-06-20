package fi.riista.feature.dashboard;

import java.util.Objects;

public class DashboardMobileLoginPlatformDTO {
    private String key;
    private long android;
    private long ios;
    private long wp;

    public DashboardMobileLoginPlatformDTO(final String key, final long android, final long ios, final long wp) {
        this.key = Objects.requireNonNull(key);
        this.android = android;
        this.ios = ios;
        this.wp = wp;
    }

    public String getKey() {
        return key;
    }

    public long getAndroid() {
        return android;
    }

    public long getIos() {
        return ios;
    }

    public long getWp() {
        return wp;
    }
}
