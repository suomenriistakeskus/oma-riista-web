package fi.riista.feature.permit.decision.publish;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

class HarvestPermitSpeciesUpdateResult {
    private List<HarvestPermitSpeciesAmount> created = new LinkedList<>();
    private List<HarvestPermitSpeciesAmount> updated = new LinkedList<>();
    private List<HarvestPermitSpeciesAmount> deleted = new LinkedList<>();

    public void addCreated(final HarvestPermitSpeciesAmount speciesAmount) {
        this.created.add(requireNonNull(speciesAmount));
    }

    public void addUpdated(final HarvestPermitSpeciesAmount speciesAmount) {
        this.updated.add(requireNonNull(speciesAmount));
    }

    public void addDeleted(final HarvestPermitSpeciesAmount speciesAmount) {
        this.deleted.add(requireNonNull(speciesAmount));
    }

    public List<HarvestPermitSpeciesAmount> getCreated() {
        return ImmutableList.copyOf(created);
    }

    public List<HarvestPermitSpeciesAmount> getUpdated() {
        return ImmutableList.copyOf(updated);
    }

    public List<HarvestPermitSpeciesAmount> getDeleted() {
        return ImmutableList.copyOf(deleted);
    }

    public boolean hasCreated() {
        return created.size() > 0;
    }

    public boolean hasUpdated() {
        return updated.size() > 0;
    }

    public boolean hasDeleted() {
        return deleted.size() > 0;
    }
}
