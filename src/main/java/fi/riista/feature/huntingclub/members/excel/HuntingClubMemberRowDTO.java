package fi.riista.feature.huntingclub.members.excel;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;

public class HuntingClubMemberRowDTO {
    private final LocalisedString clubName;
    private final String firstName;
    private final String lastName;
    private final String hunterNumber;
    private final String streetAddress;
    private final String postalCode;
    private final String city;
    private final String country;
    private final String phoneNumber;
    private final String email;
    private final LocalisedString homeMunicipalityName;

    public HuntingClubMemberRowDTO(final LocalisedString clubName, final Person person,
                                   final boolean shareContactInfo) {
        this.clubName = clubName;
        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.hunterNumber = person.getHunterNumber();

        final Address address = person.getAddress();

        if (shareContactInfo && address != null) {
            this.streetAddress = address.getStreetAddress();
            this.postalCode = address.getPostalCode();
            this.city = address.getCity();
            this.country = address.getCountry();
        } else {
            this.streetAddress = null;
            this.postalCode = null;
            this.city = null;
            this.country = null;
        }

        if (shareContactInfo) {
            this.phoneNumber = person.getPhoneNumber();
            this.email = person.getEmail();
            this.homeMunicipalityName = person.getHomeMunicipalityName();
        } else {
            this.phoneNumber = null;
            this.email = null;
            this.homeMunicipalityName = LocalisedString.EMPTY;
        }
    }

    public LocalisedString getClubName() {
        return clubName;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public LocalisedString getHomeMunicipalityName() {
        return homeMunicipalityName;
    }
}
