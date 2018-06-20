package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;

public interface HarvestSeasonRepositoryCustom {
    List<HarvestSeason> getAllSeasonsForHarvest(@Nonnull GameSpecies species, @Nonnull LocalDate harvestDate);

    List<HarvestSeason> listAllForReportingFetchSpecies(LocalDate activeOnDate);
}
