package fi.riista.feature.dashboard;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class HuntingClubInfoDTO  {

    public static HuntingClubInfoDTO from(final @Nonnull LocalisedString huntingClub,
                                          final @Nullable GeoLocation geoLocation){
        return new HuntingClubInfoDTO(
                requireNonNull(huntingClub),
                ofNullable(geoLocation).map(GeoLocation::getLatitude).orElse(null),
                ofNullable(geoLocation).map(GeoLocation::getLongitude).orElse(null));
    }

    private HuntingClubInfoDTO(final LocalisedString huntingClub,
                               final Integer clubLatitude, final Integer clubLongitude) {
        this.huntingClub = huntingClub;
        this.clubLatitude = clubLatitude;
        this.clubLongitude = clubLongitude;
    }

    private LocalisedString huntingClub;
    private Integer clubLatitude;
    private Integer clubLongitude;

    public LocalisedString getHuntingClub() {
        return huntingClub;
    }

    public Integer getClubLatitude() {
        return clubLatitude;
    }

    public Integer getClubLongitude() {
        return clubLongitude;
    }
}
