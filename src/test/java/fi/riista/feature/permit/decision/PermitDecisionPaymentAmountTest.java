package fi.riista.feature.permit.decision;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.List;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.BIRD;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE_NEW;
import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.HARVEST_PERMIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class PermitDecisionPaymentAmountTest implements DefaultEntitySupplierProvider {

    private static final BigDecimal NO_PAYMENT = new BigDecimal("0.00");
    private static final BigDecimal MOOSELIKE_PAYMENT = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, MOOSELIKE);
    private static final BigDecimal BIRD_PAYMENT = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, BIRD);

    private final EntitySupplier model = getEntitySupplier();

    @Test
    public void testMooselike() {

        final List<BigDecimal> options = getDecisionPaymentOptionsFor(MOOSELIKE, HARVEST_PERMIT);

        assertThat(options, hasSize(2));
        assertThat(options, Matchers.contains(NO_PAYMENT, MOOSELIKE_PAYMENT));
    }

    @Test
    public void testMooselikeNew() {

        final List<BigDecimal> options = getDecisionPaymentOptionsFor(MOOSELIKE_NEW, HARVEST_PERMIT);

        assertThat(options, hasSize(1));
        assertThat(options, Matchers.contains(NO_PAYMENT));
    }

    @Test
    public void testBird() {

        final List<BigDecimal> options = getDecisionPaymentOptionsFor(BIRD, HARVEST_PERMIT);

        assertThat(options, hasSize(2));
        assertThat(options, Matchers.contains(NO_PAYMENT, BIRD_PAYMENT));
    }

    @Theory
    public void testAllOtherTypesHaveNoPaymentOptions(HarvestPermitCategory category, PermitDecision.DecisionType decisionType) {
        assumeTrue(decisionType != HARVEST_PERMIT);

        final List<BigDecimal> options = getDecisionPaymentOptionsFor(category, decisionType);

        assertThat(options, hasSize(1));
        assertThat(options, Matchers.contains(NO_PAYMENT));
    }

    private List<BigDecimal> getDecisionPaymentOptionsFor(HarvestPermitCategory category, PermitDecision.DecisionType decisionType) {
        final HarvestPermitApplication application = model.newHarvestPermitApplication(
                model.newRiistanhoitoyhdistys(), model.newHarvestPermitArea(), category);

        final HarvestPermitApplicationSpeciesAmount spa = model.newHarvestPermitApplicationSpeciesAmount(application, model.newGameSpecies(), 5.0f);
        spa.setValidityYears(1);
        application.setSpeciesAmounts(ImmutableList.of(spa));

        final PermitDecision decision = model.newPermitDecision(application);
        decision.setDecisionType(decisionType);

        return PermitDecisionPaymentAmount.getPaymentOptionsFor(decision);
    }
}
