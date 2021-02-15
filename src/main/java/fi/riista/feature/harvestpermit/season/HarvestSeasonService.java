package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.GISQueryService;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@Service
public class HarvestSeasonService {

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Resource
    private GISQueryService gisQueryService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Tuple2<HarvestSeason, HarvestQuota> findHarvestSeasonAndQuota(final @Nonnull GameSpecies gameSpecies,
                                                                         final @Nonnull GeoLocation location,
                                                                         final @Nonnull LocalDate harvestDate,
                                                                         final boolean failIfQuotaNotFound) {
        Objects.requireNonNull(gameSpecies, "gameSpecies is null");
        Objects.requireNonNull(location, "location is null");
        Objects.requireNonNull(harvestDate, "harvestDate is null");

        final List<HarvestSeason> validSeasonForHarvest =
                harvestSeasonRepository.getAllSeasonsForHarvest(gameSpecies, harvestDate);

        if (validSeasonForHarvest.isEmpty()) {
            return null;
        }

        if (validSeasonForHarvest.size() != 1) {
            throw HarvestQuotaNotFoundException.uniqueQuotaNotFound(gameSpecies, harvestDate);
        }

        final HarvestSeason harvestSeason = validSeasonForHarvest.get(0);

        final HarvestQuota harvestQuota =
                ofNullable(HarvestArea.HarvestAreaType.getValidTypeFor(gameSpecies.getOfficialCode()))
                .flatMap(areaType-> gisQueryService.findHarvestAreaByLocation(areaType, location))
                .map(harvestArea-> harvestQuotaRepository.findByHarvestSeasonAndArea(harvestSeason, harvestArea))
                .orElse(null);

        if (harvestQuota == null && harvestSeason.hasQuotas()) {
            if (failIfQuotaNotFound) {
                throw HarvestQuotaNotFoundException.missingQuotaForLocation(gameSpecies, harvestDate, location);
            }
            return null;
        }

        return Tuple.of(harvestSeason, harvestQuota);
    }
}
