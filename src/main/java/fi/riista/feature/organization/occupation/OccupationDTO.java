package fi.riista.feature.organization.occupation;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OccupationDTO extends BaseEntityDTO<Long> implements HasBeginAndEndDate {

    public static OccupationDTO create(final Occupation occupation, final boolean showPersonInfo, final boolean showContactInformation) {
        final OccupationDTO dto = createDto(occupation);

        dto.setPersonId(occupation.getPerson().getId());
        dto.setPerson(createPersonDTO(occupation, showPersonInfo, showContactInformation));
        dto.setModificationTime(DateUtil.toLocalDateTimeNullSafe(occupation.getModificationTime()));
        if (showContactInformation) {
            dto.setContactInfoShare(occupation.getContactInfoShare());
        }
        return dto;
    }

    private static PersonContactInfoDTO createPersonDTO(final Occupation occupation, final boolean showPersonInfo, final boolean showContactInformation) {
        final PersonContactInfoDTO personDTO = new PersonContactInfoDTO();
        final Person person = occupation.getPerson();
        personDTO.setByName(person.getByName());
        personDTO.setLastName(person.getLastName());
        personDTO.setHunterNumber(person.getHunterNumber());
        if (showPersonInfo) {
            personDTO.setRegistered(occupation.getPerson().isRegistered());
            personDTO.setAdult(occupation.getPerson().isAdult());
        }
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
        dto.setPerson(PersonContactInfoDTO.create(person));
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
        dto.setBoardRepresentation(occupation.getBoardRepresentation());
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
    private PersonContactInfoDTO person;

    private ContactInfoShare contactInfoShare;

    private LocalDateTime modificationTime;

    private OccupationBoardRepresentationRole boardRepresentation;

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

    public PersonContactInfoDTO getPerson() {
        return person;
    }

    public void setPerson(PersonContactInfoDTO person) {
        this.person = person;
    }

    public ContactInfoShare getContactInfoShare() {
        return contactInfoShare;
    }

    public void setContactInfoShare(ContactInfoShare contactInfoShare) {
        this.contactInfoShare = contactInfoShare;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(LocalDateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    public OccupationBoardRepresentationRole getBoardRepresentation() {
        return boardRepresentation;
    }

    public void setBoardRepresentation(OccupationBoardRepresentationRole boardRepresentation) {
        this.boardRepresentation = boardRepresentation;
    }
}
