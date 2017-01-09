package fi.riista.feature.sms;

import fi.riista.feature.sms.storage.SMSPersistentMessage;

import java.util.Date;

public class SMSReceivedMessage {
    private final Long id;
    private final Date receivedTimestamp;
    private final String numberFrom;
    private final String numberTo;
    private final String message;

    public SMSReceivedMessage(SMSPersistentMessage entity) {
        this.id = entity.getId();
        this.receivedTimestamp = entity.getCreationTime();
        this.message = entity.getMessage();
        this.numberFrom = entity.getNumberFrom();
        this.numberTo = entity.getNumberTo();
    }

    public Long getId() {
        return id;
    }

    public Date getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public String getNumberFrom() {
        return numberFrom;
    }

    public String getNumberTo() {
        return numberTo;
    }

    public String getMessage() {
        return message;
    }
}
