package fi.riista.feature.permit.decision.informationrequest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.mail.queue.MailMessage;
import fi.riista.feature.mail.queue.MailMessageRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.archive.PermitApplicationArchive;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.DecisionInformationPublishingDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PublishDecisionInformationDTO;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.Locales;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static fi.riista.feature.account.user.SystemUserPrivilege.INFORMATION_REQUEST_LINK_HANDLER;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.permit.decision.informationrequest.PermitDecisionInformationRequestFeature.VALID_UNTIL_DAYS;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PermitDecisionInformationRequestFeatureTest extends EmbeddedDatabaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Resource
    private PermitDecisionInformationRequestFeature permitDecisionInformationRequestFeature;

    @Resource
    private MailMessageRepository mailMessageRepository;

    @Resource
    private InformationRequestLinkRepository informationRequestLinkRepository;

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;
    private HuntingClub club;
    private HarvestPermitApplication application;
    private SystemUser admin;
    private SystemUser moderatorWithPerm;
    private SystemUser moderatorWithoutPerm;
    private SystemUser coordinator;
    private SystemUser user;
    private SystemUser rest;

    private PermitDecision decision;
    private PermitDecisionRevision revision;

    private PermitDecisionAttachment attachmentOnDecision;
    private PermitDecisionAttachment otherAttachment;

    private InformationRequestLink informationRequestLink;

    @Before
    public void setup() throws Exception {
        rhy = model().newRiistanhoitoyhdistys();
        area = model().newHarvestPermitArea();
        club = model().newHuntingClub(rhy);

        admin = createNewAdmin();
        moderatorWithPerm = createNewModerator(INFORMATION_REQUEST_LINK_HANDLER);
        moderatorWithoutPerm = createNewModerator();
        final Person coordinatorPerson = model().newPerson();
        model().newOccupation(rhy, coordinatorPerson, TOIMINNANOHJAAJA);
        coordinator = createNewUser("coordinator", coordinatorPerson);

        final Person userPerson = model().newPerson();
        model().newOccupation(club, userPerson, SEURAN_JASEN);
        user = createNewUser("club_user", userPerson);

        rest = createNewApiUser();


        application = model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.MOOSELIKE);

        // add attachment
        model().newHarvestPermitApplicationAttachment(application);

        application.setStatus(HarvestPermitApplication.Status.ACTIVE);
        decision = model().newPermitDecision(application);
        revision = model().newPermitDecisionRevision(decision);

        try {
            getMockApplicationArchive("archive.txt", application);


            final File file = folder.newFile("paatos.pdf");
            revision.getPdfMetadata().setResourceUrl(file.toURI().toURL());

            attachmentOnDecision = getPermitDecisionAttachment("temp.txt", 1, decision, revision);
            otherAttachment = getPermitDecisionAttachment("temp2.txt", 2, decision, revision);

            // attachment is internal when ordering number is null
            getPermitDecisionAttachment("temp_internal.txt", null, decision, revision);


        } catch (final IOException e) {
            e.printStackTrace();
            assert false;
        }


        informationRequestLink = model().newInformationRequestLink(
                "GENERATED-LINK-IDENTIFIER",
                decision,
                "invalid@invalid",
                "Recipient",
                InformationRequestLinkType.APPLICATION_AND_DECISION,
                DateTime.now().plusDays(33),
                "Title",
                "Desc"
        );

        persistInNewTransaction();
    }

    @Test
    public void testCreateLinkForApplicationAndDecision() {
        onSavedAndAuthenticated(admin, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);

            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);

            // check emails
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(mailMessages, hasSize(1));

            final MailMessage message = mailMessages.get(0);

            assertEquals(createDto.getTitle(), message.getSubject());
            assertTrue(String.valueOf(message.getBody()).contains(createDto.getDescription()));

            // get list
            final List<DecisionInformationPublishingDTO> list = informationRequestLinkRepository.getDecisionLinkList(decision);
            assertThat(list, hasSize(2));
        });
    }


    @Test
    public void testCreateLinkOnlyForApplication() {
        PermitDecisionInformationRequestFeatureTest.this.onSavedAndAuthenticated(admin, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION);

            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);

            // check emails
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(mailMessages, hasSize(1));

            final MailMessage message = mailMessages.get(0);

            assertEquals(createDto.getTitle(), message.getSubject());
            assertTrue(String.valueOf(message.getBody()).contains(createDto.getDescription()));

            // get list
            final List<DecisionInformationPublishingDTO> list = informationRequestLinkRepository.getDecisionLinkList(decision);
            assertThat(list, hasSize(2));
        });
    }

    @Test
    public void testCreateLinkOnlyForDecision() {
        onSavedAndAuthenticated(admin, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.DECISION);

            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);

            // check emails
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(mailMessages, hasSize(1));

            final MailMessage message = mailMessages.get(0);

            assertEquals(createDto.getTitle(), message.getSubject());
            assertTrue(String.valueOf(message.getBody()).contains(createDto.getDescription()));

            // get list
            final List<DecisionInformationPublishingDTO> list = informationRequestLinkRepository.getDecisionLinkList(decision);
            assertThat(list, hasSize(2));
        });
    }

    @Test
    public void testOpenLinkForApplicationAndDecisionFromEmail() {

        final InformationRequestLink link = model().newInformationRequestLink("TEST_ID",
                decision,
                "invalid@invalid",
                "Recipient",
                InformationRequestLinkType.APPLICATION_AND_DECISION,
                new DateTime().plusDays(3),
                "title",
                "desc"
        );

        final List<String> acceptedFilesInZip = new ArrayList<>();

        acceptedFilesInZip.add(
                String.format("%s-%s.zip",
                        "001_Hakemus",
                        application.getApplicationNumber()));

        acceptedFilesInZip.add(
                String.format("%s-%s.pdf",
                        "002_Päätös", decision.createPermitNumber()));

        acceptedFilesInZip.add(String.format("%s_%s",
                "003",
                attachmentOnDecision.getAttachmentMetadata().getOriginalFilename())
        );

        acceptedFilesInZip.add(String.format("%s_%s",
                "004",
                otherAttachment.getAttachmentMetadata().getOriginalFilename())
        );

        persistInNewTransaction();

        // download
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ResponseEntity<byte[]> responseEntity;
        final String decisionDocumentNumber = decision.createPermitNumber();
        try {
            responseEntity = permitDecisionInformationRequestFeature.downloadPublicCarnivoreDecisionThroughInformationRequestNoAuthentication(
                    response,
                    link.getLinkIdentifier(),
                    decisionDocumentNumber,
                    Locales.FI);

            // assert response changes
            assertThat(response.getStatus(), equalTo(SC_OK));
            final String headerValue = response.getHeader(ContentDispositionUtil.CONTENT_DISPOSITION);
            assert headerValue != null;
            final String fileName = ContentDispositionUtil.decodeAttachmentFileName(headerValue);
            assertThat(fileName, equalTo(String.format("Liitteet-%s.zip", decisionDocumentNumber)));

            // assert return response content
            assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
            final String entityHeaderValue = Objects.requireNonNull(responseEntity.getHeaders().get(ContentDispositionUtil.CONTENT_DISPOSITION)).toString();
            final String entityFileName = ContentDispositionUtil.decodeAttachmentFileName(entityHeaderValue);
            assertThat(entityFileName, equalTo(String.format("Liitteet-%s.zip", decisionDocumentNumber)));
            assertThat(responseEntity.getBody(), notNullValue());

            // assert zip content
            final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(responseEntity.getBody()));
            ZipEntry zipEntry = zis.getNextEntry();

            int entryCount = 0;
            while (zipEntry != null) {
                entryCount++;
                assertThat(zipEntry.getName(), isIn(acceptedFilesInZip));
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            // no internal attachments should be zipped in file
            assertThat(entryCount, equalTo(4));
        } catch (final IOException e) {
            e.printStackTrace();
            assert false;
        }
    }


    @Test
    public void testOpenLinkForApplicationFromEmail() {
        final InformationRequestLink link = model().newInformationRequestLink("TEST_ID",
                decision,
                "invalid@invalid",
                "Recipient",
                InformationRequestLinkType.APPLICATION,
                new DateTime().plusDays(3),
                "title",
                "desc"
        );

        final List<String> acceptedFilesInZip = new ArrayList<>();

        acceptedFilesInZip.add(
                String.format("%s-%s.zip",
                        "001_Hakemus",
                        application.getApplicationNumber()));

        persistInNewTransaction();
        // download
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ResponseEntity<byte[]> responseEntity;
        final String decisionDocumentNumber = decision.createPermitNumber();
        try {
            responseEntity = permitDecisionInformationRequestFeature.downloadPublicCarnivoreDecisionThroughInformationRequestNoAuthentication(
                    response,
                    link.getLinkIdentifier(),
                    decisionDocumentNumber,
                    Locales.FI);

            // assert response changes
            assertThat(response.getStatus(), equalTo(SC_OK));
            final String headerValue = response.getHeader(ContentDispositionUtil.CONTENT_DISPOSITION);
            assert headerValue != null;
            final String fileName = ContentDispositionUtil.decodeAttachmentFileName(headerValue);
            assertThat(fileName, equalTo(String.format("Liitteet-%s.zip", decisionDocumentNumber)));

            // assert return response content
            assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
            final String entityHeaderValue = Objects.requireNonNull(responseEntity.getHeaders().get(ContentDispositionUtil.CONTENT_DISPOSITION)).toString();
            final String entityFileName = ContentDispositionUtil.decodeAttachmentFileName(entityHeaderValue);
            assertThat(entityFileName, equalTo(String.format("Liitteet-%s.zip", decisionDocumentNumber)));
            assertThat(responseEntity.getBody(), notNullValue());

            // assert zip content
            final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(responseEntity.getBody()));
            ZipEntry zipEntry = zis.getNextEntry();

            int entryCount = 0;
            while (zipEntry != null) {
                entryCount++;
                assertThat(zipEntry.getName(), isIn(acceptedFilesInZip));
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            // no internal attachments should be zipped in file
            assertThat(entryCount, equalTo(1));
        } catch (final IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testOpenLinkForDecisionFromEmail() {
        final InformationRequestLink link = model().newInformationRequestLink("TEST_ID",
                decision,
                "invalid@invalid",
                "Recipient",
                InformationRequestLinkType.DECISION,
                new DateTime().plusDays(3),
                "title",
                "desc"
        );
        final List<String> acceptedFilesInZip = new ArrayList<>();

        acceptedFilesInZip.add(
                String.format("%s-%s.pdf",
                        "001_Päätös", decision.createPermitNumber()));

        acceptedFilesInZip.add(String.format("%s_%s",
                "002",
                attachmentOnDecision.getAttachmentMetadata().getOriginalFilename())
        );

        acceptedFilesInZip.add(String.format("%s_%s",
                "003",
                otherAttachment.getAttachmentMetadata().getOriginalFilename())
        );
        persistInNewTransaction();

        // download
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ResponseEntity<byte[]> responseEntity;
        final String decisionDocumentNumber = decision.createPermitNumber();
        try {
            responseEntity = permitDecisionInformationRequestFeature.downloadPublicCarnivoreDecisionThroughInformationRequestNoAuthentication(
                    response,
                    link.getLinkIdentifier(),
                    decisionDocumentNumber,
                    Locales.FI);

            // assert response changes
            assertThat(response.getStatus(), equalTo(SC_OK));
            final String headerValue = response.getHeader(ContentDispositionUtil.CONTENT_DISPOSITION);
            assert headerValue != null;
            final String fileName = ContentDispositionUtil.decodeAttachmentFileName(headerValue);
            assertThat(fileName, equalTo(String.format("Liitteet-%s.zip", decisionDocumentNumber)));

            // assert return response content
            assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
            final String entityHeaderValue = Objects.requireNonNull(responseEntity.getHeaders().get(ContentDispositionUtil.CONTENT_DISPOSITION)).toString();
            final String entityFileName = ContentDispositionUtil.decodeAttachmentFileName(entityHeaderValue);
            assertThat(entityFileName, equalTo(String.format("Liitteet-%s.zip", decisionDocumentNumber)));
            assertThat(responseEntity.getBody(), notNullValue());

            // assert zip content
            final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(responseEntity.getBody()));
            ZipEntry zipEntry = zis.getNextEntry();

            int entryCount = 0;
            while (zipEntry != null) {
                entryCount++;
                assertThat(zipEntry.getName(), isIn(acceptedFilesInZip));
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            // no internal attachments should be zipped in file
            assertThat(entryCount, equalTo(3));
        } catch (final IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testInvalidateLink() {
        onSavedAndAuthenticated(admin, () -> {

            // invalidate link
            final List<DecisionInformationPublishingDTO> beforeInvalidationList = permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId());
            assertThat(beforeInvalidationList, hasSize(1));

            permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId()));

            final List<DecisionInformationPublishingDTO> afterInvalidationList = permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId());
            assertThat(afterInvalidationList, hasSize(0));

            final Optional<InformationRequestLink> optLinkAfterDeletion = informationRequestLinkRepository.findById(Objects.requireNonNull(informationRequestLink.getId()));
            assertTrue(optLinkAfterDeletion.isPresent());
            assertThat(optLinkAfterDeletion.get().getRecipientEmail(), isEmptyString());
            assertThat(optLinkAfterDeletion.get().getRecipientName(), isEmptyString());
            assertTrue(DateTime.now().isAfter(optLinkAfterDeletion.get().getValidUntil()));

        });
    }

    @Test
    public void testLinkExpiration() {
        onSavedAndAuthenticated(admin, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.DECISION);
            final InformationRequestLink link = permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);

            // get list
            final List<DecisionInformationPublishingDTO> list = permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId());
            assertThat(list, hasSize(2));

            assertEquals(link.getValidUntil().toLocalDate(), LocalDateTime.now().plusDays(VALID_UNTIL_DAYS).toLocalDate());

            // set expiration to the past
            link.setValidUntil(DateTime.now().minusMillis(1));
            informationRequestLinkRepository.saveAndFlush(link);

            // get list
            final List<DecisionInformationPublishingDTO> listAfterExpiration = permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId());
            assertThat(listAfterExpiration, hasSize(1));
        });
    }

    @Test
    public void testLinkLogging() {

        onSavedAndAuthenticated(admin, () -> {
            // download
            final MockHttpServletResponse response = new MockHttpServletResponse();
            final String decisionDocumentNumber = decision.createPermitNumber();
            try {
                permitDecisionInformationRequestFeature.downloadPublicCarnivoreDecisionThroughInformationRequestNoAuthentication(
                        response,
                        informationRequestLink.getLinkIdentifier(),
                        decisionDocumentNumber,
                        Locales.FI);
                assertThat(response.getStatus(), equalTo(SC_OK));

            } catch (final IOException e) {
                e.printStackTrace();
                assert false;
            }

            final List<DecisionInformationPublishingDTO> list = permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId());
            assertThat(list, hasSize(1));
            for (final DecisionInformationPublishingDTO stats : list) {
                if (stats.getId().equals(informationRequestLink.getId())) {
                    assertEquals(1, stats.getLinkOpenedCount().longValue());
                }
            }
        });
    }

    @Test
    public void testCreateAndSendInformationRequestEmail_admin() {
        onSavedAndAuthenticated(admin, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);
            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);

            // get list
            final List<DecisionInformationPublishingDTO> list = informationRequestLinkRepository.getDecisionLinkList(decision);
            assertThat(list, hasSize(2));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateAndSendInformationRequestEmail_admin_disabilityApplication() {
        final HarvestPermitApplication appl = model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.DISABILITY);
        appl.setStatus(HarvestPermitApplication.Status.ACTIVE);
        final PermitDecision dec = model().newPermitDecision(appl);
        model().newPermitDecisionRevision(dec);
        persistInNewTransaction();

        onSavedAndAuthenticated(admin, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(dec, InformationRequestLinkType.APPLICATION_AND_DECISION);
            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);
        });
    }

    @Test
    public void testCreateAndSendInformationRequestEmail_moderatorWithPermission() {
        onSavedAndAuthenticated(moderatorWithPerm, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);
            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);

            // get list
            final List<DecisionInformationPublishingDTO> list = informationRequestLinkRepository.getDecisionLinkList(decision);
            assertThat(list, hasSize(2));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateAndSendInformationRequestEmail_moderatorWithPermission_disabilityApplication() {
        final HarvestPermitApplication appl = model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.DISABILITY);
        appl.setStatus(HarvestPermitApplication.Status.ACTIVE);
        final PermitDecision dec = model().newPermitDecision(appl);
        persistInNewTransaction();

        onSavedAndAuthenticated(moderatorWithPerm, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(dec, InformationRequestLinkType.APPLICATION_AND_DECISION);
            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateAndSendInformationRequestEmail_moderatorWithoutPermission() {
        onSavedAndAuthenticated(moderatorWithoutPerm, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);
            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateAndSendInformationRequestEmail_coordinator() {
        onSavedAndAuthenticated(coordinator, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);
            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateAndSendInformationRequestEmail_user() {
        final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);
        permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateAndSendInformationRequestEmail_rest() {
        onSavedAndAuthenticated(rest, () -> {
            final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);
            permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateAndSendInformationRequestEmail_unauthenticated() {
        final PublishDecisionInformationDTO createDto = getInformationDTO(decision, InformationRequestLinkType.APPLICATION_AND_DECISION);
        permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(createDto);
    }

    @Test
    public void testGetInformationRequestsStatistics_admin() {
        onSavedAndAuthenticated(admin, () -> permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId()));
    }

    @Test
    public void testGetInformationRequestsStatistics_moderatorWithPermission() {
        onSavedAndAuthenticated(moderatorWithPerm, () -> permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId()));
    }

    @Test
    public void testGetInformationRequestsStatistics_moderatorWithoutPermission() {
        // all moderators have permission to read statistics
        onSavedAndAuthenticated(moderatorWithoutPerm, () -> permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId()));
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetInformationRequestsStatistics_coordinator() {
        onSavedAndAuthenticated(coordinator, () -> permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId()));
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetInformationRequestsStatistics_user() {
        onSavedAndAuthenticated(user, () -> permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId()));
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetInformationRequestsStatistics_rest() {
        onSavedAndAuthenticated(rest, () -> permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId()));
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetInformationRequestsStatistics_unauthenticated() {
        permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decision.getId());
    }

    @Test
    public void testInvalidateLink_admin() {
        onSavedAndAuthenticated(admin, () -> permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId())));
    }

    @Test
    public void testInvalidateLink_moderatorWithPermission() {
        onSavedAndAuthenticated(moderatorWithPerm, () -> permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId())));
    }

    @Test(expected = AccessDeniedException.class)
    public void testInvalidateLink_moderatorWithoutPermission() {
        onSavedAndAuthenticated(moderatorWithoutPerm, () -> permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId())));
    }

    @Test(expected = AccessDeniedException.class)
    public void testInvalidateLink_coordinator() {
        onSavedAndAuthenticated(coordinator, () -> permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId())));
    }

    @Test(expected = AccessDeniedException.class)
    public void testInvalidateLink_user() {
        onSavedAndAuthenticated(user, () -> permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId())));
    }

    @Test(expected = AccessDeniedException.class)
    public void testInvalidateLink_rest() {
        onSavedAndAuthenticated(rest, () -> permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId())));
    }

    @Test(expected = AccessDeniedException.class)
    public void testInvalidateLink_unauthenticated() {
        permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decision.getId(), Objects.requireNonNull(informationRequestLink.getId()));
    }


    private PermitDecisionAttachment getPermitDecisionAttachment(final String fileName, final Integer orderingNumber, final PermitDecision decision, final PermitDecisionRevision revision) throws IOException {
        final PermitDecisionAttachment attachmentOnDecision = model().newPermitDecisionAttachment(decision);
        final File file = folder.newFile(fileName);
        attachmentOnDecision.setOrderingNumber(orderingNumber);
        attachmentOnDecision.getAttachmentMetadata().setResourceUrl(file.toURI().toURL());
        model().newPermitDecisionRevisionAttachment(revision, attachmentOnDecision);
        return attachmentOnDecision;
    }

    private PermitApplicationArchive getMockApplicationArchive(final String fileName, final HarvestPermitApplication application) throws IOException {
        final PersistentFileMetadata fileMetadata = model().newPersistentFileMetadata();
        // fileMetadata.setOriginalFilename(null); // archive doesn't have original filename
        final File file = folder.newFile(fileName);
        fileMetadata.setResourceUrl(file.toURI().toURL());

        return model().newPermitApplicationArchive(application, fileMetadata);
    }

    private PublishDecisionInformationDTO getInformationDTO(final PermitDecision decision, final InformationRequestLinkType applicationAndDecision) {
        return new PublishDecisionInformationDTO(
                decision.getId(),
                "invalid@invalid",
                "Recipient",
                "Title",
                "Desc",
                applicationAndDecision
        );
    }

}
