package fi.riista.feature.organization.occupation;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.validation.Valid;

public class OccupationDTO extends BaseEntityDTO<Long> implements HasBeginAndEndDate {

    public static OccupationDTO create(final Occupation occupation, final boolean showRegistered, final boolean showContactInformation) {
        final OccupationDTO dto = createDto(occupation);

        dto.setPersonId(occupation.getPerson().getId());
        dto.setPerson(createPersonDTO(occupation, showContactInformation));
        dto.setCreationTime(DateUtil.toLocalDateTimeNullSafe(occupation.getCreationTime()));
        if (showRegistered) {
            dto.getPerson().setRegistered(occupation.getPerson().isRegistered());
        }
        if (showContactInformation) {
            dto.setContactInfoShare(occupation.getContactInfoShare());
        }
        return dto;
    }

    private static PersonDTO createPersonDTO(final Occupation occupation, final boolean showContactInformation) {
        final PersonDTO personDTO = new PersonDTO();
        Person person = occupation.getPerson();
        personDTO.setByName(person.getByName());
        personDTO.setLastName(person.getLastName());
        personDTO.setHunterNumber(person.getHunterNumber());
        if (showContactInformation) {
            personDTO.setEmail(person.getEmail());
            personDTO.setPhoneNumber(person.getPhoneNumber());
            personDTO.setAddress(AddressDTO.from(person.getAddress()));
        }
        return personDTO;
    }

    public static OccupationDTO createWithPerson(final Occupation occupation) {
        final OccupationDTO dto = createDto(occupation);

        final Person person = occupation.getPerson();
        dto.setPerson(PersonDTO.create(person));
        dto.setAdditionalInfo(occupation.getAdditionalInfo());

        if (dto.getPerson().getAddress() != null) {
            dto.getPerson().getAddress().setEditable(person.isAddressEditable());
        }

        return dto;
    }

    private static OccupationDTO createDto(final Occupation occupation) {
        final OccupationDTO dto = new OccupationDTO();
        DtoUtil.copyBaseFields(occupation, dto);

        dto.setOrganisationId(occupation.getOrganisation().getId());
        dto.setOccupationType(occupation.getOccupationType());
        dto.setBeginDate(occupation.getBeginDate());
        dto.setEndDate(occupation.getEndDate());
        dto.setCallOrder(occupation.getCallOrder());
        return dto;
    }

    private Long id;
    private Integer rev;

    private Long organisationId;
    private Long personId;

    private OccupationType occupationType;
    private LocalDate beginDate;
    private LocalDate endDate;
    private Integer callOrder;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    @Valid
    private PersonDTO person;

    private ContactInfoShare contactInfoShare;

    private LocalDateTime creationTime;

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

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCallOrder() {
        return callOrder;
    }

    public void setCallOrder(Integer callOrder) {
        this.callOrder = callOrder;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(PersonDTO person) {
        this.person = person;
    }

    public ContactInfoShare getContactInfoShare() {
        return contactInfoShare;
    }

    public void setContactInfoShare(ContactInfoShare contactInfoShare) {
        this.contactInfoShare = contactInfoShare;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }
}
