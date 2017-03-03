package fi.riista.feature.account.user;

import fi.riista.security.authorization.AbstractEntityAuthorization;
import org.springframework.stereotype.Component;

@Component
public class UserEntityAuthorization extends AbstractEntityAuthorization<SystemUser> {
    public UserEntityAuthorization() {
        allowCRUD(SystemUser.Role.ROLE_ADMIN);
    }
}
