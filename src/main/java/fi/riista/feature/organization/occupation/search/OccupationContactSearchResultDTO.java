package fi.riista.feature.organization.occupation.search;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.pub.occupation.PublicOccupationDTO;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class OccupationContactSearchResultDTO {

    public static OccupationContactSearchResultDTO create(Occupation occupation, Locale userLocale, MessageSource messageSource) {
        OccupationContactSearchResultDTO dto = new OccupationContactSearchResultDTO();

        if(occupation.getOrganisation().getParentOrganisation() != null){
            dto.setParentOrganisationName(occupation.getOrganisation().getParentOrganisation().getNameLocalisation()
                    .getAnyTranslation(userLocale));
        }
        dto.setOrganisationName(occupation.getOrganisation().getNameLocalisation().getAnyTranslation(userLocale));
        dto.setOccupationType(occupation.getOccupationType());
        dto.setOccupationName(messageSource.getMessage(PublicOccupationDTO.class.getSimpleName() + "."
                + occupation.getOrganisation().getOrganisationType().name()
                + "."
                + occupation.getOccupationType().name(), null, userLocale));

        Person person = occupation.getPerson();
        dto.setLastName(person.getLastName());
        dto.setFirstName(person.getFirstName());
        dto.setByName(person.getByName());
        dto.setEmail(person.getEmail());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setRegistered(person.isRegistered());

        Address address = person.getAddress();
        if (address != null) {
            dto.setStreetAddress(address.getStreetAddress());
            dto.setPostalCode(address.getPostalCode());
            dto.setCity(address.getCity());
            dto.setCountry(address.getCountry());
        }
        return dto;
    }

    private OccupationType occupationType;
    private String parentOrganisationName;
    private String organisationName;
    private String occupationName;
    private String lastName;
    private String firstName;
    private String byName;
    private String email;
    private String phoneNumber;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String country;
    private boolean registered;

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public String getParentOrganisationName() {
        return parentOrganisationName;
    }

    public void setParentOrganisationName(String parentOrganisationName) {
        this.parentOrganisationName = parentOrganisationName;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getOccupationName() {
        return occupationName;
    }

    public void setOccupationName(String occupationName) {
        this.occupationName = occupationName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(String byName) {
        this.byName = byName;
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

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
