package fi.riista.feature.harvestpermit.report.category;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestReportFieldsDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonRepository;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class HarvestReportCategoryFeature {

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Transactional(readOnly = true)
    public List<HarvestReportCategoryDTO> list(final boolean availableForReportingTodayOnly,
                                               final boolean excludePermitNotRequiredWithoutSeason) {

        final LocalDate reportingDate = availableForReportingTodayOnly ? DateUtil.today() : null;
        final List<HarvestSeason> seasons = harvestSeasonRepository.listAllForReportingFetchSpecies(reportingDate);

        final List<HarvestReportCategoryDTO> result = new LinkedList<>();

        for (final HarvestSeason season : seasons) {
            final GameSpecies species = season.getSpecies();
            final int gameSpeciesCode = species.getOfficialCode();
            final int huntingYear = DateUtil.huntingYearContaining(season.getBeginDate());

            // FIXME "fields" is actually unused property in front-end. To be removed.
            final RequiredHarvestReportFieldsDTO fields = RequiredHarvestReportFieldsDTO
                    .create(gameSpeciesCode, huntingYear, HarvestReportingType.SEASON);

            result.add(HarvestReportCategoryDTO.createForSeason(season, fields));
        }

        final List<GameSpecies> allSpecies = gameSpeciesRepository.findAll();
        final int currentHuntingYear = DateUtil.huntingYear();

        for (final GameSpecies species : allSpecies) {
            final int gameSpeciesCode = species.getOfficialCode();

            if (!excludePermitNotRequiredWithoutSeason || GameSpecies.isPermitRequiredWithoutSeason(gameSpeciesCode)) {
                // FIXME "fields" is actually unused property in front-end. To be removed.
                final RequiredHarvestReportFieldsDTO fields = RequiredHarvestReportFieldsDTO
                        .create(gameSpeciesCode, currentHuntingYear, HarvestReportingType.PERMIT);

                result.add(HarvestReportCategoryDTO.createForPermit(species, fields));
            }
        }

        return result;
    }
}
