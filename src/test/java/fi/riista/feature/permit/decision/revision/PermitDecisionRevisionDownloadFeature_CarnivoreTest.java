package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitFixtureMixin;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.Locales;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static fi.riista.util.DateUtil.now;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class PermitDecisionRevisionDownloadFeature_CarnivoreTest extends EmbeddedDatabaseTest implements HarvestPermitFixtureMixin {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Resource
    private PermitDecisionRevisionDownloadFeature feature;

    private GameSpecies lynx;

    @Before
    public void setup() {
        lynx = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_LYNX);

    }

    @Test
    public void testFindsCarnivorePermits() {
        withPublicPermit(lynx, lynxFixture -> {
            persistInNewTransaction();
            runInTransaction(() -> {
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final String decisionDocumentNumber = lynxFixture.decision.createPermitNumber();
                feature.downloadPublicCarnivoreDecisionNoAuthentication(response, decisionDocumentNumber, Locales.FI);

                assertThat(response.getStatus(), equalTo(SC_OK));
                final String headerValue = response.getHeader(ContentDispositionUtil.CONTENT_DISPOSITION);
                final String fileName = ContentDispositionUtil.decodeAttachmentFileName(headerValue);
                assertThat(fileName, equalTo(String.format("Paatos-%s.pdf", decisionDocumentNumber)));
            });
        });
    }

    @Test
    public void testFindsOnlyPublishedPermits() {
        withPublicPermit(lynx, draftFixture -> {
            final PermitDecision draftDecision = draftFixture.decision;
            draftDecision.setLockedDate(null);
            draftDecision.setStatusDraft();

            persistInNewTransaction();
            runInTransaction(() -> {
                final MockHttpServletResponse response = new MockHttpServletResponse();
                feature.downloadPublicCarnivoreDecisionNoAuthentication(
                        response, draftFixture.decision.createPermitNumber(), Locales.FI);

                assertThat(response.getStatus(), equalTo(SC_NOT_FOUND));
            });
        });
    }

    @Test
    public void testCanDownloadCarnivoreDecisionAttachments() {

        withPublicPermit(lynx, lynxFixture -> {
            final PermitDecision decision = lynxFixture.decision;
            decision.setPermitTypeCode(PermitTypeCode.LYNX_KANNANHOIDOLLINEN);
            final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
            final List<String> acceptedFilesInZip = new ArrayList<>();
            try {
                final PermitDecisionAttachment attachmentOnDecision = getPermitDecisionAttachment("temp.txt", 1, decision, revision);
                acceptedFilesInZip.add(String.format("%03d_%s",
                        attachmentOnDecision.getOrderingNumber(),
                        attachmentOnDecision.getAttachmentMetadata().getOriginalFilename())
                );

                final PermitDecisionAttachment otherAttachment = getPermitDecisionAttachment("temp2.txt", 2, decision, revision);
                acceptedFilesInZip.add(String.format("%03d_%s",
                        otherAttachment.getOrderingNumber(),
                        otherAttachment.getAttachmentMetadata().getOriginalFilename())
                );

                // attachment is internal when ordering number is null
                getPermitDecisionAttachment("temp_internal.txt", null, decision, revision);

                persistInNewTransaction();

                runInTransaction(() -> {
                    final MockHttpServletResponse response = new MockHttpServletResponse();
                    final ResponseEntity<byte[]> responseEntity;
                    final String decisionDocumentNumber = decision.createPermitNumber();
                    try {
                        responseEntity = feature.downloadPublicCarnivoreDecisionAttachmentsNoAuthentication(response, decisionDocumentNumber, Locales.FI);

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
                        assertThat(entryCount, equalTo(2));
                    } catch (IOException e) {
                        e.printStackTrace();
                        assert false;
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                assert false;
            }
        });
    }

    @Test
    public void testCanDownloadCarnivoreDecisionAttachmentsFindsOnlyPublishedPermits() {
        withPublicPermit(lynx, lynxFixture -> {
            final PermitDecision decision = lynxFixture.decision;
            decision.setPermitTypeCode(PermitTypeCode.LYNX_KANNANHOIDOLLINEN);
            decision.setStatus(DecisionStatus.LOCKED);

            persistInNewTransaction();

            runInTransaction(() -> {
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final ResponseEntity<byte[]> responseEntity;
                final String decisionDocumentNumber = decision.createPermitNumber();
                try {
                    responseEntity = feature.downloadPublicCarnivoreDecisionAttachmentsNoAuthentication(response, decisionDocumentNumber, Locales.FI);

                    // assert response changes
                    assertThat(response.getStatus(), equalTo(SC_BAD_REQUEST));
                    assertThat(responseEntity, nullValue());

                } catch (IOException e) {
                    e.printStackTrace();
                    assert false;
                }
            });
        });
    }
    @Test
    public void testCanDownloadCarnivoreDecisionAttachmentsFindsOnlyLargeCarnivorePermits() {
        GameSpecies squirrel = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_RED_SQUIRREL);
        withPublicPermit(squirrel, squirrelFixture -> {
            final PermitDecision decision = squirrelFixture.decision;
            decision.setPermitTypeCode(PermitTypeCode.LYNX_KANNANHOIDOLLINEN);
            decision.setStatus(DecisionStatus.LOCKED);

            persistInNewTransaction();

            runInTransaction(() -> {
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final ResponseEntity<byte[]> responseEntity;
                final String decisionDocumentNumber = decision.createPermitNumber();
                try {
                    responseEntity = feature.downloadPublicCarnivoreDecisionAttachmentsNoAuthentication(response, decisionDocumentNumber, Locales.FI);

                    // assert response changes
                    assertThat(response.getStatus(), equalTo(SC_BAD_REQUEST));
                    assertThat(responseEntity, nullValue());

                } catch (IOException e) {
                    e.printStackTrace();
                    assert false;
                }
            });
        });
    }

    @Test
    public void testCanDownloadCarnivoreDecisionAttachmentsFindsOnlyDecisionTypeHarvestPermits() {
        withPublicPermit(lynx, lynxFixture -> {
            final PermitDecision decision = lynxFixture.decision;
            decision.setPermitTypeCode(PermitTypeCode.LYNX_KANNANHOIDOLLINEN);
            decision.setDecisionType(PermitDecision.DecisionType.CANCEL_APPLICATION);

            persistInNewTransaction();

            runInTransaction(() -> {
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final ResponseEntity<byte[]> responseEntity;
                final String decisionDocumentNumber = decision.createPermitNumber();
                try {
                    responseEntity = feature.downloadPublicCarnivoreDecisionAttachmentsNoAuthentication(response, decisionDocumentNumber, Locales.FI);

                    // assert response changes
                    assertThat(response.getStatus(), equalTo(SC_BAD_REQUEST));
                    assertThat(responseEntity, nullValue());

                } catch (IOException e) {
                    e.printStackTrace();
                    assert false;
                }
            });
        });
    }

    @Test
    public void testCanDownloadCarnivoreDecisionAttachmentsFindsOnlyCarnivorePermitCodePermits() {
        withPublicPermit(lynx, lynxFixture -> {
            final PermitDecision decision = lynxFixture.decision;
            decision.setPermitTypeCode(PermitTypeCode.MOOSELIKE);

            persistInNewTransaction();

            runInTransaction(() -> {
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final ResponseEntity<byte[]> responseEntity;
                final String decisionDocumentNumber = decision.createPermitNumber();
                try {
                    responseEntity = feature.downloadPublicCarnivoreDecisionAttachmentsNoAuthentication(response, decisionDocumentNumber, Locales.FI);

                    // assert response changes
                    assertThat(response.getStatus(), equalTo(SC_BAD_REQUEST));
                    assertThat(responseEntity, nullValue());

                } catch (IOException e) {
                    e.printStackTrace();
                    assert false;
                }
            });
        });
    }

    private PermitDecisionAttachment getPermitDecisionAttachment(final String fileName, final Integer orderingNumber, final PermitDecision decision, final PermitDecisionRevision revision) throws IOException {
        final PermitDecisionAttachment attachmentOnDecision = model().newPermitDecisionAttachment(decision);
        final File file = folder.newFile(fileName);
        attachmentOnDecision.setOrderingNumber(orderingNumber);
        attachmentOnDecision.getAttachmentMetadata().setResourceUrl(file.toURI().toURL());
        model().newPermitDecisionRevisionAttachment(revision, attachmentOnDecision);
        return attachmentOnDecision;
    }

    @Override
    public TemporaryFolder getTemporaryFolder() {
        return folder;
    }

    // Decisions are available through public api with delay so use earlier publish date for these decisions
    private void withPublicPermit(final GameSpecies species, final Consumer<HarvestPermitFixture> consumer) {
        final DateTime oneWeekAgo = now().minusWeeks(1);

        withPermit(species, oneWeekAgo.getYear(), fixture -> {
            fixture.decision.setPublishDate(oneWeekAgo);
            consumer.accept(fixture);
        });
    }
}
