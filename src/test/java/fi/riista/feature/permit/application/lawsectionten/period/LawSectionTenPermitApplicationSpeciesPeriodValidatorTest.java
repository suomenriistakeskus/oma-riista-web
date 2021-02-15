package fi.riista.feature.permit.application.lawsectionten.period;

import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Test;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;

public class LawSectionTenPermitApplicationSpeciesPeriodValidatorTest {

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void test_validateCurrentSeason() {
        MockTimeProvider.mockTime(new LocalDate(2020, 8, 20).toDate().getTime());
        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2020, 8, 20),
                new LocalDate(2021, 4, 16));

        MockTimeProvider.mockTime(new LocalDate(2020, 8, 1).toDate().getTime());
        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2020, 8, 1),
                new LocalDate(2021, 7, 31));

        MockTimeProvider.mockTime(new LocalDate(2020, 9, 1).toDate().getTime());
        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_PARTRIDGE,
                new LocalDate(2020, 9, 1),
                new LocalDate(2020, 12, 31));
    }

    @Test
    public void test_validateNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2021, 8, 20),
                new LocalDate(2022, 4, 16));

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2021, 8, 1),
                new LocalDate(2022, 7, 31));

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_PARTRIDGE,
                new LocalDate(2021, 9, 1),
                new LocalDate(2021, 12, 31));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_beginDateAfterEndDate() {
        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2020, 2, 1),
                new LocalDate(2020, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_europeanBeaverInvalidPeriodBeforeCurrentSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2020, 1, 1),
                new LocalDate(2020, 2, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_europeanBeaverInvalidPeriodAfterCurrentSeasonAndBeforeNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2021, 4, 17),
                new LocalDate(2021, 5, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_europeanBeaverInvalidPeriodAfterNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2022, 4, 17),
                new LocalDate(2022, 5, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_europeanBeaverInvalidPeriodInSeasonAfterNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 6,1);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2021, 8, 20),
                new LocalDate(2022, 4, 16));
    }

    @Test
    public void test_europeanBeaverPeriodCurrentSeasonBeforeToday() {
        final LocalDate mockedTime = new LocalDate(2020, 4,15);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2020, 4, 14),
                new LocalDate(2020, 4, 16));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ringedSealInvalidPeriodBeforeCurrentSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2020, 4, 1),
                new LocalDate(2020, 4, 15));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ringedSealInvalidPeriodAfterCurrentSeasonAndBeforeNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2021, 1, 1),
                new LocalDate(2021, 2, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ringedSealInvalidPeriodAfterNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2022, 1, 1),
                new LocalDate(2022, 2, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ringedSealInvalidPeriodInSeasonAfterNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 4,1);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2021, 8, 1),
                new LocalDate(2021, 8, 31));
    }

    @Test
    public void test_ringedSealPeriodCurrentSeasonBeforeToday() {
        final LocalDate mockedTime = new LocalDate(2020, 4,20);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2020, 4, 16),
                new LocalDate(2020, 4, 25));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ringedSealInvalidPeriodEndDateInBetweenRange() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2020, 8, 1),
                new LocalDate(2021, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ringedSealInvalidPeriodBeginDateInBetweenRange() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2021, 1, 1),
                new LocalDate(2021, 7, 31));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_partridgeInvalidPeriodBeforeCurrentSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_PARTRIDGE,
                new LocalDate(2020, 1, 1),
                new LocalDate(2020, 2, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_partridgeInvalidPeriodAfterCurrentSeasonAndBeforeNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_PARTRIDGE,
                new LocalDate(2021, 1, 1),
                new LocalDate(2021, 2, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_partridgeInvalidPeriodAfterNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 12, 31);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_PARTRIDGE,
                new LocalDate(2022, 1, 1),
                new LocalDate(2022, 2, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_partridgeInvalidPeriodInSeasonAfterNextSeason() {
        final LocalDate mockedTime = new LocalDate(2020, 6,1);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_EUROPEAN_BEAVER,
                new LocalDate(2021, 9, 1),
                new LocalDate(2021, 12, 31));
    }

    @Test
    public void test_partridgePeriodCurrentSeasonBeforeToday() {
        final LocalDate mockedTime = new LocalDate(2020, 9,20);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(OFFICIAL_CODE_RINGED_SEAL,
                new LocalDate(2020, 9, 1),
                new LocalDate(2020, 9, 25));
    }

}
