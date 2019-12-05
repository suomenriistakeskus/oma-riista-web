package fi.riista.feature.organization.rhy.gamedamageinspection;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameService;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.iban4j.Iban;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.gamediary.GameSpecies.LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageType.LARGE_CARNIVORE;
import static fi.riista.util.Collect.indexingByIdOf;
import static java.util.stream.Collectors.groupingBy;

@Component
public class GameDamageInspectionEventExportService {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    RiistanhoitoyhdistysNameService rhyNameService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameDamageInspectionEventRepository eventRepository;

    @Resource
    private GameDamageInspectionEventDTOTransformer dtoTransformer;

    @Resource
    private RhyAnnualStatisticsRepository statisticsRepository;

    @Transactional(readOnly = true)
    public List<GameDamageInspectionEventSummaryDTO> getGameDamageInspectionEventSummary(final int year,
                                                                                         final GameDamageType gameDamageType) {
        final Date startDate = DateUtil.toDateNullSafe(new LocalDate(year, 1, 1));
        final Date endDate = DateUtil.toDateNullSafe(new LocalDate(year, 12, 31));

        final Set<Integer> speciesCodes = gameDamageType == LARGE_CARNIVORE ? LARGE_CARNIVORES :
                ImmutableSet.<Integer>builder()
                        .addAll(MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING)
                        .add(OFFICIAL_CODE_ROE_DEER).build();

        final Map<Riistanhoitoyhdistys, List<GameDamageInspectionEvent>> rhyToEvent =
                eventRepository.findByDateBetweenAndGameSpeciesOfficialCodeIn(startDate, endDate, speciesCodes).stream()
                        .collect(groupingBy(event -> event.getRhy()));

        final Map<Long, RhyAnnualStatistics> rhyIdToStatistics = statisticsRepository
                .findByYear(year)
                .stream()
                .collect(indexingByIdOf(RhyAnnualStatistics::getRhy));

        return rhyToEvent.entrySet().stream()
                .map(entry -> {
                    final Riistanhoitoyhdistys rhy = entry.getKey();
                    final List<GameDamageInspectionEvent> events = entry.getValue();
                    final Iban iban =
                            rhyIdToStatistics.get(rhy.getId()) != null ?
                                    rhyIdToStatistics.get(rhy.getId()).getOrCreateBasicInfo().getIban() :
                                    null;
                    return GameDamageInspectionEventSummaryDTO.create(rhy, events, iban);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GameDamageInspectionEventExportDTO getGameDamageInspectionEventExport(final long rhyId,
                                                                                 final int year,
                                                                                 final GameDamageType gameDamageType) {
        final List<GameDamageInspectionEventDTO> dtos = listEventsByType(rhyId, year, gameDamageType);

        final Map<Long, GameSpecies> idToGameSpecies = getIdToGameSpeciesMapping(dtos);

        final LocalisedString rhyName = rhyNameService.getNameIndex().get(rhyId).getRhyName();

        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);
        final RhyAnnualStatistics annualStatistics = statisticsRepository.findByRhyAndYear(rhy, year).orElse(null);
        final Iban iban = annualStatistics != null ? annualStatistics.getOrCreateBasicInfo().getIban() : null;
        final FinnishBankAccount bankAccount = iban != null ? FinnishBankAccount.fromIban(iban) : null;

        return GameDamageInspectionEventExportDTO.create(year, gameDamageType, dtos, idToGameSpecies, rhyName, bankAccount);

    }

    private Map<Long, GameSpecies> getIdToGameSpeciesMapping(List<GameDamageInspectionEventDTO> dtos) {
        final List<Integer> officialCodes =
                dtos.stream().map(GameDamageInspectionEventDTO::getGameSpeciesCode).collect(Collectors.toList());

        if (officialCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<GameSpecies> species = gameSpeciesService.requireByOfficialCodes(officialCodes);

        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getId(),
                        dto -> species.stream().filter(s -> s.getOfficialCode() == dto.getGameSpeciesCode()).findAny().orElse(null)
                ));
    }

    private List<GameDamageInspectionEventDTO> listEventsByType(final long rhyId, final int year, final GameDamageType gameDamageType) {
        final Set<Integer> speciesCodes = gameDamageType == LARGE_CARNIVORE ? LARGE_CARNIVORES :
                ImmutableSet.<Integer>builder()
                        .addAll(MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING)
                        .add(OFFICIAL_CODE_ROE_DEER).build();

        final Date startTime = DateUtil.toDateNullSafe(new LocalDate(year, 1, 1));
        final Date endTime = DateUtil.toDateNullSafe(new LocalDate(year, 12, 31));

        final List<GameDamageInspectionEvent> events =
                eventRepository.findByRhyIdAndDateBetweenAndGameSpeciesOfficialCodeIn(rhyId, startTime, endTime, speciesCodes);

        return dtoTransformer.apply(events);
    }
}
