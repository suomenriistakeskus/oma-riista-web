package fi.riista.feature.permit.application.research;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fi.riista.feature.gamediary.GameCategory.FOWL;
import static fi.riista.feature.gamediary.GameCategory.GAME_MAMMAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WIGEON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.RESEARCH;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.DRAFT;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FAUNA_41C;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FLORA;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FLORA_41A;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThrows;

public class ResearchPermitApplicationValidatorTest extends EmbeddedDatabaseTest {

    private HarvestPermitApplication application;
    private HarvestPermitApplicationSpeciesAmount wolverineSpa;
    private HarvestPermitApplicationSpeciesAmount wigeonSpa;
    private ResearchPermitApplication researchPermitApplication;
    private List<DerogationPermitApplicationReason> reasons = new ArrayList<>();

    @Before
    public void setup() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, RESEARCH);

        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE, GAME_MAMMAL, "ahma", "ahma-sv", "ahma-en");
        wolverineSpa = model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies, 1.0f, 1);
        wolverineSpa.setBeginDate(new LocalDate(2020, 12, 1));
        wolverineSpa.setEndDate(new LocalDate(2020, 12, 31));
        wolverineSpa.setValidityYears(2);

        final GameSpecies wigeonSpecies = model().newGameSpecies(OFFICIAL_CODE_WIGEON, FOWL, "haapana", "haapana-sv", "haapana-en");
        wigeonSpa = model().newHarvestPermitApplicationSpeciesAmount(application, wigeonSpecies, 1.0f, 1);
        wigeonSpa.setBeginDate(new LocalDate(2020, 1, 1));
        wigeonSpa.setEndDate(new LocalDate(2020, 12, 31));
        wigeonSpa.setValidityYears(2);

        application.setSpeciesAmounts(Arrays.asList(wolverineSpa, wigeonSpa));

        researchPermitApplication = model().newResearchPermitApplication(application);
        researchPermitApplication.setAreaDescription("Area description");
        researchPermitApplication.setJustification("Research justification");

        final DerogationPermitApplicationReason wolverineReason = new DerogationPermitApplicationReason(application, REASON_FLORA_41A);
        final DerogationPermitApplicationReason wigeonReason = new DerogationPermitApplicationReason(application, REASON_FLORA);
        reasons.add(wolverineReason);
        reasons.add(wigeonReason);
    }

    @Test
    public void testValidateContent() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            ResearchPermitApplicationValidator.validateContent(application, researchPermitApplication, reasons);
        });
    }

    @Test
    public void testValidateContent_invalidPeriod() {
        wolverineSpa.setBeginDate(new LocalDate(2020, 1, 1));
        wolverineSpa.setEndDate(new LocalDate(2021, 1, 1));

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            ResearchPermitApplicationValidator.validateContent(
                                    application,
                                    researchPermitApplication,
                                    reasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Invalid time period for species ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidValidityYears() {
        wolverineSpa.setValidityYears(1);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            ResearchPermitApplicationValidator.validateContent(
                                    application,
                                    researchPermitApplication,
                                    reasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("All validity years must match")));
        });
    }

    @Test
    public void testValidateContent_invalidAreaInformation() {
        researchPermitApplication.setGeoLocation(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            ResearchPermitApplicationValidator.validateContent(
                                    application,
                                    researchPermitApplication,
                                    reasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Geolocation missing")));
        });
    }

    @Test
    public void testValidateContent_areaAttachmentMissing() {
        researchPermitApplication.setAreaDescription(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            ResearchPermitApplicationValidator.validateContent(
                                    application,
                                    researchPermitApplication,
                                    reasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Area attachment is missing")));
        });
    }

    @Test
    public void testValidateContent_permitHolderNameMissing() {
        final PermitHolder permitHolder = PermitHolder.create(null, null, PermitHolder.PermitHolderType.PERSON);
        application.setPermitHolder(permitHolder);
        application.setStatus(DRAFT);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            ResearchPermitApplicationValidator.validateContent(
                                    application,
                                    researchPermitApplication,
                                    reasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Permit holder name missing")));
        });
    }

    @Test
    public void testValidateContent_permitHolderCodeMissing() {
        final PermitHolder permitHolder = PermitHolder.create("name", null, PermitHolder.PermitHolderType.RY);
        application.setPermitHolder(permitHolder);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            ResearchPermitApplicationValidator.validateContent(
                                    application,
                                    researchPermitApplication,
                                    reasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Code missing for permit holder")));
        });
    }

    @Test
    public void testValidateContent_invalidResearchJustification() {
        researchPermitApplication.setJustification(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            ResearchPermitApplicationValidator.validateContent(
                                    application,
                                    researchPermitApplication,
                                    reasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Invalid research justification")));
        });
    }
}
