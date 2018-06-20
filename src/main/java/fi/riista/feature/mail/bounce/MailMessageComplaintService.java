package fi.riista.feature.mail.bounce;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Service
public class MailMessageComplaintService {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT = ISODateTimeFormat.dateTime();

    @Resource
    private MailMessageComplaintRepository mailMessageComplaintRepository;

    @Transactional
    public void storeComplaint(final AmazonSesComplaintNotification complaintNotification) {
        final AmazonSesComplaint complaint = complaintNotification.getComplaint();
        final AmazonSesOriginalMailMessage mail = complaintNotification.getMail();

        final List<MailMessageComplaint> result = new LinkedList<>();

        for (final AmazonSesComplainedRecipient recipient : complaint.getComplainedRecipients()) {
            final MailMessageComplaint entity = new MailMessageComplaint();
            entity.setRecipientEmailAddress(recipient.getEmailAddress());

            entity.setComplaintTimestamp(ISO_DATETIME_FORMAT.parseDateTime(complaint.getTimestamp()));
            entity.setComplaintFeedbackId(complaint.getFeedbackId());

            if (mail != null) {
                entity.setMailTimestamp(ISO_DATETIME_FORMAT.parseDateTime(mail.getTimestamp()));

                if (mail.getCommonHeaders() != null) {
                    entity.setMailMessageId(mail.getCommonHeaders().getMessageId());
                    entity.setMailSubject(mail.getCommonHeaders().getSubject());
                }
            }

            result.add(entity);
        }

        mailMessageComplaintRepository.save(result);
    }
}
