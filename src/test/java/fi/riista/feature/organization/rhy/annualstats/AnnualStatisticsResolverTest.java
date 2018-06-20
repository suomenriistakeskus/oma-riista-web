package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.iban4j.Iban;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AnnualStatisticsResolverTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnualStatisticsService service;

    @Test
    public void testGetIbanFromPreviousYear_whenAnnualStatisticsOfPreviousYearDoesNotExist() {
        final int currentYear = DateUtil.currentYear();

        withRhy(rhy -> {
            persistInNewTransaction();
            assertNull(resolveIban(rhy, currentYear));
        });
    }

    @Test
    public void testGetIbanFromPreviousYear_whenAnnualStatisticsOfPreviousYearExists_butIbanIsMissing() {
        final int currentYear = DateUtil.currentYear();

        withRhy(rhy -> {
            model().newRhyAnnualStatistics(rhy, currentYear - 1);

            persistInNewTransaction();

            assertNull(resolveIban(rhy, currentYear));
        });
    }

    @Test
    public void testGetIbanFromPreviousYear_whenAnnualStatisticsOfPreviousYearExists_andHasValidIban() {
        final int currentYear = DateUtil.currentYear();

        withRhy(rhy -> {
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy, currentYear - 1);

            final Iban iban = iban();
            statistics.getOrCreateBasicInfo().setIban(iban);

            persistInNewTransaction();

            assertEquals(iban, resolveIban(rhy, currentYear));
        });
    }

    private Iban resolveIban(final Riistanhoitoyhdistys rhy, final int year) {
        return getResolver(rhy, year).getIbanFromPreviousYear();
    }

    private AnnualStatisticsResolver getResolver(final Riistanhoitoyhdistys rhy, final int year) {
        return service.getAnnualStatisticsResolver(rhy, year);
    }
}
