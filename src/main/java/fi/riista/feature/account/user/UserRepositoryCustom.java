package fi.riista.feature.account.user;

import fi.riista.feature.common.entity.LifecycleEntity;

import java.util.Map;

public interface UserRepositoryCustom {
    void deactivateAccountsForDeceased();

    boolean isModeratorOrAdmin(long userId);

    Map<Long, String> getModeratorFullNames(Iterable<? extends LifecycleEntity<Long>> entities);
}
