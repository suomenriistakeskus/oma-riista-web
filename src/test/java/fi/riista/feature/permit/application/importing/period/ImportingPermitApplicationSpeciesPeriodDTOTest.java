package fi.riista.feature.permit.application.importing.period;

import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.is;

public class ImportingPermitApplicationSpeciesPeriodDTOTest{


    @Test
    public void testPeriodValid() {
        assertThat(create(today(), today()).isPeriodValid(), is(true));
        assertThat(create(today(), today().plusMonths(1)).isPeriodValid(), is(true));
        assertThat(create(today(), today().plusYears(1).minusDays(1)).isPeriodValid(), is(true));
        assertThat(create(today(), today().plusYears(1)).isPeriodValid(), is(false));
    }

    private ImportingPermitApplicationSpeciesPeriodDTO create(final LocalDate begin, final LocalDate end) {
        final ImportingPermitApplicationSpeciesPeriodDTO dto = new ImportingPermitApplicationSpeciesPeriodDTO();
        dto.setBeginDate(begin);
        dto.setEndDate(end);
        return dto;
    }
}
