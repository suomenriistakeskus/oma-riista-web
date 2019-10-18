package fi.riista.feature.huntingclub.permit.statistics;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ClubHuntingSummaryBasicInfoByPermitAndClub {
    private final Map<PermitAndClubId, ClubHuntingSummaryBasicInfoDTO> summaries;

    public ClubHuntingSummaryBasicInfoByPermitAndClub(final @Nonnull Map<PermitAndClubId, ClubHuntingSummaryBasicInfoDTO> summaries) {
        this.summaries = Collections.unmodifiableMap(summaries);
    }

    @Nullable
    public ClubHuntingSummaryBasicInfoDTO findSummary(final HarvestPermit permit, final HuntingClub partner) {
        return this.summaries.get(new PermitAndClubId(permit.getId(), partner.getId()));
    }

    public Map<Long, ClubHuntingSummaryBasicInfoDTO> indexByClubId(final @Nonnull HarvestPermit harvestPermit) {
        return summaries.entrySet().stream()
                .filter(c -> Objects.equals(harvestPermit.getId(), c.getKey().getPermitId()))
                .collect(toMap(
                        entry -> entry.getKey().getClubId(),
                        Map.Entry::getValue));
    }

    public List<ClubHuntingSummaryBasicInfoDTO> listByHarvestPermit(final @Nonnull Long permitId) {
        return summaries.entrySet().stream()
                .filter(c -> Objects.equals(permitId, c.getKey().getPermitId()))
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    public Stream<ClubHuntingSummaryBasicInfoDTO> streamSummaries() {
        return summaries.values().stream();
    }
}
