package fi.riista.feature.harvestpermit.area;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface HarvestPermitAreaHtaRepository extends JpaRepository<HarvestPermitAreaHta, Long> {
    @Modifying
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);
}
