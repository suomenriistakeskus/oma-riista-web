package fi.riista.feature.mail;

public interface MailService {
    String getDefaultFromAddress();

    /**
     * Send email and attempt redelivery on failure.
     */
    void send(MailMessageDTO dto);

    /*
     * Private API:
     * Method called internally to poll for unsent messages to be sent in batches
     */
    void processOutgoingMail();
}
