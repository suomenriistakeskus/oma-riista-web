package fi.riista.feature.permit.decision;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.PermitTypeCode;
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
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DEPORTATION;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DOG_DISTURBANCE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DOG_UNLEASH;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.IMPORTING;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LAW_SECTION_TEN;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MAMMAL;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE_NEW;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.RESEARCH;
import static fi.riista.feature.permit.PermitTypeCode.ANNUAL_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.DOG_DISTURBANCE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.DOG_UNLEASH_BASED;
import static fi.riista.feature.permit.PermitTypeCode.FORBIDDEN_METHODS;
import static fi.riista.feature.permit.PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.LAW_SECTION_TEN_BASED;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.HARVEST_PERMIT;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.NO_PAYMENT;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.PRICE_DEROGATION;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.PRICE_DOG_EVENT;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.PRICE_FORBIDDEN_METHOD;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.PRICE_IMPORTING;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.PRICE_LAW_SECTION_TEN;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.PRICE_MOOSELIKE;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmount.PRICE_RESEARCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class PermitDecisionPaymentAmountTest implements DefaultEntitySupplierProvider {

    private final EntitySupplier model = getEntitySupplier();

    @Test
    public void testMooselike() {
        assertPayment(MOOSELIKE, PermitTypeCode.MOOSELIKE, PRICE_MOOSELIKE);
    }

    @Test
    public void testMooselikeNew() {
        final List<BigDecimal> options = getDecisionPaymentOptionsFor(MOOSELIKE_NEW, PermitTypeCode.MOOSELIKE_AMENDMENT, HARVEST_PERMIT);

        assertThat(options, hasSize(1));
        assertThat(options, Matchers.contains(NO_PAYMENT));
    }

    @Test
    public void testBird() {
        assertPayment(BIRD, ANNUAL_UNPROTECTED_BIRD, PRICE_DEROGATION);
        assertPayment(BIRD, FOWL_AND_UNPROTECTED_BIRD, PRICE_DEROGATION);
    }

    @Test
    public void testMammal() {
        assertPayment(MAMMAL, MAMMAL_DAMAGE_BASED, PRICE_DEROGATION);
    }

    @Test
    public void testDogPermit() {
        assertPayment(DOG_DISTURBANCE, DOG_DISTURBANCE_BASED, PRICE_DOG_EVENT);
        assertPayment(DOG_UNLEASH, DOG_UNLEASH_BASED, PRICE_DOG_EVENT);
    }

    @Test
    public void testSectionTen() {
        assertPayment(LAW_SECTION_TEN, LAW_SECTION_TEN_BASED, PRICE_LAW_SECTION_TEN);
    }

    @Test
    public void testDeportation() {
        final List<BigDecimal> options = getDecisionPaymentOptionsFor(DEPORTATION, PermitTypeCode.DEPORTATION,  HARVEST_PERMIT);

        assertThat(options, hasSize(1));
        assertThat(options, Matchers.contains(NO_PAYMENT));
    }

    @Test
    public void testResearch() {
        assertPayment(RESEARCH, PermitTypeCode.RESEARCH, PRICE_RESEARCH);
    }

    @Test
    public void testImporting() {
        assertPayment(IMPORTING, PermitTypeCode.IMPORTING, PRICE_IMPORTING);
    }

    @Test
    public void testForbiddenMethods() {
        assertPayment(BIRD, FORBIDDEN_METHODS, PRICE_FORBIDDEN_METHOD);
        assertPayment(MAMMAL, FORBIDDEN_METHODS, PRICE_FORBIDDEN_METHOD);
    }


    private void assertPayment(final HarvestPermitCategory category, final String permitTypeCode,
                               final BigDecimal expectedPayment) {
        final List<BigDecimal> options = getDecisionPaymentOptionsFor(category, permitTypeCode, HARVEST_PERMIT);
        assertThat(options, hasSize(2));
        assertThat(options, Matchers.contains(NO_PAYMENT, expectedPayment));
    }

    @Theory
    public void testAllOtherTypesHaveNoPaymentOptions(HarvestPermitCategory category, PermitDecision.DecisionType decisionType) {
        assumeTrue(decisionType != HARVEST_PERMIT);

        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(category, 1);
        final List<BigDecimal> options = getDecisionPaymentOptionsFor(category, permitTypeCode, decisionType);

        assertThat(options, hasSize(1));
        assertThat(options, Matchers.contains(NO_PAYMENT));
    }

    private List<BigDecimal> getDecisionPaymentOptionsFor(final HarvestPermitCategory category,
                                                          final String permitTypeCode,
                                                          final PermitDecision.DecisionType decisionType) {
        final HarvestPermitApplication application = model.newHarvestPermitApplication(
                model.newRiistanhoitoyhdistys(), model.newHarvestPermitArea(), category);

        final HarvestPermitApplicationSpeciesAmount spa = model.newHarvestPermitApplicationSpeciesAmount(application, model.newGameSpecies(), 5.0f);
        spa.setValidityYears(1);
        application.setSpeciesAmounts(ImmutableList.of(spa));

        final PermitDecision decision = model.newPermitDecision(application);
        decision.setDecisionType(decisionType);
        decision.setPermitTypeCode(permitTypeCode);

        return PermitDecisionPaymentAmount.getPaymentOptionsFor(decision);
    }
}
