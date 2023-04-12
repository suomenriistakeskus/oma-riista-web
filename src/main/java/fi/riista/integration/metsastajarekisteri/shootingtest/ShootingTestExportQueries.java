package fi.riista.integration.metsastajarekisteri.shootingtest;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.shootingtest.QShootingTestAttempt;
import fi.riista.feature.shootingtest.QShootingTestEvent;
import fi.riista.feature.shootingtest.QShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTest;
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

import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.util.Collect.mappingAndCollectingFirst;
import static fi.riista.util.Collect.toImmutableSortedMap;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
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
    public SortedMap<String, MR_Person> fetchPersonsWithHunterNumberGreaterThan(@Nonnull final String exclusiveLowerBoundForHunterNumber,
                                                                                final long numberOfPersonsToFetch,
                                                                                @Nonnull final LocalDate searchPeriodEndDate) {

        requireNonNull(exclusiveLowerBoundForHunterNumber, "exclusiveLowerBoundForHunterNumber is null");
        requireNonNull(searchPeriodEndDate, "searchPeriodEndDate is null");

        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;

        final SortedMap<String, MR_Person> personMap = jpqlQueryFactory
                .select(PARTICIPANT.hunterNumber).distinct()
                .from(PARTICIPANT)
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .where(createDatePredicate(CALENDAR_EVENT, searchPeriodEndDate),
                        PARTICIPANT.completed.isTrue(),
                        PARTICIPANT.hunterNumber.isNotNull(),
                        PARTICIPANT.hunterNumber.gt(exclusiveLowerBoundForHunterNumber))
                .orderBy(PARTICIPANT.hunterNumber.asc())
                .limit(numberOfPersonsToFetch)
                .fetch()
                .stream()
                .collect(toImmutableSortedMap(identity(), hunterNumber -> new MR_Person()
                        .withHunterNumber(hunterNumber)
                        .withValidTests(new MR_ShootingTestList())));

        if (!personMap.isEmpty()) {
            final String minHunterNumber = personMap.firstKey();
            final String maxHunterNumber = personMap.lastKey();

            fetchShootingTests(searchPeriodEndDate, minHunterNumber, maxHunterNumber)
                    .forEach((hunterNumber, shootingTestMap) -> personMap
                            .get(hunterNumber)
                            .getValidTests()
                            .withShootingTest(shootingTestMap
                                    .values()
                                    .stream()
                                    .sorted(MR_SHOOTING_TEST_ORDERING)
                                    .collect(toList())));
        }

        return personMap;
    }

    private Map<String, Map<ShootingTestType, MR_ShootingTest>> fetchShootingTests(final LocalDate searchPeriodEndDate,
                                                                                   final String minHunterNumber,
                                                                                   final String maxHunterNumber) {

        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QOrganisation RHY = QOrganisation.organisation;

        return jpqlQueryFactory
                .select(ATTEMPT.id,
                        ATTEMPT.type,
                        PARTICIPANT.id,
                        PARTICIPANT.hunterNumber,
                        EVENT.id,
                        CALENDAR_EVENT.date,
                        RHY.officialCode)
                .from(ATTEMPT)
                .join(ATTEMPT.participant, PARTICIPANT)
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .join(CALENDAR_EVENT.organisation, RHY)
                .where(createDatePredicate(CALENDAR_EVENT, searchPeriodEndDate),
                        PARTICIPANT.completed.isTrue(),
                        PARTICIPANT.totalDueAmount.eq(PARTICIPANT.paidAmount),
                        PARTICIPANT.hunterNumber.between(minHunterNumber, maxHunterNumber),
                        ATTEMPT.result.eq(QUALIFIED))
                .orderBy(CALENDAR_EVENT.date.desc())
                .fetch()
                .stream()
                .collect(groupingBy(
                        t -> t.get(PARTICIPANT.hunterNumber),
                        // Collect the most recent shooting test for each ShootingTestType.
                        groupingBy(
                                t -> t.get(ATTEMPT.type),
                                mappingAndCollectingFirst(t -> {

                                    final LocalDate validityBegin =
                                            DateUtil.toLocalDateNullSafe(t.get(CALENDAR_EVENT.date));

                                    return new MR_ShootingTest()
                                            .withType(t.get(ATTEMPT.type).toExportType())
                                            .withValidityBegin(validityBegin)
                                            .withValidityEnd(validityBegin.plus(ShootingTest.VALIDITY_PERIOD))
                                            .withRHY(t.get(RHY.officialCode))
                                            .withEventId(t.get(EVENT.id))
                                            .withParticipantId(t.get(PARTICIPANT.id))
                                            .withExecutionId(t.get(ATTEMPT.id));

                                }))));
    }

    private static BooleanExpression createDatePredicate(final QCalendarEvent calendarEventMeta,
                                                         final LocalDate searchPeriodEndDate) {

        final LocalDate searchPeriodBeginDate = searchPeriodEndDate.minus(ShootingTest.VALIDITY_PERIOD);

        return calendarEventMeta.date.between(searchPeriodBeginDate.toDate(), searchPeriodEndDate.toDate());
    }
}
