package fi.riista.feature.huntingclub.permit.statistics;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class HarvestCountByPermitAndClub {

    private final Map<PermitAndClubId, HarvestCountDTO> counts;

    public HarvestCountByPermitAndClub(final Map<PermitAndClubId, HarvestCountDTO> counts) {
        this.counts = Collections.unmodifiableMap(counts);
    }

    @Nullable
    public HasHarvestCountsForPermit findCount(final @Nonnull HarvestPermit permit,
                                               final @Nonnull HuntingClub partner) {
        return this.counts.get(new PermitAndClubId(permit.getId(), partner.getId()));
    }

    @Nonnull
    public Map<Long, HarvestCountDTO> indexByClubId(final @Nonnull HarvestPermit harvestPermit) {
        requireNonNull(harvestPermit);

        return counts.entrySet().stream()
                .filter(c -> Objects.equals(harvestPermit.getId(), c.getKey().getPermitId()))
                .collect(toMap(
                        entry -> entry.getKey().getClubId(),
                        Map.Entry::getValue));
    }

    @Nonnull
    public HarvestCountDTO sumCountsByPermit(final @Nonnull Long permitId) {
        requireNonNull(permitId);

        return HarvestCountDTO.createTotal(counts.entrySet().stream()
                .filter(c -> Objects.equals(permitId, c.getKey().getPermitId()))
                .map(Map.Entry::getValue)
                .collect(toList()));
    }

    @Nonnull
    public HarvestCountDTO sumAllCounts() {
        return HarvestCountDTO.createTotal(counts.values());
    }
}
