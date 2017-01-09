package fi.riista.feature.organization.person;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;

public class PersonDTO extends BaseEntityDTO<Long> {

    public static PersonDTO create(Person person) {
        PersonDTO dto = new PersonDTO();

        dto.setId(person.getId());
        dto.setRev(person.getConsistencyVersion());
        dto.setFirstName(person.getFirstName());
        dto.setByName(person.getByName());
        dto.setLastName(person.getLastName());
        dto.setHunterNumber(person.getHunterNumber());
        dto.setAddress(AddressDTO.from(person.getAddress()));
        dto.setEmail(person.getEmail());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setRegistered(person.isRegistered());
        if (person.getRhyMembership() != null) {
            dto.setRhyMembership(OrganisationDTO.create(person.getRhyMembership()));
        }
        return dto;
    }

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String byName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    @FinnishHunterNumber
    private String hunterNumber;

    @Valid
    private AddressDTO address;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String email;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String phoneNumber;

    private boolean registered;

    @Valid
    private OrganisationDTO rhyMembership;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(String byName) {
        this.byName = byName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setHunterNumber(String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public AddressDTO getAddress() {
        return address;
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

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered;
    }

    public OrganisationDTO getRhyMembership() {
        return rhyMembership;
    }

    public void setRhyMembership(OrganisationDTO rhyMembership) {
        this.rhyMembership = rhyMembership;
    }
}
