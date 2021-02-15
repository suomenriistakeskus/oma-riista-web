package fi.riista.feature.permit.application.search;

public class ModeratorApplicationsTodoDTO {

    private final boolean decisionsToRenew;
    private final long myDecisionsToRenew;

    public ModeratorApplicationsTodoDTO(final boolean decisionsToRenew, final long myDecisionsToRenew) {
        this.decisionsToRenew = decisionsToRenew;
        this.myDecisionsToRenew = myDecisionsToRenew;
    }

    public boolean isDecisionsToRenew() {
        return decisionsToRenew;
    }

    public long getMyDecisionsToRenew() {
        return myDecisionsToRenew;
    }
}
