package fi.riista.security.authorization.api;

import java.io.Serializable;

public interface AuthorizationTargetFactory {
    EntityAuthorizationTarget create(Object targetDomainObject);

    EntityAuthorizationTarget create(String type, Serializable primaryKey);
}
