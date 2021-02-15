package fi.riista.feature.harvestpermit.report.paper;

import fi.riista.util.ContentDispositionUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static java.util.Objects.requireNonNull;

public class PermitHarvestReportPdf {

    public static PermitHarvestReportPdf create(final byte[] data) {
        return new PermitHarvestReportPdf(data, "filename.pdf");
    }

    private final byte[] data;
    private final String fileName;

    private PermitHarvestReportPdf(final byte[] data, final String fileName) {
        this.data = requireNonNull(data);
        this.fileName = requireNonNull(fileName);
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public ResponseEntity<byte[]> asResponseEntity() {
        return ResponseEntity.ok()
                .headers(ContentDispositionUtil.header(fileName))
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(data.length)
                .body(data);
    }
}
