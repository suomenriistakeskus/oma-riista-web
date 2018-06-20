package fi.riista.feature.mail.bounce;

import java.util.List;

public class AmazonSesOriginalMailMessage {
    private String timestamp;
    private String source;
    private String sourceArn;
    private String sourceIp;
    private String sendingAccountId;
    private String messageId;
    private List<String> destination;
    private boolean headersTruncated;
    private List<Header> headers;
    private CommonHeader commonHeaders;

    public static class Header {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    public static class CommonHeader {
        private List<String> from;
        private String date;
        private List<String> to;
        private String messageId;
        private String subject;

        public List<String> getFrom() {
            return from;
        }

        public String getDate() {
            return date;
        }

        public List<String> getTo() {
            return to;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getSubject() {
            return subject;
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    public String getSourceArn() {
        return sourceArn;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getSendingAccountId() {
        return sendingAccountId;
    }

    public String getMessageId() {
        return messageId;
    }

    public List<String> getDestination() {
        return destination;
    }

    public boolean isHeadersTruncated() {
        return headersTruncated;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public CommonHeader getCommonHeaders() {
        return commonHeaders;
    }
}
