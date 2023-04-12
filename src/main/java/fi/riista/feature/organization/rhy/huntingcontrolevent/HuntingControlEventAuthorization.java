package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.rhy.RhyAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class HuntingControlEventAuthorization extends AbstractEntityAuthorization<HuntingControlEvent> {

    @Resource
    private RhyAuthorizationHelper helper;

    public HuntingControlEventAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, METSASTYKSENVALVOJA);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HuntingControlEvent huntingControlEvent,
                                   @Nonnull final UserInfo userInfo) {

        helper.collectAllRhyRoles(huntingControlEvent.getRhy(), collector, userInfo);
    }
}
