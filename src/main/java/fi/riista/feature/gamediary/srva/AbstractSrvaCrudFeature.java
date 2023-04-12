package fi.riista.feature.gamediary.srva;

import fi.riista.config.Constants;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.srva.method.SrvaMethodService;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenService;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public abstract class AbstractSrvaCrudFeature<DTO extends SrvaEventDTOBase>
        extends AbstractCrudFeature<Long, SrvaEvent, DTO> {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private DeletedSrvaEventRepository deletedSrvaEventRepository;

    @Resource
    private SrvaMethodService srvaMethodService;

    @Resource
    private SrvaSpecimenService srvaSpecimenService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    protected GameDiaryImageService gameDiaryImageService;

    @Resource
    protected GameSpeciesService gameSpeciesService;

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
        final GameSpecies gameSpecies = Optional.ofNullable(dto.getGameSpeciesCode())
                .map(gameSpeciesService::requireByOfficialCode).orElse(null);
        final Riistanhoitoyhdistys rhyByLocation = gisQueryService.findRhyByLocation(dto.getGeoLocation());
        final Person author = resolveAuthor(dto.getAuthorInfo(), rhyByLocation);

        entity.setEventName(dto.getEventName());
        entity.setEventType(dto.getEventType());
        entity.setTotalSpecimenAmount(dto.getTotalSpecimenAmount());
        entity.setEventResult(dto.getEventResult());
        entity.setDescription(dto.getDescription());
        entity.setOtherMethodDescription(dto.getOtherMethodDescription());
        entity.setOtherTypeDescription(dto.getOtherTypeDescription());
        entity.setPointOfTime(dto.getPointOfTime().toDateTime(Constants.DEFAULT_TIMEZONE));
        entity.setTimeSpent(dto.getTimeSpent());
        entity.setEventResult(dto.getEventResult());
        entity.setSpecies(gameSpecies);
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setPersonCount(dto.getPersonCount());
        entity.setRhy(rhyByLocation);
        entity.setAuthor(author);
        entity.setOtherSpeciesDescription(dto.getOtherSpeciesDescription());

        // entity.state and entity.approverInfo is not set here by intention.
        // state and approverInfo of event are changed via separate api
    }

    protected void updateEntitySpecV2Fields(@Nonnull final SrvaEvent entity, @Nonnull final DTO dto) {
        entity.setDeportationOrderNumber(dto.getDeportationOrderNumber());
        entity.setEventTypeDetail(dto.getEventTypeDetail());
        entity.setOtherEventTypeDetailDescription(dto.getOtherEventTypeDetailDescription());
        entity.setEventResultDetail(dto.getEventResultDetail());
    }

    @Transactional(readOnly = true)
    public SrvaParametersDTO getSrvaParameters(final SrvaEventSpecVersion specVersion) {
        return new SrvaParametersDTO(getGameSpeciesForSrva(), specVersion);
    }

    private List<GameSpeciesDTO> getGameSpeciesForSrva() {
        return GameSpeciesDTO.transformList(gameSpeciesRepository.findBySrvaOrdinalNotNullOrderBySrvaOrdinal());
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

        final boolean imagesUpdated = updateImages && gameDiaryImageService.updateImages(entity, dto.getImageIds());

        //Make sure that rev is updated in every update.
        //Updates in methods, specimens or images does not update rev automatically
        if (Objects.equals(entity.getConsistencyVersion(), dto.getRev()) && (methodsUpdated || specimensUpdated || imagesUpdated)) {
            entity.forceRevisionUpdate();
            srvaEventRepository.flush();
        }

        return entity;
    }

    @Transactional
    public void deleteSrvaEvent(@Nonnull final Long id) {
        final SrvaEvent entity = requireEntity(id, EntityPermission.DELETE);

        if (entity.getState() == SrvaEventStateEnum.APPROVED) {
            throw new SrvaApprovedException("Cannot delete approved srva event.");
        }

        //NOTE! No need to delete methods or specimens separately, since it's handled with onDelete="CASCADE".

        gameDiaryImageService.deleteGameDiaryImages(entity);

        deletedSrvaEventRepository.save(new DeletedSrvaEvent(id, entity.getAuthor().getId()));

        delete(entity);
    }

    protected Person resolveAuthor(final PersonWithNameDTO authorDto, final Riistanhoitoyhdistys rhy) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (activeUser.isModeratorOrAdmin()) {
            if (!F.hasId(authorDto)) {
                throw new IllegalStateException(
                        "Currently logged-in user is admin/moderator but author info is missing from SRVA event dto.");
            }

            return personRepository.getOne(authorDto.getId());
        }

        final Person currentPerson = activeUser.getPerson();

        // Not moderator but a different person. Person should be either coordinator or SRVA contact person of RHY.
        if (F.hasId(authorDto) && !Objects.equals(authorDto.getId(), currentPerson.getId())) {
            if (!userAuthorizationHelper.isCoordinator(rhy, currentPerson) &&
                    !userAuthorizationHelper.isSrvaContactPerson(rhy, currentPerson)) {

                throw new IllegalStateException(
                        "Current logged-in user is not author, SRVA contact person or coordinator of this SRVA event.");
            }

            return personRepository.getOne(authorDto.getId());
        }

        return currentPerson;
    }
}
