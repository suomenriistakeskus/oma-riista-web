package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface HarvestTaxationReportAttachmentRepository extends BaseRepository<HarvestTaxationReportAttachment, Long> {
    List<HarvestTaxationReportAttachment> findAllByHarvestTaxationReport(final HarvestTaxationReport harvestTaxationReport);
}
