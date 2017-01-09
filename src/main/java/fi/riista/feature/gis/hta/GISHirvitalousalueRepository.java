package fi.riista.feature.gis.hta;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GISHirvitalousalueRepository extends BaseRepository<GISHirvitalousalue, Integer>, GISHirvitalousalueRepositoryCustom {

    GISHirvitalousalue findByNumber(String numero);

    @Query("select o from #{#entityName} o where o.number IN (?1)")
    List<GISHirvitalousalue> findByNumber(List<String> numero);
}
