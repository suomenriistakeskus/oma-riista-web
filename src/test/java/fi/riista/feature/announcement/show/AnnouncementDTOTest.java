package fi.riista.feature.announcement.show;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.announcement.crud.AnnouncementDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnouncementDTOTest {

    // CLUB

    @Test
    public void testRecipients_CLUB_visibleAll() {
        final AnnouncementDTO dto = createDTO(OrganisationType.CLUB);
        dto.setVisibleToAll(true);

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_CLUB_visibleToRhyMembers() {
        final AnnouncementDTO dto = createDTO(OrganisationType.CLUB);
        dto.setVisibleToRhyMembers(true);

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_CLUB_withOrganisations() {
        final AnnouncementDTO dto = createDTO(OrganisationType.CLUB);
        dto.setOccupationTypes(ImmutableSet.of(OccupationType.SEURAN_JASEN));
        dto.setSubscriberOrganisations(ImmutableSet.of(createOrganisation()));

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_CLUB_withOccupationsOnly() {
        final AnnouncementDTO dto = createDTO(OrganisationType.CLUB);
        dto.setOccupationTypes(ImmutableSet.of(OccupationType.SEURAN_JASEN, OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_JASEN, OccupationType.RYHMAN_METSASTYKSENJOHTAJA));

        assertTrue(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_CLUB_withInvalidOccupations() {
        final AnnouncementDTO dto = createDTO(OrganisationType.CLUB);
        dto.setOccupationTypes(ImmutableSet.of(OccupationType.SRVA_YHTEYSHENKILO));

        assertFalse(dto.isRecipientsOk());
    }

    // RHY

    @Test
    public void testRecipients_RHY_visibleAll() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RHY);
        dto.setVisibleToAll(true);

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RHY_visibleToRhyMembers() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RHY);
        dto.setVisibleToRhyMembers(true);

        assertTrue(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RHY_withOrganisations() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RHY);
        dto.setOccupationTypes(ImmutableSet.of(OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA));
        dto.setSubscriberOrganisations(ImmutableSet.of(createOrganisation()));

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RHY_withOccupationsOnly() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RHY);
        dto.setOccupationTypes(ImmutableSet.of(OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA));

        assertTrue(dto.isRecipientsOk());
    }

    // RK

    @Test
    public void testRecipients_RK_visibleToAll() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RK);
        dto.setVisibleToAll(true);

        assertTrue(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RK_visibleToRhyMembers() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RK);
        dto.setVisibleToRhyMembers(true);

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RK_none() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RK);

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RK_withOrganisations() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RK);
        dto.setOccupationTypes(ImmutableSet.of(OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA, OccupationType.SEURAN_JASEN));
        dto.setSubscriberOrganisations(ImmutableSet.of(createOrganisation()));

        assertTrue(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RK_emptyOccupations() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RK);
        dto.setOccupationTypes(ImmutableSet.of());
        dto.setSubscriberOrganisations(ImmutableSet.of(createOrganisation()));

        assertFalse(dto.isRecipientsOk());
    }

    @Test
    public void testRecipients_RK_emptyOrganisations() {
        final AnnouncementDTO dto = createDTO(OrganisationType.RK);
        dto.setOccupationTypes(ImmutableSet.of(OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA));
        dto.setSubscriberOrganisations(ImmutableSet.of());

        assertFalse(dto.isRecipientsOk());
    }

    private static AnnouncementDTO.OrganisationDTO createOrganisation() {
        final AnnouncementDTO.OrganisationDTO org = new AnnouncementDTO.OrganisationDTO();
        org.setOrganisationType(OrganisationType.RHY);
        org.setOfficialCode("200");
        return org;
    }

    private static AnnouncementDTO createDTO(final OrganisationType from) {
        final AnnouncementDTO.OrganisationDTO org = new AnnouncementDTO.OrganisationDTO();
        org.setOrganisationType(from);

        final AnnouncementDTO dto = new AnnouncementDTO();
        dto.setFromOrganisation(org);

        return dto;
    }
}
