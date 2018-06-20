package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.OptionalInt;

@Service
public class ObservationService {
    private static final Logger LOG = LoggerFactory.getLogger(ObservationService.class);

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private GISQueryService gisQueryService;

    private static void assertNotAttachedToHuntingDay(final Observation observation) {
        if (observation.getHuntingDayOfGroup() != null) {
            throw new RuntimeException("Cannot delete observation with an associated hunting day.");
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateMutableFields(@Nonnull final Observation observation,
                                    @Nonnull final ObservationDTOBase dto,
                                    final boolean carnivoreAuthorityGranted) {
        Objects.requireNonNull(observation, "observation is null");
        Objects.requireNonNull(dto, "dto is null");

        if (observation.getHuntingDayOfGroup() != null && !dto.observedWithinMooseHunting()) {
            throw new MessageExposableValidationException(
                    "Observation must be done within moose hunting when linked to hunting group");
        }

        final GameSpecies species = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
        final GeoLocation location = Objects.requireNonNull(dto.getGeoLocation(), "geoLocation is null");
        final boolean locationChanged = !location.equals(observation.getGeoLocation());
        final Riistanhoitoyhdistys rhyByLocation = gisQueryService.findRhyByLocation(location);

        observation.setGeoLocation(location);
        observation.setRhy(rhyByLocation);
        observation.setSpecies(species);
        observation.setPointOfTime(DateUtil.toDateNullSafe(dto.getPointOfTime()));
        observation.setWithinMooseHunting(dto.getWithinMooseHunting());
        observation.setObservationType(dto.getObservationType());

        observation.setMooselikeMaleAmount(dto.getMooselikeMaleAmount());
        observation.setMooselikeFemaleAmount(dto.getMooselikeFemaleAmount());
        observation.setMooselikeCalfAmount(dto.getMooselikeCalfAmount());
        observation.setMooselikeFemale1CalfAmount(dto.getMooselikeFemale1CalfAmount());
        observation.setMooselikeFemale2CalfsAmount(dto.getMooselikeFemale2CalfsAmount());
        observation.setMooselikeFemale3CalfsAmount(dto.getMooselikeFemale3CalfsAmount());
        observation.setMooselikeFemale4CalfsAmount(dto.getMooselikeFemale4CalfsAmount());
        observation.setMooselikeUnknownSpecimenAmount(dto.getMooselikeUnknownSpecimenAmount());

        if (observation.hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies()) {
            observation.setAmountToSumOfMooselikeAmounts();
        } else {
            observation.setAmount(dto.getAmount());
        }

        if (!species.isLargeCarnivore() || locationChanged && !carnivoreAuthorityGranted) {
            observation.setInYardDistanceToResidence(null);
        } else if (carnivoreAuthorityGranted) {

            if (dto.getObservationSpecVersion().supportsLargeCarnivoreFields()) {
                observation.setVerifiedByCarnivoreAuthority(dto.getVerifiedByCarnivoreAuthority());
                observation.setObserverName(dto.getObserverName());
                observation.setObserverPhoneNumber(dto.getObserverPhoneNumber());
                observation.setOfficialAdditionalInfo(dto.getOfficialAdditionalInfo());
            }

            if (locationChanged || observation.getInYardDistanceToResidence() == null) {
                try {
                    final OptionalInt distance =
                            gisQueryService.findInhabitedBuildingDistance(GISPoint.create(location), 100);
                    observation.setInYardDistanceToResidence(distance.isPresent() ? distance.getAsInt() : null);
                } catch (final RuntimeException e) {
                    observation.setInYardDistanceToResidence(null);
                    LOG.error("Could not resolve distance to inhabited building for observation location", e);
                }
            }
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteObservation(final Observation observation) {
        assertNotAttachedToHuntingDay(observation);
        observationRepository.delete(observation);
    }
}
