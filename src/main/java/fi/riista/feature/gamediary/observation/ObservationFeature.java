package fi.riista.feature.gamediary.observation;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntryAuthorActorService;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldValidator;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

@Service
public class ObservationFeature {

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private ObservationUpdateService observationUpdateService;

    @Resource
    private GameDiaryEntryAuthorActorService diaryEntryAuthorActorService;

    @Resource
    private ObservationSpecimenService observationSpecimenService;

    @Resource
    private GameDiaryImageService gameDiaryImageService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ObservationFieldsMetadataService observationFieldsMetadataService;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private ObservationDTOTransformer observationDtoTransformer;

    @Resource
    private ObservationService observationService;

    @Transactional(readOnly = true)
    public ObservationDTO getObservation(final Long id) {
        return observationDtoTransformer.apply(requireEntityService.requireObservation(id, EntityPermission.READ));
    }

    @Transactional
    public ObservationDTO createObservation(@Nonnull final ObservationDTO dto) {
        requireNonNull(dto);

        final ObservationLockInfo lockInfo =
                observationUpdateService.getObservationLockInfoForNewObservation(dto.getPointOfTime().toLocalDate());

        final ObservationFieldValidator validator = observationFieldsMetadataService.getObservationFieldValidator(
                dto.getObservationContext(),
                lockInfo.getModifierInfo().isCarnivoreAuthorityInAnyRhyAtObservationDate());
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        final Observation observation = new Observation();
        observation.setFromMobile(false);

        updateMutableFields(observation, dto, lockInfo);

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
        requireNonNull(dto);

        final Observation observation = requireEntityService.requireObservation(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(observation, dto);

        final ObservationLockInfo lockInfo = observationUpdateService.getObservationLockInfo(
                observation, dto.getPointOfTime().toLocalDate(), dto.getObservationSpecVersion());

        final ObservationModifierInfo modifierInfo = lockInfo.getModifierInfo();

        final ObservationFieldValidator validator = observationFieldsMetadataService.getObservationFieldValidator(
                dto.getObservationContext(), modifierInfo.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        updateMutableFields(observation, dto, lockInfo);

        final List<ObservationSpecimen> specimens =
                observationUpdateService.updateSpecimens(observation, dto, lockInfo);

        if (!lockInfo.isLocked()) {
            // Validate entity graph after specimens are persisted. Amount field is excluded
            // because in within-hunting categories the field is computed automatically based on
            // the separate moose amount fields. Therefore the field is prohibited in the REST API.
            validator.validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());
        }

        if (modifierInfo.isAuthorOrObserver()) {
            gameDiaryImageService.updateImages(observation, dto.getImageIds());
        }

        return observationDtoTransformer.apply(observationRepository.saveAndFlush(observation));
    }

    private void updateMutableFields(final Observation observation,
                                     final ObservationDTO dto,
                                     final ObservationLockInfo lockInfo) {

        final ObservationCategory existingCategory = observation.getObservationCategory();
        final ObservationCategory updatedCategory = dto.getObservationCategory();

        observationUpdateService.updateMutableFields(observation, dto, lockInfo);

        final ObservationModifierInfo modifierInfo = lockInfo.getModifierInfo();

        if (!lockInfo.isLocked()) {
            diaryEntryAuthorActorService.setAuthorAndActor(observation, dto, modifierInfo.getActiveUser());
        }

        if (lockInfo.canChangeHuntingDay()) {
            final SystemUser activeUser = modifierInfo.getActiveUser();

            if (updatedCategory.isWithinDeerHunting()) {
                // For observations within deer hunting, link hunting day by location automatically.
                groupHuntingDayService.updateGroupHuntingDayForDeerObservation(observation);

            } else {
                // Remove linkage to deer hunting day when category is changed to something else than DEER_HUNTING.
                if (existingCategory.isWithinDeerHunting() && observation.getHuntingDayOfGroup() != null) {
                    final HuntingClubGroup huntingGroup = observation.getHuntingDayOfGroup().getGroup();
                    groupHuntingDayService.unlinkDiaryEntryFromHuntingDay(observation, huntingGroup);

                } else if (dto.getHuntingDayId() != null) {
                    ObservationLinkableToHuntingDayOnlyWithHuntingCategoryException.assertWithinMooseHunting(updatedCategory);

                    groupHuntingDayService.linkDiaryEntryToHuntingDay(
                            observation, dto.getHuntingDayId(), activeUser.getPerson());
                }
            }

            if (activeUser.isModeratorOrAdmin()) {
                observation.setModeratorOverride(true);
            }
        }
    }

    @Transactional
    public void deleteObservation(final long observationId) {
        final Observation observation = requireEntityService.requireObservation(observationId, EntityPermission.DELETE);

        observationService.deleteObservation(observation);
    }
}
