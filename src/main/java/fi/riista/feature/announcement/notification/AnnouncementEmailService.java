package fi.riista.feature.announcement.notification;

import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.util.Locales;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static java.util.stream.Collectors.joining;

@Service
public class AnnouncementEmailService {
    @Resource
    private MailService mailService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendEmail(final Announcement announcement,
                          final Locale locale,
                          final Collection<String> toEmailList) {
        if (toEmailList.isEmpty()) {
            return;
        }

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(toEmailList)
                .withSubject(announcement.getSubject())
                .appendBody(formatMessageHeader(announcement, locale))
                .appendBody(formatMessageBody(announcement.getBody()))
                .build());
    }

    private static String formatMessageHeader(final Announcement announcement,
                                              final Locale locale) {
        if (Locales.isSwedish(locale)) {
            return "<p>Det här meddelandet har förmedlats via Oma riista.fi –tjänsten.</p>" +
                    "<p>\"" + announcement.getFromUser().getPerson().getFullName() + " - " +
                    announcement.getFromOrganisation().getNameLocalisation().getAnyTranslation() + "\" has sänt meddelandet.</p>" +
                    "<p>Svara inte på det här meddelandet.</p>" +
                    "<p>----------- Meddelandet som förmedlats börjar ----------------</p>";

        }

        return "<p>Tämä on Oma.riista.fi -palvelun avulla välitetty viesti.</p>" +
                "<p>Viestin on lähettänyt \"" + announcement.getFromUser().getPerson().getFullName() + " - " +
                announcement.getFromOrganisation().getNameLocalisation().getAnyTranslation() + "\".</p>" +
                "<p>Älä vastaa tähän viestiin.</p>" +
                "<p>----------- Välitetty viesti alkaa ----------------</p>";
    }

    private static String formatMessageBody(final String body) {
        return Arrays.stream(body.split("\n"))
                .map(part -> "<p>" + part + "</p>")
                .collect(joining("\n"));
    }
}
