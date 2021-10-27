package fi.riista.feature.announcement.crud;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.security.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
public class AnnouncementSenderRoleService {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<AnnouncementSenderType> resolveRoleInOrganisation(final Organisation organisation,
                                                                      final SystemUser activeUser) {
        if (activeUser.isModeratorOrAdmin()) {
            return Optional.of(AnnouncementSenderType.RIISTAKESKUS);
        }

        return occupationRepository.findActiveByOrganisationAndPerson(organisation, activeUser.getPerson()).stream()
                .map(AnnouncementSenderRoleService::resolveSenderType)
                .filter(Objects::nonNull)
                .findAny();
    }

    private static AnnouncementSenderType resolveSenderType(final Occupation occupationType) {
        switch (occupationType.getOccupationType()) {
            case TOIMINNANOHJAAJA:
                return AnnouncementSenderType.TOIMINNANOHJAAJA;
            case SEURAN_YHDYSHENKILO:
                return AnnouncementSenderType.SEURAN_YHDYSHENKILO;
            default:
                return null;
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<Organisation> resolveFromOrganisation(final AnnouncementDTO dto, final SystemUser activeUser) {
        final AnnouncementDTO.OrganisationDTO org = dto.getFromOrganisation();

        final Organisation organisation =
                organisationRepository.findByTypeAndOfficialCode(org.getOrganisationType(), org.getOfficialCode());

        if (organisation == null) {
            return Optional.empty();
        }

        switch (org.getOrganisationType()) {
            case RK:
                Preconditions.checkState(activeUser.isModeratorOrAdmin());
                break;
            case RHY:
                Preconditions.checkState(activeUser.isModeratorOrAdmin() ||
                        userAuthorizationHelper.isCoordinator(organisation, requireNonNull(activeUser.getPerson())));
                break;
            case CLUB:
                Preconditions.checkState(userAuthorizationHelper.isClubContact(organisation, requireNonNull(activeUser.getPerson())));
                break;
            default:
                throw new IllegalArgumentException("Invalid fromOrganisation type: " + org.getOrganisationType());
        }

        return Optional.of(organisation);
    }

    public Organisation resolveRhySubscriberOrganisation(final AnnouncementDTO dto, final UserInfo userInfo) {
        final AnnouncementDTO.OrganisationDTO org = dto.getRhyMembershipSubscriber();
        if (org != null) {
            Preconditions.checkState(userInfo.isAdminOrModerator());
            return organisationRepository.findByTypeAndOfficialCode(org.getOrganisationType(), org.getOfficialCode());
        }
        return null;
    }
}
