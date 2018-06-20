package fi.riista.integration.lupahallinta.club;

public class LHSynchronizeClubItem {
    private final Long clubId;
    private final Integer longitude;
    private final Integer latitude;
    private final String nameFinnish;
    private final String nameSwedish;
    private final Integer areaSize;
    private final Long rhyId;
    private final Integer htaId;

    public LHSynchronizeClubItem(final Long clubId, final Integer longitude, final Integer latitude,
                                 final String nameFinnish, final String nameSwedish, final Integer areaSize,
                                 final Long rhyId, final Integer htaId) {
        this.clubId = clubId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
        this.areaSize = areaSize;
        this.rhyId = rhyId;
        this.htaId = htaId;
    }

    public Long getClubId() {
        return clubId;
    }

    public Integer getLongitude() {
        return longitude;
    }

    public Integer getLatitude() {
        return latitude;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public Integer getAreaSize() {
        return areaSize;
    }

    public Long getRhyId() {
        return rhyId;
    }

    public Integer getHtaId() {
        return htaId;
    }
}
