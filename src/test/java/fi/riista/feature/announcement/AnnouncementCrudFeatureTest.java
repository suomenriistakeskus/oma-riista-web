package fi.riista.feature.announcement;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.announcement.crud.AnnouncementCrudFeature;
import fi.riista.feature.announcement.crud.AnnouncementDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.mail.queue.MailMessage;
import fi.riista.feature.mail.queue.MailMessageRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AnnouncementCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnouncementCrudFeature announcementCrudFeature;

    @Resource
    private AnnouncementRepository announcementRepository;

    @Resource
    private MailMessageRepository mailMessageRepository;

    @Resource
    private AnnouncementSubscriberRepository announcementSubscriberRepository;

    @Resource
    private ThreadPoolTaskScheduler commonTaskScheduler;

    private void awaitAsyncTasks() {
        try {
            commonTaskScheduler.submit(() -> 1).get();
        } catch (Exception ignore) {
        }
    }

    private Person createClubContactPerson(final HuntingClub club) {
        final Person contactPerson = model().newPerson();
        model().newOccupation(club, contactPerson, OccupationType.SEURAN_YHDYSHENKILO);
        return contactPerson;
    }

    @Test
    public void testCreateAnnouncement() {
        final HuntingClub club = model().newHuntingClub();
        final Person contactPerson = createClubContactPerson(club);
        final SystemUser contactPersonUser = createUser(contactPerson);

        final AnnouncementDTO dto = new AnnouncementDTO();
        dto.setSubject("AnnouncementSubject");
        dto.setBody("AnnouncementBody");
        dto.setOccupationTypes(EnumSet.of(OccupationType.SEURAN_JASEN));

        onSavedAndAuthenticated(contactPersonUser, () -> {
            dto.setFromOrganisation(AnnouncementDTO.OrganisationDTO.create(club));

            announcementCrudFeature.createAnnouncement(dto);
        });

        runInTransaction(() -> {
            final List<Announcement> announcementList = announcementRepository.findAll();
            final List<AnnouncementSubscriber> subscriberList = announcementSubscriberRepository.findAll();

            assertThat(announcementList, hasSize(1));
            assertThat(subscriberList, hasSize(1));

            final Announcement announcement = announcementList.get(0);
            assertEquals(announcement.getBody(), dto.getBody());
            assertEquals(announcement.getSubject(), dto.getSubject());
            assertEquals(announcement.getSenderType(), AnnouncementSenderType.SEURAN_YHDYSHENKILO);
            assertEquals(announcement.getFromOrganisation().getOfficialCode(),
                    dto.getFromOrganisation().getOfficialCode());
            assertEquals(announcement.getFromOrganisation().getOrganisationType(),
                    dto.getFromOrganisation().getOrganisationType());
            assertEquals(announcement.getFromUser(), contactPersonUser);

            final AnnouncementSubscriber subscriber = subscriberList.get(0);
            assertEquals(OccupationType.SEURAN_JASEN, subscriber.getOccupationType());
            assertEquals(announcement, subscriber.getAnnouncement());
            assertEquals(club, subscriber.getOrganisation());
        });
    }


    @Test
    public void testEmail_moderatorToAll() {
        LocaleContextHolder.setLocale(Locales.FI);

        final HuntingClub club = model().newHuntingClub();
        final SystemUser moderator = createNewModerator();
        moderator.addPrivilege(SystemUserPrivilege.SEND_BULK_MESSAGES);
        model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN);

        final AnnouncementDTO dto = new AnnouncementDTO();
        dto.setSubject("AnnouncementSubject");
        dto.setBody("AnnouncementBody");
        dto.setVisibleToAll(true);
        dto.setSendEmail(false);
        dto.setFromOrganisation(AnnouncementDTO.OrganisationDTO.create(getRiistakeskus()));

        onSavedAndAuthenticated(moderator, () -> {
            announcementCrudFeature.createAnnouncement(dto);
        });

        awaitAsyncTasks();

        runInTransaction(() -> {
            final List<Announcement> announcementList = announcementRepository.findAll();
            final List<AnnouncementSubscriber> subscriberList = announcementSubscriberRepository.findAll();
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(announcementList, hasSize(1));
            assertTrue(announcementList.get(0).isVisibleToAll());
            assertThat(subscriberList, hasSize(0)); // Subscribers not collected for messages to all
            assertThat(mailMessages, hasSize(0)); // Email sending not supported for message to all

        });
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEmail_moderatorToAll_noBulkMessagePrivilege() {
        LocaleContextHolder.setLocale(Locales.FI);

        final SystemUser moderator = createNewModerator();

        final AnnouncementDTO dto = new AnnouncementDTO();
        dto.setSubject("AnnouncementSubject");
        dto.setBody("AnnouncementBody");
        dto.setVisibleToAll(true);
        dto.setSendEmail(true);
        dto.setFromOrganisation(AnnouncementDTO.OrganisationDTO.create(getRiistakeskus()));

        onSavedAndAuthenticated(moderator, () -> {
            announcementCrudFeature.createAnnouncement(dto);
            Assert.fail("Should have thrown an exception");
        });
    }

    @Test
    public void testEmail_clubContactPerson() {
        LocaleContextHolder.setLocale(Locales.FI);

        final HuntingClub club = model().newHuntingClub();
        final Person contactPerson = createClubContactPerson(club);
        contactPerson.setFirstName("Yrjö");
        contactPerson.setLastName("Yhteysmies");
        final SystemUser contactPersonUser = createUser(contactPerson);

        model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN);

        final AnnouncementDTO dto = new AnnouncementDTO();
        dto.setSubject("AnnouncementSubject");
        dto.setBody("AnnouncementBody");
        dto.setOccupationTypes(EnumSet.of(OccupationType.SEURAN_JASEN));
        dto.setSendEmail(true);

        onSavedAndAuthenticated(contactPersonUser, () -> {
            dto.setFromOrganisation(AnnouncementDTO.OrganisationDTO.create(club));
            announcementCrudFeature.createAnnouncement(dto);
        });

        awaitAsyncTasks();

        runInTransaction(() -> {
            final List<Announcement> announcementList = announcementRepository.findAll();
            final List<AnnouncementSubscriber> subscriberList = announcementSubscriberRepository.findAll();
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(announcementList, hasSize(1));
            assertThat(subscriberList, hasSize(1));
            assertThat(mailMessages, hasSize(1));

            mailMessages.forEach(message -> {
                assertTrue(message.getBody().contains(contactPerson.getFirstName()));
                assertTrue(message.getBody().contains(contactPerson.getLastName()));
                assertTrue(message.getBody().contains(club.getNameFinnish()));
            });
        });
    }

    @Test
    public void testEmail_moderator() {
        LocaleContextHolder.setLocale(Locales.FI);

        final HuntingClub club = model().newHuntingClub();
        final SystemUser moderator = createNewModerator();

        model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN);

        final AnnouncementDTO dto = new AnnouncementDTO();
        dto.setSubject("AnnouncementSubject");
        dto.setBody("AnnouncementBody");
        dto.setOccupationTypes(EnumSet.of(OccupationType.SEURAN_JASEN));
        dto.setSendEmail(true);
        dto.setFromOrganisation(AnnouncementDTO.OrganisationDTO.create(getRiistakeskus()));

        onSavedAndAuthenticated(moderator, () -> {
            announcementCrudFeature.createAnnouncement(dto);
        });

        awaitAsyncTasks();

        runInTransaction(() -> {
            final List<Announcement> announcementList = announcementRepository.findAll();
            final List<AnnouncementSubscriber> subscriberList = announcementSubscriberRepository.findAll();
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(announcementList, hasSize(1));
            assertThat(subscriberList, hasSize(1));
            assertThat(mailMessages, hasSize(1));

            final MailMessage message = mailMessages.get(0);
            assertFalse(message.getBody().contains(moderator.getFirstName()));
            assertFalse(message.getBody().contains(moderator.getLastName()));
            assertTrue(message.getBody().contains(getRiistakeskus().getNameFinnish()));
        });
    }

    @Test
    public void testEmail_moderator_fromRhyView() {
        LocaleContextHolder.setLocale(Locales.FI);

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final SystemUser moderator = createNewModerator();

        final Person person = model().newPersonWithAddress();
        person.setRhyMembership(rhy);
        person.setEmail("member@rhy.fi");

        final AnnouncementDTO dto = new AnnouncementDTO();
        dto.setSubject("AnnouncementSubject");
        dto.setBody("AnnouncementBody");
        dto.setVisibleToRhyMembers(true);
        dto.setSendEmail(true);
        dto.setFromOrganisation(AnnouncementDTO.OrganisationDTO.create(rhy));

        onSavedAndAuthenticated(moderator, () -> {
            announcementCrudFeature.createAnnouncement(dto);
        });

        awaitAsyncTasks();

        runInTransaction(() -> {
            final List<Announcement> announcementList = announcementRepository.findAll();
            final List<AnnouncementSubscriber> subscriberList = announcementSubscriberRepository.findAll();
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(announcementList, hasSize(1));
            assertThat(subscriberList, hasSize(0));
            assertThat(mailMessages, hasSize(1));

            final MailMessage message = mailMessages.get(0);
            assertFalse(message.getBody().contains(moderator.getFirstName()));
            assertFalse(message.getBody().contains(moderator.getLastName()));
            assertFalse(message.getBody().contains(rhy.getNameFinnish()));
            assertTrue(message.getBody().contains(getRiistakeskus().getNameFinnish()));
        });
    }
}
