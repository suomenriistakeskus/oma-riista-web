package fi.riista.integration.habides.export.derogations;

import java.util.List;

public class DraftDecisionsExistException extends Exception {
    private List<String> draftDecisions;

    public DraftDecisionsExistException(final List<String> draftDecisions) {
        this.draftDecisions = draftDecisions;
    }

    public List<String> getDraftDecisions() {
        return draftDecisions;
    }
}
