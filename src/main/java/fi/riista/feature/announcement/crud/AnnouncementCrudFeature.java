package fi.riista.feature.announcement.crud;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementRepository;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.announcement.AnnouncementSubscriberRepository;
import fi.riista.feature.announcement.notification.AnnouncementEmailService;
import fi.riista.feature.announcement.notification.AnnouncementPushNotificationService;
import fi.riista.feature.announcement.notification.AnnouncementSubscriberPersonResolver;
import fi.riista.feature.common.CommitHookService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AnnouncementCrudFeature {
    private static final Logger LOG = LoggerFactory.getLogger(AnnouncementCrudFeature.class);

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
    private CommitHookService commitHookService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private AnnouncementSubscriberPersonResolver announcementSubscriberPersonResolver;

    @Resource
    private AnnouncementEmailService announcementEmailService;

    @Resource
    private AnnouncementPushNotificationService announcementPushNotificationService;

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
                .collect(Collectors.toSet());
    }

    private Optional<AnnouncementSenderType> resolveRoleInOrganisation(final Organisation organisation, final SystemUser activeUser) {
        if (activeUser.isModeratorOrAdmin()) {
            return Optional.of(AnnouncementSenderType.RIISTAKESKUS);
        }

        final EnumSet<OccupationType> includedOccupationTypes = EnumSet.of(OccupationType.TOIMINNANOHJAAJA, OccupationType.SEURAN_YHDYSHENKILO);

        return occupationRepository.findActiveByOrganisationAndPerson(organisation, activeUser.getPerson()).stream()
                .map(Occupation::getOccupationType)
                .filter(includedOccupationTypes::contains)
                .map(occupationType -> occupationType == OccupationType.TOIMINNANOHJAAJA ? AnnouncementSenderType.TOIMINNANOHJAAJA
                        : occupationType == OccupationType.SEURAN_YHDYSHENKILO ? AnnouncementSenderType.SEURAN_YHDYSHENKILO
                        : null)
                .findAny();
    }

    private Optional<Organisation> resolveFromOrganisation(final AnnouncementDTO dto) {
        final AnnouncementDTO.OrganisationDTO org = dto.getFromOrganisation();

        final Organisation organisation =
                organisationRepository.findByTypeAndOfficialCode(org.getOrganisationType(), org.getOfficialCode());

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
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Organisation fromOrganisation = resolveFromOrganisation(dto)
                .orElseThrow(() -> new IllegalStateException("Could not determine from organisation"));
        final AnnouncementSenderType senderType = resolveRoleInOrganisation(fromOrganisation, activeUser)
                .orElseThrow(() -> new IllegalStateException("Could not find organisation role for active user"));

        final Announcement announcement =
                new Announcement(dto.getSubject(), dto.getBody(), activeUser, fromOrganisation, senderType);
        announcement.setVisibleToAll(activeUser.isAdmin() && dto.isVisibleToAll());

        final List<AnnouncementSubscriber> subscribers = createSubscribers(dto, announcement);

        announcementRepository.save(announcement);
        announcementSubscriberRepository.save(subscribers);

        sendNotifications(announcement.getId(), dto.isSendEmail());
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
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Announcement announcement = requireEntityService.requireAnnouncement(
                dto.getId(), EntityPermission.UPDATE);
        announcement.setVisibleToAll(activeUser.isAdmin() && dto.isVisibleToAll());
        announcementSubscriberRepository.deleteByAnnouncement(announcement);

        announcement.setSubject(dto.getSubject());
        announcement.setBody(dto.getBody());

        final List<AnnouncementSubscriber> subscribers = createSubscribers(dto, announcement);

        announcementSubscriberRepository.save(subscribers);

        sendNotifications(announcement.getId(), dto.isSendEmail());
    }

    @Transactional
    public void removeAnnouncement(final long announcementId) {
        final Announcement announcement =
                requireEntityService.requireAnnouncement(announcementId, EntityPermission.DELETE);

        announcementSubscriberRepository.deleteByAnnouncement(announcement);
        announcementRepository.delete(announcement);
    }

    private List<AnnouncementSubscriber> createSubscribers(final AnnouncementDTO dto, final Announcement announcement) {
        final Organisation fromOrganisation = announcement.getFromOrganisation();

        if (fromOrganisation.getOrganisationType() == OrganisationType.RK) {
            // Only moderator can select target organisations freely
            Preconditions.checkState(activeUserService.isModeratorOrAdmin());

            if (dto.isVisibleToAll()) {
                Preconditions.checkArgument(F.isNullOrEmpty(dto.getOccupationTypes()));
                Preconditions.checkArgument(F.isNullOrEmpty(dto.getSubscriberOrganisations()));
            } else {
                Preconditions.checkArgument(!F.isNullOrEmpty(dto.getOccupationTypes()));
                Preconditions.checkArgument(!F.isNullOrEmpty(dto.getSubscriberOrganisations()));
            }

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
        }

        return dto.getOccupationTypes().stream()
                .map(occupationType -> new AnnouncementSubscriber(announcement, fromOrganisation, occupationType))
                .collect(Collectors.toList());
    }

    private void sendNotifications(final Long announcementId, final boolean sendEmail) {
        final Locale locale = LocaleContextHolder.getLocale();

        commitHookService.runInTransactionAfterCommit(() -> {
            final Announcement announcement = announcementRepository.getOne(announcementId);
            final Organisation fromOrganisation = announcement.getFromOrganisation();
            final List<AnnouncementSubscriber> subscribers =
                    announcementSubscriberRepository.findByAnnouncement(announcement);

            if (sendEmail && fromOrganisation.getOrganisationType() == OrganisationType.CLUB) {
                try {
                    final Set<String> emails = announcementSubscriberPersonResolver.collectReceiverEmails(subscribers);
                    announcementEmailService.sendEmail(announcement, locale, emails);

                } catch (Exception e) {
                    LOG.error("Could not send email for announcement", e);
                }
            }

            try {
                final List<String> pushTokenIds = announcement.isVisibleToAll()
                        ? announcementSubscriberPersonResolver.collectAllPushTokens()
                        : announcementSubscriberPersonResolver.collectReceiverPushTokens(subscribers);

                announcementPushNotificationService.sendNotification(announcement, pushTokenIds);

            } catch (Exception e) {
                LOG.error("Could not send push notification for announcement", e);
            }
        });
    }
}
