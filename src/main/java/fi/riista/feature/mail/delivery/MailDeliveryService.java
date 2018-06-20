package fi.riista.feature.mail.delivery;

import fi.riista.feature.mail.HasMailMessageFields;

public interface MailDeliveryService {
    long getRemainingQuota();

    void send(HasMailMessageFields message, String recipientEmail);
}
