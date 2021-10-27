package fi.riista.feature.organization.jht.training;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.validation.DoNotValidate;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class JHTTrainingDTO extends BaseEntityDTO<Long> {
    public interface IdAndRhyValidation {
    }

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

    public static JHTTrainingDTO create(final JHTTraining training,
                                        final PersonDTO person) {
        final JHTTrainingDTO dto = new JHTTrainingDTO();

        dto.id = training.getId();
        dto.rev = training.getConsistencyVersion();
        dto.occupationType = training.getOccupationType();
        dto.trainingType = training.getTrainingType();
        dto.trainingDate = training.getTrainingDate();
        dto.trainingLocation = training.getTrainingLocation();
        dto.person = person;

        return dto;
    }

    @NotNull(groups = IdAndRhyValidation.class)
    private Long id;
    private Integer rev;

    @NotNull(groups = {IdAndRhyValidation.class})
    private Long rhyId;

    @NotNull
    private OccupationType occupationType;

    @NotNull
    private JHTTraining.TrainingType trainingType;

    @NotNull
    private LocalDate trainingDate;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String trainingLocation;

    @Valid
    @NotNull
    private PersonDTO person;

    private boolean nominated;
    private boolean accepted;

    @JsonIgnore
    @AssertTrue
    public boolean isJhtOccupationTypeValid() {
        return this.occupationType != null && OccupationType.isValidJhtOccupationType(this.occupationType);
    }

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

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final Long rhyId) {
        this.rhyId = rhyId;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public JHTTraining.TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(final JHTTraining.TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(final LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public String getTrainingLocation() {
        return trainingLocation;
    }

    public void setTrainingLocation(final String trainingLocation) {
        this.trainingLocation = trainingLocation;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(final PersonDTO person) {
        this.person = person;
    }

    public boolean isNominated() {
        return nominated;
    }

    public void setNominated(final boolean nominated) {
        this.nominated = nominated;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }
}
