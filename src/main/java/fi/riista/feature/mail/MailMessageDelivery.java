package fi.riista.feature.mail;

import fi.riista.feature.mail.queue.MailMessage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class MailMessageDelivery implements HasMailMessageFields {
    public static class Recipient {
        private final Long id;
        private final String email;

        public Recipient(final Long id, final String email) {
            this.id = id;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }
    }

    private final String from;
    private final String subject;
    private final String body;
    private final Iterator<Recipient> remaining;
    private final LinkedList<Recipient> delivered = new LinkedList<>();
    private final LinkedList<Recipient> failed = new LinkedList<>();

    public MailMessageDelivery(final MailMessage msg, final Iterable<Recipient> recipients) {
        this.from = msg.getFromEmail();
        this.remaining = recipients.iterator();
        this.subject = msg.getSubject();
        this.body = msg.getBody();
    }

    public MailMessageDelivery(final MailMessageDTO dto) {
        this.from = dto.getFrom();
        this.subject = dto.getSubject();
        this.body = dto.getBody();
        this.remaining = dto.getRecipients().stream()
                .map(email -> new Recipient(null, email))
                .collect(toList()).iterator();
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

    public void consumeRemainingRecipients(final Consumer<String> recipientConsumer) {
        while (remaining.hasNext()) {
            final Recipient recipient = remaining.next();

            try {
                recipientConsumer.accept(recipient.getEmail());
                delivered.add(recipient);
            } catch (Exception e) {
                failed.add(recipient);
                throw e;
            }
        }
    }

    public void consumeDeliveredRecipientIds(final Consumer<Set<Long>> consumer) {
        if (!delivered.isEmpty()) {
            try {
                consumer.accept(delivered.stream().map(Recipient::getId).filter(Objects::nonNull).collect(toSet()));
            } finally {
                delivered.clear();
            }
        }
    }

    public void consumeFailedRecipientIds(final Consumer<Set<Long>> consumer) {
        if (!failed.isEmpty()) {
            try {
                consumer.accept(failed.stream().map(Recipient::getId).filter(Objects::nonNull).collect(toSet()));
            } finally {
                failed.clear();
            }
        }
    }
}
