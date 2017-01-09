package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.calendar.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface VenueRepository extends BaseRepository<Venue, Long> {
    @Query("select v from #{#entityName} v " +
            "left join v.address " +
            "where upper(v.name) like upper(?1) || '%'")
    Page<Venue> searchByName(String name, Pageable page);
}
