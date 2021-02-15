package fi.riista.feature.mail.bounce;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Service
public class MailMessageBounceService {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT = ISODateTimeFormat.dateTime();

    @Resource
    private MailMessageBounceRepository mailMessageBounceRepository;


    @Transactional
    public void storeBounce(final AmazonSesBounceNotification bounceNotification) {
        final AmazonSesBounce bounce = bounceNotification.getBounce();
        final AmazonSesOriginalMailMessage mail = bounceNotification.getMail();

        final List<MailMessageBounce> result = new LinkedList<>();

        for (final AmazonSesBouncedRecipient recipient : bounce.getBouncedRecipients()) {
            final MailMessageBounce entity = new MailMessageBounce();
            entity.setRecipientAction(recipient.getAction());
            entity.setRecipientDiagnosticCode(recipient.getDiagnosticCode());
            entity.setRecipientEmailAddress(recipient.getEmailAddress());
            entity.setRecipientStatus(recipient.getStatus());

            entity.setBounceTimestamp(ISO_DATETIME_FORMAT.parseDateTime(bounce.getTimestamp()));
            entity.setBounceType(MailMessageBounce.BounceType.valueOf(bounce.getBounceType()));
            entity.setBounceSubType(MailMessageBounce.BounceSubType.valueOf(bounce.getBounceSubType()));
            entity.setBounceFeedbackId(bounce.getFeedbackId());

            if (mail != null) {
                entity.setMailTimestamp(ISO_DATETIME_FORMAT.parseDateTime(mail.getTimestamp()));

                if (mail.getCommonHeaders() != null) {
                    entity.setMailMessageId(mail.getCommonHeaders().getMessageId());
                    entity.setMailSubject(mail.getCommonHeaders().getSubject());
                }
            }

            result.add(entity);
        }

        mailMessageBounceRepository.saveAll(result);
    }
}
