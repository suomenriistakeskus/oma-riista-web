package fi.riista.feature.permit.decision.pdf;

public class PermitDecisionPdfFileDTO {
    private final String filename;
    private final String headerText;

    public PermitDecisionPdfFileDTO(final String filename, final String headerText) {
        this.filename = filename;
        this.headerText = headerText;
    }

    public String getFilename() {
        return filename;
    }

    public String getHeaderText() {
        return headerText;
    }

}
