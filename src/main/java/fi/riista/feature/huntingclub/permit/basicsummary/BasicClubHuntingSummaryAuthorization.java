package fi.riista.feature.huntingclub.permit.basicsummary;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.permit.partner.DeerHuntingPermitPartner;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Optional;
import java.util.function.BiConsumer;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryAuthorization.Role.PERMIT_CONTACT_PERSON;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;

@Component
public class BasicClubHuntingSummaryAuthorization extends AbstractEntityAuthorization {

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
    private BasicClubHuntingSummaryRepository summaryRepo;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public BasicClubHuntingSummaryAuthorization() {
        super("basicclubhuntingsummary");

        allow(READ,   ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, PERMIT_CONTACT_PERSON, SEURAN_JASEN);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, PERMIT_CONTACT_PERSON);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, PERMIT_CONTACT_PERSON);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, PERMIT_CONTACT_PERSON);

        allow(Permission.CREATE_MODERATOR_OVERRIDDEN_SUMMARY, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.UPDATE_MODERATOR_OVERRIDDEN_SUMMARY, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.DELETE_MODERATOR_OVERRIDDEN_SUMMARY, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class<?>[] { BasicClubHuntingSummary.class, DeerHuntingPermitPartner.class };
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(final EntityAuthorizationTarget target, final Object permission) {
        return hasPermission(target, permission, SecurityContextHolder.getContext().getAuthentication());
    }

    @Override
    public boolean hasPermission(
            final EntityAuthorizationTarget target,
            final Object permission,
            final Authentication authentication) {

        // Partner verification done in overridden method to ensure that all users (including
        // admins/moderators) are blocked from attempting to create summaries for fake partners.

        return isVerifiedPartner(target) && super.hasPermission(target, permission, authentication);
    }

    private static boolean isVerifiedPartner(final EntityAuthorizationTarget target) {
        return findPermitPartner(target).map(DeerHuntingPermitPartner::isVerifiedPartner).orElse(true);
    }

    @Override
    protected void authorizeTarget(
            final AuthorizationTokenCollector collector,
            final EntityAuthorizationTarget target,
            final UserInfo userInfo) {

        Optional.ofNullable(userAuthorizationHelper.getPerson(userInfo)).ifPresent(person -> {

            ifPermitAndClubResolved(target, (permit, club) -> {
                collector.addAuthorizationRole(SEURAN_YHDYSHENKILO,
                        () -> userAuthorizationHelper.isClubContact(club, person));

                collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA,
                        () -> userAuthorizationHelper.isLeaderOfSomePermitHuntingGroup(person, permit, club));

                collector.addAuthorizationRole(SEURAN_JASEN,
                        () -> userAuthorizationHelper.isClubMember(club, person));

                collector.addAuthorizationRole(PERMIT_CONTACT_PERSON, () -> permit.hasContactPerson(person));
            });
        });
    }

    private static Optional<DeerHuntingPermitPartner> findPermitPartner(final EntityAuthorizationTarget target) {
        return Optional.ofNullable(target.getAuthorizationTarget(DeerHuntingPermitPartner.class));
    }

    private static Optional<Long> findAuthorizationTargetId(final EntityAuthorizationTarget target) {
        return Optional.ofNullable(target.getAuthorizationTargetId()).map(Long.class::cast);
    }

    private void ifPermitAndClubResolved(
            final EntityAuthorizationTarget target, final BiConsumer<HarvestPermit, HuntingClub> consumer) {

        F.optionalFromSuppliers(
                () -> findPermitPartner(target),
                () -> findEntity(target)
                        .map(e -> new DeerHuntingPermitPartner(e.getSpeciesAmount().getHarvestPermit(), e.getClub())))
                .ifPresent(partner -> consumer.accept(partner.permit, partner.club));
    }

    private Optional<BasicClubHuntingSummary> findEntity(final EntityAuthorizationTarget target) {
        final BasicClubHuntingSummary entity = target.getAuthorizationTarget(BasicClubHuntingSummary.class);
        return entity != null ? Optional.of(entity) : findAuthorizationTargetId(target).map(summaryRepo::findOne);
    }

}
