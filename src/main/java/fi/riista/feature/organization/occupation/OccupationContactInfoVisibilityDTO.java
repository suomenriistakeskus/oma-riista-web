package fi.riista.feature.organization.occupation;

public class OccupationContactInfoVisibilityDTO {

    private long id;

    private boolean nameVisibility;
    private boolean phoneNumberVisibility;
    private boolean emailVisibility;

    OccupationContactInfoVisibilityDTO() {}

    public OccupationContactInfoVisibilityDTO(final long id,
                                              final boolean nameVisibility,
                                              final boolean phoneNumberVisibility,
                                              final boolean emailVisibility) {
        this.id = id;
        this.nameVisibility = nameVisibility;
        this.phoneNumberVisibility = phoneNumberVisibility;
        this.emailVisibility = emailVisibility;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
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
