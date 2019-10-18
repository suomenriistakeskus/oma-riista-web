package fi.riista.feature.permit.application;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.common.repository.NativeQueries;
import fi.riista.feature.permit.area.HarvestPermitArea;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HarvestPermitApplicationRepository extends BaseRepository<HarvestPermitApplication, Long>,
        HarvestPermitApplicationRepositoryCustom {

    HarvestPermitApplication findByUuid(UUID uuid);

    @Query("SELECT a FROM HarvestPermitApplication a WHERE a.area = ?1")
    List<HarvestPermitApplication> findByPermitArea(final HarvestPermitArea area);

    @Query("SELECT a FROM HarvestPermitApplication a WHERE a.applicationNumber = ?1")
    Optional<HarvestPermitApplication> findByApplicationNumber(int applicationNumber);

    @Query(nativeQuery = true, value = NativeQueries.LIST_HARVEST_PERMIT_APPLICATION_CONFLICTS)
    List<HarvestPermitApplication> findIntersecting(
            @Param("harvestPermitApplicationId") long harvestPermitApplicationId,
            @Param("applicationYear") int huntingYear);

}
