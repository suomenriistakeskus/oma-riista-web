package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Component
public class HarvestReportReminderEmailService {

    private static final LocalisedString EMAIL_TEMPLATE = new LocalisedString(
            "email_harvest_report_reminder", "email_harvest_report_reminder.sv");

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendMails(final Harvest harvest, final Set<String> emailReceivers) {
        final GameSpeciesDTO dto = GameSpeciesDTO.create(harvest.getSpecies());
        final Date pointOfTime = harvest.getPointOfTime();
        final PersonDTO author = PersonDTO.create(harvest.getAuthor());
        final PersonDTO hunter = PersonDTO.create(harvest.getActualShooter());

        final String emailSubject = messageSource.getMessage("harvest.report.reminder.email.subject", null, Locales.FI);

        final Map<String, Object> model = ImmutableMap.of(
                "pointOfTime", pointOfTime,
                "species", dto,
                "author", author,
                "hunter", hunter);

        for (final String email : emailReceivers) {
            mailService.send(new MailMessageDTO.Builder()
                    .withTo(email)
                    .withSubject(emailSubject)
                    .appendHandlebarsBody(handlebars, EMAIL_TEMPLATE.getFinnish(), model)
                    .appendBody("\n\n--------------------------------------------------------------------------------\n\n")
                    .appendHandlebarsBody(handlebars, EMAIL_TEMPLATE.getSwedish(), model));
        }
    }
}
