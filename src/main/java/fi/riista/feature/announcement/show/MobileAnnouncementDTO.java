package fi.riista.feature.announcement.show;

import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class MobileAnnouncementDTO {
    private final Long id;
    private final Integer rev;
    private final LocalDateTime pointOfTime;
    private final MobileAnnouncementSenderDTO sender;
    private final String subject;
    private final String body;

    public MobileAnnouncementDTO(final @Nonnull Long id,
                                 final @Nonnull Integer rev,
                                 final @Nonnull LocalDateTime pointOfTime,
                                 final @Nonnull MobileAnnouncementSenderDTO sender,
                                 final @Nonnull String subject,
                                 final @Nonnull String body) {
        this.id = requireNonNull(id);
        this.rev = requireNonNull(rev);
        this.pointOfTime = requireNonNull(pointOfTime);
        this.sender = requireNonNull(sender);
        this.subject = requireNonNull(subject);
        this.body = requireNonNull(body);
    }

    public Long getId() {
        return id;
    }

    public Integer getRev() {
        return rev;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public MobileAnnouncementSenderDTO getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
