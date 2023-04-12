package fi.riista.feature.huntingclub.members.group;

import fi.riista.feature.organization.person.ContactInfoShare;

import java.io.Serializable;

public class ContactInfoShareAndVisibilityUpdateDTO implements Serializable {

    private Long permitId;
    private ContactInfoShare share;

    private boolean nameVisibility;
    private boolean phoneNumberVisibility;
    private boolean emailVisibility;

    public Long getPermitId() {
        return permitId;
    }

    public void setPermitId(final Long permitId) {
        this.permitId = permitId;
    }

    public ContactInfoShare getShare() {
        return share;
    }

    public void setShare(final ContactInfoShare share) {
        this.share = share;
    }

    public boolean isNameVisibility() {
        return nameVisibility;
    }

    public void setNameVisibility(final boolean nameVisibility) {
        this.nameVisibility = nameVisibility;
    }

    public boolean isPhoneNumberVisibility() {
        return phoneNumberVisibility;
    }

    public void setPhoneNumberVisibility(final boolean phoneNumberVisibility) {
        this.phoneNumberVisibility = phoneNumberVisibility;
    }

    public boolean isEmailVisibility() {
        return emailVisibility;
    }

    public void setEmailVisibility(final boolean emailVisibility) {
        this.emailVisibility = emailVisibility;
    }
}
