package fi.riista.feature.permit.application;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Size;

public class HarvestPermitApplicationAdditionalDataDTO {

    @Email
    @Size(max = 255)
    private String email1;

    @Email
    @Size(max = 255)
    private String email2;

    private boolean deliveryByMail;

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
}
