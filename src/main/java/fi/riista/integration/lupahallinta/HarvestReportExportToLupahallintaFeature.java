package fi.riista.integration.lupahallinta;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * HarvestReports are exported to LH (Lupahallinta) as CSV transfer files.
 * Only send permit completed reports, with or withour harvests.
 * If permit completion is deleted, nothing is sent. Updated data is sent when permit is completed again.
 */
@Component
public class HarvestReportExportToLupahallintaFeature {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_HARVESTREPORTS')")
    public List<HarvestReportExportCSVDTO> exportToCSCV(final Long afterTimestamp) {
        // By default return all changes occurred since yesterday
        final DateTime changedAfter = afterTimestamp != null
                ? new DateTime(afterTimestamp)
                : DateTime.now().minusDays(1);

        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        return queryFactory.selectFrom(PERMIT)
                .where(PERMIT.harvestReportState.eq(HarvestReportState.APPROVED))
                .where(PERMIT.lifecycleFields.modificationTime.goe(changedAfter))
                .fetch().stream()
                .flatMap(permit -> HarvestReportExportCSVDTO.create(permit, messageSource).stream())
                .collect(toList());
    }
}
