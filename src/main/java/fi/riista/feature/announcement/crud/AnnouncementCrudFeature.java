package fi.riista.feature.announcement.crud;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementRepository;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.announcement.AnnouncementSubscriberRepository;
import fi.riista.feature.announcement.email.AnnouncementEmailResolver;
import fi.riista.feature.announcement.email.AnnouncementEmailService;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AnnouncementCrudFeature {

    @Resource
    private AnnouncementRepository announcementRepository;

    @Resource
    private AnnouncementSubscriberRepository announcementSubscriberRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private AnnouncementEmailResolver announcementEmailResolver;

    @Resource
    private AnnouncementEmailService announcementEmailService;

    public static HashMap<OrganisationType, Set<OccupationType>> listSubscriberOccupationTypes(
            final OrganisationType fromOrganisationType) {
        final HashMap<OrganisationType, Set<OccupationType>> result = new HashMap<>();

        if (EnumSet.of(OrganisationType.RHY, OrganisationType.RK).contains(fromOrganisationType)) {
            result.put(OrganisationType.RHY, getRhyOccupationTypes());
        }

        result.put(OrganisationType.CLUB, EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA));

        return result;
    }

    private static Set<OccupationType> getRhyOccupationTypes() {
        return Arrays.stream(OccupationType.values())
                .filter(o -> o.isApplicableFor(OrganisationType.RHY))
                .filter(o -> !o.isBoardSpecific())
                .collect(Collectors.toSet());
    }

    private Optional<AnnouncementSenderType> resolveRoleInOrganisation(final Organisation organisation, final Person person) {
        if (activeUserService.isModeratorOrAdmin()) {
            return Optional.of(AnnouncementSenderType.RIISTAKESKUS);
        }

        final EnumSet<OccupationType> includedOccupationTypes = EnumSet.of(OccupationType.TOIMINNANOHJAAJA, OccupationType.SEURAN_YHDYSHENKILO);

        return occupationRepository.findActiveByOrganisationAndPerson(organisation, person).stream()
                .map(Occupation::getOccupationType)
                .filter(includedOccupationTypes::contains)
                .map(occupationType -> occupationType == OccupationType.TOIMINNANOHJAAJA ? AnnouncementSenderType.TOIMINNANOHJAAJA
                        : occupationType == OccupationType.SEURAN_YHDYSHENKILO ? AnnouncementSenderType.SEURAN_YHDYSHENKILO
                        : null)
                .findAny();
    }

    private Optional<Organisation> resolveFromOrganisation(final AnnouncementDTO dto) {
        final AnnouncementDTO.OrganisationDTO org = dto.getFromOrganisation();

        final Organisation organisation = organisationRepository.findByTypeAndOfficialCode(
                org.getOrganisationType(), org.getOfficialCode());

        if (organisation == null) {
            return Optional.empty();
        }

        switch (org.getOrganisationType()) {
            case RK:
                Preconditions.checkState(activeUserService.isModeratorOrAdmin());
                break;
            case RHY:
                Preconditions.checkState(userAuthorizationHelper.isCoordinator(organisation));
                break;
            case CLUB:
                final Person person = activeUserService.requireActivePerson();
                Preconditions.checkState(userAuthorizationHelper.isClubContact(organisation, person));
                break;
            default:
                throw new IllegalArgumentException("Invalid fromOrganisation type: " + org.getOrganisationType());
        }

        return Optional.of(organisation);
    }

    @Transactional
    public void createAnnouncement(final AnnouncementDTO dto) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        final Organisation fromOrganisation = resolveFromOrganisation(dto)
                .orElseThrow(() -> new IllegalStateException("Could not determine from organisation"));
        final AnnouncementSenderType senderType = resolveRoleInOrganisation(fromOrganisation, activeUser.getPerson())
                .orElseThrow(() -> new IllegalStateException("Could not find organisation role for active user"));

        final Announcement announcement = new Announcement(dto.getSubject(), dto.getBody(),
                activeUser, fromOrganisation, senderType);
        final List<AnnouncementSubscriber> subscribers = createSubscribers(dto, announcement);

        announcementRepository.save(announcement);
        announcementSubscriberRepository.save(subscribers);

        if (dto.isSendEmail()) {
            sendEmail(announcement, subscribers);
        }
    }

    @Transactional(readOnly = true)
    public AnnouncementDTO readAnnouncement(final long announcementId) {
        final Announcement announcement = requireEntityService.requireAnnouncement(
                announcementId, EntityPermission.READ);

        final List<AnnouncementSubscriber> subscribers =
                announcementSubscriberRepository.findByAnnouncement(announcement);

        return AnnouncementDTO.create(announcement, subscribers);
    }

    @Transactional
    public void updateAnnouncement(final AnnouncementDTO dto) {
        final Announcement announcement = requireEntityService.requireAnnouncement(
                dto.getId(), EntityPermission.UPDATE);
        announcementSubscriberRepository.deleteByAnnouncement(announcement);

        announcement.setSubject(dto.getSubject());
        announcement.setBody(dto.getBody());

        final List<AnnouncementSubscriber> subscribers = createSubscribers(dto, announcement);

        announcementSubscriberRepository.save(subscribers);

        if (dto.isSendEmail()) {
            sendEmail(announcement, subscribers);
        }
    }

    @Transactional
    public void removeAnnouncement(final long announcementId) {
        final Announcement announcement = requireEntityService.requireAnnouncement(
                announcementId, EntityPermission.DELETE);

        announcementSubscriberRepository.deleteByAnnouncement(announcement);
        announcementRepository.delete(announcement);
    }

    private List<AnnouncementSubscriber> createSubscribers(final AnnouncementDTO dto,
                                                           final Announcement announcement) {
        final Organisation fromOrganisation = announcement.getFromOrganisation();

        if (fromOrganisation.getOrganisationType() == OrganisationType.RK) {
            // Only moderator can select target organisations freely
            Preconditions.checkState(activeUserService.isModeratorOrAdmin());
            Preconditions.checkArgument(!F.isNullOrEmpty(dto.getOccupationTypes()));
            Preconditions.checkArgument(!F.isNullOrEmpty(dto.getSubscriberOrganisations()));

            final List<AnnouncementSubscriber> result = new LinkedList<>();

            for (final AnnouncementDTO.OrganisationDTO organisationDTO : dto.getSubscriberOrganisations()) {
                final Organisation subscriberOrganisation = organisationRepository.findByTypeAndOfficialCode(
                        organisationDTO.getOrganisationType(), organisationDTO.getOfficialCode());

                if (subscriberOrganisation != null) {
                    for (OccupationType occupationType : dto.getOccupationTypes()) {
                        result.add(new AnnouncementSubscriber(announcement, subscriberOrganisation, occupationType));
                    }
                }
            }

            return result;

        } else {
            return dto.getOccupationTypes().stream()
                    .map(occupationType -> new AnnouncementSubscriber(announcement, fromOrganisation, occupationType))
                    .collect(Collectors.toList());
        }
    }

    private void sendEmail(final Announcement announcement, final List<AnnouncementSubscriber> subscribers) {
        announcementEmailService.sendEmail(announcement, LocaleContextHolder.getLocale(),
                announcementEmailResolver.collectReceiverEmails(
                        announcement.getFromOrganisation(), subscribers));
    }
}
