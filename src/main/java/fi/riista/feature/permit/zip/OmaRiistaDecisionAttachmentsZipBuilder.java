package fi.riista.feature.permit.zip;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.decision.DecisionUtil;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.DateUtil;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;

public class OmaRiistaDecisionAttachmentsZipBuilder {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy HH:mm");


    private String documentNumber;
    private List<PersistentFileMetadata> attachmentsMetadata;
    private final Locale locale;


    private final FileStorageService fileStorageService;

    public OmaRiistaDecisionAttachmentsZipBuilder(final FileStorageService fileStorageService, final Locale locale) {
        this.fileStorageService = requireNonNull(fileStorageService);
        this.locale = locale;
    }

    public OmaRiistaDecisionAttachmentsZipBuilder withAttachments(final List<PersistentFileMetadata> attachmentsMetadata) {
        this.attachmentsMetadata = requireNonNull(attachmentsMetadata);
        return this;
    }

    public OmaRiistaDecisionAttachmentsZipBuilder withDecisionNumber(final String documentNumber) {
        this.documentNumber = requireNonNull(documentNumber);
        return this;
    }

    public OmaRiistaDecisionAttachmentsZip build() throws IOException {
        Preconditions.checkState(attachmentsMetadata != null
                && documentNumber != null);

        return new OmaRiistaDecisionAttachmentsZip(buildData(), DecisionUtil.getPermitDecisionAttachmentArchiveFileName(locale, documentNumber));
    }

    private byte[] buildData() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (final ZipOutputStream zip = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
            zip.setComment("Exported from oma.riista.fi on " + DTF.print(DateUtil.now()));
            zip.setLevel(9);

            appendAttachments(zip);

            zip.flush();
        }

        return bos.toByteArray();
    }

    private void appendAttachments(final ZipOutputStream zip) throws IOException {
        int bound = this.attachmentsMetadata.size();
        for (int i = 0; i < bound; i++) {
            appendAttachment(zip, i);
        }
    }

    private void appendAttachment(final ZipOutputStream zip, final int idx) throws IOException {
        PersistentFileMetadata attachmentsMetadata = this.attachmentsMetadata.get(idx);

        final String newFilename = String.format("%03d_%s", idx + 1, attachmentsMetadata.getOriginalFilename());

        final Path sourcePath = Files.createTempFile("attachment", null);
        fileStorageService.downloadTo(attachmentsMetadata.getId(), sourcePath);

        zip.putNextEntry(new ZipEntry(newFilename));
        Files.copy(sourcePath, zip);
        zip.closeEntry();
    }
}
