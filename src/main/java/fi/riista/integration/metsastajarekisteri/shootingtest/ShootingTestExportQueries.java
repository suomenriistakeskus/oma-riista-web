package fi.riista.integration.metsastajarekisteri.shootingtest;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.shootingtest.QShootingTestAttempt;
import fi.riista.feature.shootingtest.QShootingTestEvent;
import fi.riista.feature.shootingtest.QShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestType;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

import static fi.riista.feature.shootingtest.ShootingTestAttempt.SHOOTING_TEST_VALIDITY_PERIOD;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.util.Collect.mappingAndCollectingFirst;
import static fi.riista.util.Collect.toImmutableSortedMap;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class ShootingTestExportQueries {

    protected static final Comparator<MR_ShootingTest> MR_SHOOTING_TEST_ORDERING =
            comparing(MR_ShootingTest::getValidityBegin)
                    .reversed()
                    .thenComparing(MR_ShootingTest::getType);

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public SortedMap<Long, MR_Person> fetchPersonsWithIdGreaterThan(final long exclusiveLowerBoundForPersonId,
                                                                    final long numberOfPersonsToFetch,
                                                                    @Nonnull final LocalDate searchPeriodEndDate) {

        requireNonNull(searchPeriodEndDate);

        final QPerson PERSON = QPerson.person;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;

        final SortedMap<Long, MR_Person> personsById = jpqlQueryFactory
                .select(PERSON.id, PERSON.hunterNumber)
                .distinct()
                .from(PARTICIPANT)
                .join(PARTICIPANT.person, PERSON)
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .where(createDatePredicate(CALENDAR_EVENT, searchPeriodEndDate),
                        EVENT.lockedTime.isNotNull(),
                        PARTICIPANT.completed.isTrue(),
                        // Using person ID predicate instead of SQL offset
                        // parameter guarantees use of database index.
                        PERSON.id.gt(exclusiveLowerBoundForPersonId))
                .orderBy(PERSON.id.asc())
                .limit(numberOfPersonsToFetch)
                .fetch()
                .stream()
                .collect(toImmutableSortedMap(
                        t -> t.get(PERSON.id),
                        t -> new MR_Person()
                                .withHunterNumber(t.get(PERSON.hunterNumber))
                                .withValidTests(new MR_ShootingTestList())));

        if (!personsById.isEmpty()) {
            final long minPersonId = personsById.firstKey();
            final long maxPersonId = personsById.lastKey();

            fetchShootingTests(searchPeriodEndDate, minPersonId, maxPersonId).forEach((personId, shootingTestMap) -> {

                personsById.get(personId)
                        .getValidTests()
                        .withShootingTest(shootingTestMap
                                .values()
                                .stream()
                                .sorted(MR_SHOOTING_TEST_ORDERING)
                                .collect(toList()));
            });
        }

        return personsById;
    }

    private Map<Long, Map<ShootingTestType, MR_ShootingTest>> fetchShootingTests(final LocalDate searchPeriodEndDate,
                                                                                 final long minPersonId,
                                                                                 final long maxPersonId) {

        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QOrganisation RHY = QOrganisation.organisation;

        final NumberPath<Long> personId = PARTICIPANT.person.id;

        return jpqlQueryFactory
                .select(ATTEMPT.id,
                        ATTEMPT.type,
                        PARTICIPANT.id,
                        personId,
                        EVENT.id,
                        CALENDAR_EVENT.date,
                        RHY.officialCode)
                .from(ATTEMPT)
                .join(ATTEMPT.participant, PARTICIPANT)
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .join(CALENDAR_EVENT.organisation, RHY)
                .where(createDatePredicate(CALENDAR_EVENT, searchPeriodEndDate),
                        EVENT.lockedTime.isNotNull(),
                        PARTICIPANT.completed.isTrue(),
                        PARTICIPANT.totalDueAmount.eq(PARTICIPANT.paidAmount),
                        personId.between(minPersonId, maxPersonId),
                        ATTEMPT.result.eq(QUALIFIED))
                .fetch()
                .stream()
                .collect(groupingBy(
                        t -> t.get(personId),
                        // Collect the most recent shooting test for each ShootingTestType.
                        groupingBy(
                                t -> t.get(ATTEMPT.type),
                                mappingAndCollectingFirst(t -> {

                                    final LocalDate validityBegin =
                                            DateUtil.toLocalDateNullSafe(t.get(CALENDAR_EVENT.date));

                                    return new MR_ShootingTest()
                                            .withType(t.get(ATTEMPT.type).toExportType())
                                            .withValidityBegin(validityBegin)
                                            .withValidityEnd(validityBegin.plus(SHOOTING_TEST_VALIDITY_PERIOD))
                                            .withRHY(t.get(RHY.officialCode))
                                            .withEventId(t.get(EVENT.id))
                                            .withParticipantId(t.get(PARTICIPANT.id))
                                            .withExecutionId(t.get(ATTEMPT.id));

                                }))));
    }

    private static BooleanExpression createDatePredicate(final QCalendarEvent calendarEventMeta,
                                                         final LocalDate searchPeriodEndDate) {

        final LocalDate searchPeriodBeginDate = searchPeriodEndDate.minus(SHOOTING_TEST_VALIDITY_PERIOD);

        return calendarEventMeta.date.between(searchPeriodBeginDate.toDate(), searchPeriodEndDate.toDate());
    }
}
