package fi.riista.feature.common.decision.authority;

import org.hibernate.validator.constraints.SafeHtml;

public class DecisionAuthorityDTO {

    public static DecisionAuthorityDTO create(final DecisionRkaAuthorityDetails authorityDetails) {
        if (authorityDetails == null) {
            return null;
        }
        return new DecisionAuthorityDTO(
                authorityDetails.getFirstName(),
                authorityDetails.getLastName(),
                authorityDetails.getTitle(),
                authorityDetails.getPhoneNumber(),
                authorityDetails.getEmail());
    }

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String title;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String phoneNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String email;

    public DecisionAuthorityDTO() {
    }

    public DecisionAuthorityDTO(final String firstName,
                                final String lastName,
                                final String title,
                                final String phoneNumber,
                                final String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.phoneNumber = phoneNumber;
        this.email = email;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
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
