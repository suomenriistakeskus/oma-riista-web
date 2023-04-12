package fi.riista.feature.huntingclub.group;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;


public class HuntingClubGroupLeaderEmail {

    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_hunting_club_group_leader_notification", "email_hunting_club_group_leader_notification.sv");

    private final Handlebars handlebars;
    private final MessageSource messageSource;
    private Set<String> recipients;

    private final HashMap<String, Object> model = Maps.newHashMap();

    public HuntingClubGroupLeaderEmail(
            final Handlebars handlebars,
            final MessageSource messageSource) {
        this.handlebars = handlebars;
        this.messageSource = messageSource;
    }

    public MailMessageDTO build(final String emailFrom) {

        final String emailSubject =
                String.format("%s / %s",
                        messageSource.getMessage("huntingClubGroup.email.title", null, Locales.FI),
                        messageSource.getMessage("huntingClubGroup.email.title", null, Locales.SV));


        return MailMessageDTO.builder()
                .withFrom(emailFrom)
                .withSubject(emailSubject)
                .withRecipients(recipients)
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .build();
    }

    public HuntingClubGroupLeaderEmail withRecipient(final String recipient) {
        this.recipients = ImmutableSet.of(recipient);
        return this;
    }

    public HuntingClubGroupLeaderEmail withHuntingClubName(final LocalisedString huntingClubName) {
        this.model.put("clubName", huntingClubName);
        return this;
    }

    public HuntingClubGroupLeaderEmail withHuntingGroupName(final LocalisedString huntingGroupName) {
        this.model.put("groupName", huntingGroupName);
        return this;
    }

    public HuntingClubGroupLeaderEmail withSpeciesName(final LocalisedString speciesName) {
        this.model.put("speciesName", speciesName);
        return this;
    }

    public HuntingClubGroupLeaderEmail withPermitNumber(final String permitNumber) {
        this.model.put("permitNumber", permitNumber);
        return this;
    }
}
