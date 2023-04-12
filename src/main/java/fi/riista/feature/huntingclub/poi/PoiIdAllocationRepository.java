package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.huntingclub.HuntingClub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PoiIdAllocationRepository extends JpaRepository<PoiIdAllocation, Long> {

    List<PoiIdAllocation> findByClub(HuntingClub club);
}
