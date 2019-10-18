package fi.riista.feature.permit.application.bird;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Persistable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class BirdPermitApplicationTest {

    private final List<Persistable<?>> transientEntityList = new ArrayList<>();
    private final EntitySupplier model = new EntitySupplier(NumberSequence.INSTANCE,
            transientEntityList, () -> new Riistakeskus("", ""));

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;
    private GameSpecies unprotectedBird;
    private GameSpecies fowl;

    private HarvestPermitApplication application;

    private BirdPermitApplication birdApplication;

    @DataPoints
    public static final List<String> causeSetters = ImmutableList.of(
            "setCausePublicHealth",
            "setCausePublicSafety",
            "setCauseAviationSafety",
            "setCauseCropsDamage",
            "setCauseDomesticPets",
            "setCauseForestDamage",
            "setCauseFishing",
            "setCauseWaterSystem",
            "setCauseFlora",
            "setCauseFauna",
            "setCauseResearch");

    private final List<String> allowedCauseSetters = ImmutableList.of(
            "setCausePublicHealth",
            "setCausePublicSafety",
            "setCauseAviationSafety",
            "setCauseCropsDamage",
            "setCauseDomesticPets",
            "setCauseForestDamage",
            "setCauseFishing",
            "setCauseWaterSystem");

    @Before
    public void setup() {
        rhy = model.newRiistanhoitoyhdistys();
        area = model.newHarvestPermitArea();
        unprotectedBird = model.newGameSpecies(123, GameCategory.UNPROTECTED, "varis", "varis", "varis");
        unprotectedBird.setId(1L);
        fowl = model.newGameSpecies(456, GameCategory.FOWL, "naakka", "naakka", "naakka");
        fowl.setId(2L);
    }

    @After
    public void tearDown() {
        transientEntityList.clear();
    }

    @Test
    public void limitlessPermitNotAllowedForUndefinedTypeOfArea() {

        initializeValidBirdPermitFor(unprotectedBird);
        birdApplication.getProtectedArea().setProtectedAreaType(ProtectedAreaType.OTHER);

        assertFalse(birdApplication.isLimitlessPermitAllowed());

    }

    @Theory
    public void limitlessAllowedOtherThanUndefined(ProtectedAreaType type) {
        assumeFalse(type.equals(ProtectedAreaType.OTHER));

        initializeValidBirdPermitFor(unprotectedBird);
        birdApplication.getProtectedArea().setProtectedAreaType(type);

        assertTrue(birdApplication.isLimitlessPermitAllowed());

    }

    @Theory
    public void limitlessAllowedForOnlyCertainCauses(String setterNameForCause) throws Exception {
        assumeTrue(allowedCauseSetters.contains(setterNameForCause));

        initializeValidBirdPermitFor(unprotectedBird);

        Method setter = BirdPermitApplicationCause.class.getMethod(setterNameForCause, boolean.class);
        setter.invoke(birdApplication.getCause(), true);

        assertTrue(birdApplication.isLimitlessPermitAllowed());
    }

    @Theory
    public void limitlessNotllowedForCertainCauses(String setterNameForCause) throws Exception {
        assumeFalse(allowedCauseSetters.contains(setterNameForCause));

        initializeValidBirdPermitFor(unprotectedBird);

        Method setter = BirdPermitApplicationCause.class.getMethod(setterNameForCause, boolean.class);
        setter.invoke(birdApplication.getCause(), true);

        assertFalse(birdApplication.isLimitlessPermitAllowed());
    }

    private void initializeValidBirdPermitFor(GameSpecies species) {
        application = model.newHarvestPermitApplication(rhy, area, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount speciesAmount = createValidSpeciesAmount(application, species);
        application.setSpeciesAmounts(ImmutableList.of(speciesAmount));
        birdApplication = model.newBirdPermitApplication(application);
    }

    private HarvestPermitApplicationSpeciesAmount createValidSpeciesAmount(HarvestPermitApplication application,
                                                                           GameSpecies species) {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model.newHarvestPermitApplicationSpeciesAmount(application, species, 30.0f);
        speciesAmount.setBeginDate(DateUtil.today());
        speciesAmount.setEndDate(DateUtil.today());
        speciesAmount.setValidityYears(1);
        speciesAmount.setCausedDamageAmount(3000);
        speciesAmount.setCausedDamageDescription("Vahingon kuvaus");
        speciesAmount.setEvictionMeasureDescription("Kartoitustoimet");
        speciesAmount.setEvictionMeasureEffect("Karkoitustoimien vaikutukset");
        speciesAmount.setPopulationAmount("Noin 200");
        speciesAmount.setPopulationDescription("Kuvaus kannan tilasta");
        return speciesAmount;
    }
}
