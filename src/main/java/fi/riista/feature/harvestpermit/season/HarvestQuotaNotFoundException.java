package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;

public class HarvestQuotaNotFoundException extends IllegalStateException {

    public static HarvestQuotaNotFoundException missingQuotaForRhy(final GameSpecies species,
                                                                   final LocalDate harvestDate,
                                                                   final Riistanhoitoyhdistys rhy) {
        final String errorMessage = String.format(
                "HarvestQuota not found for season with quotas. species=%s (%d) harvestDate=%s rhy=%s (%s)",
                species.getNameFinnish(), species.getOfficialCode(), harvestDate, rhy.getNameFinnish(),
                rhy.getOfficialCode());
        return new HarvestQuotaNotFoundException(errorMessage);
    }

    public static HarvestQuotaNotFoundException uniqueQuotaNotFound(final GameSpecies species,
                                                                    final LocalDate harvestDate) {
        final String errorMessage = String.format(
                "Unique HarvestSeason not found for species=%s (%d) harvestDate=%s",
                species.getNameFinnish(), species.getOfficialCode(), harvestDate);
        return new HarvestQuotaNotFoundException(errorMessage);
    }

    private HarvestQuotaNotFoundException(final String message) {
        super(message);
    }
}
