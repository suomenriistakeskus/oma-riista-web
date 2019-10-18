package fi.riista.feature.permit.area.partner;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.permit.area.HarvestPermitArea;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface HarvestPermitAreaPartnerRepository extends BaseRepository<HarvestPermitAreaPartner, Long> {
    List<HarvestPermitAreaPartner> findByHarvestPermitArea(HarvestPermitArea area);

    @Query("SELECT DISTINCT c FROM HarvestPermitAreaPartner p JOIN p.sourceArea s JOIN s.club c WHERE p.harvestPermitArea = ?1")
    Set<HuntingClub> findPartnerClubs(HarvestPermitArea area);

    @Query("SELECT p.zone.id FROM #{#entityName} p WHERE p.harvestPermitArea.id = ?1")
    List<Long> findAreaPartnerZoneIds(long harvestPermitAreaId);
}
