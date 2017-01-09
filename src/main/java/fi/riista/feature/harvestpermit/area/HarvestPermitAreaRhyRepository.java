package fi.riista.feature.harvestpermit.area;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface HarvestPermitAreaRhyRepository extends JpaRepository<HarvestPermitAreaRhy, Long> {
    @Modifying
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);
}
