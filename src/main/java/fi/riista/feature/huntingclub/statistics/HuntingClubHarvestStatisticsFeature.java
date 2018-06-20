package fi.riista.feature.huntingclub.statistics;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.statistics.HuntingClubHarvestStatisticsDTO.SummaryRow;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.Interval;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
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
    public HuntingClubHarvestStatisticsDTO getSummary(final long huntingClubId, final int calendarYear) {
        // Lookup table for GameSpecies
        final Map<Long, GameSpecies> speciesById = F.indexById(gameSpeciesRepository.findAll());

        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);

        // Query by calendar year need to be splitted to 2 queries, because 1 calendar year spans over 2 hunting years.

        final Interval interval1 = new Interval(
                DateUtil.beginOfCalendarYear(calendarYear),
                DateUtil.toDateTimeNullSafe(DateUtil.huntingYearBeginDate(calendarYear)));

        final Interval interval2 = new Interval(
                DateUtil.toDateTimeNullSafe(DateUtil.huntingYearBeginDate(calendarYear)),
                DateUtil.beginOfCalendarYear(calendarYear + 1));

        final Map<Long, Integer> amounts1 = calculate(calendarYear - 1, huntingClub, interval1);
        final Map<Long, Integer> amounts2 = calculate(calendarYear, huntingClub, interval2);

        return new HuntingClubHarvestStatisticsDTO(sum(amounts1, amounts2).entrySet().stream()
                .map(entry -> new SummaryRow(GameSpeciesDTO.create(speciesById.get(entry.getKey())), entry.getValue()))
                .collect(toList()));
    }

    public static Map<Long, Integer> sum(final Map<Long, Integer> a, final Map<Long, Integer> b) {
        final Map<Long, Integer> res = new HashMap<>();
        res.putAll(a);
        b.forEach((k, v) -> res.merge(k, v, (oldValue, value) -> oldValue + value));
        return res;
    }

    private Map<Long, Integer> calculate(final int huntingYear, final HuntingClub huntingClub, final Interval interval) {
        return harvestRepository.countClubHarvestAmountGroupByGameSpeciesId(
                huntingClub, huntingYear, interval,
                GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING);
    }
}
