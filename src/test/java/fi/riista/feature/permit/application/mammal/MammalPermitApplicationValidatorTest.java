package fi.riista.feature.permit.application.mammal;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
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
import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41A;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FAUNA_41A;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FAUNA_41C;
import static fi.riista.util.DateUtil.today;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class MammalPermitApplicationValidatorTest implements DefaultEntitySupplierProvider {

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;
    private GameSpecies lynxSpecies;
    private GameSpecies rabbitSpecies;
    private GameSpecies hareSpecies;

    private HarvestPermitApplication application;

    private MammalPermitApplication mammalPermitApplication;

    private List<DerogationPermitApplicationReason> reasons;

    @DataPoints
    public static final Method[] forbiddenMethods = DerogationPermitApplicationForbiddenMethods.class.getMethods();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        rhy = getEntitySupplier().newRiistanhoitoyhdistys();
        area = getEntitySupplier().newHarvestPermitArea();
        lynxSpecies = getEntitySupplier().newGameSpecies(GameSpecies.OFFICIAL_CODE_LYNX, GameCategory.GAME_MAMMAL,
                "ilves", "lodjur", "lynx");
        lynxSpecies.setId(1L);
        rabbitSpecies = getEntitySupplier().newGameSpecies(GameSpecies.OFFICIAL_CODE_RABBIT, GameCategory.GAME_MAMMAL
                , "jänis", "jänis-sv", "rabbit");
        rabbitSpecies.setId(2L);

        hareSpecies = getEntitySupplier().newGameSpecies(GameSpecies.OFFICIAL_CODE_BROWN_HARE,
                GameCategory.GAME_MAMMAL, "rusakko", "rusakko-sv", "hare");
        hareSpecies.setId(3L);
    }

    @Test
    public void validateContent_rabbit() {
        initializeValidMammalPermitFor(rabbitSpecies);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void validateContent_lynx() {

        initializeValidMammalPermitFor(lynxSpecies);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void validateSend() {
        initializeValidMammalPermitFor(rabbitSpecies);

        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setSubmitDate(null);
        application.setApplicationNumber(null);

        MammalPermitApplicationValidator.validateForSending(application, mammalPermitApplication, reasons);
    }

    @Theory
    public void validateContent_permitHolderNameMissing(final PermitHolder.PermitHolderType type) {
        initializeValidMammalPermitFor(rabbitSpecies);
        thrown.expect(IllegalStateException.class);

        application.setPermitHolder(PermitHolder.create(null, "12345", type));

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Theory
    public void validateContent_permitHolderCodeMissing(final PermitHolder.PermitHolderType type) {
        if (!type.equals(PermitHolder.PermitHolderType.PERSON)) {
            thrown.expect(IllegalStateException.class);
        }
        initializeValidMammalPermitFor(rabbitSpecies);

        application.setPermitHolder(PermitHolder.create("nimi", null, type));

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void lynxWithTooLongPeriod() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Invalid time period for species ilves");

        initializeValidMammalPermitFor(lynxSpecies);
        final LocalDate beginDate = application.getSpeciesAmounts().get(0).getBeginDate();
        application.getSpeciesAmounts().get(0).setEndDate(beginDate.plusDays(22));
        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void nonPositiveAreaSize() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Area size must be given as positive integer");

        initializeValidMammalPermitFor(rabbitSpecies);

        mammalPermitApplication.setAreaSize(0);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void nullLocation() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Geolocation missing");

        initializeValidMammalPermitFor(rabbitSpecies);

        mammalPermitApplication.setGeoLocation(null);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void nullValidityYears() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Validity years is missing");

        initializeValidMammalPermitFor(rabbitSpecies);

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(null));

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }


    @Test
    public void nonMammalSpecies() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Non-mammal species: 27048");
        final GameSpecies partridge = getEntitySupplier().newGameSpecies(GameSpecies.OFFICIAL_CODE_PARTRIDGE,
                GameCategory.FOWL, "pyy-fi", "pyy-sv", "partridge");
        partridge.setId(5L);
        initializeValidMammalPermitFor(partridge);


        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void invalidValidityYears() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Validity years is invalid");

        initializeValidMammalPermitFor(rabbitSpecies);

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void invalidValidityYears_tooLong() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Validity years is invalid");

        initializeValidMammalPermitFor(rabbitSpecies);

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(6));

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void derogationReasonsMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Derogation reasons not valid");

        initializeValidMammalPermitFor(lynxSpecies);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, emptyList());
    }

    @Test
    public void wrongLawSectionReason() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Derogation reasons not valid");

        initializeValidMammalPermitFor(rabbitSpecies);

        // Rabbit needs to be applied with derogation reason based on law section 41c
        final DerogationPermitApplicationReason fauna41a =
                new DerogationPermitApplicationReason(application, REASON_FAUNA_41A);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, singletonList(fauna41a));
    }

    @Test
    public void extraLawSectionReasons() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Derogation reasons not valid");

        initializeValidMammalPermitFor(rabbitSpecies);

        final DerogationPermitApplicationReason fauna41a =
                new DerogationPermitApplicationReason(application, REASON_FAUNA_41A);
        final DerogationPermitApplicationReason fauna41c =
                new DerogationPermitApplicationReason(application, REASON_FAUNA_41C);
        final ImmutableList<DerogationPermitApplicationReason> reasons = ImmutableList.of(fauna41a, fauna41c);
        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }
    @Test
    public void lynxWithMultipleYears() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Only one year long applications can be applied for species: 46615");

        initializeValidMammalPermitFor(lynxSpecies);
        application.getSpeciesAmounts().get(0).setValidityYears(3);
        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void lynxWithOtherSpecies() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Found species which needs to be applied for separately: 46615");

        initializeValidMammalPermitFor(lynxSpecies, rabbitSpecies);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    @Test
    public void validateAllValidityYearsMustMatchForAllSpecies() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("All validity years must match");

        initializeValidMammalPermitFor(hareSpecies, rabbitSpecies);

        application.getSpeciesAmounts().get(0).setValidityYears(1);
        application.getSpeciesAmounts().get(1).setValidityYears(2);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }


    @Test
    public void validateContent_noRhy() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Application RHY is not available");

        initializeValidMammalPermitFor(lynxSpecies);

        application.getSpeciesAmounts().forEach(spa -> spa.setValidityYears(0));
        application.setRhy(null);

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }


    @Test
    public void validateContent_populationAmountMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing: population amount for jänis");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setPopulationAmount(null)));
    }

    @Test
    public void validateContent_populationDescriptionMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing: population description for jänis");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setPopulationDescription(null)));
    }

    @Test
    public void validateContent_evictionMeasureDescriptionMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing: eviction methods for jänis");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setEvictionMeasureDescription(null)));
    }

    @Test
    public void validateContent_evictionMeasureMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing: eviction measures effect for jänis");

        initializeApplicationAndValidateAfterRunning(() -> application.getSpeciesAmounts()
                .forEach(spa -> spa.setEvictionMeasureEffect(null)));
    }

    @Test
    public void validateContent_causedDamageDescriptionMissing() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Required information missing: damage description");

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
        thrown.expectMessage("Date missing for species jänis");

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
        thrown.expectMessage("Invalid time period for species jänis");

        initializeApplicationAndValidateAfterRunning(() -> {
            application.getSpeciesAmounts()
                    .forEach(spa -> {
                        spa.setBeginDate(new LocalDate(2018, 1, 1));
                        spa.setEndDate(new LocalDate(2019, 1, 1));
                    });
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
                setter.invoke(mammalPermitApplication.getForbiddenMethods(), isParameterString ? "test" : true);
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

        initializeValidMammalPermitFor(rabbitSpecies);

        runnable.run();

        MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication, reasons);
    }

    private void initializeValidMammalPermitFor(final GameSpecies... speciesArray) {
        application = getEntitySupplier().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.MAMMAL);
        application.setSpeciesAmounts(Arrays.stream(speciesArray)
                .map(species -> createValidSpeciesAmount(application, species))
                .collect(toList()));

        mammalPermitApplication = getEntitySupplier().newMammalPermitApplication(application);
        // derogation reasons

        final HarvestPermitApplicationAttachment protectedArea = new HarvestPermitApplicationAttachment();
        protectedArea.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        application.setAttachments(singletonList(protectedArea));
        mammalPermitApplication.setForbiddenMethods(new DerogationPermitApplicationForbiddenMethods());

        reasons = Arrays.stream(speciesArray)
                .map(GameSpecies::getOfficialCode)
                .map(DerogationLawSection::getSpeciesLawSection)
                .map(section -> section == SECTION_41A ? REASON_FAUNA_41A : REASON_FAUNA_41C)
                .distinct()
                .map(type -> new DerogationPermitApplicationReason(application, type))
                .collect(Collectors.toList());
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
