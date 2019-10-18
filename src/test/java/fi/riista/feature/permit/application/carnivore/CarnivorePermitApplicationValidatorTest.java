package fi.riista.feature.permit.application.carnivore;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitSpecies;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Theories.class)
public class CarnivorePermitApplicationValidatorTest implements DefaultEntitySupplierProvider, ValueGeneratorMixin {

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;

    private GameSpecies bear;
    private GameSpecies lynx;
    private GameSpecies wolf;

    private Map<HarvestPermitCategory, GameSpecies> speciesCategoryMappings;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        rhy = getEntitySupplier().newRiistanhoitoyhdistys();
        area = getEntitySupplier().newHarvestPermitArea();

        bear = createBearSpecies(1L);
        lynx = createLynxSpecies(2L);
        wolf = createWolfSpecies(3L);

        speciesCategoryMappings = ImmutableMap.of(
                HarvestPermitCategory.LARGE_CARNIVORE_BEAR, bear,
                HarvestPermitCategory.LARGE_CARNIVORE_LYNX, lynx,
                HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO, lynx,
                HarvestPermitCategory.LARGE_CARNIVORE_WOLF, wolf);
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Test
    public void validateContent_whenRhyIsMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Application RHY is not available");

        testMutationForValidApplication(carnivorePermitApplication -> {
            carnivorePermitApplication
                    .getHarvestPermitApplication()
                    .setRhy(null);
        });
    }

    @Theory
    public void validateContent_withMissingPermitHolderName(final PermitHolder.PermitHolderType type) {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Permit holder name missing");

        testMutationForValidApplication(carnivorePermitApplication -> {
            carnivorePermitApplication
                    .getHarvestPermitApplication()
                    .setPermitHolder(PermitHolder.create(null, "12345", type));
        });
    }

    @Theory
    public void validateContent_withMissingPermitHolderCode(final PermitHolder.PermitHolderType type) {
        if (!type.equals(PermitHolder.PermitHolderType.PERSON)) {
            thrown.expect(IllegalStateException.class);
        }

        testMutationForValidApplication(carnivorePermitApplication -> {
            carnivorePermitApplication
                    .getHarvestPermitApplication()
                    .setPermitHolder(PermitHolder.create("nimi", null, type));
        });
    }

    @Test
    public void validateContent_withMissingAdditionalJustificationInfo() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing: additional justification info");

        testMutationForValidApplication(carnivorePermitApplication -> {
            carnivorePermitApplication.setAdditionalJustificationInfo(null);
        });
    }

    @Test
    public void validateContent_withMissingAlternativeMeasures() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing: alternative measures");

        testMutationForValidApplication(carnivorePermitApplication -> {
            carnivorePermitApplication.setAlternativeMeasures(null);
        });
    }

    @Test
    public void validateContent_whenSpeciesAmountsMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("speciesAmounts are missing");

        final CarnivorePermitApplication carnivoreApplication = createValidCarnivorePermitApplication();
        final HarvestPermitApplication application = carnivoreApplication.getHarvestPermitApplication();

        final HarvestPermitApplicationSpeciesAmount existingSpeciesAmount = application.getSpeciesAmounts().get(0);
        existingSpeciesAmount.setHarvestPermitApplication(null);

        application.setSpeciesAmounts(emptyList());

        doValidateContent(carnivoreApplication);
    }

    @Test
    public void validateContent_withMultipleSpeciesAmounts() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Exactly one species amount must be given");

        final CarnivorePermitApplication carnivoreApplication =
                createValidCarnivorePermitApplication(HarvestPermitCategory.LARGE_CARNIVORE_BEAR);

        final HarvestPermitApplication application = carnivoreApplication.getHarvestPermitApplication();

        final HarvestPermitApplicationSpeciesAmount existingBearAmount = application.getSpeciesAmounts().get(0);
        final HarvestPermitApplicationSpeciesAmount newWolfAmount = createValidSpeciesAmount(application, wolf);

        application.setSpeciesAmounts(asList(existingBearAmount, newWolfAmount));

        doValidateContent(carnivoreApplication);
    }

    @Test
    public void validateContent_withInvalidSpecies() {
        final GameSpecies invalidSpecies = getEntitySupplier().newGameSpeciesMoose();
        invalidSpecies.setId(99L);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Invalid species given: " + invalidSpecies.getOfficialCode());

        final CarnivorePermitApplication carnivoreApplication =
                createValidCarnivorePermitApplication(HarvestPermitCategory.LARGE_CARNIVORE_BEAR, invalidSpecies);

        doValidateContent(carnivoreApplication);
    }

    @Test
    public void validateContent_beginDateIsNull() {
        streamValidCarnivorePermitApplications().forEach(carnivoreApplication -> {

            mutateSpeciesAmount(carnivoreApplication, spa -> {
                spa.setBeginDate(null);
            });

            expectValidationFailure(carnivoreApplication, "Begin date must not be null");
        });
    }

    @Test
    public void validateContent_endDateIsNull() {
        streamValidCarnivorePermitApplications().forEach(carnivoreApplication -> {

            mutateSpeciesAmount(carnivoreApplication, spa -> {
                spa.setEndDate(null);
            });

            expectValidationFailure(carnivoreApplication, "End date must not be null");
        });
    }

    @Test
    public void validateContent_whenSpeciesAmountMissingPopulationAmount() {
        streamValidCarnivorePermitApplications().forEach(carnivoreApplication -> {

            mutateSpeciesAmount(carnivoreApplication, spa -> {
                spa.setPopulationAmount(null);
                expectValidationFailure(carnivoreApplication,
                        "Required information missing: population amount for " + getSpeciesName(spa));
            });
        });
    }

    @Test
    public void validateContent_invalidPeriod() {
        streamValidCarnivorePermitApplications().forEach(carnivoreApplication -> {

            mutateSpeciesAmount(carnivoreApplication, spa -> {
                spa.setBeginDate(new LocalDate(2019, 8, 1));

            });

            expectValidationFailure(carnivoreApplication,
                    "Invalid period");
        });
    }

    @Test
    public void validateContent_whenSpeciesAmountMissingPopulationDescription() {
        streamValidCarnivorePermitApplications().forEach(carnivoreApplication -> {

            mutateSpeciesAmount(carnivoreApplication, spa -> {
                spa.setPopulationDescription(null);
                expectValidationFailure(carnivoreApplication,
                        "Required information missing: population description for " + getSpeciesName(spa));
            });
        });
    }

    private static void mutateSpeciesAmount(final CarnivorePermitApplication carnivoreApplication,
                                            final Consumer<HarvestPermitApplicationSpeciesAmount> speciesAmountMutator) {

        carnivoreApplication.getHarvestPermitApplication().getSpeciesAmounts().forEach(speciesAmountMutator::accept);
    }

    @Test
    public void validateContent_withMissingAreaSize() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Area size must be given as positive integer");

        testMutationForValidApplication(carnivorePermitApplication -> carnivorePermitApplication.setAreaSize(null));
    }

    @Test
    public void validateContent_withZeroAreaSize() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Area size must be given as positive integer");

        testMutationForValidApplication(carnivorePermitApplication -> carnivorePermitApplication.setAreaSize(0));
    }

    @Test
    public void validateContent_withMissingGeolocation() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Geolocation missing");

        testMutationForValidApplication(carnivorePermitApplication -> carnivorePermitApplication.setGeoLocation(null));
    }

    @Test
    public void validateContent_whenAttachmentListIsEmpty() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Area attachment is missing");

        testMutationForValidApplication(carnivorePermitApplication -> {
            carnivorePermitApplication
                    .getHarvestPermitApplication()
                    .setAttachments(emptyList());
        });
    }

    @Test
    public void validateContent_whenAttachmentListIsEmpty_validDescriptionPresent() {


        testMutationForValidApplication(carnivorePermitApplication -> {
            carnivorePermitApplication
                    .getHarvestPermitApplication()
                    .setAttachments(emptyList());
            carnivorePermitApplication.setAreaDescription("Valid free form text area description");
        });
    }

    @Test
    public void validateSend() {
        final CarnivorePermitApplication carnivorePermitApplication = createValidCarnivorePermitApplication();
        final HarvestPermitApplication application = carnivorePermitApplication.getHarvestPermitApplication();

        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setSubmitDate(null);
        application.setApplicationNumber(null);

        doValidateContent(carnivorePermitApplication);
    }

    private void testMutationForValidApplication(final Consumer<CarnivorePermitApplication> mutator) {
        final CarnivorePermitApplication carnivorePermitApplication = createValidCarnivorePermitApplication();
        mutator.accept(carnivorePermitApplication);
        doValidateContent(carnivorePermitApplication);
    }

    private Stream<CarnivorePermitApplication> streamValidCarnivorePermitApplications() {
        return speciesCategoryMappings
                .entrySet()
                .stream()
                .map(entry -> createValidCarnivorePermitApplication(entry.getKey(), entry.getValue()));
    }

    private CarnivorePermitApplication createValidCarnivorePermitApplication() {
        return createValidCarnivorePermitApplication(speciesCategoryMappings.keySet().iterator().next());
    }

    private CarnivorePermitApplication createValidCarnivorePermitApplication(final HarvestPermitCategory category) {
        return createValidCarnivorePermitApplication(category, speciesCategoryMappings.get(category));
    }

    private CarnivorePermitApplication createValidCarnivorePermitApplication(final HarvestPermitCategory category,
                                                                             final GameSpecies species) {
        final HarvestPermitApplication application =
                getEntitySupplier().newHarvestPermitApplication(rhy, area, category);

        application.setSpeciesAmounts(singletonList(createValidSpeciesAmount(application, species)));

        final CarnivorePermitApplication carnivoreApplication =
                getEntitySupplier().newCarnivorePermitApplication(application);

        carnivoreApplication.setAdditionalJustificationInfo("Muut mahdolliset tiedot");
        carnivoreApplication.setAlternativeMeasures("Vaihtoehtoiset toimenpiteet");

        final HarvestPermitApplicationAttachment attachment = new HarvestPermitApplicationAttachment();
        attachment.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        application.setAttachments(singletonList(attachment));

        // Ensure validity of application before returning it.
        doValidateContent(carnivoreApplication);

        return carnivoreApplication;
    }

    private HarvestPermitApplicationSpeciesAmount createValidSpeciesAmount(final HarvestPermitApplication application,
                                                                           final GameSpecies species) {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                getEntitySupplier().newHarvestPermitApplicationSpeciesAmount(application, species, 5.0f);

        final Range<LocalDate> range =
                CarnivorePermitSpecies.getPeriod(application);
        speciesAmount.setBeginDate(range.lowerEndpoint());
        speciesAmount.setEndDate(range.upperEndpoint());
        speciesAmount.setPopulationAmount("Noin 50");
        speciesAmount.setPopulationDescription("Kuvaus kannan tilasta");

        return speciesAmount;
    }

    private static void expectValidationFailure(final CarnivorePermitApplication carnivoreApplication,
                                                final String expectedExceptionMessage) {

        expectValidationFailure(carnivoreApplication, IllegalStateException.class, expectedExceptionMessage);
    }

    private static void expectValidationFailure(final CarnivorePermitApplication carnivoreApplication,
                                                final Class<?> exceptionClass,
                                                final String expectedExceptionMessage) {
        try {
            doValidateContent(carnivoreApplication);
            fail(format("Ei poikkeusta %s",
                    carnivoreApplication.getHarvestPermitApplication().getHarvestPermitCategory()));
            fail(format("Expected %s to be thrown", exceptionClass.getSimpleName()));

        } catch (final Throwable t) {
            if (!exceptionClass.isAssignableFrom(t.getClass())) {
                fail(format("Expected %s to be thrown but got %s (%s)", exceptionClass.getSimpleName(),
                        t.getClass().getSimpleName(),
                        carnivoreApplication.getHarvestPermitApplication().getHarvestPermitCategory()));
            }

            assertEquals(expectedExceptionMessage, t.getMessage());
        }
    }

    private static void doValidateContent(final CarnivorePermitApplication carnivoreApplication) {
        CarnivorePermitApplicationValidator
                .validateContent(carnivoreApplication.getHarvestPermitApplication(), carnivoreApplication);
    }

    private static String getSpeciesName(final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        return speciesAmount.getGameSpecies().getNameFinnish();
    }

    private static Range<LocalDate> resolvePermitPeriod(final CarnivorePermitApplication carnivorePermitApplication) {
        return CarnivorePermitSpecies.getPeriod(carnivorePermitApplication.getHarvestPermitApplication());
    }

    private GameSpecies createBearSpecies(final long id) {
        final GameSpecies bear = getEntitySupplier().newGameSpecies(
                GameSpecies.OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "karhu", "björn", "bear");
        bear.setId(id);
        return bear;
    }

    private GameSpecies createLynxSpecies(final long id) {
        final GameSpecies lynx = getEntitySupplier().newGameSpecies(
                GameSpecies.OFFICIAL_CODE_LYNX, GameCategory.GAME_MAMMAL, "ilves", "lodjur", "lynx");
        lynx.setId(id);
        return lynx;
    }

    private GameSpecies createWolfSpecies(final long id) {
        final GameSpecies wolf = getEntitySupplier().newGameSpecies(
                GameSpecies.OFFICIAL_CODE_WOLF, GameCategory.GAME_MAMMAL, "susi", "varg", "wolf");
        wolf.setId(id);
        return wolf;
    }
}
