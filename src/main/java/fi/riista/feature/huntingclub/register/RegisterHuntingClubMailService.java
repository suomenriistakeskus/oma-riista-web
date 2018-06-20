package fi.riista.feature.huntingclub.register;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.EmailResolver;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class RegisterHuntingClubMailService {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterHuntingClubMailService.class);

    public static final LocalisedString EMAIL_TEMPLATE_CLUB_CONTACT_PERSON = new LocalisedString(
            "email_club_registered_contactperson",
            "email_club_registered_contactperson.sv");

    public static final LocalisedString EMAIL_TEMPLATE_RHY_CONTACT_PERSON = new LocalisedString(
            "email_club_registered_rhy",
            "email_club_registered_rhy.sv");

    public static final LocalisedString EMAIL_TEMPLATE_ADMIN = EMAIL_TEMPLATE_RHY_CONTACT_PERSON;

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private EmailResolver emailResolver;

    @Transactional(readOnly = true)
    public Iterable<String> getRhyContactEmails(final Organisation rhy) {
        return emailResolver.findRhyContactEmails(rhy);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotificationEmail(final HuntingClubDTO huntingClubDTO,
                                      final OccupationDTO occupationDTO,
                                      final OrganisationNameDTO rhyDTO,
                                      final Iterable<String> rhyContactEmails,
                                      final String contactPersonEmail) {
        final Map<String, Object> model = ImmutableMap.<String, Object>builder()
                .put("club", huntingClubDTO)
                .put("occupation", occupationDTO)
                .put("rhy", rhyDTO)
                .build();

        final String emailSubject = "Seura otettu haltuun - " + huntingClubDTO.getNameFI();
        final String adminEmail = EmailResolver.sanitizeEmail(runtimeEnvironmentUtil.getAdminEmail());

        // Notify admin
        if (adminEmail != null) {
            LOG.info("Sending notification to administrator {}", adminEmail);
            sendInternal(adminEmail, emailSubject, model, EMAIL_TEMPLATE_ADMIN);
        } else {
            LOG.warn("Admin email is not configured!");
        }

        // Notify created HuntingClub contact person
        if (contactPersonEmail != null) {
            LOG.info("Sending notification to contact person {}", contactPersonEmail);
            sendInternal(contactPersonEmail, emailSubject, model, EMAIL_TEMPLATE_CLUB_CONTACT_PERSON);
        }

        // Notify RHY contact person
        if (runtimeEnvironmentUtil.isProductionEnvironment() || runtimeEnvironmentUtil.isDevelopmentEnvironment()) {
            for (final String rhyEmail : rhyContactEmails) {
                LOG.info("Sending notification to RHY contact person {}", rhyEmail);

                sendInternal(rhyEmail, emailSubject, model, EMAIL_TEMPLATE_RHY_CONTACT_PERSON);
            }
        }
    }

    private void sendInternal(final String email,
                              final String subject,
                              final Map<String, Object> model,
                              final LocalisedString template) {
        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .addRecipient(email)
                .withSubject(subject)
                .appendHandlebarsBody(handlebars, template.getFinnish(), model)
                .appendBody("<br/><hr/><br/>")
                .appendHandlebarsBody(handlebars, template.getSwedish(), model)
                .build());
    }
}
