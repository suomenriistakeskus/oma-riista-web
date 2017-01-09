package fi.riista.feature.mail;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public final class MailMessageDTO {
    private final String from;
    private final String to;
    private final String subject;
    private final String body;

    public MailMessageDTO(String from, String to, String subject, String body) {
        Preconditions.checkArgument(StringUtils.hasText(from), "Mail has no from");
        Preconditions.checkArgument(StringUtils.hasText(to), "Mail has no to");
        Preconditions.checkArgument(StringUtils.hasText(subject), "Mail has no subject");
        Preconditions.checkArgument(StringUtils.hasText(body), "Mail has no body");

        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public MailMessageDTO(Builder builder) {
        this(builder.from, builder.to, builder.subject, builder.body);
    }

    public void prepareMimeMessage(final MimeMessage mimeMessage) {
        try {
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setTo(getTo());
            messageHelper.setFrom(getFrom());
            messageHelper.setSubject(getSubject());
            messageHelper.setText(getBody(), true);
            messageHelper.setValidateAddresses(true);
            mimeMessage.setSentDate(new Date());

        } catch (MessagingException ex) {
            throw new MailParseException(ex);
        } catch (Exception ex) {
            throw new MailPreparationException(ex);
        }
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MailMessageDTO that = (MailMessageDTO) o;

        return body.equals(that.body) && from.equals(that.from) && subject.equals(that.subject) && to.equals(that.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("from", from)
                .add("to", to)
                .add("subject", subject)
                //.add("body", body)
                .toString();
    }

    public final static class Builder {
        private String from;
        private String defaultFrom;
        private String to;
        private String subject;
        private String body;

        public Builder withHandlebarsBody(final Handlebars handlebars,
                                          final String templateName,
                                          final Map<String, Object> model) {
            return compileHandlebarsAndApply(handlebars, templateName, model, this::withBody);
        }

        public Builder appendHandlebarsBody(final Handlebars handlebars,
                                            final String templateName,
                                            final Map<String, Object> model) {
            return compileHandlebarsAndApply(handlebars, templateName, model, this::appendBody);
        }

        private static Builder compileHandlebarsAndApply(final Handlebars handlebars,
                                                         final String templateName,
                                                         final Map<String, Object> model,
                                                         final Function<String, Builder> method) {
            Preconditions.checkArgument(StringUtils.hasText(templateName), "Template name not specified");

            final Context context = Context.newBuilder(model).combine(model).build();
            try {
                return method.apply(handlebars.compile(templateName).apply(context));
            } catch (IOException e) {
                throw new RuntimeException("Could not render template", e);

            } finally {
                context.destroy();
            }
        }

        public Builder withFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder withDefaultFrom(String defaultFrom) {
            this.defaultFrom = defaultFrom;
            return this;
        }

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder appendBody(String body) {
            if (this.body == null) {
                this.body = body;
            } else {
                this.body = this.body + body;
            }
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public MailMessageDTO build() {
            if (!StringUtils.hasText(this.from)) {
                this.from = defaultFrom;
            }

            return new MailMessageDTO(this);
        }
    }
}
