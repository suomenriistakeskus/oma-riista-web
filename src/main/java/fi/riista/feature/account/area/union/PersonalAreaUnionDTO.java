package fi.riista.feature.account.area.union;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class PersonalAreaUnionDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;
    private String name;
    private LastModifierDTO modifier;
    private HarvestPermitArea.StatusCode status;
    private int huntingYear;
    private String externalId;
    private GISZoneSizeDTO size;
    final GISBounds bounds;
    private List<HarvestPermitAreaPartnerDTO> partners;

    public PersonalAreaUnionDTO(final PersonalAreaUnion entity, final LastModifierDTO modifier,
                                final HarvestPermitArea area, final GISZoneSizeDTO size,
                                final GISBounds bounds,
                                final List<HarvestPermitAreaPartnerDTO> partners) {
        final PersonalAreaUnion personalAreaUnion = requireNonNull(entity);
        this.id = personalAreaUnion.getId();
        this.rev = personalAreaUnion.getConsistencyVersion();
        this.name = personalAreaUnion.getName();
        this.modifier = modifier;
        this.externalId = area.getExternalId();
        this.status = area.getStatus();
        this.huntingYear = area.getHuntingYear();
        this.size = size;
        this.bounds = bounds;
        this.partners = partners;
    }

    public String getName() {
        return name;
    }

    public LastModifierDTO getModifier() {
        return modifier;
    }

    public String getExternalId() {
        return externalId;
    }

    public HarvestPermitArea.StatusCode getStatus() {
        return status;
    }

    public GISZoneSizeDTO getSize() {
        return size;
    }

    public GISBounds getBounds() {
        return bounds;
    }

    public List<HarvestPermitAreaPartnerDTO> getPartners() {
        return partners;
    }

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
        return this.rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }
}
