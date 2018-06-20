package fi.riista.integration.fivaldi;

import com.google.common.io.Files;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.PermitDecisionInvoiceRepository;
import fi.riista.feature.permit.invoice.batch.PermitDecisionInvoiceBatch;
import fi.riista.feature.permit.invoice.batch.PermitDecisionInvoiceBatchRepository;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static fi.riista.integration.fivaldi.FivaldiPaymentMethod.PAPER;
import static fi.riista.integration.fivaldi.FivaldiPaymentMethod.PAYTRAIL;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.stream.Collectors.toList;

@Service
public class FivaldiInvoiceBatchExportFeature {

    private static final DateTimeFormatter FILENAME_TS_PATTERN = DateTimeFormat.forPattern("yyyyMMdd_HHmm");

    @Resource
    private PermitDecisionInvoiceRepository permitDecisionInvoiceRepository;

    @Resource
    private PermitDecisionInvoiceBatchRepository permitDecisionInvoiceBatchRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Transactional(rollbackFor = IOException.class)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void createAndStoreFivaldiInvoiceBatchFile() throws IOException {
        final DateTime now = DateUtil.now();

        final List<PermitDecisionInvoice> processingInvoices =
                permitDecisionInvoiceRepository.getPermitDecisionInvoicesForNextFivaldiBatch();

        if (processingInvoices.size() > 0) {
            final List<FivaldiRecord> records = processingInvoices
                    .stream()
                    .map(this::toFivaldiRecordParams)
                    .map(FivaldiRecordService::createRecord)
                    .collect(toList());

            final byte[] serializedRecords = FivaldiRecordService.serialize(records).getBytes(ISO_8859_1);

            final PersistentFileMetadata batchFile = storeFivaldiFile(serializedRecords, getFivaldiExportFilename(now));
            final PermitDecisionInvoiceBatch batch = permitDecisionInvoiceBatchRepository.save(new PermitDecisionInvoiceBatch(batchFile));

            processingInvoices.forEach(invoice -> invoice.setBatch(batch));
        }
    }

    private String getFivaldiExportFilename(final DateTime now) {
        return "fivaldi" +
                (isProduction() ? "-" : "-testi-") +
                FILENAME_TS_PATTERN.print(now) +
                ".txt";
    }

    private FivaldiRecordParams toFivaldiRecordParams(final PermitDecisionInvoice processingInvoice) {
        final Invoice invoice = processingInvoice.getInvoice();
        final FivaldiPaymentMethod paymentMethod = invoice.isElectronicInvoicingEnabled() ? PAYTRAIL : PAPER;

        return new FivaldiRecordParams(invoice.getInvoiceNumber(), invoice.getAmount(), invoice.getInvoiceDate(),
                invoice.getDueDate(), invoice.getCreditorReference().parseLong(), paymentMethod, isProduction());
    }

    private PersistentFileMetadata storeFivaldiFile(final byte[] bytes, final String filename) throws IOException {
        final Path tempPath = java.nio.file.Files.createTempFile(filename, ".txt");

        try {
            final File batchFile = tempPath.toFile();
            Files.write(bytes, batchFile);

            return fileStorageService.storeFile(
                    UUID.randomUUID(), batchFile, FileType.FIVALDI_INVOICE_BATCH, MediaType.TEXT_PLAIN_VALUE,
                    filename);

        } finally {
            java.nio.file.Files.deleteIfExists(tempPath);
        }
    }

    private boolean isProduction() {
        return runtimeEnvironmentUtil.isProductionEnvironment();
    }
}
