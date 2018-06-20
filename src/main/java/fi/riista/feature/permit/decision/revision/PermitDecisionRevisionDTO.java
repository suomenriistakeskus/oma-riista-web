package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.HasID;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PermitDecisionRevisionDTO extends BaseEntityDTO<Long> {

    public static class AttachmentDTO {
        private final long id;
        private final Integer orderingNumber;
        private final String description;

        public AttachmentDTO(final PermitDecisionRevisionAttachment attachment) {
            this.id = attachment.getId();
            this.orderingNumber = attachment.getOrderingNumber();
            this.description = attachment.getDecisionAttachment().getDescription();
        }

        public long getId() {
            return id;
        }

        public Integer getOrderingNumber() {
            return orderingNumber;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class ReceiverDTO {
        private final long id;
        private final String email;
        private final String name;
        private final PermitDecisionRevisionReceiver.ReceiverType receiverType;
        private final LocalDateTime sentDate;

        public ReceiverDTO(final PermitDecisionRevisionReceiver entity) {
            this.id = entity.getId();
            this.email = entity.getEmail();
            this.name = entity.getName();
            this.receiverType = entity.getReceiverType();
            this.sentDate = toLocalDateTime(entity.getSentDate());
        }

        public long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public PermitDecisionRevisionReceiver.ReceiverType getReceiverType() {
            return receiverType;
        }

        public LocalDateTime getSentDate() {
            return sentDate;
        }
    }

    private Long id;
    private Integer rev;
    private LocalDateTime lockedDate;
    private String lockedByUsername;
    private LocalDateTime scheduledPublishDate;
    private LocalDateTime publishDate;
    private boolean postalByMail;
    private boolean canTogglePosted;
    private boolean posted;
    private LocalDateTime postedByMailDate;
    private String postedByMailUsername;
    private List<AttachmentDTO> attachments;
    private List<ReceiverDTO> receivers;

    public static PermitDecisionRevisionDTO create(
            final @Nullable PermitDecisionRevision revision, final String creatorName) {

        if (revision == null) {
            return null;
        }

        final PermitDecisionRevisionDTO dto = new PermitDecisionRevisionDTO();

        dto.setId(revision.getId());
        dto.setRev(revision.getConsistencyVersion());
        dto.setLockedDate(toLocalDateTime(revision.getLockedDate()));
        dto.setLockedByUsername(creatorName);
        dto.setScheduledPublishDate(toLocalDateTime(revision.getScheduledPublishDate()));
        dto.setPublishDate(toLocalDateTime(revision.getPublishDate()));
        dto.setPostalByMail(revision.isPostalByMail());
        dto.setCanTogglePosted(!revision.isCancelled() && revision.getScheduledPublishDate().isBeforeNow());
        dto.setPosted(revision.getPostedByMailDate() != null);
        dto.setPostedByMailDate(toLocalDateTime(revision.getPostedByMailDate()));
        dto.setPostedByMailUsername(revision.getPostedByMailUsername());

        dto.setAttachments(revision.getSortedAttachments().stream()
                .sorted(PermitDecisionRevisionAttachment.ATTACHMENT_COMPARATOR)
                .map(AttachmentDTO::new)
                .collect(Collectors.toList()));

        dto.setReceivers(revision.getReceivers().stream()
                .sorted(Comparator.comparing(HasID::getId))
                .map(ReceiverDTO::new)
                .collect(Collectors.toList()));

        return dto;
    }

    private static LocalDateTime toLocalDateTime(final DateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDateTime() : null;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return this.rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public LocalDateTime getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(final LocalDateTime lockedDate) {
        this.lockedDate = lockedDate;
    }

    public String getLockedByUsername() {
        return lockedByUsername;
    }

    public void setLockedByUsername(final String lockedByUsername) {
        this.lockedByUsername = lockedByUsername;
    }

    public LocalDateTime getScheduledPublishDate() {
        return scheduledPublishDate;
    }

    public void setScheduledPublishDate(final LocalDateTime scheduledPublishDate) {
        this.scheduledPublishDate = scheduledPublishDate;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(final LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public boolean isPostalByMail() {
        return postalByMail;
    }

    public void setPostalByMail(final boolean postalByMail) {
        this.postalByMail = postalByMail;
    }

    public boolean isCanTogglePosted() {
        return canTogglePosted;
    }

    public void setCanTogglePosted(final boolean canTogglePosted) {
        this.canTogglePosted = canTogglePosted;
    }

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(final boolean posted) {
        this.posted = posted;
    }

    public LocalDateTime getPostedByMailDate() {
        return postedByMailDate;
    }

    public void setPostedByMailDate(final LocalDateTime postedByMailDate) {
        this.postedByMailDate = postedByMailDate;
    }

    public String getPostedByMailUsername() {
        return postedByMailUsername;
    }

    public void setPostedByMailUsername(final String postedByMailUsername) {
        this.postedByMailUsername = postedByMailUsername;
    }

    public List<AttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<AttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public List<ReceiverDTO> getReceivers() {
        return receivers;
    }

    public void setReceivers(final List<ReceiverDTO> receivers) {
        this.receivers = receivers;
    }
}
