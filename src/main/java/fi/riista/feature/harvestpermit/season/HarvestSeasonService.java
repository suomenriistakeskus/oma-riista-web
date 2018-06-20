package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class HarvestSeasonService {

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Tuple2<HarvestSeason, HarvestQuota> findHarvestSeasonAndQuota(final GameSpecies gameSpecies,
                                                                         final Riistanhoitoyhdistys rhy,
                                                                         final LocalDate harvestDate,
                                                                         final boolean failIfQuotaNotFound) {
        Objects.requireNonNull(harvestDate, "harvestDate is null");
        Objects.requireNonNull(rhy, "rhy is null");

        final List<HarvestSeason> validSeasonForHarvest =
                harvestSeasonRepository.getAllSeasonsForHarvest(gameSpecies, harvestDate);

        if (validSeasonForHarvest.isEmpty()) {
            return null;
        }

        if (validSeasonForHarvest.size() != 1) {
            throw new IllegalStateException(String.format(
                    "Unique HarvestSeason not found for species=%d date=%s",
                    gameSpecies.getOfficialCode(), harvestDate));
        }

        final HarvestSeason harvestSeason = validSeasonForHarvest.get(0);
        final HarvestQuota harvestQuota = harvestQuotaRepository.findByHarvestSeasonAndRhy(harvestSeason, rhy);

        if (harvestQuota == null && harvestSeason.hasQuotas()) {
            if (failIfQuotaNotFound) {
                throw new HarvestQuotaNotFoundException(gameSpecies, harvestDate);
            }
            return null;
        }

        return Tuple.of(harvestSeason, harvestQuota);
    }
}
