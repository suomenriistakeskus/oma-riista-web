package fi.riista.feature.huntingclub.permit.harvestreport;

import com.google.common.collect.Sets;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Role.CONTACT_PERSON_FOR_PERMIT;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;

@Component
public class MooseHarvestReportAuthorization extends AbstractEntityAuthorization {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    public MooseHarvestReportAuthorization() {
        super("mooseHarvestReport");

        allow(READ,   ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, CONTACT_PERSON_FOR_PERMIT, SEURAN_JASEN);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, CONTACT_PERSON_FOR_PERMIT);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, CONTACT_PERSON_FOR_PERMIT);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, CONTACT_PERSON_FOR_PERMIT);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        final Person person = userAuthorizationHelper.getPerson(userInfo);
        if (person != null) {
            final HarvestPermit permit = getPermit(target);
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () -> userAuthorizationHelper.isClubContact(permit.getPermitHolder(), person));
            collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA, () -> isLeaderInPermitHolderClubGroup(permit, person));
            collector.addAuthorizationRole(HarvestPermitAuthorization.Role.CONTACT_PERSON_FOR_PERMIT, () -> permit.hasContactPerson(person));
        }
    }

    private boolean isLeaderInPermitHolderClubGroup(HarvestPermit permit, Person person) {
        final List<HuntingClubGroup> groups = huntingClubGroupRepository.findByPermitAndClubs(permit,
                Sets.newHashSet(permit.getPermitHolder()));
        return userAuthorizationHelper.hasAnyOfRolesInOrganisations(groups, person,
                EnumSet.of(OccupationType.RYHMAN_METSASTYKSENJOHTAJA));
    }

    private HarvestPermit getPermit(final EntityAuthorizationTarget target) {
        return target.getAuthorizationTargetId() != null
                ? findPermitByEntity(target)
                : findPermitByDto(target);
    }

    private HarvestPermit findPermitByEntity(EntityAuthorizationTarget target) {
        final Long id = (Long) target.getAuthorizationTargetId();
        return mooseHarvestReportRepository.getOne(id).getSpeciesAmount().getHarvestPermit();
    }

    private HarvestPermit findPermitByDto(EntityAuthorizationTarget target) {
        final Long harvestPermitId = target.getAuthorizationTarget(MooseHarvestReportDTO.class).getHarvestPermitId();
        return harvestPermitRepository.getOne(harvestPermitId);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[]{MooseHarvestReport.class, MooseHarvestReportDTO.class};
    }
}
