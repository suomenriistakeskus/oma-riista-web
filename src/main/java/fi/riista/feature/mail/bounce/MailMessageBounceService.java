package fi.riista.feature.mail.bounce;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Service
public class MailMessageBounceService {

    private static final Logger LOG = LoggerFactory.getLogger(MailMessageBounceService.class);
    private static final DateTimeFormatter ISO_DATETIME_FORMAT = ISODateTimeFormat.dateTime();
    private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();
    @Resource
    private MailMessageBounceRepository mailMessageBounceRepository;


    @Transactional
    public void storeBounce(final AmazonSesBounceNotification bounceNotification) {
        final AmazonSesBounce bounce = bounceNotification.getBounce();
        final AmazonSesOriginalMailMessage mail = bounceNotification.getMail();

        final List<MailMessageBounce> result = new LinkedList<>();

        for (final AmazonSesBouncedRecipient recipient : bounce.getBouncedRecipients()) {
            final String emailAddress = recipient.getEmailAddress();

            if (!EMAIL_VALIDATOR.isValid(emailAddress, null)) {
                LOG.warn("Invalid recipient email address found for bounce: {}", bounce);
                continue;
            }

            final MailMessageBounce entity = new MailMessageBounce();
            entity.setRecipientAction(recipient.getAction());
            entity.setRecipientDiagnosticCode(recipient.getDiagnosticCode());
            entity.setRecipientEmailAddress(emailAddress);
            entity.setRecipientStatus(recipient.getStatus());

            entity.setBounceTimestamp(ISO_DATETIME_FORMAT.parseDateTime(bounce.getTimestamp()));
            entity.setBounceType(getBounceType(bounce.getBounceType()));
            entity.setBounceSubType(getBounceSubType(bounce.getBounceSubType()));
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

    private static MailMessageBounce.BounceType getBounceType(final String bounceType) {
        try {
            return MailMessageBounce.BounceType.valueOf(bounceType);
        } catch (final Exception e) {
            LOG.warn("Could not parse bounce type from value {}, using {}",
                    bounceType, MailMessageBounce.BounceType.Undetermined);
            return MailMessageBounce.BounceType.Undetermined;
        }
    }

    private static MailMessageBounce.BounceSubType getBounceSubType(final String bounceSubType) {
        try {
            return MailMessageBounce.BounceSubType.valueOf(bounceSubType);
        } catch (final Exception e) {
            LOG.warn("Could not parse bounce subtype from value {}, using {}",
                    bounceSubType, MailMessageBounce.BounceSubType.Undetermined);
            return MailMessageBounce.BounceSubType.Undetermined;
        }
    }
}
