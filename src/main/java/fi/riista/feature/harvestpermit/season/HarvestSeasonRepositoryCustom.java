package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public interface HarvestSeasonRepositoryCustom {
    List<HarvestSeason> getAllSeasonsForHarvest(@Nonnull GameSpecies species, @Nonnull LocalDate harvestDate);

    List<HarvestSeason> listAllForReportingFetchSpecies(LocalDate activeOnDate);

    List<HarvestSeason> findBySeasonInHuntingYear(int huntingYear);

    List<HarvestSeason> findOverlappingSeasons(final Long id, GameSpecies species, LocalDate beginDate, LocalDate endDate,
                                               LocalDate beginDate2, LocalDate endDate2);

    List<GameSpecies> findGameSpeciesBySeasons(Collection<HarvestSeason> seasons);
}
