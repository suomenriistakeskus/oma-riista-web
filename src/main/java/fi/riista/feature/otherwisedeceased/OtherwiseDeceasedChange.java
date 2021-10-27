package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.entity.BaseEntity;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class OtherwiseDeceasedChange extends BaseEntity<Long> {

    public enum ChangeType {
        CREATE,
        MODIFY,
        DELETE,
        RESTORE,
        DELETE_ATTACHMENT
    }

    public static final String ID_COLUMN_NAME = "otherwise_deceased_change_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private OtherwiseDeceased otherwiseDeceased;

    @NotNull
    @Column(nullable = false)
    private DateTime pointOfTime;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeType changeType;

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
    OtherwiseDeceasedChange() {
    }

    public OtherwiseDeceasedChange(@Nonnull final OtherwiseDeceased otherwiseDeceased,
                                   @Nonnull final DateTime pointOfTime,
                                   final long userId,
                                   @Nonnull final ChangeType changeType,
                                   final String reasonForChange) {
        this.otherwiseDeceased = requireNonNull(otherwiseDeceased);
        this.pointOfTime = requireNonNull(pointOfTime);
        this.userId = userId;
        this.changeType = requireNonNull(changeType);
        this.reasonForChange = reasonForChange;
    }

    // Methods

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public OtherwiseDeceased getOtherwiseDeceased() {
        return otherwiseDeceased;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public Long getUserId() {
        return userId;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }
}
