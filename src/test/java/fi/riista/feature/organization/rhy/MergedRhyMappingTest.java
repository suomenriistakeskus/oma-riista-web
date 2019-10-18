package fi.riista.feature.organization.rhy;

import org.junit.Test;

import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.collect.Sets.newHashSet;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_ETELÄ_SOISALO_078;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_HAUKIVUORI_VIRTASALMI_076;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_LAKEUS_334;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_SAVONLINNA_077;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HAUKIVUORI_054;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HEINÄVESI_056;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_KANGASLAMMI_060;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_NURMO_325;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_PUNKAHARJU_067;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_SAVONLINNA_074;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_SEINÄJOKI_328;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_VIRTASALMI_075;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.getOfficialCodesOfRhysNotExistingAtYear;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.getOldRhyCodes;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.translateIfMerged;
import static org.junit.Assert.assertEquals;

public class MergedRhyMappingTest {

    private static final Set<String> OLD_RHY_CODES = newHashSet(
            OLD_HAUKIVUORI_054, OLD_HEINÄVESI_056, OLD_KANGASLAMMI_060, OLD_PUNKAHARJU_067, OLD_SAVONLINNA_074,
            OLD_VIRTASALMI_075, OLD_NURMO_325, OLD_SEINÄJOKI_328);

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
                NEW_HAUKIVUORI_VIRTASALMI_076, NEW_SAVONLINNA_077, NEW_LAKEUS_334, NEW_ETELÄ_SOISALO_078);

        final Set<String> notExistingFrom2014To2018 = newHashSet(
                OLD_HAUKIVUORI_054, OLD_VIRTASALMI_075, OLD_SAVONLINNA_074, OLD_PUNKAHARJU_067, OLD_NURMO_325,
                OLD_SEINÄJOKI_328, NEW_ETELÄ_SOISALO_078);

        assertEquals(notExistingBefore2014, getOfficialCodesOfRhysNotExistingAtYear(2013));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2014));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2015));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2016));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2017));
        assertEquals(notExistingFrom2014To2018, getOfficialCodesOfRhysNotExistingAtYear(2018));
        assertEquals(OLD_RHY_CODES, getOfficialCodesOfRhysNotExistingAtYear(2019));
        assertEquals(OLD_RHY_CODES, getOfficialCodesOfRhysNotExistingAtYear(2020));
    }
}
