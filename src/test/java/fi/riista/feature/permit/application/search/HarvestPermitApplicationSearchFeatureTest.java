package fi.riista.feature.permit.application.search;

import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class HarvestPermitApplicationSearchFeatureTest extends EmbeddedDatabaseTest {


    @Resource
    private HarvestPermitApplicationSearchFeature feature;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private HarvestPermitApplicationSpeciesAmount spa;
    private PermitDecision decision;
    private GameSpecies species;


    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        species = model().newGameSpecies(12345, GameCategory.UNPROTECTED, "fi", "sv", "en");
        spa = model().newHarvestPermitApplicationSpeciesAmount(application, species, 5.0f);
        application.setSpeciesAmounts(Collections.singletonList(spa));
        spa.setValidityYears(0);
        decision = model().newPermitDecision(application);
    }

    @Test
    public void testAuthentication() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.listAnnualPermitsToRenew(null);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.listAnnualPermitsToRenew(null);
        });
    }

    @Test
    public void testAnnualRenewals_doesNotReturnDecisionWithNoPermits() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<HarvestPermitApplicationSearchResultDTO> dtos =
                    feature.listAnnualPermitsToRenew(null);
            assertThat(dtos, hasSize(0));
        });
    }

    @Test
    public void testAnnualRenewals_doesNotReturnDecisionWhenHarvestReportNotSubmitted() {
        createPermit();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<HarvestPermitApplicationSearchResultDTO> dtos =
                    feature.listAnnualPermitsToRenew(null);
            assertThat(dtos, hasSize(0));
        });
    }

    @Test
    public void testAnnualRenewals_returnDecisionAfterHarvestReportSubmitted() {
        final HarvestPermit harvestPermit = createPermit();
        approveHarvestReport(harvestPermit);


        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<HarvestPermitApplicationSearchResultDTO> dtos =
                    feature.listAnnualPermitsToRenew(null);
            assertThat(dtos, hasSize(1));
            assertEquals(application.getId(), dtos.get(0).getId());
        });
    }

    @Test
    public void testAnnualRenewals_doesNotReturnDecisionWhenGrantStatusRejected() {
        final HarvestPermit harvestPermit = createPermit();
        approveHarvestReport(harvestPermit);

        decision.setGrantStatus(GrantStatus.REJECTED);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<HarvestPermitApplicationSearchResultDTO> dtos =
                    feature.listAnnualPermitsToRenew(null);
            assertThat(dtos, hasSize(0));
        });
    }

    private HarvestPermit createPermit() {
        final HarvestPermit harvestPermit = model().newHarvestPermit(rhy);
        harvestPermit.setPermitDecision(decision);
        harvestPermit.setPermitTypeCode(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);
        model().newHarvestPermitSpeciesAmount(harvestPermit, species, 5.0f);
        return harvestPermit;
    }

    private void approveHarvestReport(final HarvestPermit harvestPermit) {
        harvestPermit.setHarvestReportState(HarvestReportState.APPROVED);
        harvestPermit.setHarvestReportAuthor(application.getContactPerson());
        harvestPermit.setHarvestReportDate(DateUtil.now());
        harvestPermit.setHarvestReportModeratorOverride(false);
    }
}
