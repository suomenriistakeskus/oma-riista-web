package fi.riista.feature.organization.person;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.util.DtoUtil;
import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonContactInfoDTO extends BaseEntityDTO<Long> {

    public static PersonContactInfoDTO create(@Nonnull final Person person) {
        final PersonContactInfoDTO dto = new PersonContactInfoDTO();
        DtoUtil.copyBaseFields(person, dto);

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

    private Boolean registered;

    private Boolean adult;

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

    public void setByName(final String byName) {
        this.byName = byName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(final AddressDTO address) {
        this.address = address;
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

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(final Boolean registered) {
        this.registered = registered;
    }

    public OrganisationDTO getRhyMembership() {
        return rhyMembership;
    }

    public void setRhyMembership(final OrganisationDTO rhyMembership) {
        this.rhyMembership = rhyMembership;
    }

    public Boolean getAdult() {
        return adult;
    }

    public void setAdult(final Boolean adult) {
        this.adult = adult;
    }
}
