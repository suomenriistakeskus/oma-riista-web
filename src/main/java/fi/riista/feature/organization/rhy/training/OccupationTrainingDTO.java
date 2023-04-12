package fi.riista.feature.organization.rhy.training;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.validation.DoNotValidate;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OccupationTrainingDTO extends BaseEntityDTO<Long> {

    // TODO: Check what fields are necessary
    public static class PersonDTO {
        public static PersonDTO create(final Person person) {
            final PersonDTO dto = new PersonDTO();

            dto.id = person.getId();
            dto.underage = !person.isAdult();
            dto.huntingBanActive = person.isHuntingBanActiveNow();
            dto.firstName = person.getFirstName();
            dto.lastName = person.getLastName();
            dto.email = person.getEmail();
            dto.phoneNumber = person.getPhoneNumber();
            dto.address = AddressDTO.from(person.getAddress());

            return dto;
        }

        @NotNull
        private Long id;

        private boolean underage;

        private boolean huntingBanActive;

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

        public boolean isUnderage() {
            return underage;
        }

        public void setUnderage(final boolean underage) {
            this.underage = underage;
        }

        public boolean isHuntingBanActive() {
            return huntingBanActive;
        }

        public void setHuntingBanActive(final boolean huntingBanActive) {
            this.huntingBanActive = huntingBanActive;
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

    public static OccupationTrainingDTO create(final OccupationTraining training,
                                               final PersonDTO person) {
        final OccupationTrainingDTO dto = new OccupationTrainingDTO();

        dto.id = training.getId();
        dto.rev = training.getConsistencyVersion();
        dto.occupationType = training.getOccupationType();
        dto.trainingType = training.getTrainingType();
        dto.trainingDate = training.getTrainingDate();
        dto.person = person;

        return dto;
    }

    private Long id;
    private Integer rev;

    @NotNull
    private OccupationType occupationType;

    @NotNull
    private TrainingType trainingType;

    @NotNull
    private LocalDate trainingDate;

    @Valid
    @NotNull
    private PersonDTO person;

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

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(final TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(final LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(final PersonDTO person) {
        this.person = person;
    }

}
