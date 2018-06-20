package fi.riista.feature.gamediary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestService;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCanBeLinkedToHuntingDayOnlyWithinMooseHuntingException;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformer;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.ObservationService;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldValidator;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

@Service
public class GameDiaryFeature {

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private HarvestService harvestService;

    @Resource
    private ObservationService observationService;

    @Resource
    private GameDiaryEntryAuthorActorService diaryEntryAuthorActorService;

    @Resource
    private GameDiaryImageService gameDiaryImageService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestSpecimenService harvestSpecimenService;

    @Resource
    private ObservationSpecimenService observationSpecimenService;

    @Resource
    private ObservationFieldsMetadataService observationFieldsMetadataService;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private HarvestDTOTransformer harvestDtoTransformer;

    @Resource
    private ObservationDTOTransformer observationDtoTransformer;

    @Transactional(readOnly = true)
    public HarvestDTO getHarvest(final Long id) {
        return harvestDtoTransformer.apply(requireEntityService.requireHarvest(id, EntityPermission.READ));
    }

    @Transactional
    public HarvestDTO createHarvest(@Nonnull final HarvestDTO dto) {
        Objects.requireNonNull(dto);

        final Harvest harvest = new Harvest();
        harvest.setFromMobile(false);

        final SystemUser activeUser = activeUserService.requireActiveUser();

        final HarvestChangeHistory historyEvent =
                harvestService.updateMutableFields(harvest, dto, activeUser, true);

        activeUserService.assertHasPermission(harvest, EntityPermission.CREATE);

        harvestRepository.saveAndFlush(harvest);

        harvestSpecimenService.addSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), HarvestSpecVersion.MOST_RECENT);

        dto.getImageIds().forEach(uuid -> gameDiaryImageService.associateGameDiaryEntryWithImage(harvest, uuid));

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        return harvestDtoTransformer.apply(harvest);
    }

    @Transactional
    public HarvestDTO updateHarvest(@Nonnull final HarvestDTO dto) {
        Objects.requireNonNull(dto);

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = activeUser.getPerson();
        final boolean businessFieldsCanBeUpdated = harvestService.canBusinessFieldsBeUpdated(
                currentPerson, harvest, null);

        final HarvestChangeHistory historyEvent =
                harvestService.updateMutableFields(harvest, dto, activeUser, businessFieldsCanBeUpdated);

        if (businessFieldsCanBeUpdated) {
            final boolean anyChangesDetected = harvestSpecimenService
                    .setSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), HarvestSpecVersion.MOST_RECENT)
                    .apply((specimens, anyChanges) -> anyChanges);

            if (anyChangesDetected) {
                harvest.forceRevisionUpdate();
            }
        }

        if (harvest.isAuthorOrActor(currentPerson)) {
            gameDiaryImageService.updateImages(harvest, dto.getImageIds());
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        return harvestDtoTransformer.apply(harvestRepository.saveAndFlush(harvest));
    }

    @Transactional(readOnly = true)
    public ObservationDTO getObservation(final Long id) {
        return observationDtoTransformer.apply(requireEntityService.requireObservation(id, EntityPermission.READ));
    }

    @Transactional
    public ObservationDTO createObservation(@Nonnull final ObservationDTO dto) {
        Objects.requireNonNull(dto);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isModerator = activeUser.isModeratorOrAdmin();
        final boolean carnivoreAuthorityGranted =
                !isModerator && userAuthorizationHelper.isCarnivoreContactPersonAnywhere(dto.getPointOfTime().toLocalDate());

        final ObservationFieldValidator validator =
                observationFieldsMetadataService.getObservationFieldValidator(dto.getObservationContext(), carnivoreAuthorityGranted);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        final Observation observation = new Observation();
        observation.setFromMobile(false);

        if (!isModerator) {
            observation.setDescription(dto.getDescription());
        }

        updateMutableFields(observation, dto, activeUser, carnivoreAuthorityGranted);

        activeUserService.assertHasPermission(observation, EntityPermission.CREATE);

        observationRepository.saveAndFlush(observation);

        final List<ObservationSpecimen> specimens = F.isNullOrEmpty(dto.getSpecimens())
                ? null
                : observationSpecimenService.addSpecimens(observation, dto.getAmount(), dto.getSpecimens());

        // Validate entity graph after specimens are persisted. Amount field is excluded
        // because in within-moose-hunting cases the field is computed automatically based on
        // the separate moose amount fields. Therefore the field is prohibited in the REST API.
        validator.validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());

        dto.getImageIds().forEach(uuid -> gameDiaryImageService.associateGameDiaryEntryWithImage(observation, uuid));

        return observationDtoTransformer.apply(observation);
    }

    @Transactional
    public ObservationDTO updateObservation(@Nonnull final ObservationDTO dto) {
        Objects.requireNonNull(dto);

        final Observation observation = requireEntityService.requireObservation(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(observation, dto);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isModerator = activeUser.isModeratorOrAdmin();

        final boolean isCurrentUserActiveCarnivoreContactPerson =
                !isModerator && userAuthorizationHelper.isCarnivoreContactPersonAnywhere(dto.getPointOfTime().toLocalDate());

        final ObservationFieldValidator validator =
                observationFieldsMetadataService.getObservationFieldValidator(dto.getObservationContext(), isCurrentUserActiveCarnivoreContactPerson);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        final boolean authorOrObserver = !isModerator && observation.isAuthorOrActor(activeUser.getPerson());
        final boolean observationLockedByClubHunting = clubHuntingStatusService.isDiaryEntryLocked(observation);

        if (!observationLockedByClubHunting) {
            final boolean carnivoreAuthorityGranted = authorOrObserver && isCurrentUserActiveCarnivoreContactPerson;

            updateMutableFields(observation, dto, activeUser, carnivoreAuthorityGranted);

            // Specimens can be updated if observation is not locked by club hunting or carnivore authority.
            if (carnivoreAuthorityGranted || !observation.isAnyLargeCarnivoreFieldPresent()) {
                final List<ObservationSpecimen> specimens;

                if (dto.getAmount() != null) {
                    specimens = observationSpecimenService.setSpecimens(observation, dto.getAmount(), dto.getSpecimens())
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

                // Validate entity graph after specimens are persisted. Amount field is excluded
                // because in within-moose-hunting cases the field is computed automatically based on
                // the separate moose amount fields. Therefore the field is prohibited in the REST API.
                validator.validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());
            }
        }

        if (authorOrObserver) {
            observation.setDescription(dto.getDescription());
            gameDiaryImageService.updateImages(observation, dto.getImageIds());
        }

        return observationDtoTransformer.apply(observationRepository.saveAndFlush(observation));
    }

    private void updateMutableFields(final Observation observation,
                                     final ObservationDTO dto,
                                     final SystemUser activeUser,
                                     final boolean carnivoreAuthorityGranted) {

        final Person currentPerson = activeUser.getPerson();

        // Check whether observation is locked by presence of large carnivore fields.
        if (carnivoreAuthorityGranted || !observation.isAnyLargeCarnivoreFieldPresent()) {
            observationService.updateMutableFields(observation, dto, carnivoreAuthorityGranted);
            diaryEntryAuthorActorService.setAuthorAndActor(observation, dto, activeUser);
        }

        if (dto.getHuntingDayId() != null) {
            if (!dto.observedWithinMooseHunting()) {
                throw new ObservationCanBeLinkedToHuntingDayOnlyWithinMooseHuntingException();
            }
            groupHuntingDayService.linkDiaryEntryToHuntingDay(observation, dto.getHuntingDayId(), currentPerson);
        }

        if (activeUser.isModeratorOrAdmin()) {
            observation.setModeratorOverride(true);
        }
    }

    @Transactional
    public void deleteHarvest(final long harvestId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.DELETE);
        harvestSpecimenService.deleteAllSpecimens(harvest);
        gameDiaryImageService.deleteGameDiaryImages(harvest);
        harvestService.deleteHarvest(harvest, activeUser);
    }

    @Transactional
    public void deleteObservation(final long observationId) {
        final Observation observation = requireEntityService.requireObservation(observationId, EntityPermission.DELETE);
        observationSpecimenService.deleteAllSpecimens(observation);
        gameDiaryImageService.deleteGameDiaryImages(observation);
        observationService.deleteObservation(observation);
    }
}
