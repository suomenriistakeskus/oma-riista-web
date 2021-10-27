package fi.riista.feature.organization.occupation;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class OccupationContactInfoVisibilityRule {

    public enum VisibilitySetting {
        ALWAYS,
        NEVER,
        OPTIONAL
    }

    private final VisibilitySetting nameVisibility;
    private final VisibilitySetting phoneNumberVisibility;
    private final VisibilitySetting emailVisibility;

    public static OccupationContactInfoVisibilityRule createRule(@Nonnull final VisibilitySetting nameVisibility,
                                                                 @Nonnull final VisibilitySetting phoneNumberVisibility,
                                                                 @Nonnull final VisibilitySetting emailVisibility) {
        requireNonNull(nameVisibility);
        requireNonNull(phoneNumberVisibility);
        requireNonNull(emailVisibility);

        return new OccupationContactInfoVisibilityRule(nameVisibility, phoneNumberVisibility, emailVisibility);
    }

    private OccupationContactInfoVisibilityRule(final VisibilitySetting nameVisibility,
                                                final VisibilitySetting phoneNumberVisibility,
                                                final VisibilitySetting emailVisibility) {
        this.nameVisibility = nameVisibility;
        this.phoneNumberVisibility = phoneNumberVisibility;
        this.emailVisibility = emailVisibility;
    }

    public boolean canEditNameVisibility() {
        return nameVisibility == VisibilitySetting.OPTIONAL;
    }

    public boolean canEditPhoneNumberVisibility() {
        return phoneNumberVisibility == VisibilitySetting.OPTIONAL;
    }

    public boolean canEditEmailVisibility() {
        return emailVisibility == VisibilitySetting.OPTIONAL;
    }

    public VisibilitySetting getNameVisibility() {
        return nameVisibility;
    }

    public VisibilitySetting getPhoneNumberVisibility() {
        return phoneNumberVisibility;
    }

    public VisibilitySetting getEmailVisibility() {
        return emailVisibility;
    }
}
