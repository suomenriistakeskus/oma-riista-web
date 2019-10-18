package fi.riista.feature.huntingclub.permit.partner;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;

import javax.annotation.Nullable;

public class HarvestPermitPartnerAreaDTO {
    private final long clubId;
    private final GISZoneSizeDTO areaSize;
    private final String externalId;

    public HarvestPermitPartnerAreaDTO(final long clubId, final GISZoneSizeDTO areaSize, final String externalId) {
        this.clubId = clubId;
        this.areaSize = areaSize;
        this.externalId = externalId;
    }

    public long getClubId() {
        return clubId;
    }

    @Nullable
    public GISZoneSizeDTO getAreaSize() {
        return areaSize;
    }

    public String getExternalId() {
        return externalId;
    }
}
