package fi.riista.feature.permit.invoice.harvest;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.test.TestUtils.bd;
import static fi.riista.test.TestUtils.currency;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class PermitHarvestInvoiceRepositoryTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitHarvestInvoiceRepository repository;

    @Test
    public void testGetMooselikeHarvestInvoicePaymentAmounts() {
        final GameSpecies moose = model().newGameSpeciesMoose();
        final GameSpecies whiteTailedDeer = model().newGameSpecies(OFFICIAL_CODE_WHITE_TAILED_DEER);

        final PermitHarvestInvoice i1 = newHarvestInvoice(moose, true, bd(120), null, null);
        final PermitHarvestInvoice i2 = newHarvestInvoice(moose, true, bd(240), bd(360), bd(360));
        final PermitHarvestInvoice i3 = newHarvestInvoice(moose, true, bd(50), null, bd(40));
        final PermitHarvestInvoice i4 = newHarvestInvoice(moose, true, bd(50), null, bd(60));

        // Should not affect the result because hunting is not finished.
        final PermitHarvestInvoice i5 = newHarvestInvoice(moose, false, bd(120), null, null);

        // Should not affect the result because species differs from what is requested.
        final PermitHarvestInvoice i6 = newHarvestInvoice(whiteTailedDeer, true, bd(120), null, null);

        // Should not affect the result because permit type code will not be correct.
        final PermitHarvestInvoice i7 = newHarvestInvoice(moose, true, bd(120), null, null);
        i7.getSpeciesAmount().getHarvestPermit().setPermitTypeCode(PermitTypeCode.WOLVERINE_DAMAGE_BASED);

        // Relevant but not included in permitIds parameter
        newHarvestInvoice(moose, true, bd(120), null, null);

        persistInNewTransaction();

        final Set<Long> permitIds = extractPermitIds(i1, i2, i3, i4, i5, i6, i7);

        final Map<Long, InvoicePaymentAmountsDTO> result =
                repository.getMooselikeHarvestInvoicePaymentAmounts(permitIds, moose.getOfficialCode());

        final Map<Long, InvoicePaymentAmountsDTO> expected = ImmutableMap.of(
                getPermitId(i1), new InvoicePaymentAmountsDTO(ZERO_MONETARY_AMOUNT, ZERO_MONETARY_AMOUNT, currency(120)),
                getPermitId(i2), new InvoicePaymentAmountsDTO(currency(360), ZERO_MONETARY_AMOUNT, ZERO_MONETARY_AMOUNT),
                getPermitId(i3), new InvoicePaymentAmountsDTO(currency(40), ZERO_MONETARY_AMOUNT, currency(10)),
                getPermitId(i4), new InvoicePaymentAmountsDTO(currency(60), bd(10), ZERO_MONETARY_AMOUNT));

        assertEquals(expected, result);
    }

    private PermitHarvestInvoice newHarvestInvoice(final GameSpecies species,
                                                   final boolean mooseHuntingFinished,
                                                   final BigDecimal amount,
                                                   final BigDecimal correctedAmount,
                                                   final BigDecimal receivedAmount) {

        final HarvestPermit permit = newPermit(model().newRiistanhoitoyhdistys(), huntingYear());
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, species);

        spa.setMooselikeHuntingFinished(mooseHuntingFinished);

        final PermitHarvestInvoice harvestInvoice = model().newPermitHarvestInvoice(spa);

        final Invoice invoice = harvestInvoice.getInvoice();
        invoice.setAmount(amount);
        invoice.setCorrectedAmount(correctedAmount);

        if (receivedAmount != null) {
            invoice.setPaid(today());
            invoice.setReceivedAmount(receivedAmount);
        }

        return harvestInvoice;
    }

    private HarvestPermit newPermit(final Riistanhoitoyhdistys rhy, final int year) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MOOSELIKE);
        application.setApplicationYear(year);

        return model().newMooselikePermit(model().newPermitDecision(application));
    }

    private static Set<Long> extractPermitIds(final PermitHarvestInvoice... harvestInvoices) {
        return F.mapNonNullsToSet(harvestInvoices, PermitHarvestInvoiceRepositoryTest::getPermitId);
    }

    private static Long getPermitId(final PermitHarvestInvoice phi) {
        return phi.getSpeciesAmount().getHarvestPermit().getId();
    }
}
