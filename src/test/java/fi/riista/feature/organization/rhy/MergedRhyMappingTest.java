package fi.riista.feature.organization.rhy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.collect.Sets.newHashSet;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_ETELÄ_SOISALO_078;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_HAUKIVUORI_VIRTASALMI_076;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_KESKI_KARJALA_417;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_KOILLIS_SAVO_475;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_LAKEUS_334;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_LÄNSI_UUSIMAA_632;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_PERHOJOKILAAKSO_335;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_PIEKSÄMÄKI_079;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_RENGON_SEUTU_017;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_SAVONLINNA_077;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_TAMPERE_382;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_YLÄ_KARJALA_416;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HALSUA_304;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HANKONIEMI_621;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HAUKIVUORI_054;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HEINÄVESI_056;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HÄMEENLINNA_006;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_INKOO_SNAPPERTUNA_623;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_JÄPPILÄ_059;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_KANGASLAMMI_060;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_KARJAA_624;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_KAUSTINEN_314;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_KESÄLAHTI_405;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_KITEE_406;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_NURMES_408;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_NURMO_325;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_PIEKSÄMÄKI_066;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_POHJA_628;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_PUNKAHARJU_067;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_RENKO_013;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_RIISTAVESI_465;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_SAVONLINNA_074;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_SEINÄJOKI_328;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_SIUNTIO_630;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_TAMMISAARI_619;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_TAMPERE_376;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_TEISKO_378;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_TENHOLA_631;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_TUUSNIEMI_470;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_VALTIMO_415;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_VETELI_331;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_VIRTASALMI_075;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.getExistenceRangeOrDefault;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.getOfficialCodesOfRhysNotExistingAtYear;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.getOldRhyCodes;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.translateIfMerged;
import static org.junit.Assert.assertEquals;

public class MergedRhyMappingTest {

    private static final Set<String> OLD_RHY_CODES = newHashSet(
            OLD_HAUKIVUORI_054, OLD_HEINÄVESI_056, OLD_KANGASLAMMI_060, OLD_PUNKAHARJU_067, OLD_SAVONLINNA_074,
            OLD_VIRTASALMI_075, OLD_NURMO_325, OLD_SEINÄJOKI_328, OLD_HALSUA_304, OLD_KAUSTINEN_314, OLD_VETELI_331,
            OLD_TENHOLA_631, OLD_INKOO_SNAPPERTUNA_623, OLD_TAMMISAARI_619, OLD_HANKONIEMI_621, OLD_KARJAA_624,
            OLD_POHJA_628, OLD_SIUNTIO_630, OLD_HÄMEENLINNA_006, OLD_RENKO_013, OLD_TAMPERE_376, OLD_TEISKO_378,
            OLD_TUUSNIEMI_470, OLD_RIISTAVESI_465, OLD_NURMES_408, OLD_VALTIMO_415, OLD_JÄPPILÄ_059, OLD_PIEKSÄMÄKI_066,
            OLD_KESÄLAHTI_405, OLD_KITEE_406);

    @Test
    public void testTranslateIfMerged() {
        assertEquals(NEW_HAUKIVUORI_VIRTASALMI_076, translateIfMerged(OLD_HAUKIVUORI_054));
        assertEquals(NEW_HAUKIVUORI_VIRTASALMI_076, translateIfMerged(OLD_VIRTASALMI_075));

        assertEquals(NEW_SAVONLINNA_077, translateIfMerged(OLD_PUNKAHARJU_067));
        assertEquals(NEW_SAVONLINNA_077, translateIfMerged(OLD_SAVONLINNA_074));

        assertEquals(NEW_LAKEUS_334, translateIfMerged(OLD_NURMO_325));
        assertEquals(NEW_LAKEUS_334, translateIfMerged(OLD_SEINÄJOKI_328));

        assertEquals(NEW_ETELÄ_SOISALO_078, translateIfMerged(OLD_HEINÄVESI_056));
        assertEquals(NEW_ETELÄ_SOISALO_078, translateIfMerged(OLD_KANGASLAMMI_060));

        assertEquals(NEW_PERHOJOKILAAKSO_335, translateIfMerged(OLD_HALSUA_304));
        assertEquals(NEW_PERHOJOKILAAKSO_335, translateIfMerged(OLD_KAUSTINEN_314));
        assertEquals(NEW_PERHOJOKILAAKSO_335, translateIfMerged(OLD_VETELI_331));

        assertEquals(NEW_LÄNSI_UUSIMAA_632, translateIfMerged(OLD_TENHOLA_631));
        assertEquals(NEW_LÄNSI_UUSIMAA_632, translateIfMerged(OLD_INKOO_SNAPPERTUNA_623));
        assertEquals(NEW_LÄNSI_UUSIMAA_632, translateIfMerged(OLD_TAMMISAARI_619));
        assertEquals(NEW_LÄNSI_UUSIMAA_632, translateIfMerged(OLD_HANKONIEMI_621));
        assertEquals(NEW_LÄNSI_UUSIMAA_632, translateIfMerged(OLD_KARJAA_624));
        assertEquals(NEW_LÄNSI_UUSIMAA_632, translateIfMerged(OLD_POHJA_628));
        assertEquals(NEW_LÄNSI_UUSIMAA_632, translateIfMerged(OLD_SIUNTIO_630));

        assertEquals(NEW_RENGON_SEUTU_017, translateIfMerged(OLD_HÄMEENLINNA_006));
        assertEquals(NEW_RENGON_SEUTU_017, translateIfMerged(OLD_RENKO_013));

        assertEquals(NEW_TAMPERE_382, translateIfMerged(OLD_TAMPERE_376));
        assertEquals(NEW_TAMPERE_382, translateIfMerged(OLD_TEISKO_378));

        assertEquals(NEW_KOILLIS_SAVO_475, translateIfMerged(OLD_TUUSNIEMI_470));
        assertEquals(NEW_KOILLIS_SAVO_475, translateIfMerged(OLD_RIISTAVESI_465));

        assertEquals(NEW_YLÄ_KARJALA_416, translateIfMerged(OLD_NURMES_408));
        assertEquals(NEW_YLÄ_KARJALA_416, translateIfMerged(OLD_VALTIMO_415));

        assertEquals(NEW_PIEKSÄMÄKI_079, translateIfMerged(OLD_JÄPPILÄ_059));
        assertEquals(NEW_PIEKSÄMÄKI_079, translateIfMerged(OLD_PIEKSÄMÄKI_066));

        assertEquals(NEW_KESKI_KARJALA_417, translateIfMerged(OLD_KESÄLAHTI_405));
        assertEquals(NEW_KESKI_KARJALA_417, translateIfMerged(OLD_KITEE_406));

        IntStream.range(1, 1000)
                .mapToObj(String::valueOf)
                .filter(rhyCode -> !OLD_RHY_CODES.contains(rhyCode))
                .forEach(rhyCode -> assertEquals(rhyCode, translateIfMerged(rhyCode)));
    }

    @Test
    public void testGetOldRhyCodes() {
        assertEquals(OLD_RHY_CODES, getOldRhyCodes());
    }

    @Test
    public void testGetOfficialCodesOfRhysNotExistingAtYear() {
        final Set<String> notExistingBefore2014 = newHashSet(
                NEW_HAUKIVUORI_VIRTASALMI_076, NEW_SAVONLINNA_077, NEW_LAKEUS_334, NEW_ETELÄ_SOISALO_078,
                NEW_PERHOJOKILAAKSO_335, NEW_LÄNSI_UUSIMAA_632, NEW_RENGON_SEUTU_017, NEW_TAMPERE_382,
                NEW_KOILLIS_SAVO_475, NEW_YLÄ_KARJALA_416, NEW_PIEKSÄMÄKI_079, NEW_KESKI_KARJALA_417);

        final Set<String> notExistingFrom2014To2018 = newHashSet(
                OLD_HAUKIVUORI_054, OLD_VIRTASALMI_075, OLD_SAVONLINNA_074, OLD_PUNKAHARJU_067, OLD_NURMO_325,
                OLD_SEINÄJOKI_328, NEW_ETELÄ_SOISALO_078, NEW_PERHOJOKILAAKSO_335, NEW_LÄNSI_UUSIMAA_632,
                NEW_RENGON_SEUTU_017, NEW_TAMPERE_382, NEW_KOILLIS_SAVO_475, NEW_YLÄ_KARJALA_416, NEW_PIEKSÄMÄKI_079,
                NEW_KESKI_KARJALA_417);

        final Set<String> notExisting2019 = newHashSet(
                OLD_HAUKIVUORI_054, OLD_VIRTASALMI_075, OLD_SAVONLINNA_074, OLD_PUNKAHARJU_067, OLD_NURMO_325,
                OLD_SEINÄJOKI_328, OLD_HEINÄVESI_056, OLD_KANGASLAMMI_060, NEW_PERHOJOKILAAKSO_335, NEW_LÄNSI_UUSIMAA_632,
                NEW_RENGON_SEUTU_017, NEW_TAMPERE_382, NEW_KOILLIS_SAVO_475, NEW_YLÄ_KARJALA_416, NEW_PIEKSÄMÄKI_079,
                NEW_KESKI_KARJALA_417);

        final Set<String> notExistingFrom2020To2022 = newHashSet(
                OLD_HAUKIVUORI_054, OLD_HEINÄVESI_056, OLD_KANGASLAMMI_060, OLD_PUNKAHARJU_067, OLD_SAVONLINNA_074,
                OLD_VIRTASALMI_075, OLD_NURMO_325, OLD_SEINÄJOKI_328, OLD_HALSUA_304, OLD_KAUSTINEN_314, OLD_VETELI_331,
                OLD_TENHOLA_631, OLD_INKOO_SNAPPERTUNA_623, OLD_TAMMISAARI_619, OLD_HANKONIEMI_621, OLD_KARJAA_624,
                OLD_POHJA_628, OLD_SIUNTIO_630, OLD_HÄMEENLINNA_006, OLD_RENKO_013, OLD_TAMPERE_376, OLD_TEISKO_378,
                OLD_TUUSNIEMI_470, OLD_RIISTAVESI_465, OLD_NURMES_408, OLD_VALTIMO_415, NEW_PIEKSÄMÄKI_079,
                NEW_KESKI_KARJALA_417);

        assertEquals(notExistingBefore2014, getOfficialCodesOfRhysNotExistingAtYear(2013));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2014));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2015));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2016));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2017));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2018));
        assertEquals(notExisting2019, getOfficialCodesOfRhysNotExistingAtYear(2019));
        assertEquals(notExistingFrom2020To2022, getOfficialCodesOfRhysNotExistingAtYear(2020));
        assertEquals(notExistingFrom2020To2022, getOfficialCodesOfRhysNotExistingAtYear(2021));
        assertEquals(notExistingFrom2020To2022, getOfficialCodesOfRhysNotExistingAtYear(2022));
        assertEquals(OLD_RHY_CODES, getOfficialCodesOfRhysNotExistingAtYear(2023));
    }

    @Test
    public void testGetExistenceRangeOrDefault() {
        final List<Integer> existingDefaultRange = Arrays.asList(2015, 2016, 2017);
        final List<Integer> existingUpperBound = Arrays.asList(2017, 2018);
        final List<Integer> existingLowerBound = Arrays.asList(2019, 2020);

        assertEquals(existingDefaultRange, getExistenceRangeOrDefault(OLD_HEINÄVESI_056, 2015, 2017));
        assertEquals(existingUpperBound, getExistenceRangeOrDefault(OLD_HEINÄVESI_056, 2017, 2020));
        assertEquals(existingLowerBound, getExistenceRangeOrDefault(NEW_ETELÄ_SOISALO_078, 2016, 2020));
    }
}
