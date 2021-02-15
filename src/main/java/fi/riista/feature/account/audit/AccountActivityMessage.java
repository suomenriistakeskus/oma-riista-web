package fi.riista.feature.account.audit;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class AccountActivityMessage extends BaseEntity<Long> {
    public enum ActivityType {
        LOGIN_SUCCESS,
        LOGIN_FAILRE,
        LOGOUT,
        PASSWORD_CHANGE,
        PASSWORD_RESET,
        PASSWORD_RESET_REQUESTED,
        PDF_HUNTER_CARD,
        PDF_FOREIGN_CERTIFICATE,
        PDF_HUNTER_PAYMENT,
        VTJ
    }

    private Long id;

    @NotNull
    @Column(nullable = false, updatable = false)
    private DateTime creationTime;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    //an IPV6 address max length is 39 characters
    @Size(min = 0, max = 39)
    @Column(length = 39)
    private String ipAddress;

    @Size(max = 255)
    @Column
    private String username;

    @Column
    private Long userId;

    @Column(columnDefinition = "text")
    private String exceptionMessage;

    @Column(columnDefinition = "text")
    private String message;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "message_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public AccountActivityMessage() {
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String remoteAddr) {
        this.ipAddress = remoteAddr;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @PrePersist
    protected void prePersist() {
        creationTime = DateUtil.now();
    }
}
