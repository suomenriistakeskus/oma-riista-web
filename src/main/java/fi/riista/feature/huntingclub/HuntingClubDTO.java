package fi.riista.feature.huntingclub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.hta.HirvitalousalueDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.util.DtoUtil;
import fi.riista.validation.DoNotValidate;
import fi.riista.validation.FinnishBusinessId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.util.List;

public class HuntingClubDTO extends BaseEntityDTO<Long> {

    public static HuntingClubDTO create(final HuntingClub club, final boolean canEdit, final List<OccupationDTO> yhdyshenkilot, final HirvitalousalueDTO mooseAreaDto) {
        final HuntingClubDTO dto = new HuntingClubDTO();
        DtoUtil.copyBaseFields(club, dto);
        dto.setOfficialCode(club.getOfficialCode());
        dto.setNameFI(club.getNameFinnish());
        dto.setNameSV(club.getNameSwedish());
        dto.setCustomerId(club.getOfficialCode());
        dto.setEmail(club.getEmail());
        dto.setGeoLocation(club.getGeoLocation());

        dto.setCanEdit(canEdit);
        dto.setYhdyshenkilot(yhdyshenkilot);

        final Organisation parentOrganisation = club.getParentOrganisation();
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

    public static HuntingClubDTO create(final Organisation clubOrganisation) {
        final HuntingClubDTO dto = new HuntingClubDTO();
        DtoUtil.copyBaseFields(clubOrganisation, dto);
        dto.setOfficialCode(clubOrganisation.getOfficialCode());
        dto.setNameFI(clubOrganisation.getNameFinnish());
        dto.setNameSV(clubOrganisation.getNameSwedish());
        dto.setCustomerId(clubOrganisation.getOfficialCode());
        dto.setEmail(clubOrganisation.getEmail());
        dto.setGeoLocation(clubOrganisation.getGeoLocation());

        final Organisation parentOrganisation = clubOrganisation.getParentOrganisation();
        if (parentOrganisation != null && parentOrganisation.getOrganisationType() == OrganisationType.RHY) {
            dto.setRhy(OrganisationDTO.create(parentOrganisation));
        }

        dto.setActive(clubOrganisation.isActive());
        return dto;
    }

    private Long id;
    private Integer rev;

    private final OrganisationType organisationType = OrganisationType.CLUB;

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

    @DoNotValidate
    @JsonIgnore
    private HirvitalousalueDTO mooseArea;

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

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
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

    public OrganisationDTO getRhy() {
        return rhy;
    }

    public void setRhy(final OrganisationDTO rhy) {
        this.rhy = rhy;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final String customerId) {
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

    public void setCanEdit(final boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public List<OccupationDTO> getYhdyshenkilot() {
        return yhdyshenkilot;
    }

    public void setYhdyshenkilot(final List<OccupationDTO> yhdyshenkilot) {
        this.yhdyshenkilot = yhdyshenkilot;
    }

    @JsonProperty
    public HirvitalousalueDTO getMooseArea() {
        return mooseArea;
    }

    @JsonIgnore
    public void setMooseArea(final HirvitalousalueDTO mooseArea) {
        this.mooseArea = mooseArea;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
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

    public OrganisationType getOrganisationType() {
        return organisationType;
    }
}
