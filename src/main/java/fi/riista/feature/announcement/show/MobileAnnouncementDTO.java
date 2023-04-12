package fi.riista.feature.announcement.show;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.abbreviate;

import javax.annotation.Nonnull;
import org.joda.time.LocalDateTime;

public class MobileAnnouncementDTO {
    private final Long id;
    private final Integer rev;
    private final LocalDateTime pointOfTime;
    private final MobileAnnouncementSenderDTO sender;
    private final String subject;
    private final String body;
    private final boolean abbreviated;

    public MobileAnnouncementDTO copyAbbreviated(final int maxSubjectLength, final int maxBodyLength) {
        final String newSubject = abbreviate(this.subject, maxSubjectLength);
        final String newBody = abbreviate(this.body, maxBodyLength);
        final boolean abbreviated = this.abbreviated
                || newSubject.length() != this.subject.length()
                || newBody.length() != this.body.length();
        return new MobileAnnouncementDTO(id, rev, pointOfTime, sender, newSubject, newBody, abbreviated);
    }

    public MobileAnnouncementDTO(final @Nonnull Long id,
                                 final @Nonnull Integer rev,
                                 final @Nonnull LocalDateTime pointOfTime,
                                 final @Nonnull MobileAnnouncementSenderDTO sender,
                                 final @Nonnull String subject,
                                 final @Nonnull String body) {
        this(id, rev, pointOfTime, sender, subject, body, false);
    }

    public MobileAnnouncementDTO(final @Nonnull Long id,
                                 final @Nonnull Integer rev,
                                 final @Nonnull LocalDateTime pointOfTime,
                                 final @Nonnull MobileAnnouncementSenderDTO sender,
                                 final @Nonnull String subject,
                                 final @Nonnull String body,
                                 final boolean abbreviated) {
        this.id = requireNonNull(id);
        this.rev = requireNonNull(rev);
        this.pointOfTime = requireNonNull(pointOfTime);
        this.sender = requireNonNull(sender);
        this.subject = requireNonNull(subject);
        this.body = requireNonNull(body);
        this.abbreviated = abbreviated;
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

    public boolean isAbbreviated() {
        return abbreviated;
    }
}
