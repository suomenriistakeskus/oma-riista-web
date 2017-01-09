package fi.riista.integration.lupahallinta.club;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LHHuntingClubItemValidatorTest {

    private LHHuntingClubItemValidator validator = new LHHuntingClubItemValidator();

    @Test
    public void excludeKaupunki() {
        final LHHuntingClubCSVRow row = row("1234567", "Helsingin kaupunki");
        assertNull(validator.process(row));
    }

    @Test
    public void includeAlwaysBySpecialCustomerNumber() {
        LHHuntingClubCSVRow row = row("1039177", "Tampereen kaupunki");
        assertEquals(row, validator.process(row));
    }

    private static LHHuntingClubCSVRow row(final String asiakasnumero, final String name) {
        LHHuntingClubCSVRow row = new LHHuntingClubCSVRow();
        row.setAsiakasNumero(asiakasnumero);
        row.setNimiSuomi(name);
        row.setNimiRuotsi(name);
        return row;
    }
}
