package fi.riista.feature.huntingclub.permit.endofhunting;


import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Set;

@Service
public class AllPartnersFinishedHuntingMailService {

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEmailAsync(final Set<String> emails, final AllPartnersFinishedHuntingDTO data) {
        sendEmailInternal(emails, data);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEmailInternal(final Set<String> emails, final AllPartnersFinishedHuntingDTO dto) {
        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(emails)
                .withSubject(dto.getMailSubject())
                .appendBody(createMessageBody(dto.getMailTemplate(), dto))
                .build());
    }

    private String createMessageBody(final String template, final AllPartnersFinishedHuntingDTO model) {
        try {
            return handlebars.compile(template).apply(model);
        } catch (IOException e) {
            throw new RuntimeException("Could not render template", e);
        }
    }
}
