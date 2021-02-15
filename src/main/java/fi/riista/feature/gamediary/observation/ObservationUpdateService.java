package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;

@Component
public class ObservationUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(ObservationUpdateService.class);

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private ObservationSpecimenRepository specimenRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private ObservationSpecimenService observationSpecimenService;

    @Resource
    private ObservationModifierService observationModifierService;

    @Resource
    private ObservationLockChecker observationLockChecker;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Transactional(readOnly = true)
    public ObservationLockInfo getObservationLockInfoForNewObservation(@Nonnull final LocalDate observationDate) {
        final ObservationModifierInfo modifierInfo =
                observationModifierService.getObservationCreatorInfo(observationDate);

        return new ObservationLockInfo(modifierInfo, false, true);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = Exception.class)
    public ObservationLockInfo getObservationLockInfo(@Nonnull final Observation observation,
                                                      @Nonnull final ObservationSpecVersion specVersion) {
        requireNonNull(observation);

        return getObservationLockInfo(observation, observation.getPointOfTimeAsLocalDate(), specVersion);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = Exception.class)
    public ObservationLockInfo getObservationLockInfo(@Nonnull final Observation observation,
                                                      @Nonnull final LocalDate proposedNewObservationDate,
                                                      @Nonnull final ObservationSpecVersion specVersion) {

        final ObservationModifierInfo modifierInfo =
                observationModifierService.getObservationModifierInfo(observation, proposedNewObservationDate);

        return observationLockChecker.getObservationLockInfo(observation, specVersion, modifierInfo);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateMutableFields(@Nonnull final Observation observation,
                                    @Nonnull final ObservationDTOBase dto,
                                    @Nonnull final ObservationLockInfo lockInfo) {

        requireNonNull(observation, "observation is null");
        requireNonNull(dto, "dto is null");
        requireNonNull(lockInfo, "lockInfo is null");

        final ObservationModifierInfo modifierInfo = lockInfo.getModifierInfo();

        if (modifierInfo.isAuthorOrObserver()) {
            observation.setDescription(dto.getDescription());
        }

        // Cannot update most of the fields e.g. if user's carnivore authority has expired or club hunting is finished.
        if (lockInfo.isLocked()) {
            return;
        }

        final GameSpecies species = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
        final GeoLocation location = requireNonNull(dto.getGeoLocation(), "geoLocation is null");
        final boolean locationChanged = !location.equals(observation.getGeoLocation());
        final Riistanhoitoyhdistys rhyByLocation = gisQueryService.findRhyByLocation(location);

        observation.setGeoLocation(location);
        observation.setRhy(rhyByLocation);

        observation.setPointOfTime(DateUtil.toDateTimeNullSafe(dto.getPointOfTime()));

        observation.setSpecies(species);
        observation.setObservationCategory(dto.getObservationCategory());
        observation.setObservationType(dto.getObservationType());

        observation.setDeerHuntingType(dto.getDeerHuntingType());
        observation.setDeerHuntingTypeDescription(dto.getDeerHuntingTypeDescription());

        observation.setMooselikeMaleAmount(dto.getMooselikeMaleAmount());
        observation.setMooselikeFemaleAmount(dto.getMooselikeFemaleAmount());
        observation.setMooselikeCalfAmount(dto.getMooselikeCalfAmount());
        observation.setMooselikeFemale1CalfAmount(dto.getMooselikeFemale1CalfAmount());
        observation.setMooselikeFemale2CalfsAmount(dto.getMooselikeFemale2CalfsAmount());
        observation.setMooselikeFemale3CalfsAmount(dto.getMooselikeFemale3CalfsAmount());
        observation.setMooselikeFemale4CalfsAmount(dto.getMooselikeFemale4CalfsAmount());
        observation.setMooselikeUnknownSpecimenAmount(dto.getMooselikeUnknownSpecimenAmount());

        if (observation.hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies()) {
            final int sumOfMooselikeAmounts = observation.getSumOfMooselikeAmounts();

            // Do not use ternary operator to avoid NPE
            if (sumOfMooselikeAmounts > 0) {
                observation.setAmount(sumOfMooselikeAmounts);
            } else {
                observation.setAmount(dto.getAmount());
            }
        } else {
            observation.setAmount(dto.getAmount());
        }

        final boolean canUpdateCarnivoreFields = modifierInfo.canUpdateCarnivoreFields();

        if (!species.isLargeCarnivore() || locationChanged && !canUpdateCarnivoreFields) {
            observation.setInYardDistanceToResidence(null);
        } else if (canUpdateCarnivoreFields) {

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
    public List<ObservationSpecimen> updateSpecimens(@Nonnull final Observation observation,
                                                     @Nonnull final ObservationDTOBase dto,
                                                     @Nonnull final ObservationLockInfo lockInfo) {

        requireNonNull(observation, "observation is null");
        requireNonNull(dto, "dto is null");
        requireNonNull(lockInfo, "lockInfo is null");

        final List<ObservationSpecimen> specimens;

        if (!lockInfo.isLocked()) {
            if (dto.getAmount() != null) {
                specimens = observationSpecimenService
                        .setSpecimens(observation, dto.getAmount(), dto.getSpecimens(), dto.getObservationSpecVersion())
                        .apply((persistedSpecimens, anyChangesDetected) -> {
                            if (anyChangesDetected) {
                                observation.forceRevisionUpdate();
                            }

                            return persistedSpecimens;
                        });
            } else {
                specimens = null;
                observationSpecimenService.deleteAllSpecimens(observation);
            }
        } else {
            // Return unaltered specimens from database.
            specimens = specimenRepository.findByObservation(observation, JpaSort.of(ObservationSpecimen_.id));
        }

        return specimens;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteObservation(final Observation observation, final ObservationLockInfo lockInfo) {
        if (observation.getObservationCategory().isWithinDeerHunting() && !lockInfo.isLocked()) {
            observation.getHuntingClubGroup().ifPresent(
                    group -> groupHuntingDayService.unlinkDiaryEntryFromHuntingDay(observation, group));
        }

        assertNotAttachedToHuntingDay(observation);
        observationRepository.delete(observation);
    }

    private static void assertNotAttachedToHuntingDay(final Observation observation) {
        if (observation.getHuntingDayOfGroup() != null) {
            throw new RuntimeException("Cannot delete observation with an associated hunting day.");
        }
    }
}
