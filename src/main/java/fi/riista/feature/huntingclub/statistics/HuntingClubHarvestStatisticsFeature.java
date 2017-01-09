package fi.riista.feature.huntingclub.statistics;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.huntingclub.statistics.HuntingClubHarvestStatisticsDTO.SummaryRow;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.Interval;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubHarvestStatisticsFeature {

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Transactional(readOnly = true)
    public HuntingClubHarvestStatisticsDTO getSummary(final long huntingClubId, final int huntingYear) {
        // Lookup table for GameSpecies
        final Map<Long, GameSpecies> speciesById = F.indexById(gameSpeciesRepository.findAll());

        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);
        final Interval interval = DateUtil.huntingYearInterval(huntingYear);

        // Filtered harvest
        final Map<Long, Integer> speciesToAmount = harvestRepository.countClubHarvestAmountGroupByGameSpeciesId(
                huntingClub, huntingYear, interval,
                GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING);

        return new HuntingClubHarvestStatisticsDTO(speciesToAmount.entrySet().stream()
                .map(entry -> new SummaryRow(GameSpeciesDTO.create(speciesById.get(entry.getKey())), entry.getValue()))
                .collect(toList()));
    }
}
