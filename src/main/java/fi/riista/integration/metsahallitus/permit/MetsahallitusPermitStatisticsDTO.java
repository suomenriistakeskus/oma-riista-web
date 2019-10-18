package fi.riista.integration.metsahallitus.permit;

import fi.riista.util.F;

import java.util.List;
import java.util.Map;

public class MetsahallitusPermitStatisticsDTO {

    public static final class PermitCountDTO {
        private final String permitType;
        private final long permitCount;

        public PermitCountDTO(final String permitType, final long permitCount) {
            this.permitType = permitType;
            this.permitCount = permitCount;
        }

        public String getPermitType() {
            return permitType;
        }

        public long getPermitCount() {
            return permitCount;
        }
    }

    private final long hunterCount;

    private final List<PermitCountDTO> permitCounts;

    private final long invalidPeriodPermitCount;
    private final long swedishTypeMissingPermitCount;

    public MetsahallitusPermitStatisticsDTO(final long hunterCount, final Map<String, Long> permitCounts,
                                            final long invalidPeriodPermitCount,
                                            final long swedishTypeMissingPermitCount) {
        this.hunterCount = hunterCount;
        this.permitCounts = F.mapNonNullsToList(permitCounts.entrySet(), e -> new PermitCountDTO(e.getKey(),
                e.getValue()));
        this.invalidPeriodPermitCount = invalidPeriodPermitCount;
        this.swedishTypeMissingPermitCount = swedishTypeMissingPermitCount;
    }

    public long getHunterCount() {
        return hunterCount;
    }

    public List<PermitCountDTO> getPermitCounts() {
        return permitCounts;
    }

    public long getInvalidPeriodPermitCount() {
        return invalidPeriodPermitCount;
    }

    public long getSwedishTypeMissingPermitCount() {
        return swedishTypeMissingPermitCount;
    }
}
