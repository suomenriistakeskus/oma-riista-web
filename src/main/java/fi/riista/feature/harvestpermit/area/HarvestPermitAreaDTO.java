package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.entity.BaseEntityDTO;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import java.util.List;

public class HarvestPermitAreaDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;

    @NotNull
    private Long clubId;

    @Range(min = 2000, max = 2100)
    private int huntingYear;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String externalId;

    private HarvestPermitArea.StatusCode status;

    private List<HarvestPermitAreaRhyDTO> rhy;
    private List<HarvestPermitAreaHtaDTO> hta;

    // Read-only properties

    private double computedAreaSize;
    private double waterAreaSize;
    private long partnerCount;

    private LocalDateTime lastModifiedDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastModifierName;

    private boolean lastModifierRiistakeskus;

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

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(final Long clubId) {
        this.clubId = clubId;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(final String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(final String nameSV) {
        this.nameSV = nameSV;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public HarvestPermitArea.StatusCode getStatus() {
        return status;
    }

    public void setStatus(HarvestPermitArea.StatusCode status) {
        this.status = status;
    }

    public double getComputedAreaSize() {
        return computedAreaSize;
    }

    public void setComputedAreaSize(final double computedAreaSize) {
        this.computedAreaSize = computedAreaSize;
    }

    public double getWaterAreaSize() {
        return waterAreaSize;
    }

    public void setWaterAreaSize(final double waterAreaSize) {
        this.waterAreaSize = waterAreaSize;
    }

    public long getPartnerCount() {
        return partnerCount;
    }

    public void setPartnerCount(final long partnerCount) {
        this.partnerCount = partnerCount;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifierName() {
        return lastModifierName;
    }

    public void setLastModifierName(final String lastModifierName) {
        this.lastModifierName = lastModifierName;
    }

    public boolean isLastModifierRiistakeskus() {
        return lastModifierRiistakeskus;
    }

    public void setLastModifierRiistakeskus(final boolean lastModifierRiistakeskus) {
        this.lastModifierRiistakeskus = lastModifierRiistakeskus;
    }

    public List<HarvestPermitAreaRhyDTO> getRhy() {
        return rhy;
    }

    public void setRhy(List<HarvestPermitAreaRhyDTO> rhy) {
        this.rhy = rhy;
    }

    public List<HarvestPermitAreaHtaDTO> getHta() {
        return hta;
    }

    public void setHta(List<HarvestPermitAreaHtaDTO> hta) {
        this.hta = hta;
    }
}
