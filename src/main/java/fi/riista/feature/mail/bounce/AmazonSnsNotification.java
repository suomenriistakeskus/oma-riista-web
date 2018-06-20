package fi.riista.feature.mail.bounce;

public class AmazonSnsNotification {
    private String Type;
    private String Token;
    private String MessageId;
    private String TopicArn;
    private String Message;
    private String Subject;
    private String Timestamp;
    private String SignatureVersion;
    private String Signature;
    private String SigningCertURL;
    private String UnsubscribeURL;
    private String SubscribeURL;

    public String getType() {
        return Type;
    }

    public String getToken() {
        return Token;
    }

    public String getMessageId() {
        return MessageId;
    }

    public String getTopicArn() {
        return TopicArn;
    }

    public String getMessage() {
        return Message;
    }

    public String getSubject() {
        return Subject;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public String getSignatureVersion() {
        return SignatureVersion;
    }

    public String getSignature() {
        return Signature;
    }

    public String getSigningCertURL() {
        return SigningCertURL;
    }

    public String getUnsubscribeURL() {
        return UnsubscribeURL;
    }

    public String getSubscribeURL() {
        return SubscribeURL;
    }
}
