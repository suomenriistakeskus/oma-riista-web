package fi.riista.feature.account.area;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.util.DtoUtil;
import fi.riista.validation.DoNotValidate;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Size;

public class PersonalAreaDTO extends BaseEntityDTO<Long> {

    public static PersonalAreaDTO create(final PersonalArea personalArea,
                                         final GISZoneWithoutGeometryDTO zoneWithSize,
                                         final GISBounds bounds,
                                         final int metsahallitusYear) {
        final PersonalAreaDTO dto = new PersonalAreaDTO();
        DtoUtil.copyBaseFields(personalArea, dto);

        dto.setName(personalArea.getName());
        dto.setExternalId(personalArea.getExternalId());
        dto.setMetsahallitusYear(metsahallitusYear);
        dto.setBounds(bounds);

        if (zoneWithSize != null) {
            dto.setZoneId(zoneWithSize.getId());
            dto.setSize(zoneWithSize.getSize());
        } else {
            dto.setSize(GISZoneSizeDTO.createEmpty());
        }

        return dto;
    }

    private Long id;
    private Integer rev;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    // Read-only properties

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String externalId;

    private Long zoneId;

    private int metsahallitusYear;

    @JsonIgnore
    @DoNotValidate
    private GISZoneSizeDTO size;

    @JsonIgnore
    @DoNotValidate
    private GISBounds bounds;

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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public int getMetsahallitusYear() {
        return metsahallitusYear;
    }

    public void setMetsahallitusYear(final int metsahallitusYear) {
        this.metsahallitusYear = metsahallitusYear;
    }

    @JsonProperty
    public GISZoneSizeDTO getSize() {
        return size;
    }

    @JsonIgnore
    public void setSize(final GISZoneSizeDTO size) {
        this.size = size;
    }

    @JsonProperty
    public GISBounds getBounds() {
        return bounds;
    }

    @JsonIgnore
    public void setBounds(final GISBounds bounds) {
        this.bounds = bounds;
    }
}
