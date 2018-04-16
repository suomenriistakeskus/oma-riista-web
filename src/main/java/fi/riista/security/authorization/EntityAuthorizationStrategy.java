package fi.riista.security.authorization;

import fi.riista.feature.common.entity.BaseEntity;
import org.springframework.security.core.Authentication;

import javax.annotation.Nonnull;

public interface EntityAuthorizationStrategy<T extends BaseEntity> {
    Class<T> getEntityClass();

    boolean hasPermission(@Nonnull T target,
                          @Nonnull Enum<?> permission,
                          @Nonnull Authentication authentication);
}
