package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface HarvestReportRepository extends BaseRepository<HarvestReport, Long> {
    @Query("SELECT h FROM HarvestReport h WHERE h.lifecycleFields.modificationTime > :after")
    List<HarvestReport> findModifiedAfter(@Param("after") Date after);
}
