package fi.riista.feature.huntingclub.poi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoiLocationGroupRepository extends JpaRepository<PoiLocationGroup, Long> {
    List<PoiLocationGroup> findAllByPoiIdAllocation(PoiIdAllocation poi);
}
