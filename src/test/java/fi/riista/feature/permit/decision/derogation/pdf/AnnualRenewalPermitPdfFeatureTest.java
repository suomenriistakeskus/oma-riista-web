package fi.riista.feature.permit.decision.derogation.pdf;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.now;
import static org.hamcrest.Matchers.equalTo;

public class AnnualRenewalPermitPdfFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnualRenewalPermitPdfFeature feature;

    private Riistanhoitoyhdistys rhy;
    private GameSpecies species;
    private PermitDecision decision;
    private HarvestPermit firstPermit;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WIGEON);
        decision = model().newPermitDecision(rhy, species);
        decision.setPermitTypeCode(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);
        firstPermit = model().newHarvestPermit(rhy, decision.createPermitNumber(decision.getDecisionYear()), PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);
        firstPermit.setPermitDecision(decision);
        model().newHarvestPermitSpeciesAmount(firstPermit, species);
    }

    @Test
    public void testGetModel() {
        final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
        revision.setPublishDate(DateTime.now().minusMinutes(1));

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final AnnualRenewalPermitPdfModelDTO model = feature.getModel(firstPermit.getId());
            assertThat(model.getPermitNumber(), equalTo(firstPermit.getPermitNumber()));
            assertThat(model.getDecisionDate(), equalTo(revision.getPublishDate().toLocalDate()));
        });
    }

    @Test
    public void testGetModel_firstRevisionCancelled() {
        final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
        revision.setCancelled(true);
        revision.setPublishDate(null);

        final PermitDecisionRevision publishedRevision = model().newPermitDecisionRevision(decision);
        publishedRevision.setPublishDate(DateTime.now().plusDays(5));

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final AnnualRenewalPermitPdfModelDTO model = feature.getModel(firstPermit.getId());

            assertThat(model.getPermitNumber(), equalTo(firstPermit.getPermitNumber()));
            assertThat(model.getDecisionDate(), equalTo(publishedRevision.getPublishDate().toLocalDate()));
        });
    }

    @Test
    public void testGetModel_secondRevisionCancelled() {
        // Date of first revision's publish date should end up in the model
        final PermitDecisionRevision firstRevision = model().newPermitDecisionRevision(decision);
        firstRevision.setCancelled(true);
        firstRevision.setPublishDate(now().minusDays(2));

        final PermitDecisionRevision secondRevision = model().newPermitDecisionRevision(decision);
        secondRevision.setCancelled(true);
        secondRevision.setPublishDate(null);

        final PermitDecisionRevision thirdRevision = model().newPermitDecisionRevision(decision);
        thirdRevision.setPublishDate(now());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final AnnualRenewalPermitPdfModelDTO model = feature.getModel(firstPermit.getId());

            assertThat(model.getPermitNumber(), equalTo(firstPermit.getPermitNumber()));
            assertThat(model.getDecisionDate(), equalTo(firstRevision.getPublishDate().toLocalDate()));
        });
    }


}
