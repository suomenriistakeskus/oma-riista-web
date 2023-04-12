package fi.riista.feature.gamediary.mobile;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.EntityLifecycleFields_;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.observation.DeletedObservation;
import fi.riista.feature.gamediary.observation.DeletedObservationRepository;
import fi.riista.feature.gamediary.observation.DeletedObservation_;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationLockInfo;
import fi.riista.feature.gamediary.observation.ObservationModifierInfo;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.ObservationService;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.ObservationUpdateService;
import fi.riista.feature.gamediary.observation.Observation_;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldValidator;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fi.riista.feature.gamediary.GameDiarySpecs.author;
import static fi.riista.feature.gamediary.GameDiarySpecs.observationAuthor;
import static fi.riista.feature.gamediary.GameDiarySpecs.observationsByHuntingYear;
import static fi.riista.feature.gamediary.GameDiarySpecs.observer;
import static fi.riista.feature.gamediary.GameDiarySpecs.temporalSort;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_SEKA;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.jpa.domain.Specification.not;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class MobileObservationFeature {

    private static final int SYNC_PAGE_SIZE = 50;
    @Resource
    private ObservationFieldsMetadataService observationFieldsMetadataService;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private DeletedObservationRepository deletedObservationRepository;

    @Resource
    private ObservationUpdateService observationUpdateService;

    @Resource
    private ObservationSpecimenService observationSpecimenService;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MobileObservationDTOTransformer observationDtoTransformer;

    @Resource
    private MobileObservationService mobileObservationService;

    @Resource
    private ObservationService observationService;

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getMobileObservationFieldMetadata(@Nonnull final ObservationSpecVersion specVersion) {
        final ObservationMetadataDTO dto = observationFieldsMetadataService.getObservationFieldsMetadata(specVersion);
        dto.setMobileApiObservationSpecVersion(specVersion);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<MobileObservationDTO> getObservations(final int firstCalendarYearOfHuntingYear,
                                                      @Nonnull final ObservationSpecVersion specVersion) {
        requireNonNull(specVersion);

        final Person person = activeUserService.requireActivePerson();

        // Observation-specific authorization built into query
        final List<Observation> observations = observationRepository.findAll(
                where(observer(person))
                        .and(observationsByHuntingYear(firstCalendarYearOfHuntingYear)),
                temporalSort(Direction.ASC));

        return observationDtoTransformer.apply(observations, specVersion);
    }


    @Transactional(readOnly = true)
    public MobileDiaryEntryPageDTO<MobileObservationDTO> fetchPageForActiveUser(final LocalDateTime modifiedAfter,
                                                                                final ObservationSpecVersion specVersion) {
        requireNonNull(specVersion);

        final Person person = activeUserService.requireActivePerson();


        final Specification<Observation> specs = Specification
                .where(observer(person))
                .and(modifiedAfter == null
                        ? null
                        : JpaSpecs.modificationTimeAfter(DateUtil.toDateTimeNullSafe(modifiedAfter)));

        final JpaSort sort = JpaSort.of(Sort.Direction.ASC,
                JpaSort.path(SrvaEvent_.lifecycleFields).dot(EntityLifecycleFields_.modificationTime),
                JpaSort.path(SrvaEvent_.id));

        final Page<Observation> page = observationRepository.findAll(specs, PageRequest.of(0, SYNC_PAGE_SIZE, sort));
        final List<Observation> content = page.getContent();

        final DateTime latestModificationTime = content.isEmpty()
                ? null
                : content.get(content.size() - 1).getModificationTime();


        final List<Observation> otherWithSameModificationTime = page.hasNext()
                ? fetchMissingForTimestamp(F.getUniqueIds(content), person, latestModificationTime)
                : emptyList();

        final List<Observation> entities = ImmutableList.<Observation>builder()
                .addAll(content)
                .addAll(otherWithSameModificationTime)
                .build();

        final List<MobileObservationDTO> dtos =
                observationDtoTransformer.apply(entities, specVersion);

        return new MobileDiaryEntryPageDTO<>(dtos, DateUtil.toLocalDateTimeNullSafe(latestModificationTime), page.hasNext());
    }

    @Transactional
    public MobileObservationDTO createObservation(@Nonnull final MobileObservationDTO dto) {
        requireNonNull(dto);

        final ObservationLockInfo lockInfo =
                observationUpdateService.getObservationLockInfoForNewObservation(dto.getPointOfTime().toLocalDate());

        final ObservationModifierInfo modifierInfo = lockInfo.getModifierInfo();
        final Person authenticatedPerson = modifierInfo.getActiveUser().requirePerson();

        final ObservationSpecVersion specVersion = dto.getObservationSpecVersion();

        // Duplicate prevention check
        if (dto.getMobileClientRefId() != null) {
            final MobileObservationDTO existing = observationRepository
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
        observation.setAuthor(authenticatedPerson);
        observation.setObserver(authenticatedPerson);
        observation.setDescription(dto.getDescription());

        updateMutableFields(observation, dto, lockInfo);

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
    public MobileObservationDTO updateObservation(@Nonnull final MobileObservationDTO dto) {
        requireNonNull(dto);

        final Observation observation = requireEntityService.requireObservation(dto.getId(), EntityPermission.UPDATE);
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
                                     final MobileObservationDTO dto,
                                     final ObservationLockInfo lockInfo) {

        final ObservationCategory existingCategory = observation.getObservationCategory();
        final ObservationType existingType = observation.getObservationType();

        observationUpdateService.updateMutableFields(observation, dto, lockInfo);

        if (dto.requiresBeaverObservationTypeTranslation() && dto.getObservationType() == PESA) {
            observation.setObservationType(
                    existingType == PESA_PENKKA || existingType == PESA_SEKA ? existingType : PESA_KEKO);
        }

        if (lockInfo.canChangeHuntingDay()) {
            if (dto.getObservationCategory().isWithinDeerHunting()) {

                groupHuntingDayService.updateGroupHuntingDayForDeerObservation(observation);

            } else if (existingCategory.isWithinDeerHunting()) {

                final GroupHuntingDay huntingDay = observation.getHuntingDayOfGroup();

                if (huntingDay != null) {
                    // Remove linkage to deer hunting day when category is changed to something else than DEER_HUNTING.
                    groupHuntingDayService.unlinkDiaryEntryFromHuntingDay(observation, huntingDay.getGroup());
                }
            }
        }
    }

    @Transactional
    public void deleteObservation(final long observationId) {
        final Observation observation = requireEntityService.requireObservation(observationId, EntityPermission.DELETE);

        observationService.deleteObservation(observation);
    }

    @Transactional(readOnly = true)
    public MobileDeletedDiaryEntriesDTO getDeletedObservationIds(final LocalDateTime deletedAfter) {
        final List<DeletedObservation> deletedObservations = deletedObservationRepository.findAll(
                deletionTimeNewerThan(DateUtil.toDateTimeNullSafe(deletedAfter),
                        activeUserService.requireActivePerson().getId()));

        final LocalDateTime latestDeletion = deletedObservations.stream()
                .map(DeletedObservation::getDeletionTime)
                .max(DateTimeComparator.getInstance())
                .map(DateUtil::toLocalDateTimeNullSafe)
                .orElse(null);

        return new MobileDeletedDiaryEntriesDTO(
                latestDeletion, F.mapNonNullsToList(deletedObservations, DeletedObservation::getObservationId));
    }

    @Transactional(readOnly = true)
    public MobileDeletedDiaryEntriesDTO getObservationsWhereOnlyAuthor() {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person activePerson = activeUser.requirePerson();

        final List<Long> observationIdsWhereOnlyAuthor =
                observationRepository.getObservationIdsWhereOnlyAuthor(activePerson.getId());
        return new MobileDeletedDiaryEntriesDTO(null, observationIdsWhereOnlyAuthor);
    }


    private static Specification<DeletedObservation> deletionTimeNewerThan(
            @Nullable final DateTime deletedSince,
            final long activePersonId) {

        return (root, query, cb) -> {
            final Path<Long> authorId = root.get(DeletedObservation_.authorId);
            final Path<Long> observerId = root.get(DeletedObservation_.observerId);

            final Predicate authorOrObserver = cb.or(cb.equal(authorId, activePersonId), cb.equal(observerId, activePersonId));
            if (deletedSince == null) {
                return authorOrObserver;
            }

            final Path<DateTime> dateField =
                    root.get(DeletedObservation_.deletionTime);
            return cb.and(cb.greaterThan(dateField, deletedSince),
                    authorOrObserver);
        };
    }

    private List<Observation> fetchMissingForTimestamp(final Set<Long> alreadyFoundIds,
                                                       final Person person,
                                                       final DateTime latestModificationTime) {
        if (latestModificationTime == null) {
            return emptyList();
        }

        // If more than pageful was found, find rest with same modification time to avoid issues in sync
        final Specification<Observation> specs = Specification
                .where(observer(person))
                .and(JpaSpecs.modificationTimeEqual(latestModificationTime))
                .and(not(JpaSpecs.inCollection(Observation_.id, alreadyFoundIds)));

        return observationRepository.findAll(specs);
    }
}
