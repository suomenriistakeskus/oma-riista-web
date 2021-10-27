package fi.riista.feature.gamediary.harvest.mutation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.PermittedMethod;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestAuthorActorMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestCommonMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestDeerHuntingMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestGISMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestLocationMutation;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSpeciesRequiresPermitException;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestForDiaryMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestForHuntingDayMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestForPermitMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestForSeasonMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestMutationForReportType;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.gis.metsahallitus.MetsahallitusAreaLookupResult;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHarvestDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.gamediary.GameDiaryEntry.FOREIGN_PERSON_ELIGIBLE_AS_ACTOR;
import static fi.riista.feature.gamediary.GameDiaryEntry.FOREIGN_PERSON_ELIGIBLE_AS_AUTHOR;

@Component
@Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
public class HarvestMutationFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestMutationFactory.class);

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestSeasonService harvestSeasonService;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Transactional
    public HarvestGISMutation createGISMutation(final HarvestLocationMutation locationMutation) {
        final GeoLocation location = locationMutation.getUpdatedLocation();
        final Riistanhoitoyhdistys rhyByLocation = gisQueryService.findRhyByLocation(location);

        if (rhyByLocation == null) {
            // Outside Finland or inside Finnish economic zone, skip other GIS with null result expected
            final Riistanhoitoyhdistys rhyForEconomicZone = gisQueryService.findRhyForEconomicZone(location);

            return new HarvestGISMutation(rhyForEconomicZone, null, null, null);
        }

        final Municipality municipality = gisQueryService.findMunicipality(location);
        final MMLRekisteriyksikonTietoja rekisteriyksikonTietoja = gisQueryService.findPropertyByLocation(location).orElse(null);
        final MetsahallitusAreaLookupResult metsahallitusArea = gisQueryService.findMetsahallitusAreas(location);

        return new HarvestGISMutation(rhyByLocation, municipality, rekisteriyksikonTietoja, metsahallitusArea);
    }

    @Transactional
    public HarvestCommonMutation createCommonMutation(final HarvestMutationRole mutationRole, final HarvestDTO dto) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
        return new HarvestCommonMutation(dto, species, mutationRole);
    }

    @Transactional
    public HarvestCommonMutation createCommonMutation(final HarvestMutationRole mutationRole,
                                                      final MobileHarvestDTO dto) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
        return new HarvestCommonMutation(dto, species, mutationRole);
    }

    @Transactional
    public HarvestCommonMutation createCommonMutation(final HarvestMutationRole mutationRole,
                                                      final MobileGroupHarvestDTO dto) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
        return new HarvestCommonMutation(dto, species, mutationRole);
    }

    @Transactional
    public HarvestAuthorActorMutation createAuthorActorMutation(final HarvestMutationRole mutationRole,
                                                                final Person activePerson,
                                                                final HarvestDTO dto,
                                                                final HarvestPreviousState previousState) {
        if (mutationRole == HarvestMutationRole.MODERATOR) {
            return HarvestAuthorActorMutation.createForModerator(
                    personLookupService
                            .findPerson(dto.getAuthorInfo(), FOREIGN_PERSON_ELIGIBLE_AS_AUTHOR)
                            .orElseThrow(() -> new IllegalArgumentException("authorInfo is required")),
                    personLookupService
                            .findPerson(dto.getActorInfo(), FOREIGN_PERSON_ELIGIBLE_AS_ACTOR)
                            .orElseThrow(() -> new IllegalArgumentException("actorInfo is required")));
        }

        final Person actor =
                personLookupService.findPerson(dto.getActorInfo(), FOREIGN_PERSON_ELIGIBLE_AS_ACTOR).orElse(null);

        return HarvestAuthorActorMutation.createForNormalUser(activePerson, actor, previousState.getPreviousAuthor());
    }

    @Transactional
    public HarvestAuthorActorMutation createAuthorActorMutation(final Person activePerson,
                                                                final MobileGroupHarvestDTO dto,
                                                                final HarvestPreviousState previousState) {
        final Person actor =
                personLookupService.findPerson(dto.getActorInfo(), FOREIGN_PERSON_ELIGIBLE_AS_ACTOR).orElse(null);

        return HarvestAuthorActorMutation.createForNormalUser(activePerson, actor, previousState.getPreviousAuthor());
    }

    @Nullable
    @Transactional
    public HarvestForPermitMutation createPermitMutation(@Nonnull final HarvestMutationRole mutationRole,
                                                         @Nonnull final Person activePerson,
                                                         @Nonnull final HarvestCommonMutation commonMutation,
                                                         @Nonnull final HarvestLocationMutation locationMutation,
                                                         @Nonnull final HarvestGISMutation gisMutation,
                                                         final boolean supportsPermittedMethod,
                                                         @Nonnull final HarvestSpecVersion specVersion,
                                                         @Nonnull final String permitNumber,
                                                         final PermittedMethod permittedMethod,
                                                         final HuntingMethod huntingMethod) {
        Objects.requireNonNull(permitNumber);

        if (gisMutation.getRhyByLocation() == null) {
            throw new RhyNotResolvableByGeoLocationException(locationMutation.getUpdatedLocation());
        }

        final HarvestPermit harvestPermit = harvestPermitRepository.findByPermitNumber(permitNumber);

        if (harvestPermit == null) {
            throw new HarvestPermitNotFoundException(permitNumber);
        }

        if (harvestPermit.isMooselikePermitType() || harvestPermit.isAmendmentPermit()) {
            LOG.warn(String.format("Ignoring moose-like permit %s specified for diary entry.",
                    harvestPermit.getPermitNumber()));
            return null;
        }

        return HarvestForPermitMutation.create(mutationRole, activePerson, specVersion, supportsPermittedMethod,
                commonMutation.getHarvestDate(), commonMutation.getSpeciesCode(),
                harvestPermit, permittedMethod, huntingMethod);
    }

    @Nonnull
    @Transactional
    public HarvestMutationForReportType createSeasonMutation(@Nonnull final HarvestMutationRole mutationRole,
                                                             @Nonnull final HarvestDTOBase dto,
                                                             @Nonnull final HarvestCommonMutation commonMutation,
                                                             @Nonnull final HarvestGISMutation gisMutation) {
        if (gisMutation.getRhyByLocation() == null) {
            // Outside Finland, handle as private harvest
            return new HarvestForDiaryMutation(false);
        }

        final Tuple2<HarvestSeason, HarvestQuota> harvestSeasonAndQuota = harvestSeasonService.findHarvestSeasonAndQuota(
                commonMutation.getSpecies(),
                dto.getGeoLocation(),
                commonMutation.getHarvestDate(), true);

        final boolean permitRequiredWithoutSeason =
                GameSpecies.isPermitRequiredWithoutSeason(commonMutation.getSpeciesCode());

        if (dto.getHarvestSpecVersion().supportsHarvestReport()) {
            if (harvestSeasonAndQuota != null) {
                return new HarvestForSeasonMutation(dto, mutationRole,
                        harvestSeasonAndQuota._1, harvestSeasonAndQuota._2);
            }

            if (permitRequiredWithoutSeason) {
                throw new HarvestSpeciesRequiresPermitException(dto.getGameSpeciesCode());
            }
        }

        // Fallback to basic diary mutation
        final boolean harvestReportRequired = harvestSeasonAndQuota != null || permitRequiredWithoutSeason;
        return new HarvestForDiaryMutation(harvestReportRequired);
    }

    @Nonnull
    @Transactional
    public HarvestForHuntingDayMutation createHuntingDayMutation(final long groupHuntingDayId,
                                                                 final Person activePerson) {
        return new HarvestForHuntingDayMutation(groupHuntingDayId, activePerson, groupHuntingDayService);
    }

    @Nonnull
    @Transactional
    public HarvestDeerHuntingMutation createDeerHuntingMutation(@Nonnull final HarvestDTOBase dto) {
        return new HarvestDeerHuntingMutation(dto);
    }
}
