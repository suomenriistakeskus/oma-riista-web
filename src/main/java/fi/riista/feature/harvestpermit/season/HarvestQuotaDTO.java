package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import fi.riista.validation.DoNotValidate;

import javax.annotation.Nonnull;
import java.util.Optional;

public class HarvestQuotaDTO extends BaseEntityDTO<Long> {

    public static @Nonnull HarvestQuotaDTO create(@Nonnull HarvestQuota quota) {
        HarvestQuotaDTO dto = new HarvestQuotaDTO();
        DtoUtil.copyBaseFields(quota, dto);
        dto.setQuota(quota.getQuota());
        dto.setHarvestArea(HarvestAreaDTO.create(quota.getHarvestArea()));
        return dto;
    }

    private Long id;
    private Integer rev;

    private Integer quota;

    @DoNotValidate
    private HarvestAreaDTO harvestArea;

    public HarvestQuotaDTO() {
    }

    public HarvestQuotaDTO(@Nonnull final HarvestQuotaDTO other) {
        super(other);
        setQuota(other.getQuota());
        setHarvestArea(Optional.ofNullable(other.getHarvestArea()).map(HarvestAreaDTO::new).orElse(null));
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setHarvestArea(HarvestAreaDTO harvestArea) {
        this.harvestArea = harvestArea;
    }

    public HarvestAreaDTO getHarvestArea() {
        return harvestArea;
    }
}
