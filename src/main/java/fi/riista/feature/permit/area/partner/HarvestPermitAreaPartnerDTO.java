package fi.riista.feature.permit.area.partner;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.DtoUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

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
    private final TotalLandWaterSizeDTO size;

    public HarvestPermitAreaPartnerDTO(final HarvestPermitAreaPartner partner,
                                       final SourceAreaDTO sourceArea,
                                       final OrganisationNameDTO club,
                                       final GISZoneSizeDTO size) {
        DtoUtil.copyBaseFields(partner, this);

        this.club = Objects.requireNonNull(club);
        this.sourceArea = Objects.requireNonNull(sourceArea);
        this.size = size != null ? size.getAll() : null;
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

    public TotalLandWaterSizeDTO getSize() {
        return size;
    }

}
