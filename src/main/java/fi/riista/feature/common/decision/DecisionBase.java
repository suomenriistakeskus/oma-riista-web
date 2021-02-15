package fi.riista.feature.common.decision;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.persistence.Transient;
import java.util.EnumSet;
import java.util.Objects;

public interface DecisionBase {

    SystemUser getHandler();

    DecisionStatus getStatus();

    void setStatus(DecisionStatus status);

    DateTime getLockedDate();

    void setLockedDate(DateTime lockedDate);

    void assertAllowedToLock();

    @Transient
    default void assertStatus(final DecisionStatus allowed) {
        assertStatus(EnumSet.of(allowed));
    }

    @Transient
    default void assertStatus(final EnumSet<DecisionStatus> allowed) {
        if (!allowed.contains(getStatus())) {
            throw new IllegalStateException(
                    String.format("status should be %s was %s", allowed, getStatus()));
        }
    }

    @Transient
    default void assertHandler(final SystemUser currentUser) {
        if (getHandler() == null) {
            throw new IllegalStateException("Handler is null");
        }
        if (!isHandler(currentUser)) {
            throw new IllegalStateException("Handler is not same as current user");
        }
    }

    @Transient
    default boolean isHandler(final @Nonnull SystemUser currentUser) {
        return Objects.equals(F.getId(getHandler()), F.getId(currentUser));
    }

    @Transient
    default void assertEditableBy(final @Nonnull SystemUser currentUser) {
        assertStatus(DecisionStatus.DRAFT);
        assertHandler(currentUser);
    }

    @Transient
    default void setStatusDraft() {
        assertStatus(EnumSet.of(DecisionStatus.LOCKED, DecisionStatus.PUBLISHED));

        setStatus(DecisionStatus.DRAFT);
    }

    @Transient
    default void setStatusPublished() {
        assertStatus(EnumSet.of(DecisionStatus.LOCKED));

        setStatus(DecisionStatus.PUBLISHED);
    }

    @Transient
    default void setStatusLocked() {
        assertStatus(EnumSet.of(DecisionStatus.DRAFT));

        assertAllowedToLock();

        setStatus(DecisionStatus.LOCKED);
        setLockedDate(DateUtil.now());
    }

}
