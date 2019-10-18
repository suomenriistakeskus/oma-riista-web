package fi.riista.feature.account.area.union;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class PersonalAreaUnionAuthorization extends AbstractEntityAuthorization<PersonalAreaUnion> {

    public enum Role {
        OWNER
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public PersonalAreaUnionAuthorization() {
        allowCRUD(PersonalAreaUnionAuthorization.Role.OWNER);
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final PersonalAreaUnion areaUnion,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(PersonalAreaUnionAuthorization.Role.OWNER,
                    () -> Objects.equals(F.getId(activePerson), F.getId(areaUnion.getPerson())));
        });
    }
}
