package fi.riista.feature.huntingclub.members.club;

import fi.riista.feature.organization.person.ContactInfoShare;

import java.io.Serializable;

public class ContactInfoShareUpdateDTO implements Serializable {

    Long occupationId;
    ContactInfoShare share;

    public Long getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(Long occupationId) {
        this.occupationId = occupationId;
    }

    public ContactInfoShare getShare() {
        return share;
    }

    public void setShare(ContactInfoShare share) {
        this.share = share;
    }
}
