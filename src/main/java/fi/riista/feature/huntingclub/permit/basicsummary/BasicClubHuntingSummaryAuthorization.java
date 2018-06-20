package fi.riista.feature.huntingclub.permit.basicsummary;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
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
import static fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryAuthorization.Role.PERMIT_CONTACT_PERSON;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class BasicClubHuntingSummaryAuthorization extends AbstractEntityAuthorization<BasicClubHuntingSummary> {

    public enum Permission {
        CREATE_MODERATOR_OVERRIDDEN_SUMMARY,
        UPDATE_MODERATOR_OVERRIDDEN_SUMMARY,
        DELETE_MODERATOR_OVERRIDDEN_SUMMARY
    }

    public enum Role {
        // Luvan yhteyshenkilö
        PERMIT_CONTACT_PERSON
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public BasicClubHuntingSummaryAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, PERMIT_CONTACT_PERSON);

        allow(EntityPermission.READ, SEURAN_JASEN);

        allow(Permission.CREATE_MODERATOR_OVERRIDDEN_SUMMARY, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.UPDATE_MODERATOR_OVERRIDDEN_SUMMARY, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.DELETE_MODERATOR_OVERRIDDEN_SUMMARY, ROLE_ADMIN, ROLE_MODERATOR);
    }

    // Partner verification done in overridden method to ensure that all users (including
    // admins/moderators) are blocked from attempting to create summaries for fake partners.
    @Override
    public boolean hasPermission(
            @Nonnull final BasicClubHuntingSummary summary,
            @Nonnull final Enum<?> permission,
            @Nonnull final Authentication authentication) {
        return isVerifiedPartner(summary) && super.hasPermission(summary, permission, authentication);
    }

    private static boolean isVerifiedPartner(final BasicClubHuntingSummary summary) {
        return isVerifiedPartner(summary.getSpeciesAmount().getHarvestPermit(), summary.getClub());
    }

    private static boolean isVerifiedPartner(final HarvestPermit permit, final HuntingClub club) {
        return F.getUniqueIds(permit.getPermitPartners()).contains(club.getId());
    }

    @Override
    protected void authorizeTarget(
            @Nonnull final AuthorizationTokenCollector collector,
            @Nonnull final BasicClubHuntingSummary basicClubHuntingSummary,
            @Nonnull final UserInfo userInfo) {
        final HuntingClub club = basicClubHuntingSummary.getClub();
        final HarvestPermitSpeciesAmount speciesAmount = basicClubHuntingSummary.getSpeciesAmount();

        if (club == null && speciesAmount == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            final HarvestPermit permit = speciesAmount.getHarvestPermit();

            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(club, activePerson));

            collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA, () ->
                    userAuthorizationHelper.isLeaderOfSomePermitHuntingGroup(activePerson, permit, club, speciesAmount.getGameSpecies(), speciesAmount.resolveHuntingYear()));

            collector.addAuthorizationRole(SEURAN_JASEN, () ->
                    userAuthorizationHelper.isClubMember(club, activePerson));

            collector.addAuthorizationRole(PERMIT_CONTACT_PERSON, () -> permit.hasContactPerson(activePerson));
        });
    }
}
