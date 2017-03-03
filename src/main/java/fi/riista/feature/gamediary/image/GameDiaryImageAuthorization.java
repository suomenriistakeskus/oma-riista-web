package fi.riista.feature.gamediary.image;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;

@Component
public class GameDiaryImageAuthorization extends AbstractEntityAuthorization<GameDiaryImage> {

    private enum Role {
        GAME_DIARY_OWNER
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public GameDiaryImageAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, Role.GAME_DIARY_OWNER);
        allow(EntityPermission.CREATE, ROLE_ADMIN, Role.GAME_DIARY_OWNER);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, Role.GAME_DIARY_OWNER);
        allow(EntityPermission.DELETE, ROLE_ADMIN, Role.GAME_DIARY_OWNER);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final GameDiaryImage image,
                                   @Nonnull final UserInfo userInfo) {

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(Role.GAME_DIARY_OWNER, () -> isOwner(image, activePerson));
        });
    }

    private static boolean isOwner(final GameDiaryImage image, final Person person) {
        return image.getHarvest() != null && image.getHarvest().isAuthor(person) ||
                image.getObservation() != null && image.getObservation().isAuthor(person);

    }
}
