package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;

@Component
public class HarvestReportAuthorization extends AbstractEntityAuthorization {

    private enum Role {
        CAN_READ,
        CAN_EDIT,
        CAN_DELETE,
        PERMIT_RHY_COORDINATOR,
        REPORT_RHY_COORDINATOR
    }

    @Resource
    private UserRepository userRepository;

    @Resource
    private HarvestReportRepository harvestReportRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public HarvestReportAuthorization() {
        super("HarvestReport");

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, Role.CAN_READ, Role.PERMIT_RHY_COORDINATOR, Role.REPORT_RHY_COORDINATOR);

        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, ROLE_USER);
        allow(UPDATE, Role.CAN_EDIT);
        allow(DELETE, Role.CAN_DELETE);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {
        final SystemUser user = getSystemUser(userInfo);
        final HarvestReport report = getHarvestReport(target);
        final boolean isNew = report == null;// should never be null, because it would mean it is a new entity and that is allowed in constructor

        if (user == null) {
            return;
        }

        if (user.isModeratorOrAdmin()) {
            collector.addAuthorizationRole(Role.CAN_EDIT,
                    () -> isNew || report.canModeratorEdit() || !report.getTransitions(user).isEmpty());

            collector.addAuthorizationRole(Role.CAN_DELETE,
                    () -> isNew || report.canModeratorDelete() || !report.getTransitions(user).isEmpty());

        } else {
            if (isAuthorOrShooter(user.getPerson(), report) || isContactPerson(report, user.getPerson())) {
                collector.addAuthorizationRole(Role.CAN_READ);

                if (isNew || report.canEdit(user)) {
                    collector.addAuthorizationRole(Role.CAN_EDIT);
                }

                if (isNew || report.canDelete(user)) {
                    collector.addAuthorizationRole(Role.CAN_DELETE);
                }
            }

            checkIsCoordinator(collector, report);

            collector.addAuthorizationRole(Role.PERMIT_RHY_COORDINATOR, () -> isPermitRhyCoordinator(report));
        }
    }

    private void checkIsCoordinator(final AuthorizationTokenCollector collector, final HarvestReport report) {
        collector.addAuthorizationRole(Role.REPORT_RHY_COORDINATOR, () -> {
            for (final Harvest harvest : report.getHarvests()) {
                if (userAuthorizationHelper.isCoordinator(harvest.getRhy())) {
                    return true;
                }
            }

            return false;
        });
    }

    private boolean isPermitRhyCoordinator(HarvestReport report) {
        return report.getHarvestPermit() != null
                && userAuthorizationHelper.isCoordinator(report.getHarvestPermit().getRhy());
    }

    private static boolean isContactPerson(HarvestReport report, Person person) {
        return report.getHarvestPermit() != null && report.getHarvestPermit().hasContactPerson(person);
    }

    private static boolean isAuthorOrShooter(Person person, HarvestReport report) {
        return Objects.equals(person, report.getAuthor());
    }

    private HarvestReport getHarvestReport(EntityAuthorizationTarget target) {
        Long reportId = (Long) target.getAuthorizationTargetId();
        if (reportId != null) {
            return harvestReportRepository.getOne(reportId);
        }
        return null;
    }

    private SystemUser getSystemUser(UserInfo userInfo) {
        if (userInfo.getUserId() == null) {
            return null;
        }
        return userRepository.findOne(userInfo.getUserId());
    }


    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class<?>[]{
                HarvestReport.class,
                HarvestReportDTOBase.class,
                HarvestReportForListPermitDTO.class,
                HarvestReportSingleHarvestDTO.class
        };
    }
}
