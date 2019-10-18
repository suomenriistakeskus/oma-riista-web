package fi.riista.feature.organization.jht.expiry;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class JHTOccupationExpiryEmail {
    private static final String SUBJECT_KEY = "jht.expiry.email.subject";
    private static final LocalisedString BODY_TEMPLATE = new LocalisedString(
            "email_jht_expiry", "email_jht_expiry.sv");

    public static JHTOccupationExpiryEmail create(final @Nonnull Handlebars handlebars,
                                                  final @Nonnull MessageSource messageSource,
                                                  final @Nonnull JHTOccupationExpiryDTO dto,
                                                  final @Nonnull Set<String> rhyEmails) {
        requireNonNull(handlebars);
        requireNonNull(messageSource);
        requireNonNull(dto);
        requireNonNull(rhyEmails);

        final Set<String> recipients = Stream.concat(
                Stream.of(dto.getOccupationEmail()),
                rhyEmails.stream()).collect(Collectors.toSet());
        final String subject = messageSource.getMessage(SUBJECT_KEY, null, dto.getLocale());
        final String template = BODY_TEMPLATE.getAnyTranslation(dto.getLocale());
        final String body;

        try {
            body = handlebars.compile(template).apply(dto);
        } catch (IOException e) {
            throw new RuntimeException("Could not render template", e);
        }

        return new JHTOccupationExpiryEmail(subject, body, recipients);
    }

    private final String subject;
    private final String body;
    private final Set<String> recipients;

    private JHTOccupationExpiryEmail(final String subject, final String body, final Set<String> recipients) {
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
