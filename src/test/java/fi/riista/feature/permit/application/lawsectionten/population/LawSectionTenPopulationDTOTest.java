package fi.riista.feature.permit.application.lawsectionten.population;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.EUROPEAN_BEAVER;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.PARTRIDGE;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(Theories.class)
public class LawSectionTenPopulationDTOTest {


    @Theory
    public void testCategory(final HarvestPermitCategory category) {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(category);

        final boolean isValidCategory = Arrays.asList(EUROPEAN_BEAVER, PARTRIDGE).contains(category);

        assertThat(dto.isValidCategory(), equalTo(isValidCategory));

    }

    // BEAVER

    @Test
    public void testBeaver() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(EUROPEAN_BEAVER);
        dto.setJustification("justification");
        dto.setPopulationDescription("population");
        dto.setDamagesCaused("damages");

        assertThat(dto.isBeaverDataValid(), is(true));
        assertThat(dto.isPartridgeDataValid(), is(true));
    }

    @Test
    public void testBeaver_justificationMissing() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(EUROPEAN_BEAVER);
        dto.setJustification("justification");
        dto.setDamagesCaused("damages");

        assertThat(dto.isBeaverDataValid(), is(false));
    }

    @Test
    public void testBeaver_damagesMissing() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(EUROPEAN_BEAVER);
        dto.setPopulationDescription("population");
        dto.setJustification("justification");

        assertThat(dto.isBeaverDataValid(), is(false));
    }

    @Test
    public void testBeaver_populationDescriptionMissing() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(EUROPEAN_BEAVER);
        dto.setPopulationDescription("population");
        dto.setDamagesCaused("damages");

        assertThat(dto.isBeaverDataValid(), is(false));
    }

    @Test
    public void testBeaver_extraFields() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(EUROPEAN_BEAVER);
        dto.setJustification("justification");
        dto.setPopulationDescription("population");
        dto.setDamagesCaused("damages");

        dto.setTransferredAnimalOrigin("origin");

        assertThat(dto.isBeaverDataValid(), is(false));
    }

    // PARTRIDGE

    @Test
    public void testPartridge() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(PARTRIDGE);
        dto.setJustification("justification");
        dto.setPopulationDescription("population");
        dto.setTransferredAnimalOrigin("origin");
        dto.setTransferredAnimalAmount(1500);

        assertThat(dto.isPartridgeDataValid(), is(true));
        assertThat(dto.isBeaverDataValid(), is(true));
    }
    @Test
    public void testPartridge_justificationMissing() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(PARTRIDGE);
        dto.setPopulationDescription("population");
        dto.setTransferredAnimalOrigin("origin");
        dto.setTransferredAnimalAmount(1500);

        assertThat(dto.isPartridgeDataValid(), is(false));
    }
    @Test
    public void testPartridgePopulationDesctiptionMissing() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(PARTRIDGE);
        dto.setJustification("justification");
        dto.setTransferredAnimalOrigin("origin");
        dto.setTransferredAnimalAmount(1500);

        assertThat(dto.isPartridgeDataValid(), is(false));
    }
    @Test
    public void testPartridge_originMissing() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(PARTRIDGE);
        dto.setJustification("justification");
        dto.setPopulationDescription("population");
        dto.setTransferredAnimalAmount(1500);

        assertThat(dto.isPartridgeDataValid(), is(false));
    }
    @Test
    public void testPartridgeAmountMissing() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(PARTRIDGE);
        dto.setJustification("justification");
        dto.setPopulationDescription("population");
        dto.setTransferredAnimalOrigin("origin");

        assertThat(dto.isPartridgeDataValid(), is(false));
    }
    @Test
    public void testPartridge_extraFields() {
        final LawSectionTenPopulationDTO dto = new LawSectionTenPopulationDTO();
        dto.setCategory(PARTRIDGE);
        dto.setJustification("justification");
        dto.setPopulationDescription("population");
        dto.setTransferredAnimalOrigin("origin");
        dto.setTransferredAnimalAmount(1500);

        dto.setDamagesCaused("damages");

        assertThat(dto.isPartridgeDataValid(), is(false));
    }
}
