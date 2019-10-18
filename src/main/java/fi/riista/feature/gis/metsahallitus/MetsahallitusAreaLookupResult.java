package fi.riista.feature.gis.metsahallitus;

import javax.annotation.Nullable;

public class MetsahallitusAreaLookupResult {
    private final Integer hirviAlueId;
    private final Integer pienriistaAlueId;

    public MetsahallitusAreaLookupResult(final Integer hirviAlueId, final Integer pienriistaAlueId) {
        this.hirviAlueId = hirviAlueId;
        this.pienriistaAlueId = pienriistaAlueId;
    }

    @Nullable
    public Integer getHirviAlueId() {
        return hirviAlueId;
    }

    @Nullable
    public Integer getPienriistaAlueId() {
        return pienriistaAlueId;
    }
}
