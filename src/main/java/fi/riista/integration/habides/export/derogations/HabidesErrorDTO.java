package fi.riista.integration.habides.export.derogations;

import java.util.List;

public class HabidesErrorDTO {

    public enum HabidesErrorCategory {
        NOT_FOUND,
        DRAFT_DECISIONS
    }

    private HabidesErrorCategory errorCategory;
    private String errorMessage;
    private List<String> draftDecisions;

    public HabidesErrorDTO() {}

    public HabidesErrorDTO(final HabidesErrorCategory errorCategory, final String errorMessage) {
        this.errorCategory = errorCategory;
        this.errorMessage = errorMessage;
    }

    public HabidesErrorDTO(final HabidesErrorCategory errorCategory, final List<String> draftDecisions) {
        this.errorCategory = errorCategory;
        this.draftDecisions = draftDecisions;
    }

    public HabidesErrorCategory getErrorCategory() {
        return errorCategory;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getDraftDecisions() {
        return draftDecisions;
    }
}
