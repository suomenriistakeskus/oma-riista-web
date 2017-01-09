package fi.riista.feature.sms.consumer;

import fi.riista.feature.sms.SMSReceivedMessage;

public interface SMSMessageConsumer {
    boolean consume(SMSReceivedMessage sms);
}
