package fi.riista.util;

import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.DateTestEntity;
import fi.riista.feature.common.repository.DateTestEntityRepository;
import fi.riista.feature.common.entity.EntityPersister;
import fi.riista.util.jpa.JpaPreds;

import javaslang.Tuple;
import javaslang.Tuple2;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class JpaPredsTest extends EmbeddedDatabaseTest {

    @Resource
    private DateTestEntityRepository dateTestEntityRepo;

    @Resource
    private EntityPersister persister;

    @Test
    @Transactional
    public void testOverlapsInterval_4DateParams_whenNullBeginAndEndTime() {
        final LocalDate today = DateUtil.today();

        final Set<DateTestEntity> allObjects = Sets.newHashSet(
                newDateTestEntity(null, null),
                newDateTestEntity(null, today),
                newDateTestEntity(null, today),
                newDateTestEntity(today, today),
                newDateTestEntity(today.minusYears(1), today.plusYears(1)));

        persister.saveInCurrentlyOpenTransaction(allObjects);

        // All entities should pass because of infinite interval (beginTime and endTime are both null).

        assertEqualEntities(allObjects, queryDateEntities(null, null));
    }

    @Test
    @Transactional
    public void testOverlapsInterval_4DateParams_whenBeginTimeNull() {
        final DateTime now = DateUtil.now();
        final LocalDate today = now.toLocalDate();

        final Set<DateTestEntity> expectedResults = Sets.newHashSet(
                newDateTestEntity(null, null),
                newDateTestEntity(null, today),
                newDateTestEntity(today, null),
                newDateTestEntity(today.minusDays(1), null));

        final Set<DateTestEntity> unexpectedResults = singleton(
                newDateTestEntity(today.plusDays(1), null));

        persister.saveInCurrentlyOpenTransaction(Sets.union(expectedResults, unexpectedResults));

        assertEqualEntities(expectedResults, queryDateEntities(null, now));
    }

    @Test
    @Transactional
    public void testOverlapsInterval_4DateParams_whenEndTimeNull() {
        final DateTime now = DateUtil.now();
        final LocalDate today = now.toLocalDate();

        final Set<DateTestEntity> expectedResults = Sets.newHashSet(
                newDateTestEntity(null, null),
                newDateTestEntity(today, null),
                newDateTestEntity(null, today),
                newDateTestEntity(null, today.plusDays(1)));

        final Set<DateTestEntity> unexpectedResults = singleton(
                newDateTestEntity(null, today.minusDays(1)));

        persister.saveInCurrentlyOpenTransaction(Sets.union(expectedResults, unexpectedResults));

        assertEqualEntities(expectedResults, queryDateEntities(now, null));
    }

    @Test
    @Transactional
    public void testOverlapsInterval_4DateParams_whenBeginAndEndTimeNotNull() {
        final DateTime now = DateUtil.now();
        final LocalDate today = now.toLocalDate();

        final LocalTime timeOfDay = new LocalTime(0, 0);
        final DateTime beginTime = now.minusDays(1).withTime(timeOfDay);
        final DateTime endTime = now.plusDays(2).withTime(timeOfDay);

        // Begin date of query interval.
        final LocalDate intervalBeginDate = beginTime.toLocalDate();
        // End date of query interval.
        final LocalDate intervalEndDate = endTime.toLocalDate().minusDays(1);

        final LocalDate dateBeforeInterval = intervalBeginDate.minusDays(1);
        final LocalDate dateInMiddleOfInterval = today;
        final LocalDate dateAfterInterval = intervalEndDate.plusDays(1);

        final Set<DateTestEntity> expectedResults = Sets.newHashSet(
                newDateTestEntity(null, null),

                newDateTestEntity(null, intervalBeginDate),
                newDateTestEntity(null, dateInMiddleOfInterval),
                newDateTestEntity(null, intervalEndDate),
                newDateTestEntity(null, dateAfterInterval),

                newDateTestEntity(dateBeforeInterval, intervalBeginDate),
                newDateTestEntity(dateBeforeInterval, dateInMiddleOfInterval),
                newDateTestEntity(dateBeforeInterval, intervalEndDate),
                newDateTestEntity(dateBeforeInterval, dateAfterInterval),

                newDateTestEntity(intervalBeginDate, intervalBeginDate),
                newDateTestEntity(intervalBeginDate, dateInMiddleOfInterval),
                newDateTestEntity(intervalBeginDate, intervalEndDate),
                newDateTestEntity(intervalBeginDate, dateAfterInterval),

                newDateTestEntity(dateInMiddleOfInterval, dateInMiddleOfInterval),
                newDateTestEntity(dateInMiddleOfInterval, intervalEndDate),
                newDateTestEntity(dateInMiddleOfInterval, dateAfterInterval),

                newDateTestEntity(intervalEndDate, intervalEndDate),
                newDateTestEntity(intervalEndDate, dateAfterInterval));

        final Set<DateTestEntity> unexpectedResults = Sets.newHashSet(
                newDateTestEntity(null, dateBeforeInterval),
                newDateTestEntity(dateBeforeInterval, dateBeforeInterval),
                newDateTestEntity(dateAfterInterval, null),
                newDateTestEntity(dateAfterInterval, dateAfterInterval));

        persister.saveInCurrentlyOpenTransaction(Sets.union(expectedResults, unexpectedResults));

        assertEqualEntities(expectedResults, queryDateEntities(beginTime, endTime));
    }

    private static DateTestEntity newDateTestEntity(final LocalDate beginDate, final LocalDate endDate) {
        return new DateTestEntity(beginDate, endDate);
    }

    private static void assertEqualEntities(
            final Collection<DateTestEntity> expectedResults, final Collection<DateTestEntity> results) {

        final Comparator<LocalDate> localDateComparator = Comparator.nullsFirst(LocalDate::compareTo);

        final Comparator<Tuple2<LocalDate, LocalDate>> firstDateComparator =
                comparing(Tuple2::_1, localDateComparator);

        final Comparator<Tuple2<LocalDate, LocalDate>> tupleComparator =
                firstDateComparator.thenComparing(comparing(Tuple2::_2, localDateComparator));

        final Function<Collection<DateTestEntity>, Set<Tuple2<LocalDate, LocalDate>>> transformation =
                coll -> coll.stream()
                        .map(entity -> Tuple.of(entity.getBeginDate(), entity.getEndDate()))
                        .sorted(tupleComparator)
                        .collect(toSet());

        assertEquals(transformation.apply(expectedResults), transformation.apply(results));
    }

    private Set<DateTestEntity> queryDateEntities(
            @Nullable final DateTime beginTime, @Nullable final DateTime endTime) {

        return new HashSet<>(dateTestEntityRepo.findAll((root, query, cb) -> JpaPreds.overlapsInterval(
                cb, root.get("beginDate"), root.get("endDate"), beginTime, endTime)));
    }

}
