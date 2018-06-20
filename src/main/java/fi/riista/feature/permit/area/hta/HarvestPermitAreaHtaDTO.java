package fi.riista.feature.permit.area.hta;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.organization.OrganisationNameDTO;

public class HarvestPermitAreaHtaDTO extends BaseEntityDTO<Long> {

    public static HarvestPermitAreaHtaDTO create(final HarvestPermitAreaHta permitAreaHta) {
        final OrganisationNameDTO htaDto = new OrganisationNameDTO();
        final GISHirvitalousalue hta = permitAreaHta.getHta();
        htaDto.setId(hta.getId().longValue());
        htaDto.setNameFI(hta.getNameFinnish());
        htaDto.setNameSV(hta.getNameSwedish());
        htaDto.setOfficialCode(hta.getNameAbbrv());


        final HarvestPermitAreaHtaDTO dto = new HarvestPermitAreaHtaDTO();
        dto.setId(permitAreaHta.getId());
        dto.setRev(permitAreaHta.getConsistencyVersion());
        dto.setComputedAreaSize(permitAreaHta.getAreaSize());
        dto.setHta(htaDto);
        return dto;
    }

    private Long id;
    private Integer rev;

    private OrganisationNameDTO hta;
    private double computedAreaSize;

    @Override
    public Long getId() {
        return this.id;
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

    public OrganisationNameDTO getHta() {
        return hta;
    }

    public void setHta(OrganisationNameDTO hta) {
        this.hta = hta;
    }

    public double getComputedAreaSize() {
        return computedAreaSize;
    }

    public void setComputedAreaSize(double computedAreaSize) {
        this.computedAreaSize = computedAreaSize;
    }
}
