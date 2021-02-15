package fi.riista.feature.harvestpermit;

import org.joda.time.LocalDateTime;

import java.util.List;

public class HarvestPermitPublicPdfDownloadStatisticsDTO {

    public static class SpeciesDTO {
        private final int speciesCode;
        private final long decisionCount;
        private final long downloadCount;

        public SpeciesDTO(final int speciesCode, final long decisionCount, final long downloadCount) {
            this.speciesCode = speciesCode;
            this.decisionCount = decisionCount;
            this.downloadCount = downloadCount;
        }

    }

    private final LocalDateTime downloadsSince;
    private final List<SpeciesDTO> statisctics;

    public HarvestPermitPublicPdfDownloadStatisticsDTO(final LocalDateTime downloadsSince,
                                                       final List<SpeciesDTO> statistics) {
        this.downloadsSince = downloadsSince;
        this.statisctics = statistics;
    }

}
