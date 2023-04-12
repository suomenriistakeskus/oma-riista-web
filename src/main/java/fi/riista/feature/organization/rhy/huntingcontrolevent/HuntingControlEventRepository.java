package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.jpa.JpaSubQuery;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;
import java.util.List;

import static fi.riista.util.jpa.JpaSpecs.equal;

public interface HuntingControlEventRepository extends BaseRepository<HuntingControlEvent, Long>, HuntingControlEventRepositoryCustom {
    List<HuntingControlEvent> findByRhyIdAndDateBetweenAndStatusOrderByDateDesc(final Long orgId,
                                                                                final LocalDate startDate,
                                                                                final LocalDate endDate,
                                                                                final HuntingControlEventStatus status);
    List<HuntingControlEvent> findByRhyAndDateBetweenOrderByDateDesc(final Organisation org, final LocalDate startDate, final LocalDate endDate);
    List<HuntingControlEvent> findByRhyAndDateBetweenOrderByDateAsc(final Organisation org, final LocalDate startDate, final LocalDate endDate);
    HuntingControlEvent findByMobileClientRefId(final Long refId);

    default List<HuntingControlEvent> findByRhyAndYearAndInspectorOrderByDateDesc(final Riistanhoitoyhdistys rhy,
                                                                                  final int year,
                                                                                  final Person inspector) {
        return this.findAll(
                equal(HuntingControlEvent_.rhy, rhy)
                        .and(personIsEventInspector(inspector))
                        .and(eventDateWithinCalendarYear(year)),
                Sort.by("date").descending()
                        .and(Sort.by("beginTime").descending())
                        .and(Sort.by("endTime").descending()));
    }

    default Specification<HuntingControlEvent> personIsEventInspector(final Person person) {
        return JpaSubQuery.of(HuntingControlEvent_.inspectors).exists((root, cb) -> cb.equal(root, person));
    }

    default Specification<HuntingControlEvent> eventDateWithinCalendarYear(final int year) {
        final LocalDate startDate = new LocalDate(year, 1, 1);
        final LocalDate endDate = new LocalDate(year, 12, 31);

        return (root, query, cb) -> {
            final Path<LocalDate> datePath = root.get(HuntingControlEvent_.date);
            return cb.and(cb.greaterThanOrEqualTo(datePath, startDate), cb.lessThanOrEqualTo(datePath, endDate));
        };
    }

}
