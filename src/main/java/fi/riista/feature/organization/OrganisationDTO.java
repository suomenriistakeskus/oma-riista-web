package fi.riista.feature.organization;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.occupation.OccupationType;

import org.hibernate.validator.constraints.SafeHtml;

public class OrganisationDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String officialCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    private OrganisationType organisationType;
    private boolean hasOccupations;

    private boolean editable;

    public static OrganisationDTO create(Organisation organisation) {
        return new OrganisationDTO(organisation);
    }

    public OrganisationDTO() {
    }

    public OrganisationDTO(Organisation organisation) {
        setId(organisation.getId());
        setRev(organisation.getConsistencyVersion());
        setOfficialCode(organisation.getOfficialCode());
        setNameFI(organisation.getNameFinnish());
        setNameSV(organisation.getNameSwedish());
        setOrganisationType(organisation.getOrganisationType());
        setHasOccupations(OccupationType.hasApplicableValuesFor(organisation.getOrganisationType()));
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

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(OrganisationType organisationType) {
        this.organisationType = organisationType;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isHasOccupations() {
        return hasOccupations;
    }

    public void setHasOccupations(boolean hasOccupations) {
        this.hasOccupations = hasOccupations;
    }

}
