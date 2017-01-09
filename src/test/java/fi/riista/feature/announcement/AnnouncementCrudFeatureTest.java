package fi.riista.feature.announcement;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.crud.AnnouncementDTO;
import fi.riista.feature.announcement.crud.AnnouncementCrudFeature;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AnnouncementCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnouncementCrudFeature clubAnnouncementFeature;

    @Resource
    private AnnouncementRepository announcementRepository;

    @Resource
    private AnnouncementSubscriberRepository announcementSubscriberRepository;

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

            clubAnnouncementFeature.createAnnouncement(dto);
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
            assertEquals(announcement.getFromOrganisation().getOfficialCode(), dto.getFromOrganisation().getOfficialCode());
            assertEquals(announcement.getFromOrganisation().getOrganisationType(), dto.getFromOrganisation().getOrganisationType());
            assertEquals(announcement.getFromUser(), contactPersonUser);

            final AnnouncementSubscriber subscriber = subscriberList.get(0);
            assertEquals(OccupationType.SEURAN_JASEN, subscriber.getOccupationType());
            assertEquals(announcement, subscriber.getAnnouncement());
            assertEquals(club, subscriber.getOrganisation());
        });
    }
}
