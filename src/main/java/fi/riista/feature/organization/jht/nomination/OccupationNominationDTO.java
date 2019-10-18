package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.jht.JHTPeriod;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.validation.DoNotValidate;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OccupationNominationDTO extends BaseEntityDTO<Long> {
    public interface AcceptValidation {
    }

    public static class PersonDTO {
        public static PersonDTO create(@Nonnull final Person person) {
            final PersonDTO dto = new PersonDTO();

            dto.id = person.getId();
            dto.firstName = person.getFirstName();
            dto.lastName = person.getLastName();
            dto.email = person.getEmail();
            dto.phoneNumber = person.getPhoneNumber();
            dto.address = AddressDTO.from(person.getAddress());

            return dto;
        }

        private Long id;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String firstName;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String lastName;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String email;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String phoneNumber;

        @DoNotValidate
        private AddressDTO address;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(final String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(final String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(final String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public AddressDTO getAddress() {
            return address;
        }

        public void setAddress(final AddressDTO address) {
            this.address = address;
        }
    }

    public static OccupationNominationDTO create(final OccupationNomination nomination,
                                                 final Person person,
                                                 final Organisation rhy,
                                                 final SystemUser moderator,
                                                 final Occupation occupation) {
        final OccupationNominationDTO dto = new OccupationNominationDTO();

        dto.id = nomination.getId();
        dto.rev = nomination.getConsistencyVersion();
        dto.occupationType = nomination.getOccupationType();
        dto.nominationStatus = nomination.getNominationStatus();
        dto.nominationDate = nomination.getNominationDate();
        dto.decisionDate = nomination.getDecisionDate();

        dto.person = person != null ? PersonDTO.create(person) : null;
        dto.rhy = rhy != null ? OrganisationNameDTO.createWithOfficialCode(rhy) : null;
        dto.moderatorFullName = moderator != null ? moderator.getFullName() : null;

        if (occupation != null) {
            dto.occupationPeriod = new JHTPeriod(occupation.getBeginDate(), occupation.getEndDate());
        }

        return dto;
    }

    @NotNull(groups = AcceptValidation.class)
    private Long id;
    private Integer rev;

    private OccupationType occupationType;
    private OccupationNomination.NominationStatus nominationStatus;
    private LocalDate nominationDate;
    private LocalDate decisionDate;

    @Valid
    @NotNull(groups = AcceptValidation.class)
    private JHTPeriod occupationPeriod;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String moderatorFullName;

    @DoNotValidate
    private PersonDTO person;

    @DoNotValidate
    private OrganisationNameDTO rhy;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return this.rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public OccupationNomination.NominationStatus getNominationStatus() {
        return nominationStatus;
    }

    public void setNominationStatus(final OccupationNomination.NominationStatus nominationStatus) {
        this.nominationStatus = nominationStatus;
    }

    public LocalDate getNominationDate() {
        return nominationDate;
    }

    public void setNominationDate(final LocalDate nominationDate) {
        this.nominationDate = nominationDate;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(final LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public JHTPeriod getOccupationPeriod() {
        return occupationPeriod;
    }

    public void setOccupationPeriod(final JHTPeriod occupationPeriod) {
        this.occupationPeriod = occupationPeriod;
    }

    public String getModeratorFullName() {
        return moderatorFullName;
    }

    public void setModeratorFullName(final String moderatorFullName) {
        this.moderatorFullName = moderatorFullName;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(final PersonDTO person) {
        this.person = person;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public void setRhy(final OrganisationNameDTO rhy) {
        this.rhy = rhy;
    }

}
