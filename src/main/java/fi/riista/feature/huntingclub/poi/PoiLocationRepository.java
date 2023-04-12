package fi.riista.feature.huntingclub.poi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PoiLocationRepository extends JpaRepository<PoiLocation, Long>, PoiLocationRepositoryCustom {

    List<PoiLocation> findAllByPoi(PoiLocationGroup poi);

    List<PoiLocation> findAllByPoiIn(Collection<PoiLocationGroup> pois);

    void deleteAllByIdIn(Collection<Long> ids);

    void deleteAllByPoi(PoiLocationGroup poi);
}
