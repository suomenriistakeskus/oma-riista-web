package fi.riista.feature.permit.application.gamemanagement;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.gamediary.GameCategory.GAME_MAMMAL;
import static fi.riista.feature.gamediary.GameCategory.UNPROTECTED;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.DRAFT;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThrows;

public class GameManagementPermitApplicationValidatorTest extends EmbeddedDatabaseTest {

    private HarvestPermitApplication application;
    private HarvestPermitApplicationSpeciesAmount spa;
    private GameManagementPermitApplication gameManagementPermitApplication;

    @Before
    public void setup() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.GAME_MANAGEMENT);

        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE, GAME_MAMMAL, "ahma", "ahma-sv", "ahma-en");
        spa = model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies, 1.0f, 1);
        spa.setBeginDate(new LocalDate(2020, 12, 1));
        spa.setEndDate(new LocalDate(2020, 12, 31));
        spa.setValidityYears(2);

        application.setSpeciesAmounts(ImmutableList.of(spa));

        gameManagementPermitApplication = model().newGameManagementPermitApplication(application);
        gameManagementPermitApplication.setAreaDescription("Area description");
        gameManagementPermitApplication.setJustification("Justification");
        gameManagementPermitApplication.setForbiddenMethods(new DerogationPermitApplicationForbiddenMethods());
    }

    @Test
    public void testValidateContent() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            GameManagementPermitApplicationValidator.validateContent(application, gameManagementPermitApplication);
        });
    }


    @Test
    public void testValidateContent_invalidAreaInformation() {
        gameManagementPermitApplication.setGeoLocation(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            GameManagementPermitApplicationValidator.validateContent(
                                    application,
                                    gameManagementPermitApplication));
            assertThat(illegalStateException.getMessage(), is(equalTo("Geolocation missing")));
        });
    }

    @Test
    public void testValidateContent_areaAttachmentMissing() {
        gameManagementPermitApplication.setAreaDescription(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            GameManagementPermitApplicationValidator.validateContent(
                                    application,
                                    gameManagementPermitApplication));
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
                            GameManagementPermitApplicationValidator.validateContent(
                                    application,
                                    gameManagementPermitApplication));
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
                            GameManagementPermitApplicationValidator.validateContent(
                                    application,
                                    gameManagementPermitApplication));
            assertThat(illegalStateException.getMessage(), is(equalTo("Code missing for permit holder")));
        });
    }

    @Test
    public void testValidateContent_invalidJustification() {
        gameManagementPermitApplication.setJustification(null);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            GameManagementPermitApplicationValidator.validateContent(
                                    application,
                                    gameManagementPermitApplication));
            assertThat(illegalStateException.getMessage(), is(equalTo("Required information missing: justification")));
        });
    }

    @Test
    public void testValidateContent_invalidForbiddenMethods() {
        final DerogationPermitApplicationForbiddenMethods methods = gameManagementPermitApplication.getForbiddenMethods();
        methods.setDeviateSection32("Section 32");
        methods.setDeviateSection33("Section 33");
        methods.setDeviateSection34("Section 34");
        methods.setDeviateSection35("Section 35");
        methods.setDeviateSection51("Section 51");
        gameManagementPermitApplication.setForbiddenMethods(methods);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            GameManagementPermitApplicationValidator.validateContent(
                                    application,
                                    gameManagementPermitApplication));
            assertThat(illegalStateException.getMessage(), is(equalTo("Forbidden methods used is null")));
        });
    }

    @Test
    public void testValidateContent_invalidSpeciesAmounts() {
        final GameSpecies newSpecies = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE, UNPROTECTED, "peltopyy", "peltopyy-sv", "peltopyy-en");
        final HarvestPermitApplicationSpeciesAmount newSpa = model().newHarvestPermitApplicationSpeciesAmount(application, newSpecies, 2.0f, 1);
        newSpa.setBeginDate(new LocalDate(2020, 12, 1));
        newSpa.setEndDate(new LocalDate(2020, 12, 31));

        application.setSpeciesAmounts(ImmutableList.of(spa, newSpa));

        onSavedAndAuthenticated(createNewUser(), () -> {
            final IllegalStateException illegalStateException =
                    assertThrows(IllegalStateException.class, () ->
                            GameManagementPermitApplicationValidator.validateContent(
                                    application,
                                    gameManagementPermitApplication));
            assertThat(illegalStateException.getMessage(), is(equalTo("Incorrect species amounts")));
        });
    }
}
