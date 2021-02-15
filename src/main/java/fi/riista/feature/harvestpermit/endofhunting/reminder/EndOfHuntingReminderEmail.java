package fi.riista.feature.harvestpermit.endofhunting.reminder;

import javax.annotation.Nonnull;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class EndOfHuntingReminderEmail {

    private final String subject;
    private final String body;
    private final Set<String> recipients;

    public EndOfHuntingReminderEmail(final @Nonnull String subject,
                                     final @Nonnull String body,
                                     final @Nonnull Set<String> recipients) {
        this.subject = requireNonNull(subject);
        this.body = requireNonNull(body);
        this.recipients = requireNonNull(recipients);;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Set<String> getRecipients() {
        return recipients;
    }
}
