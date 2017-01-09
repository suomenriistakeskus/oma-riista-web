package fi.riista.feature.organization.lupahallinta;

import fi.riista.feature.common.entity.BaseEntityDTO;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class LHOrganisationSearchDTO extends BaseEntityDTO<Long> {

    public interface Register {}

    @NotNull(groups = Register.class)
    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String officialCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String contactPersonName;

    private Boolean hasActiveContactPerson;

    public static LHOrganisationSearchDTO create(final LHOrganisation organisation) {
        LHOrganisationSearchDTO dto = new LHOrganisationSearchDTO(organisation);
        dto.setOfficialCode(organisation.getOfficialCode());
        return dto;
    }

    public LHOrganisationSearchDTO() {
    }

    public LHOrganisationSearchDTO(final LHOrganisation organisation) {
        setId(organisation.getId());
        setRev(organisation.getConsistencyVersion());
        setNameFI(organisation.getNameFinnish());
        setNameSV(organisation.getNameSwedish());
        setContactPersonName(organisation.getContactPersonName());
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

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(final String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public Boolean getHasActiveContactPerson() {
        return hasActiveContactPerson;
    }

    public void setHasActiveContactPerson(final Boolean hasActiveContactPerson) {
        this.hasActiveContactPerson = hasActiveContactPerson;
    }
}
