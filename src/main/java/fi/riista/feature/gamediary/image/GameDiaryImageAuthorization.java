package fi.riista.feature.gamediary.image;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;

@Component
public class GameDiaryImageAuthorization extends AbstractEntityAuthorization {

    private enum Role {
        GAME_DIARY_OWNER
    }

    @Resource
    private UserRepository userRepository;

    @Resource
    private GameDiaryImageRepository gameDiaryImageRepository;

    public GameDiaryImageAuthorization() {
        super("GameDiaryImage");

        allow(READ,   ROLE_ADMIN, Role.GAME_DIARY_OWNER);
        allow(CREATE, ROLE_ADMIN, Role.GAME_DIARY_OWNER);
        allow(UPDATE, ROLE_ADMIN, Role.GAME_DIARY_OWNER);
        allow(DELETE, ROLE_ADMIN, Role.GAME_DIARY_OWNER);
    }

    @Override
    protected void authorizeTarget(
            AuthorizationTokenCollector collector, final EntityAuthorizationTarget target, final UserInfo userInfo) {

        collector.addAuthorizationRole(Role.GAME_DIARY_OWNER, () -> isOwnerOfTarget(target, userInfo));
    }

    private boolean isOwnerOfTarget(final EntityAuthorizationTarget target, final UserInfo userInfo) {
        final Person person = getAuthenticatedPerson(userInfo);

        if (person == null) {
            return false;
        }

        final GameDiaryImage image = Optional.ofNullable((Long) target.getAuthorizationTargetId())
                .map(gameDiaryImageRepository::findOne)
                .orElseGet(() -> target.getAuthorizationTarget(GameDiaryImage.class));

        if (image != null) {
            if (image.getHarvest() != null) {
                return image.getHarvest().isAuthor(person);
            }
            if (image.getObservation() != null) {
                return image.getObservation().isAuthor(person);
            }
        }

        return false;
    }

    private Person getAuthenticatedPerson(final UserInfo userInfo) {
        return Optional.ofNullable(userInfo.getUserId())
                .map(userRepository::findOne)
                .map(SystemUser::getPerson)
                .orElse(null);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { GameDiaryImage.class };
    }

}
