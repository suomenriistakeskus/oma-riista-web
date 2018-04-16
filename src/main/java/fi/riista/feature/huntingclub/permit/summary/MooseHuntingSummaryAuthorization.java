package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryAuthorization.Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT;
import static fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryAuthorization.Permission.DELETE_MOOSE_DATA_CARD_ORIGINATED;
import static fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryAuthorization.Permission.UPDATE_MOOSE_DATA_CARD_ORIGINATED;
import static fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryAuthorization.Role.PERMIT_CONTACT_PERSON;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class MooseHuntingSummaryAuthorization extends AbstractEntityAuthorization<MooseHuntingSummary> {

    public enum Permission {
        CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT,
        UPDATE_MOOSE_DATA_CARD_ORIGINATED,
        DELETE_MOOSE_DATA_CARD_ORIGINATED,
    }

    public enum Role {
        // Luvan yhteyshenkilö
        PERMIT_CONTACT_PERSON
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public MooseHuntingSummaryAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
        allowCRUD(PERMIT_CONTACT_PERSON);

        allow(EntityPermission.READ, SEURAN_JASEN);

        allow(CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT, ROLE_ADMIN, ROLE_MODERATOR);
        allow(UPDATE_MOOSE_DATA_CARD_ORIGINATED, ROLE_ADMIN, ROLE_MODERATOR);
        allow(DELETE_MOOSE_DATA_CARD_ORIGINATED, ROLE_ADMIN, ROLE_MODERATOR);
    }

    // Partner verification done in overridden method to ensure that all users (including
    // admins/moderators) are blocked from attempting to create summaries for fake partners.
    @Override
    public boolean hasPermission(
            @Nonnull final MooseHuntingSummary summary,
            @Nonnull final Enum<?> permission,
            @Nonnull final Authentication authentication) {
        return isVerifiedPartner(summary) && super.hasPermission(summary, permission, authentication);
    }

    private static boolean isVerifiedPartner(final MooseHuntingSummary summary) {
        return isVerifiedPartner(summary.getHarvestPermit(), summary.getClub());
    }

    private static boolean isVerifiedPartner(final HarvestPermit permit, final HuntingClub club) {
        return F.getUniqueIds(permit.getPermitPartners()).contains(club.getId());
    }

    @Override
    protected void authorizeTarget(
            @Nonnull final AuthorizationTokenCollector collector,
            @Nonnull final MooseHuntingSummary mooseHuntingSummary,
            @Nonnull final UserInfo userInfo) {
        final HuntingClub club = mooseHuntingSummary.getClub();
        final HarvestPermit permit = mooseHuntingSummary.getHarvestPermit();

        if (club == null || permit == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO,
                    () -> userAuthorizationHelper.isClubContact(club, activePerson));

            collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA,
                    () -> userAuthorizationHelper.isLeaderOfSomePermitHuntingGroup(activePerson, permit, club));

            collector.addAuthorizationRole(SEURAN_JASEN,
                    () -> userAuthorizationHelper.isClubMember(club, activePerson));

            collector.addAuthorizationRole(PERMIT_CONTACT_PERSON,
                    () -> permit.hasContactPerson(activePerson));
        });
    }
}
