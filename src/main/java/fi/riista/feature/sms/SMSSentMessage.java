package fi.riista.feature.sms;

import fi.riista.feature.sms.storage.SMSMessageStatus;
import fi.riista.feature.sms.storage.SMSPersistentMessage;

public class SMSSentMessage {
    private final SMSMessageStatus status;

    private final String numberFrom;
    private final String numberTo;

    public SMSSentMessage(SMSPersistentMessage persistentMessage) {
        this.numberFrom = persistentMessage.getNumberFrom();
        this.numberTo = persistentMessage.getNumberTo();
        this.status = persistentMessage.getStatus();
    }

    public SMSMessageStatus getStatus() {
        return status;
    }

    public String getNumberFrom() {
        return numberFrom;
    }

    public String getNumberTo() {
        return numberTo;
    }
}
