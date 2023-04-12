package fi.riista.feature.account;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.Maps;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AccountUnregisterEmail {
    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_account_unregister_email", "email_account_unregister_email.sv");

    private final Handlebars handlebars;
    private final MessageSource messageSource;

    private final Set<String> recipients = new HashSet<>(2);
    private final HashMap<String, Object> model = Maps.newHashMap();

    public AccountUnregisterEmail(
            final Handlebars handlebars,
            final MessageSource messageSource) {
        this.handlebars = handlebars;
        this.messageSource = messageSource;
    }

    public MailMessageDTO build(final String emailFrom) {

        final String emailSubject =
                String.format("%s / %s",
                        messageSource.getMessage("account.unregister.email.title", null, Locales.FI),
                        messageSource.getMessage("account.unregister.email.title", null, Locales.SV));

        return MailMessageDTO.builder()
                .withFrom(emailFrom)
                .withSubject(emailSubject)
                .withRecipients(recipients)
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .build();
    }

    public AccountUnregisterEmail withRecipient(final String recipient) {
        this.recipients.add(recipient);
        return this;
    }

    public AccountUnregisterEmail withDate(final DateTime date) {
        this.model.put("resignationTime", date);
        return this;
    }

    public AccountUnregisterEmail withFirstName(final String firstName) {
        this.model.put("firstName", firstName);
        return this;
    }

    public AccountUnregisterEmail withLastName(final String lastName) {
        this.model.put("lastName", lastName);
        return this;
    }

    public AccountUnregisterEmail withHunterNumber(final String hunterNumber) {
        if (hunterNumber != null) {
            this.model.put("hunterNumber", hunterNumber);
        }
        return this;
    }

    public AccountUnregisterEmail withEmail(final String email) {
        this.model.put("email", email);
        return this;
    }
}
