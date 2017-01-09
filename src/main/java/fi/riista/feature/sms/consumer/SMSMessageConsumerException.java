package fi.riista.feature.sms.consumer;

public class SMSMessageConsumerException extends RuntimeException {
    public SMSMessageConsumerException(String message) {
        super(message);
    }

    public SMSMessageConsumerException(String message, Throwable cause) {
        super(message, cause);
    }
}
