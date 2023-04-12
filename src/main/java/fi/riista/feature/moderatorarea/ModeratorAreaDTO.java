package fi.riista.feature.moderatorarea;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.DtoUtil;
import fi.riista.validation.DoNotValidate;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ModeratorAreaDTO extends BaseEntityDTO<Long> {

    public static ModeratorAreaDTO create(final ModeratorArea moderatorArea,
                                          final SystemUser moderator,
                                          final Organisation rka,
                                          final GISZoneWithoutGeometryDTO zoneWithSize,
                                          final GISBounds bounds,
                                          final int metsahallitusYear) {
        final ModeratorAreaDTO dto = new ModeratorAreaDTO();
        DtoUtil.copyBaseFields(moderatorArea, dto);

        dto.setName(moderatorArea.getName());
        dto.setExternalId(moderatorArea.getExternalId());
        dto.setYear(moderatorArea.getYear());
        dto.setMetsahallitusYear(metsahallitusYear);
        dto.setBounds(bounds);
        dto.setRka(OrganisationNameDTO.createWithOfficialCode(rka));
        dto.setRkaCode(rka.getOfficialCode());
        dto.setModeratorName(moderator.getFullName());

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

    @Min(2000) @Max(2100)
    private int year;

    @NotBlank
    @Size(max = 3)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rkaCode;

    // Read-only properties

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String externalId;

    private Long zoneId;

    private int metsahallitusYear;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String moderatorName;

    @DoNotValidate
    private OrganisationNameDTO rka;

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

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
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

    public String getRkaCode() {
        return rkaCode;
    }

    public void setRkaCode(final String rkaCode) {
        this.rkaCode = rkaCode;
    }

    public String getModeratorName() {
        return moderatorName;
    }

    public void setModeratorName(final String moderatorName) {
        this.moderatorName = moderatorName;
    }

    public OrganisationNameDTO getRka() {
        return rka;
    }

    public void setRka(final OrganisationNameDTO rka) {
        this.rka = rka;
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
