package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MobileAnnouncementDTOTransformerTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileAnnouncementDTOTransformer mobileAnnouncementDTOTransformer;

    @Test
    public void testMessageFromCoordinator() {
        final SystemUser sender = createNewUser(SystemUser.Role.ROLE_USER);
        sender.setFirstName("first");
        sender.setLastName("second");

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Announcement announcement = model().newAnnouncement(
                sender, rhy, AnnouncementSenderType.TOIMINNANOHJAAJA);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            final MobileAnnouncementDTO dto = mobileAnnouncementDTOTransformer.apply(announcement);
            assertContentEquals(dto, announcement, true);
        });
    }

    @Test
    public void testMessageFromClubContactPerson() {
        final SystemUser sender = createNewUser(SystemUser.Role.ROLE_USER);
        sender.setFirstName("first");
        sender.setLastName("second");

        final HuntingClub club = model().newHuntingClub();
        final Announcement announcement = model().newAnnouncement(
                sender, club, AnnouncementSenderType.SEURAN_YHDYSHENKILO);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            final MobileAnnouncementDTO dto = mobileAnnouncementDTOTransformer.apply(announcement);
            assertContentEquals(dto, announcement, true);
        });
    }

    @Test
    public void testExcludeFullNameFromRiistakeskus() {
        final SystemUser sender = createNewUser(SystemUser.Role.ROLE_USER);
        sender.setFirstName("first");
        sender.setLastName("second");

        final Riistakeskus rk = getRiistakeskus();
        final Announcement announcement = model().newAnnouncement(
                sender, rk, AnnouncementSenderType.RIISTAKESKUS);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            final MobileAnnouncementDTO dto = mobileAnnouncementDTOTransformer.apply(announcement);
            assertContentEquals(dto, announcement, false);
        });
    }

    @Test
    public void testExcludeModeratorFullName() {
        final SystemUser sender = createNewUser(SystemUser.Role.ROLE_MODERATOR);
        sender.setFirstName("first");
        sender.setLastName("second");

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Announcement announcement = model().newAnnouncement(
                sender, rhy, AnnouncementSenderType.TOIMINNANOHJAAJA);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            final MobileAnnouncementDTO dto = mobileAnnouncementDTOTransformer.apply(announcement);
            assertContentEquals(dto, announcement, false);
        });
    }

    private void assertContentEquals(final MobileAnnouncementDTO dto,
                                     final Announcement announcement,
                                     final boolean includeFullName) {
        assertNotNull(dto.getSender());
        assertEquals(announcement.getBody(), dto.getBody());
        assertEquals(announcement.getSubject(), dto.getSubject());
        assertEquals(announcement.getFromOrganisation().getNameLocalisation().asMap(), dto.getSender().getOrganisation());
        assertNotNull(dto.getSender().getTitle());

        if (includeFullName) {
            assertEquals(announcement.getFromUser().getFullName(), dto.getSender().getFullName());
        } else {
            assertEquals("", dto.getSender().getFullName());
        }
    }
}
