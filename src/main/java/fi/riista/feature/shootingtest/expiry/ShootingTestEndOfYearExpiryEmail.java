package fi.riista.feature.shootingtest.expiry;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ShootingTestEndOfYearExpiryEmail {

    private final String subject;
    private final String body;
    private final Set<String> recipients;

    public ShootingTestEndOfYearExpiryEmail(final String subject, final String body, final Set<String> recipients) {
        this.subject = requireNonNull(subject);
        this.body = requireNonNull(body);
        this.recipients = requireNonNull(recipients);
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
