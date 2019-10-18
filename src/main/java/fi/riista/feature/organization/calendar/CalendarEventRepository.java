package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.Organisation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public interface CalendarEventRepository extends BaseRepository<CalendarEvent, Long>, CalendarEventRepositoryCustom {

    @Query("select e from #{#entityName} e where e.organisation = ?1 order by e.date desc, e.beginTime desc")
    List<CalendarEvent> findByOrganisation(Organisation organisation);

    @Query("select e from #{#entityName} e where e.organisation = ?1 and e.date between ?2 and ?3 order by e.date desc, e.beginTime desc")
    List<CalendarEvent> findByOrganisation(Organisation organisation, Date startTime, Date endTime);

    @Query("select e from #{#entityName} e" +
            " where e.organisation IN (:organisations)" +
            " and e.calendarEventType IN (:eventTypes)" +
            " and e.date between :beginDate and :endDate" +
            " order by e.date desc, e.beginTime desc")
    List<CalendarEvent> findBy(@Param("organisations") Collection<Organisation> organisations,
                               @Param("eventTypes") EnumSet<CalendarEventType> eventTypes,
                               @Param("beginDate") Date beginDate,
                               @Param("endDate") Date endDate);
}
