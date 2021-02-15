package fi.riista.feature.permit.decision.species;

import org.junit.Test;

import static fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountDTO.MAX_SPECIES_AMOUNT_VALUE;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.is;

public class PermitDecisionSpeciesAmountDTOTest  {

    @Test
    public void test_rejectedDecision() {
        final PermitDecisionSpeciesAmountDTO dto = new PermitDecisionSpeciesAmountDTO();
        dto.setSpecimenAmount(0.0f);
        dto.setEggAmount(0);
        dto.setConstructionAmount(0);
        dto.setNestAmount(0);

        assertThat(dto.isSomeAmountPresent(), is(true));
        assertThat(dto.isPresentAmountsValid(), is(true));
    }

    @Test
    public void test_maxAmounts() {
        final PermitDecisionSpeciesAmountDTO dto = new PermitDecisionSpeciesAmountDTO();
        dto.setSpecimenAmount(Float.valueOf(MAX_SPECIES_AMOUNT_VALUE));
        dto.setEggAmount(MAX_SPECIES_AMOUNT_VALUE);
        dto.setConstructionAmount(MAX_SPECIES_AMOUNT_VALUE);
        dto.setNestAmount(MAX_SPECIES_AMOUNT_VALUE);

        assertThat(dto.isSomeAmountPresent(), is(true));
        assertThat(dto.isPresentAmountsValid(), is(true));
    }

    @Test
    public void test_allAmountsNull() {
        final PermitDecisionSpeciesAmountDTO dto = new PermitDecisionSpeciesAmountDTO();

        assertThat(dto.isSomeAmountPresent(), is(false));
    }
}
