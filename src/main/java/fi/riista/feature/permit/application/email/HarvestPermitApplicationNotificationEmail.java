package fi.riista.feature.permit.application.email;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

public class HarvestPermitApplicationNotificationEmail {
    private static final String SUBJECT_KEY = "harvest.permit.application.email.subject";
    private static final LocalisedString BODY_TEMPLATE = new LocalisedString(
            "email_application_received", "email_application_received.sv");

    public static HarvestPermitApplicationNotificationEmail create(final @Nonnull Handlebars handlebars,
                                                                   final @Nonnull MessageSource messageSource,
                                                                   final @Nonnull HarvestPermitApplicationNotificationDTO dto) {
        requireNonNull(handlebars);
        requireNonNull(messageSource);
        requireNonNull(dto);

        final Set<String> recipients = singleton(requireNonNull(dto.getContactPersonEmail()));
        final String subject = messageSource.getMessage(SUBJECT_KEY, null, dto.getLocale());
        final String template = BODY_TEMPLATE.getAnyTranslation(dto.getLocale());
        final String body;

        try {
            body = handlebars.compile(template).apply(dto);
        } catch (IOException e) {
            throw new RuntimeException("Could not render template", e);
        }

        return new HarvestPermitApplicationNotificationEmail(subject, body, recipients);
    }

    private final String subject;
    private final String body;
    private final Set<String> recipients;

    private HarvestPermitApplicationNotificationEmail(final String subject, final String body, final Set<String> recipients) {
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
