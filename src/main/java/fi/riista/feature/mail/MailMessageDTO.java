package fi.riista.feature.mail;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.Preconditions;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class MailMessageDTO implements HasMailMessageFields {
    private final String from;
    private final Set<String> recipients;
    private final String subject;
    private final String body;
    private final DateTime scheduledTime;

    public MailMessageDTO(Builder builder) {
        Preconditions.checkArgument(builder.recipients != null && builder.recipients.size() > 0, "recipients is empty");
        Preconditions.checkArgument(StringUtils.hasText(builder.from), "from is empty");
        Preconditions.checkArgument(StringUtils.hasText(builder.subject), "subject is empty");
        Preconditions.checkArgument(StringUtils.hasText(builder.body), "body is empty");

        this.recipients = builder.recipients;
        this.from = builder.from;
        this.subject = builder.subject;
        this.body = builder.body;
        this.scheduledTime = Objects.requireNonNull(builder.scheduledTime, "scheduledTime is null");
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getBody() {
        return body;
    }

    public DateTime getScheduledTime() {
        return scheduledTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String from;
        private Set<String> recipients = new LinkedHashSet<>();
        private String subject;
        private String body;
        private DateTime scheduledTime = DateUtil.now();

        private Builder() {
        }

        public Builder appendHandlebarsBody(final Handlebars handlebars,
                                            final String templateName,
                                            final Map<String, Object> model) {
            return appendBody(compileHandlebarsAndApply(handlebars, templateName, model));
        }

        private static String compileHandlebarsAndApply(final Handlebars handlebars,
                                                 final String templateName,
                                                 final Map<String, Object> model) {
            Preconditions.checkArgument(StringUtils.hasText(templateName), "Template name not specified");

            final Context context = Context.newBuilder(model).combine(model).build();
            try {
                return handlebars.compile(templateName).apply(context);
            } catch (IOException e) {
                throw new RuntimeException("Could not render template", e);

            } finally {
                context.destroy();
            }
        }

        public Builder addRecipient(String to) {
            this.recipients.add(Objects.requireNonNull(to).trim().toLowerCase());
            return this;
        }

        public Builder withRecipients(Collection<String> to) {
            Objects.requireNonNull(to).stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(this.recipients::add);

            return this;
        }

        public Builder withFrom(String from) {
            this.from = from;
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

        public Builder withScheduledTimeAfter(Duration duration) {
            this.scheduledTime = DateUtil.now().plus(duration);
            return this;
        }

        public MailMessageDTO build() {
            return new MailMessageDTO(this);
        }
    }
}
