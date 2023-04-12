package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.iban4j.Iban;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJATUTKINTO;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTYKSENVALVOJA_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.VUOSIKOKOUS;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.ACCEPTED;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.ACCEPTED_SUBSIDIZED;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.PROPOSED;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.REJECTED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.UNQUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
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
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy, currentYear - 1);
            statistics.getOrCreateBasicInfo().setIban(null);

            persistInNewTransaction();

            assertNull(resolveIban(rhy, currentYear));
        });
    }

    @Test
    public void testGetIbanFromPreviousYear_whenAnnualStatisticsOfPreviousYearExists_andHasValidIban() {
        final int currentYear = DateUtil.currentYear();

        withRhy(rhy -> {
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy, currentYear - 1);

            persistInNewTransaction();

            assertEquals(statistics.getOrCreateBasicInfo().getIban(), resolveIban(rhy, currentYear));
        });
    }

    private Iban resolveIban(final Riistanhoitoyhdistys rhy, final int year) {
        return getResolver(rhy, year).getIbanFromPreviousYear();
    }

    private AnnualStatisticsResolver getResolver(final Riistanhoitoyhdistys rhy, final int year) {
        return service.getAnnualStatisticsResolver(rhy, year);
    }

    @Test
    public void testGetEventTypeCount() {
        withRhy(rhy -> {
            LocalDate eventDate = DateUtil.today().minusYears(1);
            LocalDate eventDateThisYear = DateUtil.today();
            LocalDate eventDatePreviousYear = DateUtil.today().minusYears(2);

            model().newCalendarEvent(rhy, AMPUMAKOE, eventDate);
            model().newCalendarEvent(rhy, AMPUMAKOE, eventDate);
            model().newCalendarEvent(rhy, AMPUMAKOE, eventDate);
            model().newCalendarEvent(rhy, AMPUMAKOE, eventDateThisYear);
            model().newCalendarEvent(rhy, AMPUMAKOE, eventDatePreviousYear);
            model().newCalendarEvent(rhy, METSASTAJATUTKINTO, eventDate);
            model().newCalendarEvent(rhy, METSASTAJATUTKINTO, eventDate);
            model().newCalendarEvent(rhy, VUOSIKOKOUS, eventDate);

            persistInNewTransaction();

            assertEquals(3, getResolver(rhy, eventDate.getYear()).getEventTypeCount(AMPUMAKOE));
            assertEquals(2, getResolver(rhy, eventDate.getYear()).getEventTypeCount(METSASTAJATUTKINTO));
            assertEquals(1, getResolver(rhy, eventDate.getYear()).getEventTypeCount(VUOSIKOKOUS));
        });
    }

    @Test
    public void testGetEventTypeCount_training() {
        withRhy(rhy -> {
            LocalDate eventDate = DateUtil.today().minusYears(1);

            model().newCalendarEvent(rhy, METSASTYKSENVALVOJA_KOULUTUS, eventDate);
            model().newCalendarEvent(rhy, METSASTYKSENVALVOJA_KOULUTUS, eventDate).setNonSubsidizable(true);
            model().newCalendarEvent(rhy, METSASTYKSENVALVOJA_KOULUTUS, eventDate).setNonSubsidizable(false);

            persistInNewTransaction();

            assertEquals(2, getResolver(rhy, eventDate.getYear()).getEventTypeCount(METSASTYKSENVALVOJA_KOULUTUS));
            assertEquals(1, getResolver(rhy, eventDate.getYear()).getNonSubsidizableEventTypeCount(METSASTYKSENVALVOJA_KOULUTUS));
        });
    }

    @Test
    public void testGetEventTypeCount_excludedFromStatistics() {
        withRhy(rhy -> {
            LocalDate eventDate = DateUtil.today().minusYears(1);

            model().newCalendarEvent(rhy, AMPUMAKOE, eventDate);
            model().newCalendarEvent(rhy, AMPUMAKOE, eventDate);
            model().newCalendarEvent(rhy, AMPUMAKOE, eventDate, true);

            persistInNewTransaction();

            assertEquals(2, getResolver(rhy, eventDate.getYear()).getEventTypeCount(AMPUMAKOE));
        });
    }

    @Test
    public void testGetShootingTestAttempts() {
        withRhy(rhy -> {
            LocalDate eventDate = new LocalDate(2018, 11, 6);
            LocalDate eventDate2 = new LocalDate(2018, 11, 5);

            CalendarEvent event = model().newCalendarEvent(rhy, AMPUMAKOE, eventDate);
            ShootingTestEvent shootingTestEvent = model().newShootingTestEvent(event);
            ShootingTestParticipant participant = model().newShootingTestParticipant(shootingTestEvent);
            model().newShootingTestAttempt(participant, MOOSE);
            model().newShootingTestAttempt(participant, MOOSE, UNQUALIFIED);

            CalendarEvent event2 = model().newCalendarEvent(rhy, AMPUMAKOE, eventDate2);
            ShootingTestEvent shootingTestEvent2 = model().newShootingTestEvent(event2);
            ShootingTestParticipant participant2 = model().newShootingTestParticipant(shootingTestEvent2);
            model().newShootingTestAttempt(participant2, MOOSE);
            model().newShootingTestAttempt(participant2, MOOSE, UNQUALIFIED);

            persistInNewTransaction();

            assertEquals(4, getResolver(rhy, eventDate.getYear()).getShootingTestTotalCount(MOOSE));
            assertEquals(2, getResolver(rhy, eventDate.getYear()).getShootingTestQualifiedCount(MOOSE));
        });
    }

    @Test
    public void testGetShootingTestAttempts_excludedFromStatistics() {
        withRhy(rhy -> {
            LocalDate eventDate = new LocalDate(2018, 11, 6);
            LocalDate eventDate2 = new LocalDate(2018, 11, 5);

            CalendarEvent event = model().newCalendarEvent(rhy, AMPUMAKOE, eventDate);
            ShootingTestEvent shootingTestEvent = model().newShootingTestEvent(event);
            ShootingTestParticipant participant = model().newShootingTestParticipant(shootingTestEvent);
            model().newShootingTestAttempt(participant, MOOSE);
            model().newShootingTestAttempt(participant, MOOSE, UNQUALIFIED);

            CalendarEvent event2 = model().newCalendarEvent(rhy, AMPUMAKOE, eventDate2, true);
            ShootingTestEvent shootingTestEvent2 = model().newShootingTestEvent(event2);
            ShootingTestParticipant participant2 = model().newShootingTestParticipant(shootingTestEvent2);
            model().newShootingTestAttempt(participant2, MOOSE);
            model().newShootingTestAttempt(participant2, MOOSE, UNQUALIFIED);

            persistInNewTransaction();

            assertEquals(2, getResolver(rhy, eventDate.getYear()).getShootingTestTotalCount(MOOSE));
            assertEquals(1, getResolver(rhy, eventDate.getYear()).getShootingTestQualifiedCount(MOOSE));
        });
    }

    @Test
    public void testGetHuntingControlEventCount_subsidizable() {
        withRhy(rhy -> {
            createHuntingControlEvents(rhy);
            assertEquals(2, getResolver(rhy, DateUtil.currentYear()).getHuntingControlEventCount());
        });
    }

    @Test
    public void testGetNonSubsidizableHuntingControlEventCount() {
        withRhy(rhy -> {
            createHuntingControlEvents(rhy);
            assertEquals(1, getResolver(rhy, DateUtil.currentYear()).getNonSubsidizableHuntingControlEventCount());
        });
    }

    @Test
    public void testGetHuntingControlCustomersCount() {
        withRhy(rhy -> {
            createHuntingControlEvents(rhy);
            assertEquals(9, getResolver(rhy, DateUtil.currentYear()).getHuntingControlCustomersCount());
        });
    }

    @Test
    public void testGetHuntingControlProofOrdersCount() {
        withRhy(rhy -> {
            createHuntingControlEvents(rhy);
            assertEquals(12, getResolver(rhy, DateUtil.currentYear()).getHuntingControlProofOrdersCount());
        });
    }

    private void createHuntingControlEvents(final Riistanhoitoyhdistys rhy) {
        // Subsidizable events
        final HuntingControlEvent newSubsidizableEvent = model().newHuntingControlEvent(rhy);
        newSubsidizableEvent.setStatus(ACCEPTED_SUBSIDIZED);
        newSubsidizableEvent.setCustomers(1);
        newSubsidizableEvent.setProofOrders(2);
        final HuntingControlEvent oldSubsidizableEvent = model().newHuntingControlEvent(rhy);
        oldSubsidizableEvent.setStatus(null);
        oldSubsidizableEvent.setCustomers(3);
        oldSubsidizableEvent.setProofOrders(4);

        // Non subsidizable event
        final HuntingControlEvent nonSubsidizableEvent = model().newHuntingControlEvent(rhy);
        nonSubsidizableEvent.setStatus(ACCEPTED);
        nonSubsidizableEvent.setCustomers(5);
        nonSubsidizableEvent.setProofOrders(6);

        // Not to be shown in statistics
        final HuntingControlEvent rejectedEvent = model().newHuntingControlEvent(rhy);
        rejectedEvent.setStatus(REJECTED);
        rejectedEvent.setCustomers(7);
        rejectedEvent.setProofOrders(8);
        final HuntingControlEvent proposedEvent = model().newHuntingControlEvent(rhy);
        proposedEvent.setStatus(PROPOSED);
        proposedEvent.setCustomers(9);
        proposedEvent.setProofOrders(10);

        persistInNewTransaction();
    }

}
