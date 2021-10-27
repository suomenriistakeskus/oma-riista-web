package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntryAuthorActorService;
import fi.riista.feature.gamediary.mobile.MobileObservationService;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationLinkableToHuntingDayOnlyWithHuntingCategoryException;
import fi.riista.feature.gamediary.observation.ObservationLockInfo;
import fi.riista.feature.gamediary.observation.ObservationModifierInfo;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.ObservationUpdateService;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldValidator;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
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
import java.util.Optional;

import static fi.riista.feature.gamediary.observation.ObservationAuthorization.Permission.LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_SEKA;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

@Service
public class MobileGroupObservationFeature {

    @Resource
    private ObservationUpdateService observationUpdateService;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private MobileGroupObservationDTOTransformer observationDtoTransformer;

    @Resource
    private MobileObservationService mobileObservationService;

    @Resource
    private ObservationFieldsMetadataService observationFieldsMetadataService;

    @Resource
    private ObservationSpecimenService observationSpecimenService;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private GameDiaryEntryAuthorActorService diaryEntryAuthorActorService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional
    public MobileGroupObservationDTO createObservation(@Nonnull final MobileGroupObservationDTO dto) {
        requireNonNull(dto);

        final ObservationLockInfo lockInfo =
                observationUpdateService.getObservationLockInfoForNewObservation(dto.getPointOfTime().toLocalDate());

        final ObservationModifierInfo modifierInfo = lockInfo.getModifierInfo();

        final ObservationSpecVersion specVersion = dto.getObservationSpecVersion();

        // Duplicate prevention check
        if (dto.getMobileClientRefId() != null) {
            final Person authenticatedPerson = modifierInfo.getActiveUser().requirePerson();
            final MobileGroupObservationDTO existing = observationRepository
                    .findByAuthorAndMobileClientRefId(authenticatedPerson, dto.getMobileClientRefId())
                    .map(observation -> observationDtoTransformer.apply(observation, specVersion))
                    .orElse(null);

            if (existing != null) {
                return existing;
            }
        } else {
            throw new MessageExposableValidationException("mobileClientRefId is missing");
        }

        mobileObservationService.fixObservationCategoryIfNeeded(dto);
        mobileObservationService.clearAmountWithinDeerHunting(dto);

        final boolean carnivoreAuthorityInAnyRhy = modifierInfo.isCarnivoreAuthorityInAnyRhyAtObservationDate();

        ObservationFieldValidator validator = observationFieldsMetadataService
                .getObservationFieldValidator(dto.getObservationContext(), carnivoreAuthorityInAnyRhy);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        // Not duplicate, create a new one

        final Observation observation = new Observation();
        observation.setFromMobile(true);
        observation.setMobileClientRefId(dto.getMobileClientRefId());
        observation.setDescription(dto.getDescription());

        updateMutableFields(observation, dto, lockInfo);

        activeUserService.assertHasPermission(observation, EntityPermission.CREATE);

        if (!specVersion.isMostRecent()) {
            validator = observationFieldsMetadataService
                    .getObservationFieldValidator(observation.getObservationContext(), carnivoreAuthorityInAnyRhy);

            mobileObservationService.fixMooseCalfAmountIfNeeded(observation, specVersion, validator);
        }

        observationRepository.saveAndFlush(observation);

        final List<ObservationSpecimen> specimens = F.isNullOrEmpty(dto.getSpecimens())
                ? null
                : observationSpecimenService.addSpecimens(
                observation, dto.getAmount(), dto.getSpecimens(), specVersion);

        // Validate entity graph after specimens are persisted. Amount field is excluded
        // because in within-moose-hunting cases the field is computed automatically based on
        // the separate moose amount fields. Therefore the field is prohibited in the REST API.
        validator.validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());

        return observationDtoTransformer.apply(observation, specVersion);
    }

    @Transactional
    public MobileGroupObservationDTO updateObservation(@Nonnull final MobileGroupObservationDTO dto) {
        requireNonNull(dto);

        final Observation observation =
                requireEntityService.requireObservation(dto.getId(), LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP);
        DtoUtil.assertNoVersionConflict(observation, dto);

        mobileObservationService.fixObservationCategoryIfNeeded(dto);
        mobileObservationService.clearAmountWithinDeerHunting(dto);

        final ObservationSpecVersion specVersion = dto.getObservationSpecVersion();

        final ObservationLockInfo lockInfo = observationUpdateService
                .getObservationLockInfo(observation, dto.getPointOfTime().toLocalDate(), specVersion);

        final ObservationModifierInfo modifierInfo = lockInfo.getModifierInfo();

        // Active person required for updates coming from mobile.
        modifierInfo.getActiveUser().requirePerson();

        final boolean carnivoreAuthorityInAnyRhy = modifierInfo.isCarnivoreAuthorityInAnyRhyAtObservationDate();

        ObservationFieldValidator validator = observationFieldsMetadataService
                .getObservationFieldValidator(dto.getObservationContext(), carnivoreAuthorityInAnyRhy);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        updateMutableFields(observation, dto, lockInfo);

        final List<ObservationSpecimen> specimens =
                observationUpdateService.updateSpecimens(observation, dto, lockInfo);

        if (!lockInfo.isLocked()) {
            if (!specVersion.isMostRecent()) {
                validator = observationFieldsMetadataService
                        .getObservationFieldValidator(observation.getObservationContext(), carnivoreAuthorityInAnyRhy);

                mobileObservationService.fixMooseCalfAmountIfNeeded(observation, specVersion, validator);
            }

            final Optional<List<?>> specimensOpt = Optional.ofNullable(specimens);

            // While supporting old spec-versions it is easier to nullify illegal fields in an
            // "after hook" manner than try to bake the logic directly into
            // game-diary-feature/specimen-service.
            validator.nullifyIllegalFields(observation, specimensOpt, singleton("amount"), emptySet());

            // Validate entity graph after specimens are persisted. Amount field is excluded
            // because in within-hunting categories the field is computed automatically based on
            // the separate moose amount fields. Therefore the field is prohibited in the REST API.
            validator.validate(observation, specimensOpt, singleton("amount"), emptySet());
        }

        // flush is mandatory! because mobile will use the returned revision, and revision is updated on save
        return observationDtoTransformer.apply(observationRepository.saveAndFlush(observation), specVersion);
    }

    private void updateMutableFields(final Observation observation,
                                     final MobileGroupObservationDTO dto,
                                     final ObservationLockInfo lockInfo) {

        final ObservationCategory existingCategory = observation.getObservationCategory();
        final ObservationCategory updatedCategory = dto.getObservationCategory();

        observationUpdateService.updateMutableFields(observation, dto, lockInfo);

        if (dto.requiresBeaverObservationTypeTranslation() && dto.getObservationType() == PESA) {
            final ObservationType existingType = observation.getObservationType();

            observation.setObservationType(
                    existingType == PESA_PENKKA || existingType == PESA_SEKA ? existingType : PESA_KEKO);
        }

        final ObservationModifierInfo modifierInfo = lockInfo.getModifierInfo();
        final SystemUser activeUser = modifierInfo.getActiveUser();

        if (!lockInfo.isLocked()) {
            diaryEntryAuthorActorService.setAuthorAndActor(observation, dto, activeUser);
        }

        if (lockInfo.canChangeHuntingDay()) {
            if (updatedCategory.isWithinDeerHunting()) {
                groupHuntingDayService.updateGroupHuntingDayForDeerObservation(observation);
            } else {
                final GroupHuntingDay huntingDay = observation.getHuntingDayOfGroup();

                if (existingCategory.isWithinDeerHunting() && huntingDay != null) {
                    // Remove linkage to deer hunting day when category is changed to something else than DEER_HUNTING.
                    groupHuntingDayService.unlinkDiaryEntryFromHuntingDay(observation, huntingDay.getGroup());
                } else if (dto.getHuntingDayId() != null) {
                    ObservationLinkableToHuntingDayOnlyWithHuntingCategoryException.assertWithinMooseHunting(updatedCategory);

                    groupHuntingDayService.linkDiaryEntryToHuntingDay(observation, dto.getHuntingDayId(), activeUser.getPerson());
                }
            }
        }
    }
}
