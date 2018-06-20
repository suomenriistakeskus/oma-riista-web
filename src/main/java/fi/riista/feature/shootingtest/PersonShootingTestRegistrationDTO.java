package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class PersonShootingTestRegistrationDTO {

    public enum ShootingTestRegistrationCheckStatus {
        HUNTING_PAYMENT_DONE,
        HUNTING_PAYMENT_NOT_DONE,
        IN_PROGRESS,
        COMPLETED,
        DISQUALIFIED_AS_OFFICIAL,
        NO_HUNTER_NUMBER,
        HUNTING_BAN
    }

    private final long id;
    private final String firstName;
    private final String lastName;

    private final String hunterNumber;
    private final LocalDate dateOfBirth;

    private final ShootingTestRegistrationCheckStatus registrationStatus;
    private final SelectedShootingTestTypesDTO selectedShootingTestTypes;

    public PersonShootingTestRegistrationDTO(@Nonnull final Person person,
                                             @Nonnull final ShootingTestRegistrationCheckStatus registrationCheckStatus,
                                             @Nonnull final SelectedShootingTestTypesDTO selectedShootingTestTypes) {

        requireNonNull(person, "person is null");

        this.id = person.getId();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();

        this.hunterNumber = person.getHunterNumber();
        this.dateOfBirth = person.parseDateOfBirth();

        this.registrationStatus = requireNonNull(registrationCheckStatus, "registrationCheckStatus");
        this.selectedShootingTestTypes = requireNonNull(selectedShootingTestTypes, "selectedShootingTestTypes");
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public ShootingTestRegistrationCheckStatus getRegistrationStatus() {
        return registrationStatus;
    }

    public SelectedShootingTestTypesDTO getSelectedShootingTestTypes() {
        return selectedShootingTestTypes;
    }
}
