package fi.riista.feature.huntingclub.permit.harvestreport;

import com.google.common.collect.Sets;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Role.CONTACT_PERSON_FOR_PERMIT;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class MooseHarvestReportAuthorization extends AbstractEntityAuthorization<MooseHarvestReport> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    public MooseHarvestReportAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
        allowCRUD(CONTACT_PERSON_FOR_PERMIT);

        allow(EntityPermission.READ, SEURAN_JASEN);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final MooseHarvestReport mooseHarvestReport,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            final HarvestPermit permit = mooseHarvestReport.getSpeciesAmount().getHarvestPermit();

            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(permit.getPermitHolder(), activePerson));

            collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA, () ->
                    isLeaderInPermitHolderClubGroup(permit, activePerson));

            collector.addAuthorizationRole(HarvestPermitAuthorization.Role.CONTACT_PERSON_FOR_PERMIT, () ->
                    permit.hasContactPerson(activePerson));
        });
    }

    private boolean isLeaderInPermitHolderClubGroup(final HarvestPermit permit,
                                                    final Person person) {
        final List<HuntingClubGroup> groups = huntingClubGroupRepository.findByPermitAndClubs(permit,
                Sets.newHashSet(permit.getPermitHolder()));

        return userAuthorizationHelper.hasAnyOfRolesInOrganisations(groups, person,
                EnumSet.of(RYHMAN_METSASTYKSENJOHTAJA));
    }
}
