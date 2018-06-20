package fi.riista.feature.organization.occupation;

import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OccupationExcelViewTest {

    private static final LocalDate today = DateUtil.today();
    private static final LocalDate yesterday = DateUtil.today().minusDays(1);
    private static final LocalDate tomorrow = yesterday.plusDays(2);

    private static final OccupationDTO current = createOccupation(null, null);
    private static final OccupationDTO currentStartsToday= createOccupation(today, null);
    private static final OccupationDTO currentEndsToday= createOccupation(null, today);
    private static final OccupationDTO currentStartedYesterday = createOccupation(yesterday, null);
    private static final OccupationDTO currentEndsTomorrow = createOccupation(null, tomorrow);
    private static final OccupationDTO currentStartedYesterdayEndsTomorrow = createOccupation(yesterday, tomorrow);

    private static final OccupationDTO future = createOccupation(tomorrow, null);
    private static final OccupationDTO future2 = createOccupation(tomorrow, tomorrow.plusDays(1));

    private static final OccupationDTO past = createOccupation(null, yesterday);
    private static final OccupationDTO past2 = createOccupation(yesterday.minusDays(1), yesterday);

    @Test
    public void testFuture() {
        List<OccupationDTO> occupations = F.filterToList(occupations(), OccupationExcelView.FUTURE);
        assertEquals(2, occupations.size());

        assertTrue(occupations.contains(future));
        assertTrue(occupations.contains(future2));
    }

    @Test
    public void testPast() {
        List<OccupationDTO> occupations = F.filterToList(occupations(), OccupationExcelView.PAST);
        assertEquals(2, occupations.size());

        assertTrue(occupations.contains(past));
        assertTrue(occupations.contains(past2));
    }

    @Test
    public void testCurrent() {
        List<OccupationDTO> occupations = F.filterToList(occupations(), OccupationExcelView.CURRENT);
        assertEquals(6, occupations.size());

        assertTrue(occupations.contains(current));
        assertTrue(occupations.contains(currentStartsToday));
        assertTrue(occupations.contains(currentEndsToday));
        assertTrue(occupations.contains(currentStartedYesterday));
        assertTrue(occupations.contains(currentEndsTomorrow));
        assertTrue(occupations.contains(currentStartedYesterdayEndsTomorrow));
    }

    private static List<OccupationDTO> occupations() {
        return Arrays.asList(current, currentStartsToday, currentEndsToday, currentStartedYesterday,
                currentEndsTomorrow, currentStartedYesterdayEndsTomorrow, future, future2, past, past2);
    }

    private static OccupationDTO createOccupation(LocalDate begin, LocalDate end) {
        OccupationDTO occ = new OccupationDTO();
        occ.setBeginDate(begin);
        occ.setEndDate(end);
        return occ;
    }
}
