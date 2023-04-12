package fi.riista.feature.permit.decision;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.MockTimeProvider;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.experimental.theories.DataPoints;
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
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmountCommon.PRICE_DEROGATION;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmountCommon.PRICE_FORBIDDEN_METHOD;
import static fi.riista.feature.permit.decision.PermitDecisionPaymentAmountCommon.PRICE_RESEARCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class PermitDecisionPaymentAmountTest implements DefaultEntitySupplierProvider {

    @DataPoints("dates")
    public static final LocalDate[] DATES = {
            new LocalDate(2021, 12,31),
            new LocalDate(2022, 1, 1)
    };

    private final EntitySupplier model = getEntitySupplier();

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Theory
    public void testMooselike(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());
        final BigDecimal expectedPaymentAmount = date.getYear() >= 2022 ?
                PermitDecisionPaymentAmount2022.PRICE_MOOSELIKE :
                PermitDecisionPaymentAmount2021.PRICE_MOOSELIKE;

        assertPayment(MOOSELIKE, PermitTypeCode.MOOSELIKE, expectedPaymentAmount);
    }

    @Theory
    public void testMooselikeNew(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());

        final List<BigDecimal> options = getDecisionPaymentOptionsFor(MOOSELIKE_NEW, PermitTypeCode.MOOSELIKE_AMENDMENT, HARVEST_PERMIT);

        assertThat(options, hasSize(1));
        assertThat(options, Matchers.contains(NO_PAYMENT));
    }

    @Theory
    public void testBird(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());

        assertPayment(BIRD, ANNUAL_UNPROTECTED_BIRD, PRICE_DEROGATION);
        assertPayment(BIRD, FOWL_AND_UNPROTECTED_BIRD, PRICE_DEROGATION);
    }

    @Theory
    public void testMammal(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());

        assertPayment(MAMMAL, MAMMAL_DAMAGE_BASED, PRICE_DEROGATION);
    }

    @Theory
    public void testDogPermit(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());
        final BigDecimal expectedPaymentAmount = date.getYear() >= 2022 ?
                PermitDecisionPaymentAmount2022.PRICE_DOG_EVENT :
                PermitDecisionPaymentAmount2021.PRICE_DOG_EVENT;

        assertPayment(DOG_DISTURBANCE, DOG_DISTURBANCE_BASED, expectedPaymentAmount);
        assertPayment(DOG_UNLEASH, DOG_UNLEASH_BASED, expectedPaymentAmount);
    }

    @Theory
    public void testSectionTen(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());
        final BigDecimal expectedPaymentAmount = date.getYear() >= 2022 ?
                PermitDecisionPaymentAmount2022.PRICE_LAW_SECTION_TEN :
                PermitDecisionPaymentAmount2021.PRICE_LAW_SECTION_TEN;

        assertPayment(LAW_SECTION_TEN, LAW_SECTION_TEN_BASED, expectedPaymentAmount);
    }

    @Theory
    public void testDeportation(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());

        final List<BigDecimal> options = getDecisionPaymentOptionsFor(DEPORTATION, PermitTypeCode.DEPORTATION,  HARVEST_PERMIT);

        assertThat(options, hasSize(1));
        assertThat(options, Matchers.contains(NO_PAYMENT));
    }

    @Theory
    public void testResearch(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());

        assertPayment(RESEARCH, PermitTypeCode.RESEARCH, PRICE_RESEARCH);
    }

    @Theory
    public void testImporting(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());
        final BigDecimal expectedPaymentAmount = date.getYear() >= 2022 ?
                PermitDecisionPaymentAmount2022.PRICE_IMPORTING :
                PermitDecisionPaymentAmount2021.PRICE_IMPORTING;

        assertPayment(IMPORTING, PermitTypeCode.IMPORTING, expectedPaymentAmount);
    }

    @Theory
    public void testForbiddenMethods(final LocalDate date) {
        MockTimeProvider.mockTime(date.toDate().getTime());

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
    public void testAllOtherTypesHaveNoPaymentOptions(final LocalDate date,
                                                      final HarvestPermitCategory category,
                                                      final PermitDecision.DecisionType decisionType) {
        assumeTrue(decisionType != HARVEST_PERMIT);

        MockTimeProvider.mockTime(date.toDate().getTime());

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
