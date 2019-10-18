package fi.riista.feature.shootingtest;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.F;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.Projections.constructor;
import static com.querydsl.core.types.dsl.Expressions.cases;
import static com.querydsl.jpa.JPAExpressions.selectOne;
import static fi.riista.util.Collect.mappingTo;
import static java.util.Collections.emptyMap;

@Transactional
@Repository
public class ShootingTestParticipantRepositoryImpl implements ShootingTestParticipantRepositoryCustom {

    @Resource
    private JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    @Override
    public Map<Person, Integer> countShootingTestParticipationsForForeignPersons(final Collection<Person> foreignPersons) {
        if (foreignPersons.isEmpty()) {
            return emptyMap();
        }

        final Map<Long, Person> foreignPersonIndex = F.indexById(foreignPersons);
        final Map<Person, Integer> results = foreignPersons.stream().collect(mappingTo(key -> 0));

        final QPerson PERSON = QPerson.person;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;

        final NumberExpression<Integer> participationCount = PARTICIPANT.id.count().intValue();

        queryFactory
                .select(PERSON.id, participationCount)
                .from(PARTICIPANT)
                .join(PARTICIPANT.person, PERSON)
                .where(PERSON.in(foreignPersons).and(PERSON.ssn.isNull()))
                .groupBy(PERSON.id)
                .transform(groupBy(PERSON.id).as(participationCount))
                .forEach((foreignPersonId, numParticipations) -> {
                    results.put(foreignPersonIndex.get(foreignPersonId), numParticipations);
                });

        return results;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, ParticipantSummary> getParticipantSummaryByShootingTestEventId(final Collection<ShootingTestEvent> events) {
        if (events.isEmpty()) {
            return emptyMap();
        }

        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;

        final NumberPath<Long> shootingTestEventId = EVENT.id;

        final NumberExpression<Integer> countAllParticipants = PARTICIPANT.id.count().intValue();

        final NumberExpression<Integer> sumCompletedParticipants = cases()
                .when(PARTICIPANT.completed.eq(true)).then(1)
                .otherwise(0)
                .sum();

        final BooleanExpression attemptsNotExisting = selectOne()
                .from(ATTEMPT)
                .where(ATTEMPT.participant.id.eq(PARTICIPANT.id))
                .notExists();

        final NumberExpression<Integer> sumParticipantsWithNoAttempts = cases()
                .when(PARTICIPANT.completed.eq(false).and(attemptsNotExisting)).then(1)
                .otherwise(0)
                .sum();

        final NumberExpression<BigDecimal> totalPaidAmount = cases()
                .when(PARTICIPANT.paidAmount.isNotNull()).then(PARTICIPANT.paidAmount)
                .otherwise(BigDecimal.ZERO)
                .sum();

        return queryFactory
                .select(shootingTestEventId, countAllParticipants, sumCompletedParticipants,
                        sumParticipantsWithNoAttempts, totalPaidAmount)
                .from(PARTICIPANT)
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .where(EVENT.in(events))
                .groupBy(shootingTestEventId)
                .transform(groupBy(shootingTestEventId).as(constructor(ParticipantSummary.class,
                        countAllParticipants, sumCompletedParticipants, sumParticipantsWithNoAttempts,
                        totalPaidAmount)));
    }
}
