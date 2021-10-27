package fi.riista.feature.gamediary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsRequestDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsResponseDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestReportFieldsDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestSpecimenFieldsDTO;
import fi.riista.feature.gamediary.mobile.MobileGameSpeciesCodesetDTO;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationMetadataDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonService;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

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
        return gameSpeciesService.listSpeciesForObservationCategory(ObservationCategory.MOOSE_HUNTING);
    }

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getObservationFieldMetadata() {
        return observationFieldsMetadataService.getObservationFieldsMetadata(ObservationSpecVersion.MOST_RECENT);
    }

    @Transactional(readOnly = true)
    public GameSpeciesObservationMetadataDTO getObservationFieldMetadataForSpecies(final int gameSpeciesCode) {
        return observationFieldsMetadataService.getObservationFieldMetadataForSingleSpecies(
                gameSpeciesCode, ObservationSpecVersion.MOST_RECENT, true);
    }

    @Transactional(readOnly = true)
    public RequiredHarvestFieldsDTO getRequiredHarvestFieldsForHuntingGroup(final long huntingGroupId,
                                                                            @Nonnull final HarvestSpecVersion specVersion) {

        return getHarvestFieldsForHuntingGroup(huntingGroupId, false, specVersion);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('SAVE_INCOMPLETE_HARVEST_DATA')")
    @Transactional(readOnly = true)
    public RequiredHarvestFieldsDTO getLegallyMandatoryHarvestFieldsForHuntingGroup(final long huntingGroupId,
                                                                                    @Nonnull final HarvestSpecVersion specVersion) {

        return getHarvestFieldsForHuntingGroup(huntingGroupId, true, specVersion);
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
    public RequiredHarvestFieldsResponseDTO getRequiredHarvestFields(@Nonnull final RequiredHarvestFieldsRequestDTO dto,
                                                                     @Nonnull final HarvestSpecVersion specVersion) {
        requireNonNull(specVersion);

        final RequiredHarvestFieldsResponseDTO.Builder builder =
                RequiredHarvestFieldsResponseDTO.builder(dto, specVersion);

        final LocalDate date = dto.getHarvestDate();
        final GeoLocation location = dto.getLocation();
        final Tuple2<Riistanhoitoyhdistys, Boolean> rhyAndEconomicZone = findHarvestRhy(location);
        final HarvestReportingType reportingType;

        if (rhyAndEconomicZone == null || date.isBefore(Harvest.REPORT_REQUIRED_SINCE)) {
            // Outside Finland or report not yet supported
            reportingType = HarvestReportingType.BASIC;

        } else if (dto.isWithPermit()) {
            reportingType = HarvestReportingType.PERMIT;

        } else {
            final int speciesCode = dto.getGameSpeciesCode();
            final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(speciesCode);

            final Tuple2<HarvestSeason, HarvestQuota> harvestSeasonAndQuota =
                    harvestSeasonService.findHarvestSeasonAndQuota(gameSpecies, location, date, false);

            if (harvestSeasonAndQuota != null) {
                reportingType = HarvestReportingType.SEASON;
                builder.withSeason(harvestSeasonAndQuota._1).withQuota(harvestSeasonAndQuota._2);

            } else if (GameSpecies.isPermitRequiredWithoutSeason(speciesCode)) {
                reportingType = HarvestReportingType.PERMIT;
            } else {
                reportingType = HarvestReportingType.BASIC;
            }
        }

        if (reportingType != HarvestReportingType.BASIC) {
            builder.withRhy(rhyAndEconomicZone._1);

            if (!rhyAndEconomicZone._2) {
                builder.withMunicipalityName(gisQueryService.findMunicipality(location))
                        .withPropertyIdentifier(gisQueryService.findPropertyByLocation(location));
            }
        }

        return builder.withReportingType(reportingType).build();
    }

    @Transactional(readOnly = true)
    public RequiredHarvestFieldsResponseDTO getRequiredFieldsForHarvest(final long harvestId,
                                                                        @Nonnull final HarvestSpecVersion specVersion) {
        requireNonNull(specVersion);

        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.READ);

        final RequiredHarvestFieldsRequestDTO request = new RequiredHarvestFieldsRequestDTO(
                harvest.getSpecies().getOfficialCode(),
                harvest.getPointOfTimeAsLocalDate(),
                harvest.getGeoLocation(),
                harvest.getHarvestPermit() != null);

        final Municipality municipality = Optional.ofNullable(harvest.getMunicipalityCode())
                .flatMap(municipalityRepository::findById)
                .orElse(null);

        final HarvestSpecVersion overrideSpecVersion = specVersion;

        return RequiredHarvestFieldsResponseDTO.builder(request, overrideSpecVersion)
                .withReportingType(harvest.resolveReportingType())
                .withSeason(harvest.getHarvestSeason())
                .withQuota(harvest.getHarvestQuota())
                .withPropertyIdentifier(harvest.getPropertyIdentifier())
                .withMunicipalityName(municipality)
                .withRhy(harvest.getRhy())
                .build();
    }

    private RequiredHarvestFieldsDTO getHarvestFieldsForHuntingGroup(final long huntingGroupId,
                                                                     final boolean onlyLegallyMandatory,
                                                                     final HarvestSpecVersion specVersion) {
        requireNonNull(specVersion);

        // Using UPDATE permission because field requirement data is used when creating/updating harvest.
        final HuntingClubGroup group =
                requireEntityService.requireHuntingGroup(huntingGroupId, EntityPermission.UPDATE);

        final int huntingYear = group.getHuntingYear();
        final int gameSpeciesCode = group.getSpecies().getOfficialCode();


        final RequiredHarvestFields.Report reportFields = RequiredHarvestFields.getFormFields(
                huntingYear, gameSpeciesCode, HarvestReportingType.HUNTING_DAY, onlyLegallyMandatory);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                huntingYear, gameSpeciesCode, null, HarvestReportingType.HUNTING_DAY, onlyLegallyMandatory,
                specVersion);

        final RequiredHarvestReportFieldsDTO reportFieldsDTO = RequiredHarvestReportFieldsDTO.create(reportFields);
        final RequiredHarvestSpecimenFieldsDTO specimenFieldsDTO =
                RequiredHarvestSpecimenFieldsDTO.create(specimenFields);

        return new RequiredHarvestFieldsDTO(reportFieldsDTO, specimenFieldsDTO);
    }
}
