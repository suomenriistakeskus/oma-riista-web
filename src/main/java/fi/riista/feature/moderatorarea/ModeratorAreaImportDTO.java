package fi.riista.feature.moderatorarea;

import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.union.PersonalAreaUnion;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ModeratorAreaImportDTO {

    public enum ModeratorAreaImportType {
        PERSONAL,
        CLUB,
        PERSONAL_AREA_UNION,
        MODERATOR_AREA
    }

    public static ModeratorAreaImportDTO createFromPersonalArea(final PersonalArea area) {
        return new ModeratorAreaImportDTO(area.getName(), area.getName(), area.getExternalId(), area.getId(), ModeratorAreaImportType.PERSONAL);
    }

    public static ModeratorAreaImportDTO createFromClubArea(final HuntingClubArea area) {
        return new ModeratorAreaImportDTO(area.getNameFinnish(), area.getNameSwedish(), area.getExternalId(), area.getId(), ModeratorAreaImportType.CLUB);
    }

    public static ModeratorAreaImportDTO createFromPersonalAreaUnion(final PersonalAreaUnion area, final String externalId) {
        return new ModeratorAreaImportDTO(area.getName(), area.getName(), externalId, area.getId(), ModeratorAreaImportType.PERSONAL_AREA_UNION);
    }
    public static ModeratorAreaImportDTO createFromModeratorArea(final ModeratorArea area) {
        return new ModeratorAreaImportDTO(area.getName(), area.getName(), area.getExternalId(), area.getId(), ModeratorAreaImportType.MODERATOR_AREA);
    }

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

    private Long areaId;

    private ModeratorAreaImportType type;

    public ModeratorAreaImportDTO() {
    }

    public ModeratorAreaImportDTO(final String nameFI,
                                  final String nameSV,
                                  final String externalId,
                                  final Long areaId,
                                  final ModeratorAreaImportType type) {
        this.nameFI = nameFI;
        this.nameSV = nameSV;
        this.externalId = externalId;
        this.areaId = areaId;
        this.type = type;
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

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(final Long areaId) {
        this.areaId = areaId;
    }

    public ModeratorAreaImportType getType() {
        return type;
    }

    public void setType(final ModeratorAreaImportType type) {
        this.type = type;
    }
}
