package fi.riista.test;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.util.TransactionalTaskExecutor;
import org.hibernate.stat.Statistics;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// TODO The API and internal implementation of this class is still a work in progress e.g. to
// enable better usability for testing different permissions for different entities.
public class PermissionCheckContext {

    private final EntityManager entityManager;
    private final ActiveUserService activeUserService;
    private final TransactionalTaskExecutor txExecutor;

    private Enum<?> permission;
    private boolean expectedResult;

    private boolean joinToCurrentTransaction;
    private Integer exactQueryCount;
    private Integer maxQueryCount;

    public PermissionCheckContext(@Nonnull final EntityManager entityManager,
                                  @Nonnull final ActiveUserService activeUserService,
                                  @Nonnull final TransactionalTaskExecutor txExecutor) {

        this.entityManager = Objects.requireNonNull(entityManager, "entityManager is null");
        this.activeUserService = Objects.requireNonNull(activeUserService, "activeUserService is null");
        this.txExecutor = Objects.requireNonNull(txExecutor, "txExecutor is null");
    }

    public PermissionCheckContext withPermission(@Nonnull final Enum<?> permission) {
        this.permission = Objects.requireNonNull(permission);
        return this;
    }

    public PermissionCheckContext expect(final boolean expectedResult) {
        this.expectedResult = expectedResult;
        return this;
    }

    public PermissionCheckContext joinToCurrentTransaction() {
        this.joinToCurrentTransaction = true;
        return this;
    }

    public PermissionCheckContext expectQueryCount(final int exactQueryCount) {
        checkState(maxQueryCount == null, "exact query count must not be defined when max query count is already set");
        this.exactQueryCount = exactQueryCount;
        return this;
    }

    public PermissionCheckContext expectNumberOfQueriesAtMost(final int maxQueryCount) {
        checkState(exactQueryCount == null, "max queries must not be defined when exact query count is already set");
        this.maxQueryCount = maxQueryCount;
        return this;
    }

    public void apply(final BaseEntity<?> target) {
        if (joinToCurrentTransaction) {
            assertTrue(entityManager.isJoinedToTransaction());
            executeTest(target);

        } else {
            txExecutor.execute(() -> {
                // Persistent entity is refreshed into transaction.
                executeTest(target.isNew() ? target : entityManager.find(target.getClass(), target.getId()));
            });
        }
    }

    private void executeTest(final BaseEntity<?> target) {
        getStatistics().clear();
        checkPermissions(target, expectedResult);

        if (exactQueryCount != null) {
            HibernateStatisticsHelper.assertCurrentQueryCount(getStatistics(), exactQueryCount);
        } else if (maxQueryCount != null) {
            HibernateStatisticsHelper.assertCurrentQueryCountAtMost(getStatistics(), maxQueryCount);
        }
    }

    private void checkPermissions(final BaseEntity<?> entity, final boolean expectedResult) {
        final String className = entity.getClass().getSimpleName();

        final String errorMessageTemplate = expectedResult
                ? "User %s should have %s permission on %s instance,"
                : "User %s should not have %s permission on %s instance,";

        final String errorMessage =
                format(errorMessageTemplate, activeUserService.getActiveUsernameOrNull(), permission.name(), className);

        assertEquals(errorMessage, expectedResult, activeUserService.checkHasPermission(entity, permission));
    }

    private Statistics getStatistics() {
        return HibernateStatisticsHelper.getStatistics(entityManager);
    }
}
