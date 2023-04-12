package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Valid
public class PoiLocationGroupDTO extends BaseEntityDTO<Long> {

    public static PoiLocationGroupDTO create(@Nonnull final PoiLocationGroup poi) {
        final PoiLocationGroupDTO dto = new PoiLocationGroupDTO();
        DtoUtil.copyBaseFields(poi, dto);

        dto.setVisibleId(poi.getVisibleId());
        dto.setType(poi.getType());
        dto.setDescription(poi.getDescription());
        return dto;
    }

    private Long id;
    private Integer rev;

    private Integer visibleId;

    private Long clubId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    @NotNull
    private PointOfInterestType type;

    @Nullable
    private LocalDateTime lastModifiedDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastModifierName;

    private boolean lastModifierRiistakeskus;

    @Valid
    @NotNull
    private List<PoiLocationDTO> locations;

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

    public int getVisibleId() {
        return visibleId;
    }

    public void setVisibleId(final int visibleId) {
        this.visibleId = visibleId;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(final Long clubId) {
        this.clubId = clubId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public PointOfInterestType getType() {
        return type;
    }

    public void setType(final PointOfInterestType type) {
        this.type = type;
    }

    @Nullable
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(@Nullable final LocalDateTime lastModifiedDate) {
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

    public List<PoiLocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(final List<PoiLocationDTO> locations) {
        this.locations = locations;
    }
}
