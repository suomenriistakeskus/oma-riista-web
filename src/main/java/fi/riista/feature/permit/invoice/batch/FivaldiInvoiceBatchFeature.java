package fi.riista.feature.permit.invoice.batch;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.storage.FileDownloadService;
import org.joda.time.YearMonth;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static fi.riista.security.EntityPermission.READ;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

@Service
public class FivaldiInvoiceBatchFeature {

    @Resource
    private PermitDecisionInvoiceBatchRepository batchRepo;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public List<YearMonth> getAvailableFivaldiBatchMonths() {
        return batchRepo.findAll()
                .stream()
                .filter(PermitDecisionInvoiceBatch::isDownloaded)
                .map(batch -> batch.getLifecycleFields().getCreationTime())
                .map(YearMonth::new)
                .sorted(reverseOrder())
                .distinct()
                .collect(toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public List<FivaldiBatchDTO> getNewFivaldiBatches() {
        final QPermitDecisionInvoiceBatch BATCH = QPermitDecisionInvoiceBatch.permitDecisionInvoiceBatch;

        return getFivaldiBatches(BATCH.downloaded.eq(false));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public List<FivaldiBatchDTO> getPreviousFivaldiBatches(final int year, final int month) {
        final QPermitDecisionInvoiceBatch BATCH = QPermitDecisionInvoiceBatch.permitDecisionInvoiceBatch;

        final BooleanExpression predicate = BATCH.downloaded.eq(true)
                .and(BATCH.lifecycleFields.creationTime.year().eq(year))
                .and(BATCH.lifecycleFields.creationTime.month().eq(month));

        return getFivaldiBatches(predicate);
    }

    private List<FivaldiBatchDTO> getFivaldiBatches(final BooleanExpression predicate) {
        return batchRepo.findAllAsStream(predicate)
                .map(FivaldiBatchDTO::create)
                .sorted(comparing(FivaldiBatchDTO::getCreationTime).reversed())
                .collect(toList());
    }

    @Transactional(rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAndMarkFivaldiBatchFileDownloaded(final long batchId) throws IOException {
        final PermitDecisionInvoiceBatch batch = requireEntityService.requirePermitDecisionInvoiceBatch(batchId, READ);
        batch.setDownloaded(true);
        return fileDownloadService.download(batch.getFivaldiAccountsReceivableFile());
    }
}
