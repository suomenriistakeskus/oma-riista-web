package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import org.joda.time.LocalDate;

public class HarvestQuotaNotFoundException extends IllegalStateException {
    public HarvestQuotaNotFoundException(final GameSpecies species,
                                         final LocalDate harvestDate) {
        super(String.format("HarvestQuota not found for season with quotas." +
                        " species=%s harvestDate=%s",
                species.getOfficialCode(), harvestDate.toString()));
    }
}
