package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
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
public class HuntingControlAttachmentAuthorization extends AbstractEntityAuthorization<HuntingControlAttachment>  {

    @Resource
    protected UserAuthorizationHelper userAuthorizationHelper;

    public HuntingControlAttachmentAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, METSASTYKSENVALVOJA);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HuntingControlAttachment huntingControlAttachment,
                                   @Nonnull final UserInfo userInfo) {

        final Riistanhoitoyhdistys rhy = huntingControlAttachment.getHuntingControlEvent().getRhy();
        collector.addAuthorizationRole(TOIMINNANOHJAAJA, () -> userAuthorizationHelper.isCoordinator(rhy));
        collector.addAuthorizationRole(METSASTYKSENVALVOJA, () -> userAuthorizationHelper.isGameWarden(rhy) && isEventInspector(huntingControlAttachment, userInfo));
    }

    private boolean isEventInspector(final HuntingControlAttachment attachment, final UserInfo userInfo) {
        return userAuthorizationHelper.getPerson(userInfo)
                .map(activePerson -> attachment.getHuntingControlEvent().getInspectors().contains(activePerson))
                .orElse(false);
    }
}
