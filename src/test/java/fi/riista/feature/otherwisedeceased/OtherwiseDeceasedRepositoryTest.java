package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static fi.riista.config.Constants.DEFAULT_TIMEZONE;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class OtherwiseDeceasedRepositoryTest extends EmbeddedDatabaseTest {

    @Resource
    private OtherwiseDeceasedRepository repository;

    @Test
    public void resultIsOrderedByPointOfTime() {
        final EntitySupplier es = getEntitySupplier();
        final DateTime date = new DateTime(2021, 6, 1, 12, 0).withZone(DEFAULT_TIMEZONE);
        final List<OtherwiseDeceased> expected = Arrays.asList(es.newOtherwiseDeceased(date),
                                                               es.newOtherwiseDeceased(date.minusDays(2)),
                                                               es.newOtherwiseDeceased(date.minusDays(1)));
        persistInNewTransaction();

        final List<OtherwiseDeceased> actual = repository.findAllByPointOfTimeBetween(date.minusDays(2), date);
        assertThat(actual, hasSize(3));
        assertThat(actual.get(0).getPointOfTime(), equalTo(expected.get(1).getPointOfTime()));
        assertThat(actual.get(1).getPointOfTime(), equalTo(expected.get(2).getPointOfTime()));
        assertThat(actual.get(2).getPointOfTime(), equalTo(expected.get(0).getPointOfTime()));
    }

    @Test
    public void resultsAreWithinGivenTimes() {
        final EntitySupplier es = getEntitySupplier();
        final DateTime date = new DateTime(2021, 6, 1, 12, 0).withZone(DEFAULT_TIMEZONE);
        final List<OtherwiseDeceased> expected = Arrays.asList(es.newOtherwiseDeceased(date.minusMillis(1)),
                                                               es.newOtherwiseDeceased(date),
                                                               es.newOtherwiseDeceased(date.plusMillis(1)));
        persistInNewTransaction();

        final List<OtherwiseDeceased> actual = repository.findAllByPointOfTimeBetween(date, date);
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0).getPointOfTime(), equalTo(expected.get(1).getPointOfTime()));
        assertThat(actual.get(0).getId(), equalTo(expected.get(1).getId()));
    }
}
