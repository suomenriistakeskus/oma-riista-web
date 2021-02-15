package fi.riista.feature.shootingtest.expiry;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class ShootingTestEndOfYearExpiryDTO {

    private final String date;
    private final String time;

    public ShootingTestEndOfYearExpiryDTO(final @Nonnull String date,
                                          final @Nonnull String time) {
        this.date = requireNonNull(date);
        this.time = requireNonNull(time);
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

}
