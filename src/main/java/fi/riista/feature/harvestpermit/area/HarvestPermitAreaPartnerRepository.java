package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HarvestPermitAreaPartnerRepository extends BaseRepository<HarvestPermitAreaPartner, Long> {
    List<HarvestPermitAreaPartner> findByHarvestPermitArea(HarvestPermitArea area);

    @Query("SELECT p.zone.id FROM #{#entityName} p WHERE p.harvestPermitArea.id = ?1")
    List<Long> findAreaPartnerZoneIds(long harvestPermitAreaId);
}
