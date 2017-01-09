package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.Organisation;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CalendarEventRepository extends BaseRepository<CalendarEvent, Long> {

    @Query("select t from #{#entityName} t" +
            " inner join fetch t.venue v" +
            " inner join fetch v.address" +
            " where t.organisation = ?1" +
            " order by t.date desc, t.beginTime desc")
    List<CalendarEvent> findByOrganisation(Organisation organisation);
}
