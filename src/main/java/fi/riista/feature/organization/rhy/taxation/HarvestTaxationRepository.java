package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HarvestTaxationRepository extends BaseRepository<HarvestTaxationReport, Long> {

    @Query("SELECT DISTINCT o.huntingYear FROM #{#entityName} o WHERE o.rhy= ?1")
    List<Integer> listTaxationReportYears(Riistanhoitoyhdistys rhy);
}