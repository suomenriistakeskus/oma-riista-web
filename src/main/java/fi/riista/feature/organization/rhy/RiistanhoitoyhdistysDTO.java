package fi.riista.feature.organization.rhy;

import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.address.AddressDTO;
import javax.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;

public class RiistanhoitoyhdistysDTO extends OrganisationDTO {

    private boolean hasOwnAddress;
    private boolean hasOwnEmail;
    private boolean hasOwnPhoneNumber;

    @Valid
    private AddressDTO address;

    @Email
    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String email;

    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String phoneNumber;

    public static RiistanhoitoyhdistysDTO create(final Riistanhoitoyhdistys rhy) {
        return new RiistanhoitoyhdistysDTO(rhy);
    }

    public RiistanhoitoyhdistysDTO() {
    }

    public RiistanhoitoyhdistysDTO(final Riistanhoitoyhdistys rhy) {
        super(rhy);

        this.address = AddressDTO.from(rhy.getAddress());
        this.hasOwnAddress = rhy.getAddress() != null;

        this.phoneNumber = rhy.getPhoneNumber();
        this.hasOwnPhoneNumber = rhy.getPhoneNumber() != null;

        this.email = rhy.getEmail();
        this.hasOwnEmail = rhy.getEmail() != null;
    }

    public boolean isHasOwnAddress() {
        return hasOwnAddress;
    }

    public void setHasOwnAddress(final boolean hasOwnAddress) {
        this.hasOwnAddress = hasOwnAddress;
    }

    public boolean isHasOwnEmail() {
        return hasOwnEmail;
    }

    public void setHasOwnEmail(final boolean hasOwnEmail) {
        this.hasOwnEmail = hasOwnEmail;
    }

    public boolean isHasOwnPhoneNumber() {
        return hasOwnPhoneNumber;
    }

    public void setHasOwnPhoneNumber(final boolean hasOwnPhoneNumber) {
        this.hasOwnPhoneNumber = hasOwnPhoneNumber;
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

}
