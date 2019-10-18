package fi.riista.feature.sso.dto;

import com.google.common.collect.Lists;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ExternalAuthenticationResponse {

    public static class Address {
        static Address from(@Nullable fi.riista.feature.organization.address.Address address) {
            return address != null ? new Address(address) : null;
        }

        private final String streetAddress;
        private final String postalCode;
        private final String city;
        private final String country;

        Address(fi.riista.feature.organization.address.Address input) {
            this.streetAddress = input.getStreetAddress();
            this.postalCode = input.getPostalCode();
            this.city = input.getCity();
            this.country = input.getCountry();
        }

        public String getStreetAddress() {
            return streetAddress;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getCity() {
            return city;
        }

        public String getCountry() {
            return country;
        }
    }

    public static class ActiveOccupation {
        static ActiveOccupation from(@Nullable final Occupation occupation) {
            if (occupation == null) {
                return null;
            }

            final LocalDate validUntil = occupation.hasEndDate()
                    ? new LocalDate(occupation.getEndDate())
                    : null;

            return new ActiveOccupation(
                    occupation.getOrganisation().getOrganisationType(),
                    occupation.getOrganisation().getOfficialCode(),
                    occupation.getOccupationType(),
                    validUntil);
        }

        private final OrganisationType organisation;
        private final String organisationCode;
        private final OccupationType occupation;
        private final LocalDate validUntil;

        public ActiveOccupation(OrganisationType organisation,
                                String organisationCode,
                                OccupationType occupation,
                                LocalDate validUntil) {
            this.organisation = organisation;
            this.organisationCode = organisationCode;
            this.occupation = occupation;
            this.validUntil = validUntil;
        }

        public OrganisationType getOrganisation() {
            return organisation;
        }

        public String getOrganisationCode() {
            return organisationCode;
        }

        public OccupationType getOccupation() {
            return occupation;
        }

        public LocalDate getValidUntil() {
            return validUntil;
        }
    }

    public static ExternalAuthenticationResponse from(final SystemUser user,
                                                      final List<Occupation> activeOccupations,
                                                      final UserInfo activeUserInfo) {
        Objects.requireNonNull(user, "no user");
        Objects.requireNonNull(user.getPerson(), "user is not registered as person");
        Objects.requireNonNull(activeUserInfo);

        final List<ActiveOccupation> occupations = Lists.transform(activeOccupations, ActiveOccupation::from);

        return new ExternalAuthenticationResponse(user.getPerson(), occupations, activeUserInfo);
    }

    private ExternalAuthenticationResponse(final Person person,
                                           final List<ActiveOccupation> activeOccupations,
                                           final UserInfo activeUserInfo) {
        this.personId = person.getId();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.byName = person.getByName();
        this.phoneNumber = person.getPhoneNumber();
        this.email = person.getEmail();
        this.languageCode = person.getLanguageCode();
        this.occupations = activeOccupations;

        // Check if we are allowed to export SSN ?
        if (activeUserInfo.hasPrivilege(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION_SSN)) {
            this.ssn = person.getSsn();
        } else {
            this.ssn = null;
        }

        // Check if we are allowed to export date of birth ?
        if (activeUserInfo.hasPrivilege(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION_DATE_OF_BIRTH)) {
            // Format date of birth to YYYY-MM-DD
            this.dateOfBirth = person.parseDateOfBirth().toString(ISODateTimeFormat.date());
        } else {
            this.dateOfBirth = null;
        }

        if (activeUserInfo.hasPrivilege(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION_HUNTERNUMBER)) {
            this.hunterNumber = person.getHunterNumber();
        } else {
            this.hunterNumber = null;
        }

        if (activeUserInfo.hasPrivilege(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION_ADDRESS)) {
            this.address = Address.from(person.getAddress());
            this.homeMunicipality = person.getHomeMunicipalityCode();
        } else {
            this.address = null;
            this.homeMunicipality = null;
        }
    }

    private final Long personId;
    private final String firstName;
    private final String lastName;
    private final String byName;
    private final String phoneNumber;
    private final String email;
    private final String languageCode;
    private final Address address;
    private final String hunterNumber;
    private final String ssn;
    private final String dateOfBirth;
    private final String homeMunicipality;
    private final List<ActiveOccupation> occupations;

    public Long getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getByName() {
        return byName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public String getSsn() {
        return ssn;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getHomeMunicipality() {
        return homeMunicipality;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public List<ActiveOccupation> getOccupations() {
        return occupations;
    }
}
