package fi.riista.feature.account.registration;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.mail.token.EmailToken;
import fi.riista.util.DateUtil;
import fi.riista.validation.VetumaTransactionId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.DateTime;
import org.joda.time.ReadableDuration;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Access(value = AccessType.FIELD)
public class VetumaTransaction extends BaseEntity<String> {
    public static final String TRID_PATTERN_STRING = "^[0-9a-f]{19}$";
    public static final Pattern TRID_PATTERN = Pattern.compile(TRID_PATTERN_STRING);
    public static final int MAX_TRID_LENGTH = 19;

    private String id;

    @NotNull
    @Column(nullable = false, length = 1)
    @Convert(converter = VetumaTransactionStatusConverter.class)
    private VetumaTransactionStatus status;

    @NotNull
    @Column(nullable = false)
    private DateTime startTime;

    @Column
    private DateTime endTime;

    @Email
    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String email;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String emailToken;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemUser user;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String remoteAddress;

    @Size(max = 255)
    @Column
    private String responseSo;

    @Override
    @Id
    @VetumaTransactionId
    @Access(value = AccessType.PROPERTY)
    @Column(name = "vetuma_transaction_id", nullable = false)
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public VetumaTransaction() {
        super();
    }

    public VetumaTransaction(final EmailToken emailToken,
                             final HttpServletRequest request) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "").substring(0, MAX_TRID_LENGTH);
        this.status = VetumaTransactionStatus.INIT;
        this.startTime = DateUtil.now();
        this.email = Objects.requireNonNull(emailToken).getEmail();
        this.emailToken = Objects.requireNonNull(emailToken).getId();
        this.remoteAddress = request.getRemoteAddr();
    }

    public boolean isExpiredNow(final ReadableDuration duration) {
        return getStartTime().isBefore(DateUtil.now().minus(duration));
    }

    @Transient
    public void assertTransactionStatus(final VetumaTransactionStatus expected) {
        if (status != expected) {
            throw new InvalidRegistrationStatus(String.format(
                    "transaction status should be %s was %s",
                    expected, this.status));
        }
    }

    public void setStatusSuccess(final SystemUser user) {
        assertTransactionStatus(VetumaTransactionStatus.INIT);
        this.status = VetumaTransactionStatus.SUCCESS;
        this.endTime = DateUtil.now();
        this.user = user;
    }

    public void setStatusError() {
        assertTransactionStatus(VetumaTransactionStatus.INIT);
        this.status = VetumaTransactionStatus.ERROR;
        this.endTime = DateUtil.now();
    }

    public void setStatusTimeout() {
        assertTransactionStatus(VetumaTransactionStatus.INIT);
        this.status = VetumaTransactionStatus.TIMEOUT;
        this.endTime = DateUtil.now();
    }

    public void setStatusFinished() {
        assertTransactionStatus(VetumaTransactionStatus.SUCCESS);
        this.status = VetumaTransactionStatus.FINISHED;
    }

    public VetumaTransactionStatus getStatus() {
        return status;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailToken() {
        return emailToken;
    }

    public SystemUser getUser() {
        return user;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
