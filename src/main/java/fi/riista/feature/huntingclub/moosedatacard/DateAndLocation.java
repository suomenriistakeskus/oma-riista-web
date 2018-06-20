package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.common.entity.GeoLocation;
import io.vavr.control.Try;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;

public interface DateAndLocation {

    LocalDate getDate();

    void setDate(LocalDate date);

    String getLatitude();

    void setLatitude(String latitude);

    String getLongitude();

    void setLongitude(String longitude);

    @Nonnull
    default Try<Long> parseLatitude() {
        return MooseDataCardExtractor.parseNumber(getLatitude(), Long::parseLong);
    }

    @Nonnull
    default Try<Long> parseLongitude() {
        return MooseDataCardExtractor.parseNumber(getLongitude(), Long::parseLong);
    }

    @Nonnull
    default Optional<Long> findLatitudeAsLong() {
        return parseLatitude()
                .map(Optional::ofNullable)
                .getOrElseGet(throwable -> Optional.empty());
    }

    @Nonnull
    default Optional<Long> findLongitudeAsLong() {
        return parseLongitude()
                .map(Optional::ofNullable)
                .getOrElseGet(throwable -> Optional.empty());
    }

    @Nullable
    default Long getLatitudeAsLong() {
        return findLatitudeAsLong().orElse(null);
    }

    @Nullable
    default Long getLongitudeAsLong() {
        return findLongitudeAsLong().orElse(null);
    }

    @Nonnull
    default Optional<GeoLocation> findGeoLocation() {
        return findLatitudeAsLong()
                .flatMap(lat -> findLongitudeAsLong().map(lng -> new GeoLocation(lat.intValue(), lng.intValue())));
    }

    @Nullable
    default GeoLocation getGeoLocation() {
        return findGeoLocation().orElse(null);
    }

    default void setGeoLocation(@Nullable final GeoLocation location) {
        if (location == null) {
            setLatitude(null);
            setLongitude(null);
        } else {
            setLatitude(String.valueOf(location.getLatitude()));
            setLongitude(String.valueOf(location.getLongitude()));
        }
    }

    default boolean isEmpty() {
        return getDate() == null && emptyToNull(getLatitude()) == null && emptyToNull(getLongitude()) == null;
    }

}
