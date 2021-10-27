package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.repository.BaseRepository;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OtherwiseDeceasedRepository extends BaseRepository<OtherwiseDeceased, Long>, OtherwiseDeceasedRepositoryCustom {

    @Query("SELECT o FROM #{#entityName} o WHERE o.pointOfTime BETWEEN ?1 AND ?2 ORDER BY o.pointOfTime ASC")
    List<OtherwiseDeceased> findAllByPointOfTimeBetween(DateTime begin, DateTime end);
}
