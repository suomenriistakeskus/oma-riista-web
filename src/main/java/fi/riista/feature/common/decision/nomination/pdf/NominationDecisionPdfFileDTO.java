package fi.riista.feature.common.decision.nomination.pdf;

public class NominationDecisionPdfFileDTO {
    private final String filename;
    private final String headerText;

    public NominationDecisionPdfFileDTO(final String filename, final String headerText) {
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
