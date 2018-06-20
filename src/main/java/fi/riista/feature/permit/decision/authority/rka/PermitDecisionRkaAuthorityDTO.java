package fi.riista.feature.permit.decision.authority.rka;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;

public class PermitDecisionRkaAuthorityDTO {

    private Long id;
    private Long rkaId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String titleFinnish;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String titleSwedish;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String phoneNumber;

    @Email
    private String email;

    public PermitDecisionRkaAuthorityDTO() {
    }

    public PermitDecisionRkaAuthorityDTO(final PermitDecisionRkaAuthority entity) {
        this.id = entity.getId();
        this.rkaId = entity.getRka().getId();
        this.firstName = entity.getFirstName();
        this.lastName = entity.getLastName();
        this.titleFinnish = entity.getTitleFinnish();
        this.titleSwedish = entity.getTitleSwedish();
        this.phoneNumber = entity.getPhoneNumber();
        this.email = entity.getEmail();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getRkaId() {
        return rkaId;
    }

    public void setRkaId(final Long rkaId) {
        this.rkaId = rkaId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getTitleFinnish() {
        return titleFinnish;
    }

    public void setTitleFinnish(final String titleFinnish) {
        this.titleFinnish = titleFinnish;
    }

    public String getTitleSwedish() {
        return titleSwedish;
    }

    public void setTitleSwedish(final String titleSwedish) {
        this.titleSwedish = titleSwedish;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
