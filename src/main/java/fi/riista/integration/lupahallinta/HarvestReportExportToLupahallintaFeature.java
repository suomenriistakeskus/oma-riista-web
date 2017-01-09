package fi.riista.integration.lupahallinta;

import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.HarvestReportRepository;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * HarvestReports are exported to LH (Lupahallinta) as CSV transfer files.
 * Each row has (changed) status-field. This is required to signal deleted reports.
 */
@Component
public class HarvestReportExportToLupahallintaFeature {

    @Resource
    private HarvestReportRepository harvestReportRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_HARVESTREPORTS')")
    public List<HarvestReportExportCSVDTO> exportToCSCV(final Long afterTimestamp) {
        // By default return all changes occurred since yesterday
        final Date changedAfter = afterTimestamp != null
                ? new Date(afterTimestamp)
                : DateTime.now().minusDays(1).toDate();

        return harvestReportRepository.findModifiedAfter(changedAfter).stream()
                .map(new DTOTransformWithChangeStatus(messageSource, changedAfter))
                .flatMap(List::stream)

                // Rows which have never been exported and
                // are not visible (ACCEPTED status) should be removed.
                .filter(input -> input != null && input.getChangeStatus() != null)
                .collect(toList());
    }

    public static class DTOTransformWithChangeStatus implements Function<HarvestReport, List<HarvestReportExportCSVDTO>> {
        private final MessageSource messageSource;
        private final Date changedAfter;

        public DTOTransformWithChangeStatus(MessageSource messageSource, Date changedAfter) {
            this.messageSource = messageSource;
            this.changedAfter = changedAfter;
        }

        @Nullable
        @Override
        public List<HarvestReportExportCSVDTO> apply(@Nullable HarvestReport input) {
            final HarvestReport harvestReport = Objects.requireNonNull(input);
            final List<HarvestReportExportCSVDTO> dtos = HarvestReportExportCSVDTO.create(harvestReport, messageSource);

            if (harvestReport.isDeleted()) {
                for (HarvestReportExportCSVDTO dto : dtos) {
                    dto.setChangeStatus(HarvestReportExportCSVDTO.ChangeState.DELETED);
                }

            } else {
                // Lookup previous state at changeAfter point in time.
                final HarvestReport.State stateBefore = harvestReport.findStateAt(changedAfter);
                final HarvestReportExportCSVDTO.ChangeState status = getChangeState(harvestReport.getState(), stateBefore);
                for (HarvestReportExportCSVDTO dto : dtos) {
                    dto.setChangeStatus(status);
                }
            }

            return dtos;
        }

        public static HarvestReportExportCSVDTO.ChangeState getChangeState(
                final HarvestReport.State current, final HarvestReport.State previous) {

            // Every non-approved report is the sames as deleted for Lupahallinto
            if (current != HarvestReport.State.APPROVED) {
                if (previous == null) {
                    // Never before exported; to be filtered out from results
                    return null;
                }
                return HarvestReportExportCSVDTO.ChangeState.DELETED;

            } else if (previous == HarvestReport.State.APPROVED) {
                return HarvestReportExportCSVDTO.ChangeState.UPDATED;
            } else {
                return HarvestReportExportCSVDTO.ChangeState.CREATED;
            }
        }
    }
}
