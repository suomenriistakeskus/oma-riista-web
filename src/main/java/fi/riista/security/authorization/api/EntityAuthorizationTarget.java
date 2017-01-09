package fi.riista.security.authorization.api;

import java.io.Serializable;

public interface EntityAuthorizationTarget {
    // Required
    String getAuthorizationTargetName();

    // Optional
    Serializable getAuthorizationTargetId();

    // Optional
    Class<?> getAuthorizationTargetClass();

    boolean is(Class<?> type);

    /**
     * Return target as given type, if available.
     *
     * @param type
     * @param <P>
     * @return null, if permission target entity is not available.
     */
    <P> P getAuthorizationTarget(Class<P> type);
}
