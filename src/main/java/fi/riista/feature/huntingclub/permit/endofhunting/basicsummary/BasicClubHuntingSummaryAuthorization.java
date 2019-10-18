package fi.riista.feature.huntingclub.permit.endofhunting.basicsummary;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryAuthorization.Role.PERMIT_CONTACT_PERSON;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class BasicClubHuntingSummaryAuthorization extends AbstractEntityAuthorization<BasicClubHuntingSummary> {

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
