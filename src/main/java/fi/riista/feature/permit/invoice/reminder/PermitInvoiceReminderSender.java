package fi.riista.feature.permit.invoice.reminder;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Map;

@Service
public class PermitInvoiceReminderSender {
    private static final LocalisedString EMAIL_TEMPLATE = new LocalisedString(
            "email_invoice_reminder", "email_invoice_reminder.sv");

    private static String getEmailTemplate(final PermitInvoiceReminderDTO dto) {
        return EMAIL_TEMPLATE.getTranslation(dto.getLocale());
    }

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Transactional
    public void sendReminder(final PermitInvoiceReminderDTO dto) {
        final Map<String, Object> model = createEmailTemplateModel(dto);

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(dto.getRecipientEmails())
                .withSubject(getEmailSubject(dto))
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendHandlebarsBody(handlebars, getEmailTemplate(dto), model)
                .appendBody("</body></html>")
                .build());
    }

    private Map<String, Object> createEmailTemplateModel(final PermitInvoiceReminderDTO dto) {
        final URI permitDashboardUri = permitClientUriFactory.getAbsolutePermitDashboardUri(dto.getHarvestPermitId());
        return ImmutableMap.of("url", permitDashboardUri.toString());
    }

    private String getEmailSubject(final PermitInvoiceReminderDTO dto) {
        return messageSource.getMessage("permit.invoice.reminder.email.subject", null, dto.getLocale());
    }
}
