package fi.riista.feature.huntingclub.statistics.gamestatistics;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.deercensus.DeerCensus;
import fi.riista.feature.huntingclub.deercensus.DeerCensusRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.BeaverAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearanceWithPiglets;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Validated
public class GameStatisticsFeature {

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicClubHuntingSummaryRepository;

    @Resource
    private DeerCensusRepository deerCensusRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true)
    public @Valid GameStatisticsDTO getDeerCensusStatisticsForHuntingClub(Long huntingClubId) {
        GameStatisticsDTO dto = new GameStatisticsDTO();

        List<DeerCensus> deerCensuses = getDeerCensuses(huntingClubId);
        deerCensuses.stream()
                .forEach(deerCensus -> dto.appendTimestampForYear(deerCensus.getObservationDate().getYear())
                        .appendData(GameStatisticsDTO.WHITE_TAIL_DEERS, deerCensus.getWhiteTailDeers())
                        .appendData(GameStatisticsDTO.ROE_DEERS, deerCensus.getRoeDeers())
                        .appendData(GameStatisticsDTO.FALLOW_DEERS, deerCensus.getFallowDeers()));
        return dto;
    }

    private List<DeerCensus> getDeerCensuses(Long huntingClubId) {
        HuntingClub huntingClub = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);
        List<DeerCensus> deerCensuses =  deerCensusRepository.findAllByHuntingClub(huntingClub);
        Collections.sort(deerCensuses, Comparator.comparing(DeerCensus::getObservationDate));
        return deerCensuses;
    }

    @Transactional(readOnly = true)
    public @Valid GameStatisticsDTO getDeerStatisticsForHuntingClub(Long huntingClubId) {
        GameStatisticsDTO dto = new GameStatisticsDTO();

        List<BasicClubHuntingSummary> basicClubHuntingSummaries = getDeerBasicClubHuntingSummaries(huntingClubId);
        basicClubHuntingSummaries.stream()
                .forEach(bchs -> dto.appendTimestampForYear(DateUtil.huntingYearContaining(bchs.getHuntingEndDate()))
                        .appendData(GameStatisticsDTO.WHITE_TAIL_DEER_REMAINING_POPULATION_IN_TOTAL_AREA,
                                Optional.ofNullable(bchs.getAreaSizeAndPopulation())
                                        .map(AreaSizeAndRemainingPopulation::getRemainingPopulationInTotalArea)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.WHITE_TAIL_DEER_REMAINING_POPULATION_IN_EFFECTIVE_AREA,
                                Optional.ofNullable(bchs.getAreaSizeAndPopulation())
                                        .map(AreaSizeAndRemainingPopulation::getRemainingPopulationInEffectiveArea)
                                        .orElse(0)));
        dto.combineDataAnnually();
        return dto;
    }
    private List<BasicClubHuntingSummary> getDeerBasicClubHuntingSummaries(Long huntingClubId) {
        HuntingClub huntingClub = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);
        List<BasicClubHuntingSummary> basicClubHuntingSummaries =
                basicClubHuntingSummaryRepository.findAllByClub(huntingClub);

        basicClubHuntingSummaries = basicClubHuntingSummaries.stream()
                .filter(bchs -> GameSpecies.isWhiteTailedDeer(bchs.getGameSpeciesCode()))
                .filter(bchs -> bchs.getHuntingEndDate() != null)
                .collect(Collectors.toList());
        Collections.sort(basicClubHuntingSummaries, Comparator.comparing(BasicClubHuntingSummary::getHuntingEndDate));

        return basicClubHuntingSummaries;
    }

    @Transactional(readOnly = true)
    public @Valid GameStatisticsDTO getMooseStatisticsForHuntingClub(Long huntingClubId) {
        GameStatisticsDTO dto = new GameStatisticsDTO();

        List<MooseHuntingSummary> mooseHuntingSummaries = getMooseHuntingSummaries(huntingClubId);
        mooseHuntingSummaries.stream()
                .forEach(mhs -> dto.appendTimestampForYear(DateUtil.huntingYearContaining(mhs.getHuntingEndDate()))
                        .appendData(GameStatisticsDTO.DROWNED_MOOSES, mhs.getNumberOfDrownedMooses())
                        .appendData(GameStatisticsDTO.MOOSES_KILLED_BY_BEAR, mhs.getNumberOfMoosesKilledByBear())
                        .appendData(GameStatisticsDTO.MOOSES_KILLED_BY_WOLF, mhs.getNumberOfMoosesKilledByWolf())
                        .appendData(GameStatisticsDTO.MOOSES_KILLED_IN_TRAFFIC_ACCIDENT, mhs.getNumberOfMoosesKilledInTrafficAccident())
                        .appendData(GameStatisticsDTO.MOOSES_KILLED_BY_POACHING, mhs.getNumberOfMoosesKilledByPoaching())
                        .appendData(GameStatisticsDTO.MOOSES_KILLED_IN_RUT_FIGHT, mhs.getNumberOfMoosesKilledInRutFight())
                        .appendData(GameStatisticsDTO.STARVED_MOOSES, mhs.getNumberOfStarvedMooses())
                        .appendData(GameStatisticsDTO.MOOSES_DECEASED_BY_OTHER_REASON, mhs.getNumberOfMoosesDeceasedByOtherReason())
                        .appendData(GameStatisticsDTO.TOTAL_DEAD_MOOSES,
                                Optional.ofNullable(mhs.getNumberOfDrownedMooses()).orElse(0) +
                                        Optional.ofNullable(mhs.getNumberOfMoosesKilledByBear()).orElse(0) +
                                        Optional.ofNullable(mhs.getNumberOfMoosesKilledByWolf()).orElse(0) +
                                        Optional.ofNullable(mhs.getNumberOfMoosesKilledInTrafficAccident()).orElse(0) +
                                        Optional.ofNullable(mhs.getNumberOfMoosesKilledByPoaching()).orElse(0) +
                                        Optional.ofNullable(mhs.getNumberOfMoosesKilledInRutFight()).orElse(0) +
                                        Optional.ofNullable(mhs.getNumberOfStarvedMooses()).orElse(0) +
                                        Optional.ofNullable(mhs.getNumberOfMoosesDeceasedByOtherReason()).orElse(0))
                        .appendData(GameStatisticsDTO.REMAINING_MOOSES_IN_TOTAL_AREA,
                                Optional.ofNullable(mhs.getAreaSizeAndPopulation())
                                        .map(AreaSizeAndRemainingPopulation::getRemainingPopulationInTotalArea)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.REMAINING_MOOSES_IN_EFFECTIVE_AREA,
                                Optional.ofNullable(mhs.getAreaSizeAndPopulation())
                                        .map(AreaSizeAndRemainingPopulation::getRemainingPopulationInEffectiveArea)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.WHITE_TAIL_DEERS,
                                getSpeciesAmount(mhs.getWhiteTailedDeerAppearance()))
                        .appendData(GameStatisticsDTO.ROE_DEERS,
                                getSpeciesAmount(mhs.getRoeDeerAppearance()))
                        .appendData(GameStatisticsDTO.WILD_FOREST_REINDEERS,
                                getSpeciesAmount(mhs.getWildForestReindeerAppearance()))
                        .appendData(GameStatisticsDTO.FALLOW_DEERS,
                                getSpeciesAmount(mhs.getFallowDeerAppearance()))
                        .appendData(GameStatisticsDTO.BEAVERS_AMOUNT_OF_INHABITED_WINTER_NESTS,
                                Optional.ofNullable(mhs.getBeaverAppearance())
                                        .map(BeaverAppearance::getAmountOfInhabitedWinterNests)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.BEAVERS_HARVEST_AMOUNT,
                                Optional.ofNullable(mhs.getBeaverAppearance())
                                        .map(BeaverAppearance::getHarvestAmount)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.BEAVERS_AREA_OF_DAMAGE,
                                Optional.ofNullable(mhs.getBeaverAppearance())
                                        .map(BeaverAppearance::getAreaOfDamage)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.BEAVERS_AREA_OCCUPIED_BY_WATER,
                                Optional.ofNullable(mhs.getBeaverAppearance())
                                        .map(BeaverAppearance::getAreaOccupiedByWater)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.WILD_BOARS_ESTIMATED_AMOUNT_OF_SPECIMENS,
                                Optional.ofNullable(mhs.getWildBoarAppearance())
                                        .map(SpeciesEstimatedAppearanceWithPiglets::getEstimatedAmountOfSpecimens)
                                        .orElse(0))
                        .appendData(GameStatisticsDTO.WILD_BOARS_ESTIMATED_AMOUNT_OF_SOW_WITH_PIGLETS,
                                Optional.ofNullable(mhs.getWildBoarAppearance())
                                        .map(SpeciesEstimatedAppearanceWithPiglets::getEstimatedAmountOfSowWithPiglets)
                                        .orElse(0)));
        dto.combineDataAnnually();
        return dto;
    }
    List<MooseHuntingSummary> getMooseHuntingSummaries(Long huntingClubId) {
        HuntingClub huntingClub = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);
        List<MooseHuntingSummary> mooseHuntingSummaries = mooseHuntingSummaryRepository.findAllByClub(huntingClub);
        mooseHuntingSummaries = mooseHuntingSummaries.stream()
                .filter(mhs -> mhs.getHuntingEndDate() != null)
                .collect(Collectors.toList());
        Collections.sort(mooseHuntingSummaries, Comparator.comparing(MooseHuntingSummary::getHuntingEndDate));
        return mooseHuntingSummaries;
    }
    private int getSpeciesAmount(SpeciesEstimatedAppearance speciesEstimatedAppearance) {
        return Optional.ofNullable(speciesEstimatedAppearance)
                .map(SpeciesEstimatedAppearance::getEstimatedAmountOfSpecimens)
                .orElse(0);
    }
}
