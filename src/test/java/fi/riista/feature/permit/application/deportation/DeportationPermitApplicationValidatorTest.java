package fi.riista.feature.permit.application.deportation;

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
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WIGEON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DEPORTATION;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.DRAFT;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FAUNA_41C;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FLORA;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FLORA_41A;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThrows;

public class DeportationPermitApplicationValidatorTest  extends EmbeddedDatabaseTest {

    private HarvestPermitApplication wolverineApplication;
    private HarvestPermitApplicationSpeciesAmount wolverineSpa;
    private DeportationPermitApplication wolverineDeportationApplication;
    private List<DerogationPermitApplicationReason> wolverineReasons = new ArrayList<>();

    private HarvestPermitApplication wigeonApplication;
    private HarvestPermitApplicationSpeciesAmount wigeonSpa;
    private DeportationPermitApplication wigeonDeportationApplication;
    private List<DerogationPermitApplicationReason> wigeonReasons = new ArrayList<>();

    @Before
    public void setup() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        wolverineApplication = model().newHarvestPermitApplication(rhy, null, DEPORTATION);

        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE, GAME_MAMMAL, "ahma", "ahma-sv", "ahma-en");
        wolverineSpa = model().newHarvestPermitApplicationSpeciesAmount(wolverineApplication, wolverineSpecies, 1.0f, 1);
        wolverineSpa.setBeginDate(new LocalDate(2020, 12, 1));
        wolverineSpa.setEndDate(new LocalDate(2020, 12, 21));

        wolverineSpa.setCausedDamageAmount(10);
        wolverineSpa.setCausedDamageDescription("Damage description");
        wolverineSpa.setEvictionMeasureEffect("Eviction effect");
        wolverineSpa.setEvictionMeasureDescription("Eviction description");

        wolverineSpa.setPopulationAmount("Population amount");
        wolverineSpa.setPopulationDescription("Population description");

        wolverineApplication.setSpeciesAmounts(Arrays.asList(wolverineSpa));

        wolverineDeportationApplication = model().newDeportationPermitApplication(wolverineApplication);
        wolverineDeportationApplication.setAreaDescription("Area description");

        wolverineReasons.add(new DerogationPermitApplicationReason(wolverineApplication, REASON_FLORA_41A));

        wigeonApplication = model().newHarvestPermitApplication(rhy, null, DEPORTATION);

        final GameSpecies wigeonSpecies = model().newGameSpecies(OFFICIAL_CODE_WIGEON, FOWL, "haapana", "haapana-sv", "haapana-en");
        wigeonSpa = model().newHarvestPermitApplicationSpeciesAmount(wigeonApplication, wigeonSpecies, 1.0f, 1);
        wigeonSpa.setBeginDate(new LocalDate(2020, 1, 1));
        wigeonSpa.setEndDate(new LocalDate(2020, 12, 31));

        wigeonSpa.setCausedDamageAmount(10);
        wigeonSpa.setCausedDamageDescription("Damage description");
        wigeonSpa.setEvictionMeasureEffect("Eviction effect");
        wigeonSpa.setEvictionMeasureDescription("Eviction description");

        wigeonSpa.setPopulationAmount("Population amount");
        wigeonSpa.setPopulationDescription("Population description");

        wigeonApplication.setSpeciesAmounts(Arrays.asList(wigeonSpa));

        wigeonDeportationApplication = model().newDeportationPermitApplication(wigeonApplication);
        wigeonDeportationApplication.setAreaDescription("Area description");

        wigeonReasons.add(new DerogationPermitApplicationReason(wigeonApplication, REASON_FLORA));
    }

    @Test
    public void testValidateContent_wolverine() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            DeportationPermitApplicationValidator.validateContent(wolverineApplication, wolverineDeportationApplication, wolverineReasons);
        });
    }

    @Test
    public void testValidateContent_wigeon() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            DeportationPermitApplicationValidator.validateContent(wigeonApplication, wigeonDeportationApplication, wigeonReasons);
        });
    }

    @Test
    public void testValidateContent_invalidSpeciesAmount() {
        final GameSpecies partridgeSpecies = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE);
        final HarvestPermitApplicationSpeciesAmount partridgeSpa = model().newHarvestPermitApplicationSpeciesAmount(wolverineApplication, partridgeSpecies);
        wolverineApplication.setSpeciesAmounts(Arrays.asList(wolverineSpa, partridgeSpa));

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Invalid species amount")));
        });
    }

    @Test
    public void testValidateContent_invalidReasons() {
        wolverineReasons.add(new DerogationPermitApplicationReason(wolverineApplication, REASON_FAUNA_41C));

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Derogation reasons not valid")));
        });
    }

    @Test
    public void testValidateContent_invalidDamageAmount() {
        wolverineSpa.setCausedDamageAmount(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Invalid damage amount for ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidDamageDescription() {
        wolverineSpa.setCausedDamageDescription(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Required information missing: damage description for ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidEvictionMethods() {
        wolverineSpa.setEvictionMeasureEffect(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Required information missing: eviction measures effect for ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidEvictionDescription() {
        wolverineSpa.setEvictionMeasureDescription(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Required information missing: eviction methods for ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidPopulationAmount() {
        wolverineSpa.setPopulationAmount(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Required information missing: population amount for ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidPopulationDescription() {
        wolverineSpa.setPopulationDescription(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Required information missing: population description for ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidPeriodWolverine() {
        wolverineSpa.setBeginDate(new LocalDate(2020, 12, 1));
        wolverineSpa.setEndDate(new LocalDate(2020, 12, 22));

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Invalid time period for species ahma")));
        });
    }

    @Test
    public void testValidateContent_invalidPeriodWigeon() {
        wigeonSpa.setBeginDate(new LocalDate(2020, 1, 1));
        wigeonSpa.setEndDate(new LocalDate(2021, 1, 1));

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wigeonApplication,
                                    wigeonDeportationApplication,
                                    wigeonReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Invalid time period for species haapana")));
        });
    }

    @Test
    public void testValidateContent_invalidValidityYears() {
        wolverineSpa.setValidityYears(3);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Validity years is invalid")));
        });
    }

    @Test
    public void testValidateContent_invalidAreaInformation() {
        wolverineDeportationApplication.setGeoLocation(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Geolocation missing")));
        });
    }

    @Test
    public void testValidateContent_areaAttachmentMissing() {
        wolverineDeportationApplication.setAreaDescription(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Area attachment is missing")));
        });
    }

    @Test
    public void testValidateContent_permitHolderNameMissing() {
        final PermitHolder permitHolder = PermitHolder.create(null, null, PermitHolder.PermitHolderType.PERSON);
        wolverineApplication.setPermitHolder(permitHolder);
        wolverineApplication.setStatus(DRAFT);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Permit holder name missing")));
        });
    }

    @Test
    public void testValidateContent_permitHolderCodeMissing() {
        final PermitHolder permitHolder = PermitHolder.create("name", null, PermitHolder.PermitHolderType.RY);
        wolverineApplication.setPermitHolder(permitHolder);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            DeportationPermitApplicationValidator.validateContent(
                                    wolverineApplication,
                                    wolverineDeportationApplication,
                                    wolverineReasons));
            assertThat(illegalStateException.getMessage(), is(equalTo("Code missing for permit holder")));
        });
    }
}
