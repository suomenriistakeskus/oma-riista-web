package fi.riista.util;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import javax.annotation.Nonnull;

import java.util.Objects;

public final class Times {

    public static int secondsBetween(@Nonnull final DateTime a, @Nonnull final DateTime b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");

        return (a.isBefore(b) ? Seconds.secondsBetween(a, b) : Seconds.secondsBetween(b, a)).getSeconds();
    }

    private Times() {
        throw new AssertionError();
    }

}
