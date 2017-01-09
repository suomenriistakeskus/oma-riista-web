package fi.riista.feature.organization.rhy;

import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.OrganisationDTO;
import org.hibernate.validator.constraints.Email;
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

    public static RiistanhoitoyhdistysDTO create(Riistanhoitoyhdistys rhy) {
        return new RiistanhoitoyhdistysDTO(rhy);
    }

    public RiistanhoitoyhdistysDTO() {
    }

    public RiistanhoitoyhdistysDTO(Riistanhoitoyhdistys rhy) {
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

    public void setHasOwnAddress(boolean hasOwnAddress) {
        this.hasOwnAddress = hasOwnAddress;
    }

    public boolean isHasOwnEmail() {
        return hasOwnEmail;
    }

    public void setHasOwnEmail(boolean hasOwnEmail) {
        this.hasOwnEmail = hasOwnEmail;
    }

    public boolean isHasOwnPhoneNumber() {
        return hasOwnPhoneNumber;
    }

    public void setHasOwnPhoneNumber(boolean hasOwnPhoneNumber) {
        this.hasOwnPhoneNumber = hasOwnPhoneNumber;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
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
}
