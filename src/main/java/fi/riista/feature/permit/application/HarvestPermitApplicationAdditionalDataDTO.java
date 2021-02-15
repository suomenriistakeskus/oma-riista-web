package fi.riista.feature.permit.application;

import javax.validation.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HarvestPermitApplicationAdditionalDataDTO {

    @Email
    @Size(max = 255)
    private String email1;

    @Email
    @Size(max = 255)
    private String email2;

    private boolean deliveryByMail;

    @Valid
    @NotNull
    private DeliveryAddressDTO deliveryAddress;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String decisionLanguage;


    public String getEmail1() {
        return email1;
    }

    public void setEmail1(final String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(final String email2) {
        this.email2 = email2;
    }

    public boolean isDeliveryByMail() {
        return deliveryByMail;
    }

    public void setDeliveryByMail(final boolean deliveryByMail) {
        this.deliveryByMail = deliveryByMail;
    }

    public String getDecisionLanguage() {
        return decisionLanguage;
    }

    public void setDecisionLanguage(String language) {
        this.decisionLanguage = language;
    }

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddressDTO deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}
