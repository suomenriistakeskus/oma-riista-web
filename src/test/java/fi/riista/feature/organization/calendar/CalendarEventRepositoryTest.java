package fi.riista.feature.organization.calendar;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKILPAILU;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJAKURSSI;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJATUTKINTO;
import static fi.riista.feature.organization.calendar.CalendarEventType.RIISTAPOLKUKILPAILU;
import static fi.riista.feature.organization.calendar.CalendarEventType.VUOSIKOKOUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static fi.riista.util.DateUtil.today;

public class CalendarEventRepositoryTest extends EmbeddedDatabaseTest {
    @Resource
    private CalendarEventRepository calendarEventRepository;

    private LocalDate eventDate1 = today();
    private LocalDate eventDate2 = eventDate1.plusDays(1);
    private LocalDate eventDate3 = eventDate1.plusDays(2);

    private void createEvents() {
        RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        Venue venue = model().newVenue();

        CalendarEvent event = model().newCalendarEvent(rka, VUOSIKOKOUS, eventDate1);
        model().newAdditionalCalendarEvent(event, eventDate2, new LocalTime(12, 0), venue);
        model().newCalendarEvent(rhy, METSASTAJATUTKINTO, eventDate3);

        persistInNewTransaction();
    }

    @Test
    public void testGetPublicOrganisationCalendarEvents() {
        createEvents();

        HuntingClub club = model().newHuntingClub();
        Venue venue = model().newVenue();
        model().newCalendarEvent(club, METSASTAJAKURSSI, today().plusDays(3), venue);

        persistInNewTransaction();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setOnlyPublicEvents(true);
        params.setLimit(5);

        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(VUOSIKOKOUS, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(VUOSIKOKOUS, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());
        assertEquals(METSASTAJATUTKINTO, result.get(2).getCalendarEventType());
        assertEquals(eventDate3.toDate(), result.get(2).getDate());
    }

    @Test
    public void testGetBetweenDates() {
        createEvents();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setLimit(5);
        params.setBegin(today().plusDays(0));
        params.setEnd(today().plusDays(1));

        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(VUOSIKOKOUS, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(VUOSIKOKOUS, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());
    }

    @Test
    public void testGetBetweenDatesAndAdditionalEvents() {
        createEvents();

        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        LocalDate eventDate = today().plusDays(3);
        LocalDate additionalEventDate1 = today().plusDays(4);
        LocalDate additionalEventDate2 = today().plusDays(5);
        LocalDate additionalEventDate3 = today().plusDays(6);
        CalendarEvent event = model().newCalendarEvent(rhy, RIISTAPOLKUKILPAILU, eventDate);
        Venue venue = model().newVenue();
        AdditionalCalendarEvent additionalCalendarEvent1 = model().newAdditionalCalendarEvent(event, additionalEventDate1, new LocalTime(12, 0), venue);
        AdditionalCalendarEvent additionalCalendarEvent2 = model().newAdditionalCalendarEvent(event, additionalEventDate2, new LocalTime(12, 0), venue);
        AdditionalCalendarEvent additionalCalendarEvent3 = model().newAdditionalCalendarEvent(event, additionalEventDate3, new LocalTime(12, 0), venue);

        persistInNewTransaction();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setLimit(10);
        params.setBegin(today().plusDays(1));
        params.setEnd(today().plusDays(5));

        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(VUOSIKOKOUS, result.get(0).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(0).getDate());
        assertEquals(METSASTAJATUTKINTO, result.get(1).getCalendarEventType());
        assertEquals(eventDate3.toDate(), result.get(1).getDate());
        assertEquals(RIISTAPOLKUKILPAILU, result.get(2).getCalendarEventType());
        assertEquals(eventDate.toDate(), result.get(2).getDate());
        assertEquals(RIISTAPOLKUKILPAILU, result.get(3).getCalendarEventType());
        assertEquals(additionalEventDate1.toDate(), result.get(3).getDate());
        assertEquals(RIISTAPOLKUKILPAILU, result.get(4).getCalendarEventType());
        assertEquals(additionalEventDate2.toDate(), result.get(4).getDate());
    }

    @Test
    public void testGetOnlyPubliclyVisible() {
        createEvents();

        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        model().newCalendarEvent(rhy, AMPUMAKILPAILU, false);

        persistInNewTransaction();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setOnlyPubliclyVisible(true);
        params.setLimit(5);

        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(VUOSIKOKOUS, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(VUOSIKOKOUS, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());
        assertEquals(METSASTAJATUTKINTO, result.get(2).getCalendarEventType());
        assertEquals(eventDate3.toDate(), result.get(2).getDate());
    }

    @Test
    public void testGetPaged() {
        createEvents();
        int limit = 2;

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setLimit(limit);

        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(VUOSIKOKOUS, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(VUOSIKOKOUS, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());

        params.setOffset(limit);
        result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(METSASTAJATUTKINTO, result.get(0).getCalendarEventType());
        assertEquals(eventDate3.toDate(), result.get(0).getDate());

    }

    @Test
    public void testGetPagedRhyAndRka() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setParentOrganisation(rka);
        final Riistanhoitoyhdistys anotherRhy = model().newRiistanhoitoyhdistys();

        final Venue venue = model().newVenue();

        final CalendarEvent event = model().newCalendarEvent(rka, VUOSIKOKOUS, eventDate1);
        model().newAdditionalCalendarEvent(event, eventDate2, new LocalTime(12, 0), venue);
        model().newCalendarEvent(anotherRhy, AMPUMAKILPAILU, eventDate2);
        model().newCalendarEvent(rhy, METSASTAJATUTKINTO, eventDate3);

        persistInNewTransaction();

        final CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setRhyIds(Arrays.asList(rhy.getOfficialCode()));

        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(VUOSIKOKOUS, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(VUOSIKOKOUS, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());
        assertEquals(METSASTAJATUTKINTO, result.get(2).getCalendarEventType());
        assertEquals(eventDate3.toDate(), result.get(2).getDate());
    }

    @Test
    public void testGetTrainingEvents() {
        RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        model().newCalendarEvent(rhy, CalendarEventType.METSASTYKSENJOHTAJA_HIRVIELAIMET, eventDate1);
        model().newCalendarEvent(rhy, CalendarEventType.PETOYHDYSHENKILO_KOULUTUS, eventDate2);
        model().newCalendarEvent(rhy, CalendarEventType.AMPUMAKILPAILU, eventDate3);

        persistInNewTransaction();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.KOULUTUSTILAISUUDET));
        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CalendarEventType.METSASTYKSENJOHTAJA_HIRVIELAIMET, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(CalendarEventType.PETOYHDYSHENKILO_KOULUTUS, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());
    }

    @Test
    public void testGetContests() {
        RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        model().newCalendarEvent(rhy, CalendarEventType.AMPUMAKILPAILU, eventDate1);
        model().newCalendarEvent(rhy, CalendarEventType.RIISTAPOLKUKILPAILU, eventDate2);
        model().newCalendarEvent(rhy, CalendarEventType.RIISTAKANTOJEN_HOITO_KOULUTUS, eventDate3);

        persistInNewTransaction();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.KILPAILUT));
        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CalendarEventType.AMPUMAKILPAILU, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(CalendarEventType.RIISTAPOLKUKILPAILU, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());
    }

    @Test
    public void testGetOtherEvents() {
        RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        model().newCalendarEvent(rhy, CalendarEventType.NUORISOTAPAHTUMA, eventDate1);
        model().newCalendarEvent(rhy, CalendarEventType.ERATAPAHTUMA, eventDate2);
        model().newCalendarEvent(rhy, CalendarEventType.RIISTAKANTOJEN_HOITO_KOULUTUS, eventDate3);

        persistInNewTransaction();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.MUUT_TAPAHTUMAT));
        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CalendarEventType.NUORISOTAPAHTUMA, result.get(0).getCalendarEventType());
        assertEquals(eventDate1.toDate(), result.get(0).getDate());
        assertEquals(CalendarEventType.ERATAPAHTUMA, result.get(1).getCalendarEventType());
        assertEquals(eventDate2.toDate(), result.get(1).getDate());
    }

    @Test
    public void testGetIndividualEvents() {
        RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        model().newCalendarEvent(rhy, CalendarEventType.METSASTAJATUTKINTO, eventDate1);
        model().newCalendarEvent(rhy, CalendarEventType.METSASTAJAKURSSI, eventDate1);
        model().newCalendarEvent(rhy, CalendarEventType.AMPUMAKOE, eventDate2);
        model().newCalendarEvent(rhy, CalendarEventType.JOUSIAMPUMAKOE, eventDate2);
        model().newCalendarEvent(rhy, CalendarEventType.VUOSIKOKOUS, eventDate2);

        persistInNewTransaction();

        CalendarEventSearchParamsDTO params = new CalendarEventSearchParamsDTO();
        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.METSASTAJATUTKINTO));
        List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(CalendarEventType.METSASTAJATUTKINTO, result.get(0).getCalendarEventType());

        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.METSASTAJAKURSSI));
        result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(CalendarEventType.METSASTAJAKURSSI, result.get(0).getCalendarEventType());

        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.AMPUMAKOE));
        result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(CalendarEventType.AMPUMAKOE, result.get(0).getCalendarEventType());

        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.JOUSIAMPUMAKOE));
        result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(CalendarEventType.JOUSIAMPUMAKOE, result.get(0).getCalendarEventType());

        params.setCalendarEventTypes(CalendarEventGroupType.getCalenderEventTypes(CalendarEventGroupType.VUOSIKOKOUS));
        result = calendarEventRepository.getCalendarEvents(params);

        assertNotNull(result);
        assertEquals(CalendarEventType.VUOSIKOKOUS, result.get(0).getCalendarEventType());
    }

}
