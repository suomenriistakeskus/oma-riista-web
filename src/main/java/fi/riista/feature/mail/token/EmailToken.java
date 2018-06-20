package fi.riista.feature.mail.token;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.util.DateUtil;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
public class EmailToken extends BaseEntity<String> {

    private String id;

    @NotNull
    @Column(nullable = false, length = 1)
    @Convert(converter = EmailTokenTypeConverter.class)
    private EmailTokenType tokenType;

    @NotNull
    @Column(nullable = false)
    private DateTime validFrom;

    @NotNull
    @Column(nullable = false)
    private DateTime validUntil;

    @Column
    private DateTime revokedAt;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String createRemoteAddress;

    @Size(max = 255)
    @Column(length = 255)
    private String revokeRemoteAddress;

    @Email
    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemUser user;

    public EmailToken() {
        super();
    }

    public EmailToken(final String tokenData,
                      final SystemUser user,
                      final EmailTokenType tokenType,
                      final DateTime validUntil,
                      final String remoteAddress,
                      final String email) {
        this();
        this.id = Objects.requireNonNull(tokenData);
        this.user = user;
        this.tokenType = Objects.requireNonNull(tokenType);
        this.validFrom = DateUtil.now();
        this.validUntil = Objects.requireNonNull(validUntil);
        this.createRemoteAddress = Objects.requireNonNull(remoteAddress);
        this.email = Objects.requireNonNull(email).trim().toLowerCase();
    }

    @Transient
    public boolean isValid(@Nonnull final DateTime now) {
        Objects.requireNonNull(now);
        return Range.closed(getValidFrom(), getValidUntil()).contains(now);
    }

    public void revoke(@Nonnull HttpServletRequest request) {
        Preconditions.checkState(this.revokedAt == null, "Already in revoked state");
        setRevokedAt(DateUtil.now());
        setRevokeRemoteAddress(request.getRemoteAddr());
    }

    @Transient
    @AssertTrue
    boolean isValidFromUntilOrderingValid() {
        return validFrom == null || validUntil == null || !validFrom.isAfter(validUntil);
    }

    @Override
    @Id
    @Size(min = 16, max = 255)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "token_data", nullable = false, length = 255)
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public EmailTokenType getTokenType() {
        return tokenType;
    }

    public DateTime getValidFrom() {
        return validFrom;
    }

    public DateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(final DateTime validUntil) {
        this.validUntil = validUntil;
    }

    public DateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(final DateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getCreateRemoteAddress() {
        return createRemoteAddress;
    }

    public String getRevokeRemoteAddress() {
        return revokeRemoteAddress;
    }

    public void setRevokeRemoteAddress(final String revokeRemoteAddress) {
        this.revokeRemoteAddress = revokeRemoteAddress;
    }

    public String getEmail() {
        return email;
    }

    public SystemUser getUser() {
        return user;
    }
}
