package fi.riista.feature.announcement.notification;

import com.google.common.collect.Lists;
import fi.riista.feature.announcement.show.MobileAnnouncementSenderDTO;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.bounce.MailMessageComplaintRepository;
import fi.riista.feature.mail.bounce.MailMessageComplaintService;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Service
public class AnnouncementEmailService {
    private static final Logger LOG = LoggerFactory.getLogger(AnnouncementEmailService.class);

    @Resource
    private MailService mailService;

    @Resource
    private MailMessageComplaintService complaintService;


    @Async
    @Transactional(noRollbackFor = RuntimeException.class)
    public void asyncSend(final AnnouncementNotificationDTO dto) {
        send(dto);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    /*package*/ void send(final AnnouncementNotificationDTO dto) {
        final List<String> toEmailList = Lists.partition(dto.getTargets().getEmails(), 1000).stream()
                .flatMap(partition -> handlePartition(partition))
                .collect(Collectors.toList());

        if (toEmailList.isEmpty()) {
            return;
        }

        LOG.info("Sending email to {} receivers", dto.getTargets().getEmails().size());

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(toEmailList)
                .withSubject(dto.getAnnouncement().getSubject())
                .appendBody(formatMessageHeader(dto))
                .appendBody(formatMessageBody(dto))
                .build());
    }

    private Stream<String> handlePartition(final List<String> partition) {
        final List<String> complaintEmails = complaintService.findEmailsWithComplaintIn(partition);
        if (complaintEmails.isEmpty()) {
            return partition.stream();
        }

        LOG.info("Found complaints from recipients, {} recipients will be filtered from receiving announement email",
                complaintEmails.size());

        return partition.stream().filter(email -> !complaintEmails.contains(email));
    }

    private static String formatMessageHeader(final AnnouncementNotificationDTO dto) {
        final MobileAnnouncementSenderDTO sender = dto.getAnnouncement().getSender();
        final LocalisedString fromOrganisation = LocalisedString.fromMap(sender.getOrganisation());
        final String senderFullName = sender.getFullName();
        final String senderOrganisation = fromOrganisation.getTranslation(dto.getLocale());
        final String senderText = StringUtils.hasText(senderFullName)
                ? senderFullName + " - " + senderOrganisation
                : senderOrganisation;

        if (Locales.isSwedish(dto.getLocale())) {
            return "<p>Det här meddelandet har förmedlats via Oma riista.fi –tjänsten.</p>" +
                    "<p>\"" + senderText + "\" has sänt meddelandet.</p>" +
                    "<p>Svara inte på det här meddelandet.</p>" +
                    "<p>----------- Meddelandet som förmedlats börjar ----------------</p>";

        }

        return "<p>Tämä on Oma.riista.fi -palvelun avulla välitetty viesti.</p>" +
                "<p>Viestin on lähettänyt \"" + senderText + "\".</p>" +
                "<p>Älä vastaa tähän viestiin.</p>" +
                "<p>----------- Välitetty viesti alkaa ----------------</p>";
    }

    private static String formatMessageBody(final AnnouncementNotificationDTO dto) {
        return Arrays.stream(dto.getAnnouncement().getBody().split("\n"))
                .map(part -> "<p>" + part + "</p>")
                .collect(joining("\n"));
    }
}
