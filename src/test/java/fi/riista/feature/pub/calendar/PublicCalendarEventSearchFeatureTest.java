package fi.riista.feature.pub.calendar;

import com.google.common.collect.Lists;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventGroupType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.organization.calendar.CalendarEventGroupType.KILPAILUT;
import static fi.riista.feature.organization.calendar.CalendarEventGroupType.KOULUTUSTILAISUUDET;
import static fi.riista.feature.organization.calendar.CalendarEventGroupType.MUUT_TAPAHTUMAT;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKILPAILU;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.ERATAPAHTUMA;
import static fi.riista.feature.organization.calendar.CalendarEventType.HARJOITUSAMMUNTA;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTYKSENJOHTAJA_HIRVIELAIMET;
import static fi.riista.feature.organization.calendar.CalendarEventType.NUORISOTAPAHTUMA;
import static fi.riista.feature.organization.calendar.CalendarEventType.PETOYHDYSHENKILO_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.RIISTAPOLKUKILPAILU;
import static fi.riista.feature.organization.calendar.CalendarEventType.YLIMAARAINEN_KOKOUS;
import static fi.riista.util.DateUtil.today;

import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJATUTKINTO;
import static fi.riista.feature.organization.calendar.CalendarEventType.VUOSIKOKOUS;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class PublicCalendarEventSearchFeatureTest extends EmbeddedDatabaseTest {

    private final LocalDate date = DateUtil.today();
    private RiistakeskuksenAlue rka;
    private RiistakeskuksenAlue rka2;
    private Riistanhoitoyhdistys rhy;
    private Riistanhoitoyhdistys rhyWithEvents;
    private String rkaCode;
    private String rhyCode;

    private String rka2Code;
    private String rhyWithEventsCode;

    private final LocalDate novemberDate = new LocalDate(2018, 11, 6);
    private final LocalDate decemberDate = new LocalDate(2018, 12, 24);
    
    @Resource
    private PublicCalendarEventSearchFeature publicCalendarEventSearchFeature;

    @Before
    public void setup() {
        rka = model().newRiistakeskuksenAlue();
        rkaCode = rka.getOfficialCode();
        rhy = model().newRiistanhoitoyhdistys(rka);
        rhyCode = rhy.getOfficialCode();
        rka2 = model().newRiistakeskuksenAlue();
        rka2Code = rka2.getOfficialCode();
        rhyWithEvents = model().newRiistanhoitoyhdistys(rka2);
        rhyWithEventsCode = rhyWithEvents.getOfficialCode();

        LocaleContextHolder.setLocale(Locales.FI);
    }

    @After
    public void tearDown() {
        LocaleContextHolder.setLocale(null);
    }

    @Test
    public void testDoesNotFindWithWrongRhy() {
        setupEvents();

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(novemberDate.plusWeeks(1));
        params.setAreaId(rkaCode);
        params.setRhyId(Collections.singletonList(rhyCode));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(0));
    }


    @Test
    public void testFindsWithCorrectRhy() {
        setupEvents();

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(novemberDate.plusWeeks(1));
        params.setRhyId(Collections.singletonList(rhyWithEventsCode));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(1));
        Assert.assertEquals(rhyWithEventsCode, result.getEvents().iterator().next().getOrganisation().getOfficialCode());
    }


    @Test
    public void testFindsWithRka_eventInRhy() {
        setupEvents();

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(novemberDate.plusWeeks(1));
        params.setAreaId(rka2Code);
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(1));
        Assert.assertEquals(rhyWithEventsCode, result.getEvents().iterator().next().getOrganisation().getOfficialCode());
    }

    @Test
    public void testFindsWithRka_eventInRka() {
        model().newCalendarEvent(
                rka,
                AMPUMAKOE,
                novemberDate);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(novemberDate.plusWeeks(1));
        params.setAreaId(rkaCode);
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(1));
        Assert.assertEquals(rkaCode, result.getEvents().iterator().next().getOrganisation().getOfficialCode());
    }


    @Test
    public void testFindsWithoutRka_eventInRka() {
        model().newCalendarEvent(
                rka,
                AMPUMAKOE,
                novemberDate);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(novemberDate.plusWeeks(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(1));
        Assert.assertEquals(rkaCode, result.getEvents().iterator().next().getOrganisation().getOfficialCode());
    }

    @Test
    public void testDoesNotReturnClubEvents_eventInClub() {
        HuntingClub club = model().newHuntingClub(rhy);
        model().newCalendarEvent(
                club,
                AMPUMAKOE,
                novemberDate);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(novemberDate.plusWeeks(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(0));
    }


    @Test
    public void testDoesNotReturnClubEvents_eventInClubGroup() {
        HuntingClub club = model().newHuntingClub(rhy);
        HuntingClubGroup group = model().newHuntingClubGroup(club);
        model().newCalendarEvent(
                group,
                AMPUMAKOE,
                novemberDate);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(novemberDate.plusWeeks(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(0));
    }

    @Test
    public void testFindsWithInterval_isBetweenBeginAndEnd() {
        setupEvents();

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate);
        params.setEnd(novemberDate);
        params.setAreaId(rka2Code);
        params.setRhyId(Collections.singletonList(rhyWithEventsCode));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(1));
        Assert.assertEquals(novemberDate, result.getEvents().iterator().next().getDate());
    }

    @Test
    public void testFindsWithInterval_isBeforeBeginEndNotGiven() {
        createEvents(1);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(date.plusDays(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(0));
    }


    @Test
    public void testFindsWithInterval_isAfterEndBeginNotGiven() {
        createEvents(1);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setEnd(date.minusDays(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(0));
    }

    @Test
    public void testFindsWithInterval_beginAndEndNulls() {
        createEvents(1);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(1));
    }

    @Test
    public void testFindsWithCorrectType() {
        setupEvents();

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(novemberDate.minusWeeks(1));
        params.setEnd(decemberDate.plusWeeks(1));
        params.setCalendarEventType(CalendarEventGroupType.METSASTAJATUTKINTO);
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(1));
        Assert.assertEquals(METSASTAJATUTKINTO, result.getEvents().iterator().next().getCalendarEventType().getCalendarEventType());
    }

    @Test
    public void testMaxResults() {

        final int maxResults = 3;

        createEvents(maxResults);


        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(date);
        params.setEnd(date);
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params, maxResults);

        assertFalse(result.isTooManyResults());
        assertThat(result.getEvents(), hasSize(maxResults));
    }

    @Test
    public void testMoreThanMaxResults() {
        final int maxResults = 3;
        final int numberOfEvents = maxResults + 1;

        createEvents(numberOfEvents);


        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();

        params.setBegin(date);
        params.setEnd(date);
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params, maxResults);

        assertTrue(result.isTooManyResults());
        assertNull(result.getEvents());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestedPageSizeCannotExceedMaxResults() {

        final int maxResults = 3;

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();

        params.setPageSize(maxResults + 1);
        params.setBegin(date);
        params.setEnd(date);
        params.setPageNumber(0);
        publicCalendarEventSearchFeature.findCalendarEvents(params, maxResults);

        Assert.fail("Should have thrown an exception");
    }

    @Test
    public void testPaging() {
        final int firstPageOffset = 0;
        final int secondPageOffset = 1;
        final int maxResults = 3;
        final int numberOfEvents = maxResults + 1;
        final int pageSize = maxResults;


        createEvents(numberOfEvents);


        persistInNewTransaction();

        // First page
        final PublicCalendarEventSearchDTO firstParams = new PublicCalendarEventSearchDTO();

        firstParams.setBegin(date);
        firstParams.setEnd(date);
        firstParams.setPageSize(maxResults);
        firstParams.setPageNumber(firstPageOffset);
        final PublicCalendarEventSearchResultDTO firstResult = publicCalendarEventSearchFeature.findCalendarEvents(firstParams, maxResults);

        assertFalse(firstResult.isTooManyResults());
        assertFalse(firstResult.isLastPage());
        assertThat(firstResult.getEvents(), hasSize(pageSize));

        // Second page
        final PublicCalendarEventSearchDTO secondParams = new PublicCalendarEventSearchDTO();

        secondParams.setPageSize(maxResults);
        secondParams.setBegin(date);
        secondParams.setEnd(date);
        secondParams.setPageNumber(secondPageOffset);
        final PublicCalendarEventSearchResultDTO secondResult = publicCalendarEventSearchFeature.findCalendarEvents(secondParams, maxResults);

        assertFalse(secondResult.isTooManyResults());
        assertTrue(secondResult.isLastPage());
        assertThat(secondResult.getEvents(), hasSize(numberOfEvents - pageSize));
    }

    private void createEvents(final int numberOfEvents) {
        for (int i = 0; i < numberOfEvents; i++) {
        final CalendarEvent event = model().newCalendarEvent(rhy);
            event.setDate(DateUtil.toDateNullSafe(date));

        }
    }

    @Test
    public void testSorting_byEventDateFirst() {
        model().newCalendarEvent(rhy, AMPUMAKOE, date.plusDays(1));
        model().newCalendarEvent(rhy, METSASTAJATUTKINTO, date);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(date.minusWeeks(1));
        params.setEnd(date.plusWeeks(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        final List<PublicCalendarEventDTO> events = result.getEvents();

        assertThat(events, hasSize(2));

        // Metsästäjätutkinto should be first
        Assert.assertEquals(METSASTAJATUTKINTO, events.get(0).getCalendarEventType().getCalendarEventType());
        Assert.assertEquals(AMPUMAKOE, events.get(1).getCalendarEventType().getCalendarEventType());
    }

    @Test
    public void testSorting_byEventBeginTimeSecond() {
        final CalendarEvent event1 = model().newCalendarEvent(rhy, AMPUMAKOE, date);
        event1.setBeginTime(new LocalTime(12, 0));
        final CalendarEvent event2 = model().newCalendarEvent(rhy, METSASTAJATUTKINTO, date);
        event2.setBeginTime(new LocalTime(10, 0));

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(date.minusWeeks(1));
        params.setEnd(date.plusWeeks(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        final List<PublicCalendarEventDTO> events = result.getEvents();

        assertThat(events, hasSize(2));

        // Metsästäjätutkinto should be first
        Assert.assertEquals(METSASTAJATUTKINTO, events.get(0).getCalendarEventType().getCalendarEventType());
        Assert.assertEquals(AMPUMAKOE, events.get(1).getCalendarEventType().getCalendarEventType());
    }

    @Test
    public void testSorting_byEventIdThird() {
        model().newCalendarEvent(rhy, AMPUMAKOE, date);
        model().newCalendarEvent(rhy, METSASTAJATUTKINTO, date);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setBegin(date.minusWeeks(1));
        params.setEnd(date.plusWeeks(1));
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        final List<PublicCalendarEventDTO> events = result.getEvents();

        assertThat(events, hasSize(2));

        // Ampumakoe should be first
        Assert.assertEquals(AMPUMAKOE, events.get(0).getCalendarEventType().getCalendarEventType());
        Assert.assertEquals(METSASTAJATUTKINTO, events.get(1).getCalendarEventType().getCalendarEventType());
    }

    private void setupEvents() {
        model().newCalendarEvent(
                rhyWithEvents,
                AMPUMAKOE,
                novemberDate);
        model().newCalendarEvent(
                rhyWithEvents,
                METSASTAJATUTKINTO,
                decemberDate);
    }

    @Test
    public void testFindsPublicEvents() {
        model().newCalendarEvent(rhy, AMPUMAKOE, true);
        model().newCalendarEvent(rhy, METSASTAJATUTKINTO, true);
        model().newCalendarEvent(rhy, VUOSIKOKOUS, false);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        final List<PublicCalendarEventDTO> events = result.getEvents();

        assertThat(events, hasSize(2));

        Assert.assertEquals(AMPUMAKOE, events.get(0).getCalendarEventType().getCalendarEventType());
        Assert.assertEquals(METSASTAJATUTKINTO, events.get(1).getCalendarEventType().getCalendarEventType());
    }

    @Test
    public void testFindWithAdditionalEvents() {
        final CalendarEvent event = model().newCalendarEvent(rhy, VUOSIKOKOUS);
        model().newAdditionalCalendarEvent(event, novemberDate.plusDays(1), new LocalTime(12, 0), event.getVenue());

        model().newCalendarEvent(rhy, AMPUMAKOE);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        final List<PublicCalendarEventDTO> events = result.getEvents();

        assertThat(events, hasSize(3));
    }

    @Test
    public void testSortingByDateWithAdditionalEvents() {
        final LocalDate event1Date = today();
        final LocalDate additionalEventDate = today().plusDays(6);
        final LocalDate event2Date = today().plusDays(2);

        final CalendarEvent event = model().newCalendarEvent(rhy, VUOSIKOKOUS, event1Date);
        model().newAdditionalCalendarEvent(event, additionalEventDate, new LocalTime(12, 0), event.getVenue());

        model().newCalendarEvent(rhy, AMPUMAKOE, event2Date);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        final List<PublicCalendarEventDTO> events = result.getEvents();

        assertThat(events, hasSize(3));

        assertEquals(event1Date, events.get(0).getDate());
        assertEquals(event2Date, events.get(1).getDate());
        assertEquals(additionalEventDate, events.get(2).getDate());
    }

    @Test
    public void testCalendarEvents_ByMultipleRhys() {

        final Collection<Riistanhoitoyhdistys> rhys = Lists.newArrayList(
                model().newRiistanhoitoyhdistys(rka),
                model().newRiistanhoitoyhdistys(rka),
                model().newRiistanhoitoyhdistys(rka));

        Collection<String> rhyIds = new ArrayList<>();

        for (Riistanhoitoyhdistys rhy : rhys) {
            model().newCalendarEvent(rhy, AMPUMAKOE, date.plusDays(2));
            model().newCalendarEvent(rhy, METSASTAJATUTKINTO, date.minusDays(3));
            model().newCalendarEvent(rhy, METSASTAJATUTKINTO, novemberDate);

            rhyIds.add(rhy.getOfficialCode());
        }

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();
        params.setRhyId(rhyIds);
        params.setCalendarEventType(CalendarEventGroupType.METSASTAJATUTKINTO);
        params.setBegin(date.minusWeeks(1));
        params.setEnd(date.plusWeeks(1));

        final PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);

        assertThat(result.getEvents(), hasSize(3));
        assertEquals(METSASTAJATUTKINTO, result.getEvents().get(0).getCalendarEventType().getCalendarEventType());
    }

    @Test
    public void testCalendarEvents_ByGroupType() {
        model().newCalendarEvent(rhy, AMPUMAKOE, novemberDate);

        model().newCalendarEvent(rhy, RIISTAPOLKUKILPAILU, novemberDate);
        model().newCalendarEvent(rhy, AMPUMAKILPAILU, novemberDate);

        model().newCalendarEvent(rhy, AMPUMAKOULUTUS, novemberDate);
        model().newCalendarEvent(rhy, METSASTYKSENJOHTAJA_HIRVIELAIMET, novemberDate);
        model().newCalendarEvent(rhy, PETOYHDYSHENKILO_KOULUTUS, novemberDate);

        model().newCalendarEvent(rhy, YLIMAARAINEN_KOKOUS, novemberDate);
        model().newCalendarEvent(rhy, NUORISOTAPAHTUMA, novemberDate);
        model().newCalendarEvent(rhy, ERATAPAHTUMA, novemberDate);
        model().newCalendarEvent(rhy, HARJOITUSAMMUNTA, novemberDate);

        persistInNewTransaction();

        final PublicCalendarEventSearchDTO params = new PublicCalendarEventSearchDTO();

        params.setCalendarEventType(CalendarEventGroupType.AMPUMAKOE);
        PublicCalendarEventSearchResultDTO result = publicCalendarEventSearchFeature.findCalendarEvents(params);
        assertThat(result.getEvents(), hasSize(1));

        params.setCalendarEventType(KILPAILUT);
        result = publicCalendarEventSearchFeature.findCalendarEvents(params);
        assertThat(result.getEvents(), hasSize(2));

        params.setCalendarEventType(KOULUTUSTILAISUUDET);
        result = publicCalendarEventSearchFeature.findCalendarEvents(params);
        assertThat(result.getEvents(), hasSize(3));

        params.setCalendarEventType(MUUT_TAPAHTUMAT);
        result = publicCalendarEventSearchFeature.findCalendarEvents(params);
        assertThat(result.getEvents(), hasSize(4));
    }

}


