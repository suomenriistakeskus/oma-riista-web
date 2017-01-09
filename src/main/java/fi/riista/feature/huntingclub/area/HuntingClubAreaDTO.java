package fi.riista.feature.huntingclub.area;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HuntingClubAreaDTO extends BaseEntityDTO<Long> {

    public static HuntingClubAreaDTO create(final HuntingClubArea huntingClubArea,
                                            final HuntingClub huntingClub,
                                            final GISZoneWithoutGeometryDTO zoneWithSize) {
        final HuntingClubAreaDTO dto = new HuntingClubAreaDTO();
        DtoUtil.copyBaseFields(huntingClubArea, dto);

        dto.setHuntingYear(huntingClubArea.getHuntingYear());
        dto.setMetsahallitusYear(huntingClubArea.getMetsahallitusYear());
        dto.setActive(huntingClubArea.isActive());
        dto.setNameFI(huntingClubArea.getNameFinnish());
        dto.setNameSV(huntingClubArea.getNameSwedish());
        dto.setExternalId(huntingClubArea.getExternalId());

        if (huntingClub != null) {
            dto.setClubId(huntingClub.getId());
        }

        if (zoneWithSize != null) {
            dto.setZoneId(zoneWithSize.getId());
            dto.setComputedAreaSize(zoneWithSize.getComputedAreaSize());
            dto.setWaterAreaSize(zoneWithSize.getWaterAreaSize());
            dto.setSourceType(zoneWithSize.getSourceType());
        } else {
            dto.setWaterAreaSize(0);
            dto.setComputedAreaSize(0);
            dto.setSourceType(GISZone.SourceType.LOCAL);
        }

        return dto;
    }

    private Long id;
    private Integer rev;

    private boolean active;

    @NotNull
    private Long clubId;

    @Range(min = 2000, max = 2100)
    private int huntingYear;

    @Range(min = 2000, max = 2100)
    private Integer metsahallitusYear;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String externalId;

    // Read-only properties

    private Long zoneId;
    private double computedAreaSize;
    private double waterAreaSize;
    private GISZone.SourceType sourceType;

    private boolean canEdit;
    private boolean attachedToGroup;
    private boolean hasPendingZoneChanges;

    private LocalDateTime lastModifiedDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastModifierName;

    private boolean lastModifierRiistakeskus;

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

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final Long zoneId) {
        this.zoneId = zoneId;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public Integer getMetsahallitusYear() {
        return metsahallitusYear;
    }

    public void setMetsahallitusYear(final Integer metsahallitusYear) {
        this.metsahallitusYear = metsahallitusYear;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(String nameSV) {
        this.nameSV = nameSV;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
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

    public GISZone.SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(final GISZone.SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isAttachedToGroup() {
        return attachedToGroup;
    }

    public void setAttachedToGroup(final boolean attachedToGroup) {
        this.attachedToGroup = attachedToGroup;
    }

    public boolean isHasPendingZoneChanges() {
        return hasPendingZoneChanges;
    }

    public void setHasPendingZoneChanges(boolean hasPendingZoneChanges) {
        this.hasPendingZoneChanges = hasPendingZoneChanges;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifierName() {
        return lastModifierName;
    }

    public void setLastModifierName(String lastModifierName) {
        this.lastModifierName = lastModifierName;
    }

    public boolean isLastModifierRiistakeskus() {
        return lastModifierRiistakeskus;
    }

    public void setLastModifierRiistakeskus(boolean lastModifierRiistakeskus) {
        this.lastModifierRiistakeskus = lastModifierRiistakeskus;
    }
}
