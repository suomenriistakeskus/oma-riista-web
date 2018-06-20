package fi.riista.util.jpa;

import fi.riista.feature.common.entity.DateTestEntity;
import fi.riista.feature.common.repository.DateTestEntityRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaPreds;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class JpaPredsTest extends EmbeddedDatabaseTest {

    @Resource
    private DateTestEntityRepository dateTestEntityRepo;

    @Test
    @Transactional
    public void testOverlapsInterval_4DateParams_whenNullBeginAndEndTime() {
        final LocalDate today = DateUtil.today();

        final List<DateTestEntity> allObjects = Arrays.asList(
                newDateTestEntity(null, null),
                newDateTestEntity(null, today),
                newDateTestEntity(null, today),
                newDateTestEntity(today, today),
                newDateTestEntity(today.minusYears(1), today.plusYears(1)));

        persistInCurrentlyOpenTransaction(allObjects);

        // All entities should pass because of infinite interval (beginTime and endTime are both null).

        assertEqualEntities(allObjects, queryDateEntities(null, null));
    }

    @Test
    @Transactional
    public void testOverlapsInterval_4DateParams_whenBeginTimeNull() {
        final DateTime now = DateUtil.now();
        final LocalDate today = now.toLocalDate();

        final List<DateTestEntity> expectedResults = Arrays.asList(
                newDateTestEntity(null, null),
                newDateTestEntity(null, today),
                newDateTestEntity(today, null),
                newDateTestEntity(today.minusDays(1), null));

        final List<DateTestEntity> unexpectedResults = singletonList(newDateTestEntity(today.plusDays(1), null));

        persistInCurrentlyOpenTransaction(expectedResults);
        persistInCurrentlyOpenTransaction(unexpectedResults);

        assertEqualEntities(expectedResults, queryDateEntities(null, now));
    }

    @Test
    @Transactional
    public void testOverlapsInterval_4DateParams_whenEndTimeNull() {
        final DateTime now = DateUtil.now();
        final LocalDate today = now.toLocalDate();

        final List<DateTestEntity> expectedResults = Arrays.asList(
                newDateTestEntity(null, null),
                newDateTestEntity(today, null),
                newDateTestEntity(null, today),
                newDateTestEntity(null, today.plusDays(1)));

        final List<DateTestEntity> unexpectedResults = singletonList(newDateTestEntity(null, today.minusDays(1)));

        persistInCurrentlyOpenTransaction(expectedResults);
        persistInCurrentlyOpenTransaction(unexpectedResults);

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

        final List<DateTestEntity> expectedResults = Arrays.asList(
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

        final List<DateTestEntity> unexpectedResults = Arrays.asList(
                newDateTestEntity(null, dateBeforeInterval),
                newDateTestEntity(dateBeforeInterval, dateBeforeInterval),
                newDateTestEntity(dateAfterInterval, null),
                newDateTestEntity(dateAfterInterval, dateAfterInterval));

        persistInCurrentlyOpenTransaction(expectedResults);
        persistInCurrentlyOpenTransaction(unexpectedResults);

        assertEqualEntities(expectedResults, queryDateEntities(beginTime, endTime));
    }

    private static DateTestEntity newDateTestEntity(final LocalDate beginDate, final LocalDate endDate) {
        return new DateTestEntity(beginDate, endDate);
    }

    private static void assertEqualEntities(final Collection<DateTestEntity> expectedResults,
                                            final Collection<DateTestEntity> results) {

        final Comparator<LocalDate> localDateComparator = Comparator.nullsFirst(LocalDate::compareTo);

        final Comparator<Tuple2<LocalDate, LocalDate>> firstDateComparator =
                comparing(Tuple2::_1, localDateComparator);

        final Comparator<Tuple2<LocalDate, LocalDate>> tupleComparator =
                firstDateComparator.thenComparing(comparing(Tuple2::_2, localDateComparator));

        final Function<Collection<DateTestEntity>, List<Tuple2<LocalDate, LocalDate>>> transformation =
                coll -> coll.stream()
                        .map(entity -> Tuple.of(entity.getBeginDate(), entity.getEndDate()))
                        .sorted(tupleComparator)
                        .collect(toList());

        assertEquals(transformation.apply(expectedResults), transformation.apply(results));
    }

    private List<DateTestEntity> queryDateEntities(@Nullable final DateTime beginTime,
                                                   @Nullable final DateTime endTime) {

        return dateTestEntityRepo.findAll((root, query, cb) -> {
            return JpaPreds.overlapsInterval(cb, root.get("beginDate"), root.get("endDate"), beginTime, endTime);
        });
    }
}
