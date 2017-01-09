package fi.riista.feature.organization.rhy;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RiistanhoitoyhdistysRepository extends BaseRepository<Riistanhoitoyhdistys, Long> {

    Riistanhoitoyhdistys findByOfficialCode(String officialCode);

    @Query("select o from #{#entityName} o where o.officialCode IN (?1)")
    List<Riistanhoitoyhdistys> findByOfficialCode(List<String> officialCode);
}
