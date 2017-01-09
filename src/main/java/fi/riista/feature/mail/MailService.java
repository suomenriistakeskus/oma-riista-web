package fi.riista.feature.mail;

import org.joda.time.DateTime;

public interface MailService {
    /**
     * Send email and attempt redelivery on failure.
     */
    MailMessageDTO send(MailMessageDTO.Builder builder);

    /**
     * Schedule email delivery for later time.
     *
     * @param sendAfterTime do not attempt delivery before given time.
     */
    MailMessageDTO sendLater(MailMessageDTO.Builder builder,
                             DateTime sendAfterTime);

    /**
     * Attempt immediate delivery and do not retry.
     */
    MailMessageDTO sendImmediate(MailMessageDTO.Builder builder);

    /*
     * Private API:
     * Method called internally to poll for unsent messages to be sent in batches
     */
    void processOutgoingMail();
}
