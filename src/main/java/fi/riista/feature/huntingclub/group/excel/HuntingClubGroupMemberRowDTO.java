package fi.riista.feature.huntingclub.group.excel;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;

public class HuntingClubGroupMemberRowDTO {
    private final String clubName;
    private final String groupName;
    private final String speciesName;
    private final int year;
    private final String firstName;
    private final String lastName;
    private final String hunterNumber;
    private final String streetAddress;
    private final String postalCode;
    private final String city;
    private final String country;
    private final String phoneNumber;
    private final String email;
    private final String homeMunicipalityName;

    public HuntingClubGroupMemberRowDTO(final String clubName, final String groupName,
                                        final String speciesName,
                                        final int year, final Person person,
                                        final boolean shareContactInfo) {
        this.clubName = clubName;
        this.year = year;
        this.speciesName = speciesName;
        this.groupName = groupName;
        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.hunterNumber = person.getHunterNumber();

        final Address a = person.getAddress();

        if (shareContactInfo && a != null) {
            this.streetAddress = a.getStreetAddress();
            this.postalCode = a.getPostalCode();
            this.city = a.getCity();
            this.country = a.getCountry();
        } else {
            this.streetAddress = null;
            this.postalCode = null;
            this.city = null;
            this.country = null;
        }

        if (shareContactInfo) {
            this.phoneNumber = person.getPhoneNumber();
            this.email = person.getEmail();
            this.homeMunicipalityName = person.getHomeMunicipalityName().getAnyTranslation();
        } else {
            this.phoneNumber = null;
            this.email = null;
            this.homeMunicipalityName = "";
        }
    }

    public String getClubName() {
        return clubName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public int getYear() {
        return year;
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

    public String getHomeMunicipalityName() {
        return homeMunicipalityName;
    }
}
