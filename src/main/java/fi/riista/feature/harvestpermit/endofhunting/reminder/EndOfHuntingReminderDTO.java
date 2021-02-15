package fi.riista.feature.harvestpermit.endofhunting.reminder;

import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class EndOfHuntingReminderDTO {

    private final String contactPersonEmail;
    private final List<String> additionalContactEmails;
    private final long permitId;
    private final String permitNumber;
    private final List<LocalisedString> gameSpecies;

    public EndOfHuntingReminderDTO(final String contactPersonEmail,
                                   final List<String> additionalContactEmails,
                                   final long permitId,
                                   final @Nonnull String permitNumber,
                                   final @Nonnull List<LocalisedString> gameSpecies) {
        requireNonNull(permitNumber);
        requireNonNull(gameSpecies);

        this.contactPersonEmail = contactPersonEmail;
        this.additionalContactEmails = additionalContactEmails;
        this.permitId = permitId;
        this.permitNumber = permitNumber;
        this.gameSpecies = gameSpecies;
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }

    public List<String> getAdditionalContactEmails() {
        return additionalContactEmails;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public List<LocalisedString> getGameSpecies() {
        return gameSpecies;
    }

    public long getPermitId() {
        return permitId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null || o.getClass() != this.getClass()) {
            return false;
        } else {
            final EndOfHuntingReminderDTO that = (EndOfHuntingReminderDTO) o;

            return Objects.equals(contactPersonEmail, that.contactPersonEmail) &&
                    Objects.equals(additionalContactEmails, that.additionalContactEmails) &&
                    permitId == that.getPermitId() &&
                    Objects.equals(permitNumber,that.permitNumber) &&
                    Objects.equals(gameSpecies, that.gameSpecies);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactPersonEmail, additionalContactEmails, permitId, permitNumber, gameSpecies);
    }
}
