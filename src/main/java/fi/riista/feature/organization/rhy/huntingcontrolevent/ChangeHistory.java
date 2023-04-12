package fi.riista.feature.organization.rhy.huntingcontrolevent;

import com.google.common.base.MoreObjects;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class ChangeHistory {

    public enum ChangeType {
        CREATE,
        MODIFY,
        DELETE,
        RESTORE,
        ADD_ATTACHMENTS,
        DELETE_ATTACHMENT,
        CHANGE_STATUS
    }

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(nullable = false)
    private DateTime pointOfTime;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeHistory.ChangeType changeType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String reasonForChange;

    // Validation constraints

    @AssertTrue
    public boolean isReasonForChangeNullOrNonBlank() {
        return reasonForChange == null || StringUtils.hasText(reasonForChange);
    }

    // Constructors

    /** For Hibernate */
    protected ChangeHistory() {
    }

    public ChangeHistory(@Nonnull final DateTime pointOfTime,
                         final long userId,
                         @Nonnull final ChangeType changeType,
                         final String reasonForChange) {
        this.pointOfTime = requireNonNull(pointOfTime);
        this.userId = userId;
        this.changeType = requireNonNull(changeType);
        this.reasonForChange = reasonForChange;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pointOfTime", pointOfTime)
                .add("userId", userId)
                .add("changeType", changeType)
                .add("reasonForChange", reasonForChange)
                .toString();
    }

    // Accessors

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public ChangeHistory.ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(final ChangeHistory.ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(final String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }
}
