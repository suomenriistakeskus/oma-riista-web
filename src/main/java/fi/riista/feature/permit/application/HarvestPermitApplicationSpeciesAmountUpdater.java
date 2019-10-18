package fi.riista.feature.permit.application;

import fi.riista.util.F;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class HarvestPermitApplicationSpeciesAmountUpdater<DTO> {
    public interface Callback<DTO> {
        default HarvestPermitApplicationSpeciesAmount create(DTO dto) {
            throw new IllegalArgumentException("No such species " + getSpeciesCode(dto));
        }

        void update(HarvestPermitApplicationSpeciesAmount entity, DTO dto);

        int getSpeciesCode(DTO dto);
    }

    private final Callback<DTO> callback;
    private final Map<Long, HarvestPermitApplicationSpeciesAmount> missingById;
    private final Map<Integer, HarvestPermitApplicationSpeciesAmount> existingBySpecies;
    private final List<HarvestPermitApplicationSpeciesAmount> resultList;

    public HarvestPermitApplicationSpeciesAmountUpdater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                                                        final Callback<DTO> callback) {
        requireNonNull(existingList);
        this.callback = requireNonNull(callback);
        this.missingById = F.indexById(existingList);
        this.existingBySpecies = F.index(existingList, spa -> spa.getGameSpecies().getOfficialCode());
        this.resultList = new LinkedList<>();
    }

    public void processAll(final Iterable<DTO> dtoList) {
        for (final DTO dto : dtoList) {
            processItem(dto);
        }
    }

    public void processItem(final DTO dto) {
        final int speciesCode = callback.getSpeciesCode(dto);
        final HarvestPermitApplicationSpeciesAmount existingSpeciesAmount = existingBySpecies.get(speciesCode);

        if (existingSpeciesAmount == null) {
            resultList.add(requireNonNull(callback.create(dto)));

        } else {
            missingById.remove(existingSpeciesAmount.getId());
            resultList.add(existingSpeciesAmount);
            callback.update(existingSpeciesAmount, dto);
        }
    }

    public List<HarvestPermitApplicationSpeciesAmount> getResultList() {
        return resultList;
    }

    public Collection<HarvestPermitApplicationSpeciesAmount> getMissing() {
        return missingById.values();
    }

    public void assertNoSpeciesMissing() {
        if (!missingById.isEmpty()) {
            throw new IllegalArgumentException("Species list is not complete");
        }
    }
}
