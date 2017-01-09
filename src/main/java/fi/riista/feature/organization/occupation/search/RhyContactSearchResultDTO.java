package fi.riista.feature.organization.occupation.search;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;

import java.util.Locale;

public class RhyContactSearchResultDTO {

    public static RhyContactSearchResultDTO create(Organisation rhy, Occupation coordinator, Locale userLocale) {
        RhyContactSearchResultDTO dto = new RhyContactSearchResultDTO();

        dto.setRhyName(rhy.getNameLocalisation().getAnyTranslation(userLocale));

        Person person = coordinator != null ? coordinator.getPerson() : null;

        dto.setEmail(email(rhy, person));
        dto.setPhoneNumber(phone(rhy, person));

        Address address = address(rhy, person);
        if (address != null) {
            dto.setStreetAddress(address.getStreetAddress());
            dto.setPostalCode(address.getPostalCode());
            dto.setCity(address.getCity());
            dto.setCountry(address.getCountry());
        }
        return dto;
    }

    private static String email(Organisation rhy, Person person) {
        return firstNonNull(rhy.getEmail(),
                person != null ? person.getEmail() : null);
    }

    private static String phone(Organisation rhy, Person person) {
        return firstNonNull(rhy.getPhoneNumber(),
                person != null ? person.getPhoneNumber() : null);
    }

    private static Address address(Organisation rhy, Person person) {
        if (rhy.getAddress() != null) {
            return rhy.getAddress();
        }
        if (person != null) {
            return person.getAddress();
        }
        return null;
    }

    private static String firstNonNull(String preferred, String fallback) {
        return MoreObjects.firstNonNull(
                Strings.emptyToNull(preferred),
                Strings.nullToEmpty(fallback));// nullToEmpty because otherwise throws exception
    }

    private String rhyName;
    private String email;
    private String phoneNumber;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String country;


    public String getRhyName() {
        return rhyName;
    }

    public void setRhyName(String rhyName) {
        this.rhyName = rhyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
