package fi.riista.feature.permit.invoice;

import org.junit.Test;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.permit.invoice.CreditorReferenceCalculator.computeReferenceForPermitDecisionProcessingInvoice;
import static fi.riista.feature.permit.invoice.CreditorReferenceCalculator.computeReferenceForPermitHarvestInvoice;
import static org.junit.Assert.assertEquals;

public class CreditorReferenceCalculatorTest {

    @Test
    public void testComputeReferenceForPermitDecisionProcessingInvoice() {
        assertEquals("10000 00012", computeReferenceForPermitDecisionProcessingInvoice(2001, 1).getValue());
        assertEquals("1 80001 23458", computeReferenceForPermitDecisionProcessingInvoice(2018, 12345).getValue());
        assertEquals("9 99999 99990", computeReferenceForPermitDecisionProcessingInvoice(2099, 99999999).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeReferenceForPermitDecisionProcessingInvoice_whenHuntingYearTooLow() {
        computeReferenceForPermitDecisionProcessingInvoice(2000, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeReferenceForPermitDecisionProcessingInvoice_whenHuntingYearTooHigh() {
        computeReferenceForPermitDecisionProcessingInvoice(2100, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeReferenceForPermitDecisionProcessingInvoice_whenApplicationNumberTooHigh() {
        computeReferenceForPermitDecisionProcessingInvoice(2018, 100_000_000);
    }

    @Test
    public void testComputeReferenceForPermitHarvestInvoice() {
        assertEquals("100 00000 10015", computeReferenceForPermitHarvestInvoice(2001, 1, OFFICIAL_CODE_MOOSE).getValue());
        assertEquals("1800 01234 50024", computeReferenceForPermitHarvestInvoice(2018, 12345, OFFICIAL_CODE_WHITE_TAILED_DEER).getValue());
        assertEquals("9999 99999 90055", computeReferenceForPermitHarvestInvoice(2099, 99999999, OFFICIAL_CODE_WILD_FOREST_REINDEER).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeReferenceForPermitHarvestInvoice_whenHuntingYearTooLow() {
        computeReferenceForPermitHarvestInvoice(2000, 1, OFFICIAL_CODE_MOOSE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeReferenceForPermitHarvestInvoice_whenHuntingYearTooHigh() {
        computeReferenceForPermitHarvestInvoice(2100, 1, OFFICIAL_CODE_MOOSE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeReferenceForPermitHarvestInvoice_whenApplicationNumberTooHigh() {
        computeReferenceForPermitHarvestInvoice(2018, 100_000_000, OFFICIAL_CODE_MOOSE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeReferenceForPermitHarvestInvoice_whenGameSpeciesCodeNotSupported() {
        computeReferenceForPermitHarvestInvoice(2018, 1, OFFICIAL_CODE_BEAR);
    }
}
