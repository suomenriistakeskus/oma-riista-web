package fi.riista.feature.announcement.show;

import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class MobileAnnouncementDTO extends BaseEntityDTO<Long> {
    public static MobileAnnouncementDTO create(@Nonnull final Announcement announcement,
                                               @Nonnull final MobileAnnouncementSenderDTO sender) {
        Objects.requireNonNull(announcement);
        Objects.requireNonNull(sender);

        final MobileAnnouncementDTO dto = new MobileAnnouncementDTO();

        DtoUtil.copyBaseFields(announcement, dto);

        dto.setPointOfTime(DateUtil.toLocalDateTimeNullSafe(announcement.getLifecycleFields().getCreationTime()));
        dto.setBody(announcement.getBody());
        dto.setSubject(announcement.getSubject());
        dto.setSender(sender);

        return dto;
    }

    public static MobileAnnouncementSenderDTO createSender(@Nonnull final Organisation fromOrganisation,
                                                           @Nonnull final LocalisedString senderType,
                                                           final String senderFullName) {
        Objects.requireNonNull(fromOrganisation);
        Objects.requireNonNull(senderType);

        final MobileAnnouncementSenderDTO sender = new MobileAnnouncementSenderDTO();

        sender.setOrganisation(fromOrganisation.getNameLocalisation().asMap());
        sender.setTitle(senderType.asMap());
        sender.setFullName(senderFullName != null ? senderFullName : "");

        return sender;
    }

    public static class MobileAnnouncementSenderDTO {
        private Map<String, String> organisation;
        private Map<String, String> title;
        private String fullName;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(final String fullName) {
            this.fullName = fullName;
        }

        public Map<String, String> getTitle() {
            return title;
        }

        public void setTitle(final Map<String, String> title) {
            this.title = title;
        }

        public Map<String, String> getOrganisation() {
            return organisation;
        }

        public void setOrganisation(final Map<String, String> organisation) {
            this.organisation = organisation;
        }
    }

    private Long id;
    private Integer rev;
    private LocalDateTime pointOfTime;
    private MobileAnnouncementSenderDTO sender;
    private String subject;
    private String body;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public MobileAnnouncementSenderDTO getSender() {
        return sender;
    }

    public void setSender(final MobileAnnouncementSenderDTO sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }
}
