package fi.riista.feature.organization;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.SafeHtml;

public class OrganisationNameDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String officialCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFI, nameSV);
    }

    public static OrganisationNameDTO create(Organisation organisation) {
        return new OrganisationNameDTO(organisation);
    }

    public static OrganisationNameDTO createWithOfficialCode(Organisation organisation) {
        if (organisation == null) {
            return null;
        }
        OrganisationNameDTO dto = new OrganisationNameDTO(organisation);
        dto.setOfficialCode(organisation.getOfficialCode());
        return dto;
    }

    public OrganisationNameDTO() {
    }

    public OrganisationNameDTO(Organisation organisation) {
        setId(organisation.getId());
        setRev(organisation.getConsistencyVersion());
        setNameFI(organisation.getNameFinnish());
        setNameSV(organisation.getNameSwedish());
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

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(String officialCode) {
        this.officialCode = officialCode;
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

}
