package fi.riista.feature.news;

import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUserPrivilege.PUBLISH_FRONTPAGE_NEWS;

@Component
public class NewsAuthorization extends AbstractEntityAuthorization<News> {

    enum Role {
        FRONTPAGE_NEWS_PUBLISHER;
    }
    public NewsAuthorization() {
        allowCRUD(ROLE_ADMIN, Role.FRONTPAGE_NEWS_PUBLISHER);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final News news,
                                   @Nonnull final UserInfo userInfo) {
        collector.addAuthorizationRole(Role.FRONTPAGE_NEWS_PUBLISHER, () ->
                userInfo.isModerator() && userInfo.hasPrivilege(PUBLISH_FRONTPAGE_NEWS));
    }

}
