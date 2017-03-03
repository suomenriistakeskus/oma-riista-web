package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.OrganisationNameDTO;

public class HarvestPermitAreaRhyDTO extends BaseEntityDTO<Long> {
    public static HarvestPermitAreaRhyDTO create(HarvestPermitAreaRhy permitAreaRhy) {
        final OrganisationNameDTO rhyDto = OrganisationNameDTO.create(permitAreaRhy.getRhy());

        final HarvestPermitAreaRhyDTO dto = new HarvestPermitAreaRhyDTO();
        dto.setId(permitAreaRhy.getId());
        dto.setRev(permitAreaRhy.getConsistencyVersion());
        dto.setComputedAreaSize(permitAreaRhy.getAreaSize());
        dto.setRhy(rhyDto);
        return dto;
    }

    private Long id;
    private Integer rev;

    private OrganisationNameDTO rhy;
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

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public void setRhy(OrganisationNameDTO rhy) {
        this.rhy = rhy;
    }

    public double getComputedAreaSize() {
        return computedAreaSize;
    }

    public void setComputedAreaSize(double computedAreaSize) {
        this.computedAreaSize = computedAreaSize;
    }
}
