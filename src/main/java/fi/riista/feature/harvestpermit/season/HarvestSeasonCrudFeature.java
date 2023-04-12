package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.HALLIALUE;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.NORPPAALUE;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.PORONHOITOALUE;
import static fi.riista.util.Collect.nullSafeGroupingBy;
import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class HarvestSeasonCrudFeature extends AbstractCrudFeature<Long, HarvestSeason, HarvestSeasonDTO> {

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private HarvestAreaRepository areaRepository;

    @Resource
    private HarvestQuotaService harvestQuotaService;

    @Override
    protected JpaRepository getRepository() {
        return harvestSeasonRepository;
    }

    @Override
    protected void updateEntity(final HarvestSeason entity, final HarvestSeasonDTO dto) {
        final Optional<GameSpecies> speciesOpt = gameSpeciesRepository.findByOfficialCode(dto.getGameSpeciesCode());
        if (!speciesOpt.isPresent()) {
            throw new IllegalArgumentException();
        }
        final GameSpecies species = speciesOpt.get();

        assertSeason(entity, species, dto.getBeginDate(), dto.getEndDate(), dto.getBeginDate2(), dto.getEndDate2());

        entity.setBeginDate(dto.getBeginDate());
        entity.setEndDate(dto.getEndDate());
        entity.setEndOfReportingDate(dto.getEndOfReportingDate());

        entity.setBeginDate2(dto.getBeginDate2());
        entity.setEndDate2(dto.getEndDate2());
        entity.setEndOfReportingDate2(dto.getEndOfReportingDate2());

        if (entity.isNew()) {
            final LocalisedString name = LocalisedString.fromMap(dto.getName());
            entity.setNameFinnish(name.getFinnish());
            entity.setNameSwedish(name.getSwedish());
            entity.setSpecies(species);
        }
    }

    @Override
    protected HarvestSeasonDTO toDTO(@Nonnull final HarvestSeason season) {
        final List<HarvestQuota> quotas = harvestQuotaRepository.findByHarvestSeason(season);
        return HarvestSeasonDTO.createWithSpeciesAndQuotas(season, quotas);
    }

    @Override
    protected void afterCreate(final HarvestSeason season, final HarvestSeasonDTO seasonDTO) {
        createOrUpdateQuotas(season, seasonDTO);
    }

    @Override
    protected void afterUpdate(final HarvestSeason season, final HarvestSeasonDTO seasonDTO) {
        createOrUpdateQuotas(season, seasonDTO);
    }

    @Transactional(readOnly = true)
    public List<HarvestSeasonDTO> listHarvestSeasons(final int huntingYear) {
        final List<HarvestSeason> seasons = harvestSeasonRepository.findBySeasonInHuntingYear(huntingYear);
        if (seasons.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<HarvestSeason, List<HarvestQuota>> seasonToQuotas = harvestQuotaRepository.findByHarvestSeasonIn(seasons).stream()
                .collect(groupingBy(HarvestQuota::getHarvestSeason, toList()));

        return seasons.stream()
                .map(season -> HarvestSeasonDTO.createWithSpeciesAndQuotas(season, seasonToQuotas.get(season)))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public Map<HarvestArea.HarvestAreaType, List<HarvestAreaDTO>> listQuotaAreas() {
        return areaRepository.findByTypeIn(Arrays.asList(PORONHOITOALUE, HALLIALUE, NORPPAALUE))
                .stream()
                .map(HarvestAreaDTO::create)
                .collect(nullSafeGroupingBy(HarvestAreaDTO::getHarvestAreaType));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MODERATE_HARVEST_SEASONS')")
    public void copyHarvestSeasons(final int toHuntingYear) {
        final int fromHuntingYear = toHuntingYear - 1;
        final List<HarvestSeason> fromSeasons = harvestSeasonRepository.findBySeasonInHuntingYear(fromHuntingYear);
        if (fromSeasons.isEmpty()) {
            return;
        }

        final Function<HarvestSeason, GameSpecies> fromSeasonsToSpecies =
                singleQueryFunction(fromSeasons, HarvestSeason::getSpecies, gameSpeciesRepository, true);

        final List<HarvestSeason> existingSeasons = harvestSeasonRepository.findBySeasonInHuntingYear(toHuntingYear);
        final List<Integer> existingSeasonSpeciesCodes = harvestSeasonRepository.findGameSpeciesBySeasons(existingSeasons)
                .stream()
                .map(GameSpecies::getOfficialCode)
                .collect(toList());

        final List<HarvestSeason> filteredSeasons = fromSeasons.stream()
                .filter(s -> {
                    final GameSpecies species = fromSeasonsToSpecies.apply(s);
                    return !existingSeasonSpeciesCodes.contains(species.getOfficialCode());
                })
                .collect(toList());

        final Map<HarvestSeason, List<HarvestQuota>> fromQuotas = harvestQuotaRepository.findByHarvestSeasonIn(filteredSeasons)
                .stream()
                .collect(nullSafeGroupingBy(HarvestQuota::getHarvestSeason));

        filteredSeasons.forEach(filtered -> {
                final HarvestSeason newSeason = new HarvestSeason();

                // If there is range, e.g. 2022-2023, replace in 2 parts.
                // Update first 2023 to 2024 only then 2022 to 2023
                final String replacedLastYearFi = filtered.getNameFinnish().replaceFirst(Integer.toString(toHuntingYear), Integer.toString(toHuntingYear + 1));
                newSeason.setNameFinnish(replacedLastYearFi.replaceFirst(Integer.toString(fromHuntingYear), Integer.toString(toHuntingYear)));
                final String replacedLastYearSv = filtered.getNameSwedish().replaceFirst(Integer.toString(toHuntingYear), Integer.toString(toHuntingYear + 1));
                newSeason.setNameSwedish(replacedLastYearSv.replaceFirst(Integer.toString(fromHuntingYear), Integer.toString(toHuntingYear)));

                newSeason.setSpecies(fromSeasonsToSpecies.apply(filtered));
                newSeason.setBeginDate(filtered.getBeginDate().plusYears(1));
                newSeason.setEndDate(filtered.getEndDate().plusYears(1));
                newSeason.setEndOfReportingDate(filtered.getEndOfReportingDate().plusYears(1));
                newSeason.setBeginDate2(F.mapNullable(filtered.getBeginDate2(), d -> d.plusYears(1)));
                newSeason.setEndDate2(F.mapNullable(filtered.getEndDate2(), d -> d.plusYears(1)));
                newSeason.setEndOfReportingDate2(F.mapNullable(filtered.getEndOfReportingDate2(), d -> d.plusYears(1)));

                harvestSeasonRepository.saveAndFlush(newSeason);

                final List<HarvestQuota> quotaList = fromQuotas.get(filtered);
                if (quotaList != null && !quotaList.isEmpty()) {
                    final List<HarvestQuota> newQuotas = quotaList.stream()
                            .map(quota -> new HarvestQuota(newSeason, quota.getHarvestArea(), quota.getQuota()))
                            .collect(toList());

                    harvestQuotaRepository.saveAll(newQuotas);
                    harvestQuotaRepository.flush();
                }
            });
    }

    private void createOrUpdateQuotas(final HarvestSeason season, final HarvestSeasonDTO seasonDTO) {
        final List<HarvestQuotaDTO> quotaDTOs = seasonDTO.getQuotas();
        if (quotaDTOs != null && !quotaDTOs.isEmpty()) {
            harvestQuotaService.createOrUpdateQuotas(quotaDTOs, season);
        }
    }

    private void assertSeason(final HarvestSeason season, final GameSpecies species,
                              final LocalDate beginDate, final LocalDate endDate,
                              final LocalDate beginDate2, final LocalDate endDate2) {
        final Long id = F.mapNullable(season, HarvestSeason::getId);
        final List<HarvestSeason> overlappingSeasons =
                harvestSeasonRepository.findOverlappingSeasons(id, species, beginDate, endDate, beginDate2, endDate2);
        if (!overlappingSeasons.isEmpty()) {
            final String firstPeriod = beginDate + " - " + endDate;
            final String secondPeriod = beginDate2 != null ? ", " + beginDate2 + " - " + endDate2 : "";
            throw new IllegalArgumentException("Overlapping season " + firstPeriod + secondPeriod + " for species " + species.getOfficialCode());
        }
    }
}
