package fi.riista.feature.permit.application.importing;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static fi.riista.feature.permit.application.PermitHolder.PermitHolderType.PERSON;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.today;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class ImportingPermitApplicationValidatorTest
        implements DefaultEntitySupplierProvider, OrganisationFixtureMixin {

    private EntitySupplier model;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private ImportingPermitApplication importingPermitApplication;

    @Before
    public void setup() {
        model = getEntitySupplier();
        rhy = model.newRiistanhoitoyhdistys();
    }

    @Test
    public void validateContent() {
        initializeValidImportingPermit();

        ImportingPermitApplicationValidator.validateContent(application, importingPermitApplication);
    }

    @Test
    public void validateSend() {
        initializeValidImportingPermit();

        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setSubmitDate(null);
        application.setApplicationNumber(null);

        ImportingPermitApplicationValidator.validateForSending(application, importingPermitApplication);
    }

    @Theory
    public void validateContent_permitHolderNameMissing(final PermitHolder.PermitHolderType type) {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> application.setPermitHolder(PermitHolder.create(null, "12345", type))));
        assertThat(illegalStateException.getMessage(), equalTo("Permit holder name missing"));
    }

    @Theory
    public void validateContent_permitHolderCodeMissing(final PermitHolder.PermitHolderType type) {
        assumeTrue(type != PERSON);

        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> application.setPermitHolder(PermitHolder.create("nimi", null, type))));
        assertThat(illegalStateException.getMessage(), equalTo("Code missing for permit holder"));
    }

    @Test
    public void validateAllValidityYearsMustMatchForAllSpecies() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> {
            initializeValidImportingPermitFor(model.newGameSpecies(), model.newGameSpecies());

            application.getSpeciesAmounts().get(0).setValidityYears(1);
            application.getSpeciesAmounts().get(1).setValidityYears(2);

            ImportingPermitApplicationValidator.validateContent(application, importingPermitApplication);
        });
        assertThat(illegalStateException.getMessage(), equalTo("All validity years must match"));
    }

    @Test
    public void validateContent_geolocationMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> importingPermitApplication.setGeoLocation(null)));
        assertThat(illegalStateException.getMessage(), equalTo("Geolocation missing"));
    }

    @Test
    public void validateContent_countryOfOriginMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> importingPermitApplication.setCountryOfOrigin(null)));
        assertThat(illegalStateException.getMessage(), equalTo("Required information missing: country of origin"));
    }

    @Test
    public void validateContent_detailsMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> importingPermitApplication.setDetails(null)));
        assertThat(illegalStateException.getMessage(), equalTo("Required information missing: details"));
    }

    @Test
    public void validateContent_purposeMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> importingPermitApplication.setPurpose(null)));
        assertThat(illegalStateException.getMessage(), equalTo("Required information missing: purpose"));
    }

    @Test
    public void validateContent_releaseMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> importingPermitApplication.setRelease(null)));
        assertThat(illegalStateException.getMessage(), equalTo("Required information missing: release"));
    }

    @Test
    public void validateContent_speciesBeginDateMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> {
                    application.getSpeciesAmounts()
                            .forEach(spa -> {
                                spa.setBeginDate(null);
                            });
                }));
        assertThat(illegalStateException.getMessage(), equalTo("Begin date must not be null"));
    }

    @Test
    public void validateContent_speciesEndDateMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> {
                    application.getSpeciesAmounts()
                            .forEach(spa -> {
                                spa.setEndDate(null);
                            });
                }));
        assertThat(illegalStateException.getMessage(), equalTo("End date must not be null"));
    }

    @Test
    public void validateContent_speciesPeriodOverYear() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
                initializeApplicationAndValidateAfterRunning(() -> {
                    application.getSpeciesAmounts()
                            .forEach(spa -> {
                                spa.setBeginDate(new LocalDate(2018, 1, 1));
                                spa.setEndDate(new LocalDate(2019, 1, 1));
                            });
                }));
        assertThat(illegalStateException.getMessage(), equalTo("Too long period"));
    }

    @Test
    public void validateContent_attachmentList() {
        initializeApplicationAndValidateAfterRunning(() ->
                application.setAttachments(
                        singletonList(model.newHarvestPermitApplicationAttachment(application))));
    }

    @Test
    public void validateContent_attachmentListEmpty_descriptionPresent() {
        initializeApplicationAndValidateAfterRunning(() -> {
            application.setAttachments(emptyList());
            importingPermitApplication.setAreaDescription("description");
        });
    }

    @Test
    public void validateContent_attachmentListEmpty() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> {
            initializeApplicationAndValidateAfterRunning(() -> application.setAttachments(emptyList()));
        });
        assertThat(illegalStateException.getMessage(), equalTo("Area attachment is missing"));
    }

    @Test
    public void validateContent_protectedAreaAttachmentMissing() {
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> {
            initializeApplicationAndValidateAfterRunning(() -> application.getAttachments()
                    .forEach(a -> a.setAttachmentType(HarvestPermitApplicationAttachment.Type.OTHER)));
        });
        assertThat(illegalStateException.getMessage(), equalTo("Area attachment is missing"));
    }


    private void initializeApplicationAndValidateAfterRunning(final Runnable runnable) {

        initializeValidImportingPermit();

        runnable.run();

        ImportingPermitApplicationValidator.validateContent(application, importingPermitApplication);
    }

    private void initializeValidImportingPermit() {
        initializeValidImportingPermitFor(model.newGameSpecies());
    }

    private void initializeValidImportingPermitFor(final GameSpecies... speciesArray) {
        application = model.newHarvestPermitApplication(rhy, null, HarvestPermitCategory.IMPORTING);
        application.setSpeciesAmounts(Arrays.stream(speciesArray)
                .map(species -> createValidSpeciesAmount(application, species))
                .collect(toList()));

        importingPermitApplication = ImportingPermitApplication.create(application);
        importingPermitApplication.setCountryOfOrigin("Russia");
        importingPermitApplication.setDetails("Details");
        importingPermitApplication.setPurpose("Purpose");
        importingPermitApplication.setRelease("Release");
        importingPermitApplication.setGeoLocation(model.geoLocation());
        final HarvestPermitApplicationAttachment protectedArea = new HarvestPermitApplicationAttachment();
        protectedArea.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        application.setAttachments(singletonList(protectedArea));
    }

    private HarvestPermitApplicationSpeciesAmount createValidSpeciesAmount(final HarvestPermitApplication application,
                                                                           final GameSpecies species) {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model.newHarvestPermitApplicationSpeciesAmountForImporting(
                        application, species, 30, 50, "Sub species name");
        species.setId(model.nextLong());
        speciesAmount.setBeginDate(today());
        speciesAmount.setEndDate(today());
        speciesAmount.setValidityYears(1);

        return speciesAmount;
    }
}
