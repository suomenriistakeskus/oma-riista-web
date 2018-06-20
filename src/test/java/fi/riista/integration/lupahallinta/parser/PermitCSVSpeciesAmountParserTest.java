package fi.riista.integration.lupahallinta.parser;

import fi.riista.feature.gamediary.GameSpeciesRepository;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class PermitCSVSpeciesAmountParserTest {
    @Mock
    private GameSpeciesRepository gameSpeciesRepository;

    @Test
    public void testParseBlankAmount() {
        assertEquals(BigDecimal.ZERO, PermitCSVSpeciesAmountParser.parseAmountInternal(""));
    }

    @Test
    public void testParseEmptyData() {
        List<String> errors = new ArrayList<>();
        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse("", "", "", "", "", "", "", false);

        assertThat(speciesAmount, hasSize(0));
        assertEquals(0, errors.size());
    }

    @Test
    public void testParseBlankSpecies() {
        List<String> errors = new ArrayList<>();
        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse("", "1.5", "1.4.2014 - 28.5.2014", "", "", "", "", false);

        assertThat(speciesAmount, hasSize(0));
        assertEquals(1, errors.size());
    }

    @Test
    public void testParseBlankDatesForZeroAmount() {
        List<String> errors = new ArrayList<>();

        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse("11", "0.0", "", "", "", "", "", false);

        assertThat(speciesAmount, hasSize(0));
        assertEquals(0, errors.size());
    }

    @Test
    public void testParseValid() {
        List<String> errors = new ArrayList<>();

        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse(
                        "11,22 ",
                        " 100.5 , 200",
                        " 20.8.2014 - 31.10.2014, 20.08.2015-31.10.2015 ",
                        ",",
                        ",", ",", ",", false
                );

        assertEquals(0, errors.size());
        assertThat(speciesAmount, hasSize(2));

        PermitCSVLine.SpeciesAmount a1 = speciesAmount.get(0);
        PermitCSVLine.SpeciesAmount a2 = speciesAmount.get(1);

        assertSpeciesAmount(a1, 100.5, 11, null,
                d(2014, 8, 20), d(2014, 10, 31),
                null, null);

        assertSpeciesAmount(a2, 200, 22, null,
                d(2015, 8, 20), d(2015, 10, 31),
                null, null);
    }

    @Test
    public void testParseValidWithSecondDateInterval() {
        List<String> errors = new ArrayList<>();

        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse(
                        "11,22",
                        "34,52",
                        "1.4.2014 - 28.5.2014, 2.4.2015-29.5.2015",
                        "28.6.2014 - 31.10.2014, 28.6.2015 - 31.10.2015",
                        ",", ",", ",", false
                );

        assertEquals(0, errors.size());
        assertThat(speciesAmount, hasSize(2));

        PermitCSVLine.SpeciesAmount a1 = speciesAmount.get(0);
        PermitCSVLine.SpeciesAmount a2 = speciesAmount.get(1);

        assertSpeciesAmount(a1, 34, 11, null,
                d(2014, 4, 1), d(2014, 5, 28),
                d(2014, 6, 28), d(2014, 10, 31));

        assertSpeciesAmount(a2, 52, 22, null,
                d(2015, 4, 2), d(2015, 5, 29),
                d(2015, 6, 28), d(2015, 10, 31));

        assertSpeciesAmountRestriction(a1, null, null);
        assertSpeciesAmountRestriction(a2, null, null);
    }

    @Test
    public void testParseValidWithRestrictions() {
        List<String> errors = new ArrayList<>();

        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse(
                        "11,22",
                        "34,52",
                        "1.4.2014 - 28.5.2014, 2.4.2015-29.5.2015",
                        "28.6.2014 - 31.10.2014, 28.6.2015 - 31.10.2015",
                        "AE,AU", "2.0,3.0", "123,456", false
                );

        assertEquals(0, errors.size());
        assertThat(speciesAmount, hasSize(2));

        PermitCSVLine.SpeciesAmount a1 = speciesAmount.get(0);
        PermitCSVLine.SpeciesAmount a2 = speciesAmount.get(1);

        assertSpeciesAmount(a1, 34, 11, "123",
                d(2014, 4, 1), d(2014, 5, 28),
                d(2014, 6, 28), d(2014, 10, 31));

        assertSpeciesAmount(a2, 52, 22, "456",
                d(2015, 4, 2), d(2015, 5, 29),
                d(2015, 6, 28), d(2015, 10, 31));

        assertSpeciesAmountRestriction(a1, "AE", 2.0);
        assertSpeciesAmountRestriction(a2, "AU", 3.0);
    }

    @Test
    public void testParseCreditorReferenceNotGiven() {
        List<String> errors = new ArrayList<>();

        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse("11", "1.0", "1.1.2016-2.2.2016", "", "", "", "", true);

        assertThat(speciesAmount, hasSize(0));
        assertEquals(1, errors.size());
        assertEquals("Viitenumero on pakollinen mutta se on virheellinen:", errors.get(0));
    }

    @Test
    public void testParseCreditorReferenceInvalid() {
        List<String> errors = new ArrayList<>();

        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse("11", "1.0", "1.1.2016-2.2.2016", "", "", "", "123", true);

        assertThat(speciesAmount, hasSize(0));
        assertEquals(1, errors.size());
        assertEquals("Viitenumero on pakollinen mutta se on virheellinen:123", errors.get(0));
    }

    @Test
    public void testParseCreditorReferenceValid() {
        List<String> errors = new ArrayList<>();

        List<PermitCSVLine.SpeciesAmount> speciesAmount =
                new PermitCSVSpeciesAmountParser(errors).parse("11", "1.0", "1.1.2016-2.2.2016", "", "", "", "1232", true);

        assertThat(speciesAmount, hasSize(1));
        assertEquals(0, errors.size());
    }

    private static void assertSpeciesAmountRestriction(
            PermitCSVLine.SpeciesAmount speciesAmount, String restrictionType, Double restrictionAmount) {

        assertEquals(restrictionType, speciesAmount.getRestrictionType());

        if (restrictionAmount != null) {
            assertEquals(restrictionAmount, speciesAmount.getRestrictionAmount().floatValue(), 0.001);
        } else {
            assertNull(speciesAmount.getRestrictionAmount());
        }
    }


    private static void assertSpeciesAmount(PermitCSVLine.SpeciesAmount speciesAmount,
                                            double amount, int officialCode,
                                            String referenceNumber,
                                            LocalDate begin1, LocalDate end1,
                                            LocalDate begin2, LocalDate end2) {
        assertEquals(Integer.valueOf(officialCode), speciesAmount.getSpeciesOfficialCode());
        assertEquals(amount, speciesAmount.getAmount().floatValue(), 0.001);

        assertEquals(begin1, speciesAmount.getBeginDate());
        assertEquals(end1, speciesAmount.getEndDate());

        assertEquals(begin2, speciesAmount.getBeginDate2());
        assertEquals(end2, speciesAmount.getEndDate2());

        assertEquals(referenceNumber, speciesAmount.getReferenceNumber());
    }

    @Test
    public void testBeginDateSameAsEndDate() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.1.2016-1.1.2016", "", "", "", "1232", true);
        assertEquals(0, errors.size());
    }

    @Test
    public void testBeginDateBeforeEndDate() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "2.1.2016-1.1.2016", "", "", "", "1232", true);
        assertEquals(1, errors.size());
        assertEquals("Lupa-ajat virheelliset, ensimmäinen aikaväli: alkupäivä ei ole ennen loppupäivää. 02.01.2016-01.01.2016", errors.get(0));
    }

    @Test
    public void testBeginDate2BeforeEndDate2() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.1.2016-2.1.2016", "2.2.2016-1.2.2016", "", "", "1232", true);
        assertEquals(1, errors.size());
        assertEquals("Lupa-ajat virheelliset, toinen aikaväli: alkupäivä ei ole ennen loppupäivää. 02.02.2016-01.02.2016", errors.get(0));
    }

    @Test
    public void testFirstIntervalMustBeBeforeSecondInterval() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.2.2016-2.2.2016", "1.1.2016-2.1.2016", "", "", "1232", true);
        assertEquals(1, errors.size());
        assertEquals("Lupa-ajat virheelliset: ensimmäinen aikaväli täytyy olla jälkimmäistä ennen. 01.02.2016-02.02.2016, 01.01.2016-02.01.2016", errors.get(0));
    }

    @Test
    public void testSecondIntervalMustBeAfterFirstInterval() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.2.2016-2.2.2016", "2.2.2016-3.2.2016", "", "", "1232", true);
        assertEquals(1, errors.size());
        assertEquals("Lupa-ajat virheelliset: ensimmäinen aikaväli täytyy olla jälkimmäistä ennen. 01.02.2016-02.02.2016, 02.02.2016-03.02.2016", errors.get(0));
    }

    @Test
    public void testTotalIntervalMustBeLessThanYearLong() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.1.2016-1.2.2016", "1.12.2016-1.1.2017", "", "", "1232", false);
        assertEquals(1, errors.size());
        assertEquals("Lupa-ajat virheelliset: ensimmäinen aikavälin alku ja jälkimmäisen aikavälin loppu on yli 365 päivää. 01.01.2016-01.02.2016, 01.12.2016-01.01.2017", errors.get(0));
    }

    @Test
    public void testFirstIntervalMustBeLessThanYearLong() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.1.2016-1.1.2017", "", "", "", "1232", false);
        assertEquals(1, errors.size());
        assertEquals("Lupa-ajat virheelliset: ensimmäinen aikaväli on yli 365 päivää. 01.01.2016-01.01.2017", errors.get(0));
    }

    @Test
    public void testSecondIntervalMustBeLessThanYearLong() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.1.2016-1.2.2016", "2.2.2016-2.2.2017", "", "", "1232", false);
        assertEquals(2, errors.size());
        assertEquals("Lupa-ajat virheelliset: jälkimmäinen aikaväli on yli 365 päivää. 02.02.2016-02.02.2017", errors.get(0));
        assertEquals("Lupa-ajat virheelliset: ensimmäinen aikavälin alku ja jälkimmäisen aikavälin loppu on yli 365 päivää. 01.01.2016-01.02.2016, 02.02.2016-02.02.2017", errors.get(1));
    }

    @Test
    public void testSpeciesCanBeGivenOnlyOnce_mooselike() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1,1", "1.0,1.0", "1.2.2016-2.2.2016,1.2.2016-2.2.2016", ",", ",", ",", "1232,1232", true);
        assertEquals(1, errors.size());
        assertEquals("Luvalle voi antaa eläinlajin vain kerran", errors.get(0));
    }

    @Test
    public void testSpeciesCanBeGivenOnlyOnce_nonMooselike() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1,1", "1.0,1.0", "1.2.2016-2.2.2016,1.2.2016-2.2.2016", ",", ",", ",", "1232,1232", false);
        assertEquals(1, errors.size());
        assertEquals("Luvalle voi antaa eläinlajin vain kerran", errors.get(0));
    }

    @Test
    public void testNonMooselikeCanSpanOverHuntingYears() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.1.2016-31.12.2016", "", "", "", "1232", false);
        assertEquals(0, errors.size());
    }

    @Test
    public void testMooselikeCanNotSpanOverHuntingYears() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.6.2016-31.12.2016", "", "", "", "1232", true);
        assertEquals(1, errors.size());
        assertEquals("Hirvieläinluvan voimassaoloajat täytyy olla yhden metsästysvuoden sisällä", errors.get(0));
    }

    @Test
    public void testMooselikeCanNotSpanOverHuntingYears2() {
        List<String> errors = new ArrayList<>();
        new PermitCSVSpeciesAmountParser(errors).parse("1", "1.0", "1.1.2016-2.1.2016", "1.6.2016-31.12.2016", "", "", "1232", true);
        assertEquals(1, errors.size());
        assertEquals("Hirvieläinluvan voimassaoloajat täytyy olla yhden metsästysvuoden sisällä", errors.get(0));
    }


    private static LocalDate d(int year, int month, int day) {
        return new LocalDate(year, month, day);
    }
}
