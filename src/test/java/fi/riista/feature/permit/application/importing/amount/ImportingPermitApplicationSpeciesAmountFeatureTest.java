package fi.riista.feature.permit.application.importing.amount;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ERMINE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ImportingPermitApplicationSpeciesAmountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ImportingPermitApplicationSpeciesAmountFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private GameSpecies europeanBeaver;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.NEST_REMOVAL);
        model().newNestRemovalPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        europeanBeaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetSpeciesAmounts_unauthorized() {

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getSpeciesAmounts(application.getId());
        });
    }

    @Test
    public void testGetSpeciesAmounts() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmountForImporting(application, europeanBeaver, 10, 11, null);
        speciesAmount.setSubSpeciesName("Castor fiber");

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final List<ImportingPermitApplicationSpeciesAmountDTO> speciesAmounts =
                    feature.getSpeciesAmounts(application.getId());

            assertThat(speciesAmounts, hasSize(1));
            final ImportingPermitApplicationSpeciesAmountDTO spa =
                    speciesAmounts.get(0);
            assertThat(spa.getGameSpeciesCode(), equalTo(OFFICIAL_CODE_EUROPEAN_BEAVER));
            assertThat(spa.getSpecimenAmount(), equalTo(speciesAmount.getSpecimenAmount().intValue()));
            assertThat(spa.getEggAmount(), equalTo(speciesAmount.getEggAmount()));
            assertThat(spa.getSubSpeciesName(), equalTo(speciesAmount.getSubSpeciesName()));
        });
    }

    @Test
    public void testSaveSpeciesAmounts_create() {
        final ImportingPermitApplicationSpeciesAmountDTO speciesAmountDTO =
                new ImportingPermitApplicationSpeciesAmountDTO();
        speciesAmountDTO.setSpecimenAmount(5);
        speciesAmountDTO.setEggAmount(6);
        speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);
        speciesAmountDTO.setSubSpeciesName("Castor fiber");

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(speciesAmountDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertThat(spa.getGameSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_EUROPEAN_BEAVER));
            assertThat(spa.getSpecimenAmount().intValue(), equalTo(5));
            assertThat(spa.getEggAmount(), equalTo(6));
            assertThat(spa.getConstructionAmount(), is(nullValue()));
            assertThat(spa.getSubSpeciesName(), equalTo(speciesAmountDTO.getSubSpeciesName()));
        });
    }

    @Test
    public void testGetSpeciesAmounts_specimenAmountNull() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmountForImporting(application, europeanBeaver, null, 11,
                        null);
        speciesAmount.setSubSpeciesName("Castor fiber");

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final List<ImportingPermitApplicationSpeciesAmountDTO> speciesAmounts =
                    feature.getSpeciesAmounts(application.getId());

            assertThat(speciesAmounts, hasSize(1));
            final ImportingPermitApplicationSpeciesAmountDTO spa =
                    speciesAmounts.get(0);
            assertThat(spa.getGameSpeciesCode(), equalTo(OFFICIAL_CODE_EUROPEAN_BEAVER));
            assertThat(spa.getSpecimenAmount(), is(nullValue()));
            assertThat(spa.getEggAmount(), equalTo(speciesAmount.getEggAmount()));
            assertThat(spa.getSubSpeciesName(), equalTo(speciesAmount.getSubSpeciesName()));
        });
    }

    @Test
    public void testSaveSpeciesAmounts_onlyEggAmount() {
        final ImportingPermitApplicationSpeciesAmountDTO speciesAmountDTO =
                new ImportingPermitApplicationSpeciesAmountDTO();
        speciesAmountDTO.setSpecimenAmount(null);
        speciesAmountDTO.setEggAmount(6);
        speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);
        speciesAmountDTO.setSubSpeciesName("Castor fiber");

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(speciesAmountDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertThat(spa.getGameSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_EUROPEAN_BEAVER));
            assertThat(spa.getSpecimenAmount(), is(nullValue()));
            assertThat(spa.getEggAmount(), equalTo(6));
            assertThat(spa.getConstructionAmount(), is(nullValue()));
            assertThat(spa.getSubSpeciesName(), equalTo(speciesAmountDTO.getSubSpeciesName()));
        });
    }

    @Test
    public void testSaveSpeciesAmounts_multipleSpecies() {
        final GameSpecies firstSpecies = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE);
        final GameSpecies secondSpecies = model().newGameSpecies(OFFICIAL_CODE_ERMINE);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final ImportingPermitApplicationSpeciesAmountDTO firstDTO =
                    new ImportingPermitApplicationSpeciesAmountDTO();
            firstDTO.setSpecimenAmount(5);
            firstDTO.setEggAmount(6);
            firstDTO.setGameSpeciesCode(firstSpecies.getOfficialCode());
            final ImportingPermitApplicationSpeciesAmountDTO secondDTO =
                    new ImportingPermitApplicationSpeciesAmountDTO();
            secondDTO.setSpecimenAmount(null);
            secondDTO.setEggAmount(9);
            secondDTO.setGameSpeciesCode(secondSpecies.getOfficialCode());
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(firstDTO, secondDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(2));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertThat(spa.getGameSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_PARTRIDGE));
            assertThat(spa.getSpecimenAmount().intValue(), equalTo(5));
            assertThat(spa.getEggAmount(), equalTo(6));
            assertThat(spa.getConstructionAmount(), is(nullValue()));
            assertThat(spa.getSubSpeciesName(), is(nullValue()));

            final HarvestPermitApplicationSpeciesAmount spa2 = all.get(1);
            assertThat(spa2.getGameSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_ERMINE));
            assertThat(spa2.getSpecimenAmount(), is(nullValue()));
            assertThat(spa2.getEggAmount(), equalTo(9));
            assertThat(spa2.getConstructionAmount(), is(nullValue()));
            assertThat(spa2.getSubSpeciesName(), is(nullValue()));

            final List<Integer> speciesCodes = asList(
                    spa.getGameSpecies().getOfficialCode(),
                    spa2.getGameSpecies().getOfficialCode());

            assertThat(speciesCodes, containsInAnyOrder(OFFICIAL_CODE_PARTRIDGE, OFFICIAL_CODE_ERMINE));

        });
    }

    @Test
    public void testSaveSpeciesAmounts_existingSpecimenAmountUpdatedToNull() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmountForImporting(application, europeanBeaver, 10, 11, null);
        speciesAmount.setSubSpeciesName("Castor fiber");

        final ImportingPermitApplicationSpeciesAmountDTO speciesAmountDTO =
                new ImportingPermitApplicationSpeciesAmountDTO();
        speciesAmountDTO.setSpecimenAmount(null);
        speciesAmountDTO.setEggAmount(6);
        speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);
        speciesAmountDTO.setSubSpeciesName("Castor fiber");

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(speciesAmountDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertThat(spa.getGameSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_EUROPEAN_BEAVER));
            assertThat(spa.getSpecimenAmount(), is(nullValue()));
            assertThat(spa.getEggAmount(), equalTo(6));
            assertThat(spa.getConstructionAmount(), is(nullValue()));
            assertThat(spa.getSubSpeciesName(), equalTo(speciesAmountDTO.getSubSpeciesName()));
        });
    }
}
