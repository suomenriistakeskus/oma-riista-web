package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitFixtureMixin;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.Locales;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.annotation.Resource;
import java.util.function.Consumer;

import static fi.riista.util.DateUtil.now;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
