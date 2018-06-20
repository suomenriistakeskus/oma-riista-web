package fi.riista.feature.huntingclub;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.hta.GISHirvitalousalueDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.util.DtoUtil;
import fi.riista.validation.DoNotValidate;
import fi.riista.validation.FinnishBusinessId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import java.util.List;

public class HuntingClubDTO extends BaseEntityDTO<Long> {

    public static HuntingClubDTO create(HuntingClub club, boolean canEdit, List<OccupationDTO> yhdyshenkilot, GISHirvitalousalueDTO mooseAreaDto) {
        HuntingClubDTO dto = new HuntingClubDTO();
        DtoUtil.copyBaseFields(club, dto);
        dto.setOfficialCode(club.getOfficialCode());
        dto.setNameFI(club.getNameFinnish());
        dto.setNameSV(club.getNameSwedish());
        dto.setCustomerId(club.getOfficialCode());
        dto.setEmail(club.getEmail());
        dto.setGeoLocation(club.getGeoLocation());

        dto.setCanEdit(canEdit);
        dto.setYhdyshenkilot(yhdyshenkilot);

        Organisation parentOrganisation = club.getParentOrganisation();
        if (parentOrganisation != null && parentOrganisation.getOrganisationType() == OrganisationType.RHY) {
            dto.setRhy(OrganisationDTO.create(parentOrganisation));
        }

        dto.setMooseArea(mooseAreaDto);
        dto.setActive(club.isActive());

        dto.setSubtype(club.getSubtype());
        if (club.getClubPerson() != null) {
            dto.setClubPerson(PersonWithNameDTO.create(club.getClubPerson()));
        }
        if (club.getBusinessId() != null) {
            dto.setBusinessId(club.getBusinessId().getValue());
        }
        dto.setAssociationRegistryNumber(club.getAssociationRegistryNumber());
        return dto;
    }

    private Long id;
    private Integer rev;

    private OrganisationType organisationType = OrganisationType.CLUB;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String officialCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @Valid
    private OrganisationDTO rhy;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String customerId;

    @Valid
    private GeoLocation geoLocation;

    private boolean canEdit;

    @DoNotValidate
    private List<OccupationDTO> yhdyshenkilot;

    @Valid
    private GISHirvitalousalueDTO mooseArea;

    @Email
    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String email;

    private boolean active;

    private HuntingClubSubtype subtype;

    @Valid
    private PersonWithNameDTO clubPerson;

    @FinnishBusinessId
    private String businessId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String associationRegistryNumber;

    public HuntingClubDTO() {
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

    public void setOfficialCode(final String officialCode) {
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

    public OrganisationDTO getRhy() {
        return rhy;
    }

    public void setRhy(OrganisationDTO rhy) {
        this.rhy = rhy;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OccupationDTO> getYhdyshenkilot() {
        return yhdyshenkilot;
    }

    public void setYhdyshenkilot(List<OccupationDTO> yhdyshenkilot) {
        this.yhdyshenkilot = yhdyshenkilot;
    }

    public GISHirvitalousalueDTO getMooseArea() {
        return mooseArea;
    }

    public void setMooseArea(GISHirvitalousalueDTO mooseArea) {
        this.mooseArea = mooseArea;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public HuntingClubSubtype getSubtype() {
        return subtype;
    }

    public void setSubtype(final HuntingClubSubtype subtype) {
        this.subtype = subtype;
    }

    public PersonWithNameDTO getClubPerson() {
        return clubPerson;
    }

    public void setClubPerson(final PersonWithNameDTO clubPerson) {
        this.clubPerson = clubPerson;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(final String businessId) {
        this.businessId = businessId;
    }

    public String getAssociationRegistryNumber() {
        return associationRegistryNumber;
    }

    public void setAssociationRegistryNumber(final String associationRegistryNumber) {
        this.associationRegistryNumber = associationRegistryNumber;
    }
}
