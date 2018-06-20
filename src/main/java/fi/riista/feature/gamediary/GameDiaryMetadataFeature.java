package fi.riista.feature.gamediary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsQuery;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsQueryResponse;
import fi.riista.feature.gamediary.mobile.MobileGameSpeciesCodesetDTO;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationMetadataDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class GameDiaryMetadataFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private ObservationFieldsMetadataService observationFieldsMetadataService;

    @Resource
    private HarvestSeasonService harvestSeasonService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Transactional(readOnly = true)
    public GameDiaryParametersDTO getGameDiaryParameters() {
        return new GameDiaryParametersDTO(gameSpeciesService.getCategories(), gameSpeciesService.listAll());
    }

    @Transactional(readOnly = true)
    public MobileGameSpeciesCodesetDTO getMobileGameSpecies() {
        return new MobileGameSpeciesCodesetDTO(gameSpeciesService.getCategories(), gameSpeciesService.listAll());
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> getGameSpeciesRegistrableAsObservationsWithinMooseHunting() {
        return gameSpeciesService.listRegistrableAsObservationsWithinMooseHunting();
    }

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getObservationFieldMetadata() {
        return observationFieldsMetadataService.getObservationFieldsMetadata(ObservationSpecVersion.MOST_RECENT);
    }

    @Transactional(readOnly = true)
    public GameSpeciesObservationMetadataDTO getObservationFieldMetadataForSpecies(final int gameSpeciesCode) {
        return observationFieldsMetadataService.getObservationFieldMetadataForSingleSpecies(gameSpeciesCode,
                ObservationSpecVersion.MOST_RECENT, true);
    }

    @Transactional(readOnly = true)
    public RequiredHarvestFieldsDTO getMooselikeHarvestFields(final int gameSpeciesCode) {
        if (!GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
            throw new IllegalArgumentException("Only mooselike supported");
        }
        final HarvestReportingType reportingType = HarvestReportingType.HUNTING_DAY;
        final int huntingYear = DateUtil.huntingYear();
        return RequiredHarvestFieldsDTO.create(gameSpeciesCode, huntingYear, reportingType);
    }

    private Tuple2<Riistanhoitoyhdistys, Boolean> findHarvestRhy(final GeoLocation location) {
        final Riistanhoitoyhdistys rhyByLocation = gisQueryService.findRhyByLocation(location);

        if (rhyByLocation != null) {
            return Tuple.of(rhyByLocation, false);
        }

        final Riistanhoitoyhdistys rhyForEconomicZone = gisQueryService.findRhyForEconomicZone(location);

        if (rhyForEconomicZone != null) {
            return Tuple.of(rhyForEconomicZone, true);
        }

        return null;
    }

    @Transactional(readOnly = true)
    public RequiredHarvestFieldsQueryResponse getHarvestFields(final RequiredHarvestFieldsQuery dto) {
        final Tuple2<Riistanhoitoyhdistys, Boolean> rhyAndEconomicZone = findHarvestRhy(dto.getLocation());
        final RequiredHarvestFieldsQueryResponse.Builder builder = RequiredHarvestFieldsQueryResponse.builder(dto);
        final HarvestReportingType reportingType;

        if (rhyAndEconomicZone == null || dto.getHarvestDate().isBefore(Harvest.REPORT_REQUIRED_SINCE)) {
            // Outside Finland or report not yet supported
            reportingType = HarvestReportingType.BASIC;

        } else if (dto.isWithPermit()) {
            reportingType = HarvestReportingType.PERMIT;

        } else {
            final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
            final Tuple2<HarvestSeason, HarvestQuota> harvestSeasonAndQuota = harvestSeasonService
                    .findHarvestSeasonAndQuota(gameSpecies, rhyAndEconomicZone._1, dto.getHarvestDate(), false);

            if (harvestSeasonAndQuota != null) {
                reportingType = HarvestReportingType.SEASON;
                builder.withSeason(harvestSeasonAndQuota._1).withQuota(harvestSeasonAndQuota._2);

            } else if (GameSpecies.isPermitRequiredWithoutSeason(dto.getGameSpeciesCode())) {
                reportingType = HarvestReportingType.PERMIT;
            } else {
                reportingType = HarvestReportingType.BASIC;
            }
        }

        if (reportingType != HarvestReportingType.BASIC) {
            builder.withRhy(rhyAndEconomicZone._1);

            if (!rhyAndEconomicZone._2) {
                builder.withMunicipalityName(gisQueryService.findMunicipality(dto.getLocation()))
                        .withPropertyIdentifier(gisQueryService.findPropertyByLocation(dto.getLocation()));
            }
        }

        return builder.withReportingType(reportingType).build();
    }

    @Transactional(readOnly = true)
    public RequiredHarvestFieldsQueryResponse getHarvestFields(final long harvestId) {
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.READ);

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                harvest.getSpecies().getOfficialCode(),
                harvest.getPointOfTimeAsLocalDate(),
                harvest.getGeoLocation(),
                harvest.getHarvestPermit() != null);

        final Municipality municipality = harvest.getMunicipalityCode() != null
                ? municipalityRepository.findOne(harvest.getMunicipalityCode()) : null;

        return RequiredHarvestFieldsQueryResponse.builder(query)
                .withReportingType(harvest.resolveReportingType())
                .withSeason(harvest.getHarvestSeason())
                .withQuota(harvest.getHarvestQuota())
                .withPropertyIdentifier(harvest.getPropertyIdentifier())
                .withMunicipalityName(municipality)
                .withRhy(harvest.getRhy())
                .build();
    }
}
