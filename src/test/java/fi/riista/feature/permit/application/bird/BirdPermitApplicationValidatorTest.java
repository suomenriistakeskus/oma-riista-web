package fi.riista.feature.permit.application.bird;

import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Arrays;

import static fi.riista.util.DateUtil.today;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class BirdPermitApplicationValidatorTest implements DefaultEntitySupplierProvider {

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;
    private GameSpecies unprotectedBird;
    private GameSpecies fowl;

    private HarvestPermitApplication application;

    private BirdPermitApplication birdApplication;

    @DataPoints
    public static final Method[] forbiddenMethods = DerogationPermitApplicationForbiddenMethods.class.getMethods();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        rhy = getEntitySupplier().newRiistanhoitoyhdistys();
        area = getEntitySupplier().newHarvestPermitArea();
        unprotectedBird = getEntitySupplier().newGameSpecies(123, GameCategory.UNPROTECTED, "varis", "varis", "varis");
        unprotectedBird.setId(1L);
        fowl = getEntitySupplier().newGameSpecies(456, GameCategory.FOWL, "naakka", "naakka", "naakka");
        fowl.setId(2L);
    }

    @Test
    public void validateContent_fowl() {
        initializeValidBirdPermitFor(fowl);

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateContent_unprotected() {
        initializeValidBirdPermitFor(unprotectedBird);

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateSend() {
        initializeValidBirdPermitFor(fowl);

        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setSubmitDate(null);
        application.setApplicationNumber(null);

        BirdPermitApplicationValidator.validateForSending(application, birdApplication);
    }

    @Theory
    public void validateContent_permitHolderNameMissing(final PermitHolder.PermitHolderType type) {
        initializeValidBirdPermitFor(fowl);
        thrown.expect(IllegalStateException.class);

        application.setPermitHolder(PermitHolder.create(null, "12345", type));

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Theory
    public void validateContent_permitHolderCodeMissing(final PermitHolder.PermitHolderType type) {
        if (!type.equals(PermitHolder.PermitHolderType.PERSON)) {
            thrown.expect(IllegalStateException.class);
        }
        initializeValidBirdPermitFor(fowl);

        application.setPermitHolder(PermitHolder.create("nimi", null, type));

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateAllValidityYearsMustMatchForAllSpecies() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("All validity years must match");

        initializeValidBirdPermitFor(unprotectedBird, fowl);

        application.getSpeciesAmounts().get(0).setValidityYears(1);
        application.getSpeciesAmounts().get(1).setValidityYears(2);

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateContent_limitlessForFowl() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Validity years must be between 1 and 5");

        initializeValidBirdPermitFor(fowl);

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateContent_limitlessForUndefinedArea() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Validity years must be between 1 and 5");

        initializeValidBirdPermitFor(unprotectedBird);

        birdApplication.getProtectedArea().setProtectedAreaType(ProtectedAreaType.OTHER);
        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateContent_noRhy() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Application RHY is not available");

        initializeValidBirdPermitFor(unprotectedBird);

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));
        application.setRhy(null);

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateContent_limitlessForResearch() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Validity years must be between 1 and 5");

        initializeValidBirdPermitFor(unprotectedBird);

        birdApplication.getCause().setCauseResearch(true);
        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateContent_noCauseSelected() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("No reasons selected");

        initializeValidBirdPermitFor(unprotectedBird);

        clearAllCauses();

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    @Test
    public void validateContent_populationAmountMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing:population amount for naakka");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setPopulationAmount(null)));
    }

    @Test
    public void validateContent_populationDescriptionMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing:population description for naakka");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setPopulationDescription(null)));
    }

    @Test
    public void validateContent_evictionMeasureDescriptionMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing:eviction methods for naakka");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setEvictionMeasureDescription(null)));
    }

    @Test
    public void validateContent_evictionMeasureMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing:eviction measures effect for naakka");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setEvictionMeasureEffect(null)));
    }

    @Test
    public void validateContent_causedDamageDescriptionMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing:damage description");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setCausedDamageDescription(null)));
    }

    @Test
    public void validateContent_causedDamageAmountMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Invalid damage amount");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setCausedDamageAmount(null)));
    }

    @Test
    public void validateContent_forbiddenMethodJustification() {

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setForbiddenMethodJustification(null)));
    }

    @Theory
    public void validateContent_forbiddenMethodUseCannotBeNull(Method setter) {

        assumeTrue(setter.getName().startsWith("set"));
        assumeTrue(setter.getParameterCount() == 1);

        thrown.expect(IllegalStateException.class);

        initializeApplicationAndUpdateForbiddenMethod(setter, null, "Peruste");
    }

    @Theory
    public void validateContent_justificationMustBePresentWhenForbiddenMethodsUsed(final Method setter,
                                                                                   final Boolean methodsInUse,
                                                                                   final Boolean justificationPresent) {
        assumeTrue(setter.getName().startsWith("set"));
        assumeTrue(setter.getParameterCount() == 1);

        if (methodsInUse && !justificationPresent) {
            // If method requested, justification must be present
            thrown.expect(IllegalStateException.class);
        }

        initializeApplicationAndUpdateForbiddenMethod(setter, methodsInUse, justificationPresent ? "Peruste" : "");
    }

    @Test
    public void validateContent_speciesBeginDateMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Date missing for species");

        initializeApplicationAndValidateAfterRunning(() -> {
            application.getSpeciesAmounts()
                    .forEach(spa -> {
                        spa.setBeginDate(null);
                    });
        });
    }

    @Test
    public void validateContent_speciesEndDateMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Date missing for species naakka");

        initializeApplicationAndValidateAfterRunning(() -> {
            application.getSpeciesAmounts()
                    .forEach(spa -> {
                        spa.setEndDate(null);
                    });
        });
    }

    @Test
    public void validateContent_speciesPeriodOverYear() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Invalid time period for species naakka");

        initializeApplicationAndValidateAfterRunning(() -> {
            application.getSpeciesAmounts()
                    .forEach(spa -> {
                        spa.setBeginDate(new LocalDate(2018, 1, 1));
                        spa.setEndDate(new LocalDate(2019, 1, 1));
                    });
        });
    }

    @Test
    public void validateContent_attachmentList() {
        initializeApplicationAndValidateAfterRunning(() ->
                application.setAttachments(
                        singletonList(getEntitySupplier().newHarvestPermitApplicationAttachment(application))));
    }

    @Test
    public void validateContent_attachmentListEmpty_descriptionPresent() {
        initializeApplicationAndValidateAfterRunning(() -> {
            application.setAttachments(emptyList());
            birdApplication.setAreaDescription("description");
        });
    }

    @Test(expected = IllegalStateException.class)
    public void validateContent_attachmentListEmpty() {
        initializeApplicationAndValidateAfterRunning(() -> application.setAttachments(emptyList()));
    }

    @Test(expected = IllegalStateException.class)
    public void validateContent_protectedAreaAttachmentMissing() {
        initializeApplicationAndValidateAfterRunning(() -> application.getAttachments()
                .forEach(a -> a.setAttachmentType(HarvestPermitApplicationAttachment.Type.OTHER)));
    }

    private void initializeApplicationAndUpdateForbiddenMethod(final Method setter, final Boolean methodsInUse,
                                                               final String justification) {
        final boolean isParameterString = setter.getParameterTypes()[0].equals(String.class);
        initializeApplicationAndValidateAfterRunning(() -> {
            // Justification mandatory when methods requested
            try {
                setter.invoke(birdApplication.getForbiddenMethods(), isParameterString ? "test" : true);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            application.getSpeciesAmounts()
                    .forEach(spa -> {
                        spa.setForbiddenMethodsUsed(methodsInUse);
                        spa.setForbiddenMethodJustification(justification);
                    });
        });
    }

    private void initializeApplicationAndValidateAfterRunning(final Runnable runnable) {

        initializeValidBirdPermitFor(fowl);

        runnable.run();

        BirdPermitApplicationValidator.validateContent(application, birdApplication);
    }

    private void clearAllCauses() {
        Arrays.stream(BirdPermitApplicationCause.class.getMethods())
                .filter(m -> m.getName().startsWith("setCause"))
                .forEach(m -> {
                    try {
                        m.invoke(birdApplication.getCause(), false);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void initializeValidBirdPermitFor(final GameSpecies... speciesArray) {
        application = getEntitySupplier().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.BIRD);
        application.setSpeciesAmounts(Arrays.stream(speciesArray)
                .map(species -> createValidSpeciesAmount(application, species))
                .collect(toList()));

        birdApplication = getEntitySupplier().newBirdPermitApplication(application);
        birdApplication.getCause().setCauseAviationSafety(true);

        final HarvestPermitApplicationAttachment protectedArea = new HarvestPermitApplicationAttachment();
        protectedArea.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        application.setAttachments(singletonList(protectedArea));
    }

    private HarvestPermitApplicationSpeciesAmount createValidSpeciesAmount(final HarvestPermitApplication application,
                                                                           final GameSpecies species) {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                getEntitySupplier().newHarvestPermitApplicationSpeciesAmount(application, species, 30.0f);
        speciesAmount.setBeginDate(today());
        speciesAmount.setEndDate(today());
        speciesAmount.setValidityYears(1);
        speciesAmount.setCausedDamageAmount(3000);
        speciesAmount.setCausedDamageDescription("Vahingon kuvaus");
        speciesAmount.setEvictionMeasureDescription("Karkotustoimet");
        speciesAmount.setEvictionMeasureEffect("Karkotustoimien vaikutukset");
        speciesAmount.setPopulationAmount("Noin 200");
        speciesAmount.setPopulationDescription("Kuvaus kannan tilasta");

        return speciesAmount;
    }
}
