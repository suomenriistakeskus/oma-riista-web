package fi.riista.feature.announcement.crud;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementRepository;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.announcement.notification.AnnouncementNotificationService;
import fi.riista.feature.common.CommitHookService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RK;
import static fi.riista.util.F.firstNonNull;
import static java.util.Arrays.asList;

@Component
public class AnnouncementCrudFeature {

    @Resource
    private AnnouncementRepository announcementRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private CommitHookService commitHookService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AnnouncementSenderRoleService announcementSenderRoleService;

    @Resource
    private AnnouncementSubscriberService announcementSubscriberService;

    @Resource
    private AnnouncementNotificationService announcementNotificationService;

    public static HashMap<OrganisationType, Set<OccupationType>> listSubscriberOccupationTypes(
            final OrganisationType fromOrganisationType) {
        final HashMap<OrganisationType, Set<OccupationType>> result = new HashMap<>();

        final boolean canSendToRhyOccupation = EnumSet.of(
                RHY, RK).contains(fromOrganisationType);

        if (canSendToRhyOccupation) {
            result.put(RHY, OccupationType.rhyValues());
        }

        result.put(OrganisationType.CLUB, EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA));

        return result;
    }

    @Transactional(readOnly = true)
    public AnnouncementDTO readAnnouncement(final long id) {
        final Announcement announcement = requireEntityService.requireAnnouncement(id, EntityPermission.READ);
        final List<AnnouncementSubscriber> subscribers = announcementSubscriberService.listAll(announcement);

        return AnnouncementDTO.create(announcement, subscribers);
    }

    @Transactional
    public void createAnnouncement(final AnnouncementDTO dto) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        final Organisation fromOrganisation = announcementSenderRoleService
                .resolveFromOrganisation(dto, activeUser)
                .orElseThrow(() -> new IllegalStateException("Could not determine from organisation"));

        final AnnouncementSenderType senderType = announcementSenderRoleService
                .resolveRoleInOrganisation(fromOrganisation, activeUser)
                .orElseThrow(() -> new IllegalStateException("Could not find organisation role for active user"));

        final UserInfo userInfo = activeUserService.getActiveUserInfoOrNull();
        checkLimitations(userInfo, dto, fromOrganisation);

        final Announcement announcement = new Announcement(dto.getSubject(), dto.getBody(), activeUser, fromOrganisation, senderType);
        announcement.setVisibleToAll(dto.isVisibleToAll());

        announcement.setRhyMembershipSubscriber(dto.isVisibleToRhyMembers()
                ? firstNonNull(
                    announcementSenderRoleService.resolveRhySubscriberOrganisation(dto, userInfo),
                    announcement.getFromOrganisation())
                : null);

        announcementRepository.save(announcement);
        announcementSubscriberService.create(announcement, dto);
        commitHookService.runInTransactionAfterCommit(
                () -> announcementNotificationService.sendNotifications(announcement, dto.isSendEmail()));
    }

    @Transactional
    public void updateAnnouncement(final AnnouncementDTO dto) {
        final Announcement announcement = requireEntityService.requireAnnouncement(dto.getId(), EntityPermission.UPDATE);

        final UserInfo userInfo = activeUserService.getActiveUserInfoOrNull();
        checkLimitations(userInfo, dto, announcement.getFromOrganisation());

        announcement.setSubject(dto.getSubject());
        announcement.setBody(dto.getBody());

        announcementSubscriberService.update(announcement, dto);
        commitHookService.runInTransactionAfterCommit(
                () -> announcementNotificationService.sendNotifications(announcement, dto.isSendEmail()));
    }

    private static void checkLimitations(final @Nonnull UserInfo activeUser,
                                         final @Nonnull AnnouncementDTO dto,
                                         final @Nonnull Organisation fromOrganisation) {
        Objects.requireNonNull(activeUser);
        Objects.requireNonNull(dto);
        Objects.requireNonNull(fromOrganisation);

        final OrganisationType fromOrganisationType = fromOrganisation.getOrganisationType();

        if (fromOrganisationType == RK) {
            Preconditions.checkArgument(activeUser.isAdminOrModerator(),
                    "Only moderator can send from RK");
        }

        if (dto.isVisibleToAll()) {
            Preconditions.checkArgument(activeUser.isAdmin() ||
                            activeUser.isModerator() && activeUser.hasPrivilege(SystemUserPrivilege.SEND_BULK_MESSAGES),
                    "Insufficient privileges for sending to all users");
        }

        if (dto.isVisibleToRhyMembers()) {
            Preconditions.checkArgument(asList(RHY, RK).contains(fromOrganisationType),
                    "Only RHY od RK can send message to members");
        }

        if (!dto.isSubscriberEmptyOrMatchesSender()) {
            Preconditions.checkArgument(activeUser.isAdminOrModerator(),
                    "Only moderator can select target organisations freely");
        }
    }

    @Transactional
    public void removeAnnouncement(final long id) {
        final Announcement announcement = requireEntityService.requireAnnouncement(id, EntityPermission.DELETE);

        announcementSubscriberService.deleteAll(announcement);
        announcementRepository.delete(announcement);
    }
}
