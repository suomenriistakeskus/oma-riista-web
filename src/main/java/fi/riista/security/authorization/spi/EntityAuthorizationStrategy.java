package fi.riista.security.authorization.spi;

import fi.riista.security.EntityPermission;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import org.springframework.security.core.Authentication;

public interface EntityAuthorizationStrategy {
    /**
     * Configuration string which can be used as
     * permission identifiers to hasPermission()
     */
    EntityPermission CREATE = EntityPermission.CREATE;
    EntityPermission READ = EntityPermission.READ;
    EntityPermission UPDATE = EntityPermission.UPDATE;
    EntityPermission DELETE = EntityPermission.DELETE;

    /**
     * Supported authorization target name.
     *
     * Entity target name is used in security expressions like:
     *      @PreAuthorize("hasPermission(#id, 'entityName', 'write')")
     */
    String getEntityName();

    /**
     * Supported authorization target class types.
     *
     * For example: User, UserDTO
     *
     * Target class is used in security expression like:
     *      @PreAuthorize("hasPermission(#dto, 'update')")
     */
    Class<?>[] getSupportedTypes();

    /**
     *
     * @param authorizationTarget
     * @param permission
     * @param authentication
     * @return
     */
    boolean hasPermission(EntityAuthorizationTarget authorizationTarget,
                          Object permission,
                          Authentication authentication);
}
