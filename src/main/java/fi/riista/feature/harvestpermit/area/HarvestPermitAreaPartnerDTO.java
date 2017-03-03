package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.DtoUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class HarvestPermitAreaPartnerDTO extends BaseEntityDTO<Long> {

    public static class SourceAreaDTO implements Serializable {

        private final Map<String, String> name;
        private final String externalId;
        private final boolean hasChanged;

        public SourceAreaDTO(final HuntingClubArea huntingClubArea, final boolean hasChanged) {
            this.name = huntingClubArea.getNameLocalisation().asMap();
            this.externalId = huntingClubArea.getExternalId();
            this.hasChanged = hasChanged;
        }

        public Map<String, String> getName() {
            return name;
        }

        public String getExternalId() {
            return externalId;
        }

        public boolean isHasChanged() {
            return hasChanged;
        }
    }

    private Long id;
    private Integer rev;

    private final OrganisationNameDTO club;
    private final SourceAreaDTO sourceArea;

    private final double totalAreaSize;
    private final double waterAreaSize;

    public HarvestPermitAreaPartnerDTO(final HarvestPermitAreaPartner partner,
                                       final HuntingClubArea huntingClubArea,
                                       final HuntingClub club,
                                       final GISZoneWithoutGeometryDTO zoneDTO,
                                       final Optional<Date> originalZoneMtime) {
        DtoUtil.copyBaseFields(partner, this);

        this.club = OrganisationNameDTO.createWithOfficialCode(club);

        final boolean hasChanged = originalZoneMtime
                .map(partner.getLifecycleFields().getModificationTime()::before)
                .orElse(false);
        this.sourceArea = new SourceAreaDTO(huntingClubArea, hasChanged);

        this.totalAreaSize = zoneDTO.getComputedAreaSize();
        this.waterAreaSize = zoneDTO.getWaterAreaSize();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public OrganisationNameDTO getClub() {
        return club;
    }

    public SourceAreaDTO getSourceArea() {
        return sourceArea;
    }

    public double getTotalAreaSize() {
        return totalAreaSize;
    }

    public double getWaterAreaSize() {
        return waterAreaSize;
    }

}
