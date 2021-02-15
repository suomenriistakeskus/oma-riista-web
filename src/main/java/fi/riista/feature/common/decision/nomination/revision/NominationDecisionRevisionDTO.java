package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;

import javax.annotation.Nullable;
import java.util.List;

public class NominationDecisionRevisionDTO extends BaseEntityDTO<Long> {

    public static class AttachmentDTO implements Comparable<AttachmentDTO>{
        private final long id;
        private final Integer orderingNumber;
        private final String description;

        public AttachmentDTO(final long id,
                             final @Nullable Integer orderingNumber,
                             final @Nullable String description) {
            this.id = id;
            this.orderingNumber = orderingNumber;
            this.description = description;
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

        @Override
        public int compareTo(final AttachmentDTO other) {
            if ( other == null){
                return 1;
            }
            if (this.orderingNumber == null) {
                return other.orderingNumber == null ? 0 : -1;
            }
            if ( other.orderingNumber == null){
                return 1;
            }
            return this.orderingNumber.compareTo(other.orderingNumber);
        }
    }

    public static class ReceiverDTO {
        private final long id;
        private final String email;
        private final String name;
        private final NominationDecisionRevisionReceiver.ReceiverType receiverType;
        private final LocalDateTime sentDate;

        public ReceiverDTO(final NominationDecisionRevisionReceiver entity) {
            this.id = entity.getId();
            this.email = entity.getEmail();
            this.name = entity.getName();
            this.receiverType = entity.getReceiverType();
            this.sentDate = DateUtil.toLocalDateTimeNullSafe(entity.getSentDate());
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

        public NominationDecisionRevisionReceiver.ReceiverType getReceiverType() {
            return receiverType;
        }

        public LocalDateTime getSentDate() {
            return sentDate;
        }
    }

    private Long id;
    private Integer rev;
    private String externalId;
    private LocalDateTime lockedDate;
    private String lockedByUsername;
    private LocalDateTime scheduledPublishDate;
    private LocalDateTime publishDate;
    private boolean canTogglePosted;
    private boolean posted;
    private LocalDateTime postedByMailDate;
    private String postedByMailUsername;
    private List<AttachmentDTO> attachments;
    private List<ReceiverDTO> receivers;

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

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
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
