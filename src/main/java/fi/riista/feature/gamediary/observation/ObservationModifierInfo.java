package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.SystemUser;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class ObservationModifierInfo {

    public interface Resolver {
        ObservationModifierInfo resolve(Observation observation);
    }

    private final SystemUser activeUser;

    private final boolean authorOrObserver;
    private final boolean carnivoreAuthorityInAnyRhyAtObservationDate;

    ObservationModifierInfo(@Nonnull final SystemUser activeUser,
                            final boolean authorOrObserver,
                            final boolean carnivoreAuthorityInAnyRhyAtObservationDate) {

        this.activeUser = requireNonNull(activeUser);

        if (activeUser.isModeratorOrAdmin() && (authorOrObserver || carnivoreAuthorityInAnyRhyAtObservationDate)) {
            throw new IllegalArgumentException("Moderator cannot be author / observer / carnivore authority");
        }

        this.authorOrObserver = authorOrObserver;
        this.carnivoreAuthorityInAnyRhyAtObservationDate = carnivoreAuthorityInAnyRhyAtObservationDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isModerator() {
        return activeUser.isModeratorOrAdmin();
    }

    public boolean canUpdateCarnivoreFields() {
        return authorOrObserver && carnivoreAuthorityInAnyRhyAtObservationDate;
    }

    // Accessors -->

    public SystemUser getActiveUser() {
        return activeUser;
    }

    public boolean isAuthorOrObserver() {
        return authorOrObserver;
    }

    public boolean isCarnivoreAuthorityInAnyRhyAtObservationDate() {
        return carnivoreAuthorityInAnyRhyAtObservationDate;
    }

    public static class Builder {

        private SystemUser activeUser;

        // Nullable fields here in order to have NPE being thrown if any of these is left unset.
        private Boolean authorOrObserver;
        private Boolean carnivoreAuthorityInAnyRhyAtObservationDate;

        private Builder() {
        }

        public Builder withActiveUser(@Nonnull final SystemUser activeUser) {
            this.activeUser = requireNonNull(activeUser);
            return this;
        }

        public Builder withAuthorOrObserver(final boolean authorOrObserver) {
            this.authorOrObserver = authorOrObserver;
            return this;
        }

        public Builder withCarnivoreAuthorityInAnyRhyAtObservationDate(final boolean carnivoreAuthorityInAnyRhyAtObservationDate) {
            this.carnivoreAuthorityInAnyRhyAtObservationDate = carnivoreAuthorityInAnyRhyAtObservationDate;
            return this;
        }

        public ObservationModifierInfo build() {
            return new ObservationModifierInfo(
                    activeUser, authorOrObserver, carnivoreAuthorityInAnyRhyAtObservationDate);
        }
    }
}
