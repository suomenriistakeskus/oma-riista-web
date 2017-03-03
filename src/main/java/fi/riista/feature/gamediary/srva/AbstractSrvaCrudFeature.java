package fi.riista.feature.gamediary.srva;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.srva.method.SrvaMethodService;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenService;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.Filters;
import org.joda.time.Interval;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public abstract class AbstractSrvaCrudFeature<DTO extends SrvaEventDTOBase>
        extends AbstractCrudFeature<Long, SrvaEvent, DTO> {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private SrvaMethodService srvaMethodService;

    @Resource
    private SrvaSpecimenService srvaSpecimenService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private GameDiaryImageRepository gameDiaryImageRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    protected GameDiaryImageService gameDiaryImageService;

    @Resource
    protected GameDiaryService gameDiaryService;

    @Resource
    protected RequireEntityService requireEntityService;

    @Resource
    protected GameSpeciesRepository gameSpeciesRepository;

    protected AbstractSrvaCrudFeature() {
        super(SrvaEvent.class);
    }

    protected abstract DTO createSrvaEvent(DTO dto);

    protected abstract DTO updateSrvaEvent(final DTO dto);

    @Override
    protected SrvaEventRepository getRepository() {
        return srvaEventRepository;
    }

    protected void updateEntityCommonFields(@Nonnull final SrvaEvent entity, @Nonnull final DTO dto) {
        entity.setEventName(dto.getEventName());
        entity.setEventType(dto.getEventType());
        entity.setTotalSpecimenAmount(dto.getTotalSpecimenAmount());
        entity.setEventResult(dto.getEventResult());
        entity.setDescription(dto.getDescription());
        entity.setOtherMethodDescription(dto.getOtherMethodDescription());
        entity.setOtherTypeDescription(dto.getOtherTypeDescription());
        entity.setPointOfTime(dto.getPointOfTime().toDate());
        entity.setTimeSpent(dto.getTimeSpent());
        entity.setEventResult(dto.getEventResult());
        entity.setSpecies(Optional.ofNullable(dto.getGameSpeciesCode())
                .map(gameDiaryService::getGameSpeciesByOfficialCode).orElse(null));
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setPersonCount(dto.getPersonCount());
        entity.updateGeoLocation(dto.getGeoLocation(), gisQueryService);
        entity.setAuthor(getCurrentUser(dto.getAuthorInfo(), entity.getRhy().getId()));
        entity.setOtherSpeciesDescription(dto.getOtherSpeciesDescription());

        // entity.state and entity.approverInfo is not set here by intention.
        // state and approverInfo of event are changed via separate api
    }

    @Transactional(readOnly = true)
    public SrvaParametersDTO getSrvaParameters() {
        return new SrvaParametersDTO(getGameSpeciesForSrva());
    }

    private List<GameSpeciesDTO> getGameSpeciesForSrva() {
        return GameSpeciesDTO.transformList(gameSpeciesRepository.findBySrvaOrdinalNotNullOrderBySrvaOrdinal());
    }

    @Nonnull
    protected List<SrvaEvent> getSrvaEventsForActiveUser(@Nullable Interval dateInterval) {
        final Person person = activeUserService.requireActivePerson();

        Specifications<SrvaEvent> specs = Specifications.where(SrvaSpecs.author(person));
        if (dateInterval != null) {
            specs = specs.and(SrvaSpecs.withinInterval(dateInterval));
        }

        return srvaEventRepository.findAll(specs,
                new JpaSort(Sort.Direction.DESC, SrvaEvent_.pointOfTime, SrvaEvent_.id));
    }

    protected SrvaEvent createSrvaEvent(@Nonnull final DTO dto, final boolean associateImages) {
        final SrvaEvent entity = new SrvaEvent();
        updateEntity(entity, dto);

        //Always UNFINISHED for new event
        entity.setState(SrvaEventStateEnum.UNFINISHED);

        activeUserService.assertHasPermission(entity, EntityPermission.CREATE);

        srvaEventRepository.saveAndFlush(entity);

        srvaMethodService.saveMethods(entity, dto);

        srvaSpecimenService.saveSpecimens(entity, dto);

        if (associateImages) {
            dto.getImageIds().forEach(uuid -> gameDiaryImageService.associateSrvaEventWithImage(entity, uuid));
        }

        return entity;
    }

    protected SrvaEvent updateSrvaEvent(@Nonnull final DTO dto, final boolean updateImages) {
        final SrvaEvent entity = requireEntity(dto.getId(), EntityPermission.UPDATE);

        checkForUpdateConflict(dto, entity);

        if (entity.getState() == SrvaEventStateEnum.APPROVED) {
            throw new SrvaApprovedException("Cannot update approved srva event.");
        }

        updateEntity(entity, dto);
        srvaEventRepository.saveAndFlush(entity);

        final boolean methodsUpdated = srvaMethodService.updateMethods(entity, dto);

        final boolean specimensUpdated = srvaSpecimenService.updateSpecimens(entity, dto);

        final boolean imagesUpdated = updateImages && updateImages(dto, entity);

        //Make sure that rev is updated in every update.
        //Updates in methods, specimens or images does not update rev automatically
        if (Objects.equals(entity.getConsistencyVersion(), dto.getRev()) && (methodsUpdated || specimensUpdated || imagesUpdated)) {
            entity.forceRevisionUpdate();
            srvaEventRepository.flush();
        }

        return entity;
    }

    private boolean updateImages(@Nonnull final DTO dto, @Nonnull final SrvaEvent entity) {
        final List<GameDiaryImage> existingImages = gameDiaryImageRepository.findBySrvaEvent(entity);
        final Set<UUID> existingImageIds =
                F.getUniqueIdsAfterTransform(existingImages, GameDiaryImage::getFileMetadata);

        // remove images and associations not found in list of UUIDs.
        final List<GameDiaryImage> imagesToBeRemoved = existingImages.stream()
                .filter(img -> !dto.getImageIds().contains(img.getFileMetadata().getId()))
                .collect(toList());
        gameDiaryImageService.deleteGameDiaryImages(imagesToBeRemoved);

        // associate new images
        final List<UUID> newImageIds = F.filterToList(dto.getImageIds(), Filters.notIn(existingImageIds));
        newImageIds.forEach(uuid -> gameDiaryImageService.associateSrvaEventWithImage(entity, uuid));

        return !imagesToBeRemoved.isEmpty() || !newImageIds.isEmpty();
    }

    @Transactional
    public void deleteSrvaEvent(@Nonnull final Long id) {
        final SrvaEvent entity = requireEntityService.requireSrvaEvent(id, EntityPermission.DELETE);

        if (entity.getState() == SrvaEventStateEnum.APPROVED) {
            throw new SrvaApprovedException("Cannot delete approved srva event.");
        }

        //NOTE! No need to delete methods or specimens separately, since it's handled with onDelete="CASCADE".

        gameDiaryImageService.deleteGameDiaryImages(gameDiaryImageRepository.findBySrvaEvent(entity));

        delete(entity);
    }

    protected Person getCurrentUser(final PersonWithNameDTO authorDto, long rhyId) {
        final Person currentPerson = activeUserService.getActiveUser().getPerson();
        final boolean moderator = activeUserService.isModeratorOrAdmin();

        if (moderator) {
            if (!F.hasId(authorDto)) {
                throw new IllegalStateException(
                        "Currently logged-in user is admin/moderator but author info is missing from srva event dto.");
            }

            return personRepository.getOne(authorDto.getId());
        }

        // Not moderator but different person. Person should be either coordinator or srva contact person of RHY
        if (F.hasId(authorDto) && !Objects.equals(authorDto.getId(), currentPerson.getId())) {
            if (!userAuthorizationHelper.isCoordinator(rhyId) &&
                    !userAuthorizationHelper.isSrvaContactPerson(rhyId)) {
                throw new IllegalStateException(
                        "Current logged-in user is not author, srva contact person or coordinator of this srva event.");
            }

            return personRepository.getOne(authorDto.getId());
        }

        return currentPerson;
    }
}
