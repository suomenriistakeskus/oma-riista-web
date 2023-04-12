package fi.riista.feature.huntingclub.statistics;

import fi.riista.api.club.ClubHarvestSummaryRequestDTO;
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
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class HuntingClubHarvestStatisticsFeature {

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Transactional(readOnly = true)
    public HuntingClubHarvestStatisticsDTO getSummary(final ClubHarvestSummaryRequestDTO dto) {
        final long huntingClubId = dto.getClubId();
        final LocalDate begin = dto.getBegin();
        final LocalDate end = dto.getEnd();

        // Lookup table for GameSpecies
        final Map<Long, GameSpecies> speciesById = F.indexById(gameSpeciesRepository.findAll());

        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);

        final Map<Long, Integer> harvestMap = DateUtil.huntingYearsBetween(begin, end)
                .mapToObj(huntingYear ->
                        calculate(huntingYear, huntingClub, buildInterval(huntingYear, begin, end)))
                .flatMap(m -> m.entrySet().stream())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (i1, i2) -> i1 + i2));

        return new HuntingClubHarvestStatisticsDTO(harvestMap.entrySet().stream()
                .map(entry -> new SummaryRow(GameSpeciesDTO.create(speciesById.get(entry.getKey())), entry.getValue()))
                .collect(toList()));
    }

    private Interval buildInterval(final int huntingYear, final LocalDate begin, final LocalDate end) {
        final LocalDate huntingYearBeginDate = DateUtil.huntingYearBeginDate(huntingYear);
        final LocalDate huntingYearEndDate = DateUtil.huntingYearEndDate(huntingYear);

        final LocalDate intervalBeginDate = huntingYearBeginDate.isAfter(begin) ? huntingYearBeginDate : begin;
        final LocalDate intervalEndDate = huntingYearEndDate.isBefore(end) ? huntingYearEndDate : end;
        return new Interval(
                DateUtil.toDateTimeNullSafe(intervalBeginDate),
                DateUtil.toDateTimeNullSafe(intervalEndDate));

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
