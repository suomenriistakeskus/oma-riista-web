package fi.riista.feature.gamediary.srva;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.PersonRelationshipToGameDiaryEntryDTO;
import fi.riista.feature.organization.RiistanhoitoyhdistysAuthorization;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.Interval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SrvaCrudFeature extends AbstractSrvaCrudFeature<SrvaEventDTO> {

    @Resource
    private SrvaEventDTOTransformer srvaEventDTOTransformer;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Override
    protected void updateEntity(@Nonnull final SrvaEvent entity, @Nonnull final SrvaEventDTO dto) {
        updateEntityCommonFields(entity, dto);
    }

    @Override
    protected SrvaEventDTO toDTO(@Nonnull final SrvaEvent entity) {
        return srvaEventDTOTransformer.apply(entity);
    }

    @Transactional(readOnly = true)
    public PersonRelationshipToGameDiaryEntryDTO getRelationshipToSrvaEvent(long id) {
        final SrvaEvent srvaEvent = requireEntityService.requireSrvaEvent(id, EntityPermission.READ);

        final boolean sameId = Objects.equals(
                getCurrentUser(PersonWithNameDTO.create(srvaEvent.getAuthor()), srvaEvent.getRhy().getId()).getId(),
                srvaEvent.getAuthor().getId());
        return new PersonRelationshipToGameDiaryEntryDTO(sameId, sameId);
    }

    @Transactional(readOnly = true)
    public List<SrvaEventDTO> listSrvaEventsForActiveUser(@Nonnull final Interval dateInterval) {
        return srvaEventDTOTransformer.apply(getSrvaEventsForActiveUser(dateInterval));
    }

    @Transactional
    @Override
    public SrvaEventDTO createSrvaEvent(@Nonnull final SrvaEventDTO dto) {
        return srvaEventDTOTransformer.apply(createSrvaEvent(dto, true));
    }

    @Transactional
    @Override
    public SrvaEventDTO updateSrvaEvent(@Nonnull final SrvaEventDTO dto) {
        return srvaEventDTOTransformer.apply(updateSrvaEvent(dto, true));
    }

    @Transactional(readOnly = true)
    public SrvaEventDTO getSrvaEvent(@Nonnull final Long id) {
        final SrvaEvent entity = requireEntityService.requireSrvaEvent(id, EntityPermission.READ);

        return srvaEventDTOTransformer.apply(entity);
    }

    @Transactional
    public SrvaEventDTO changeState(final Long id, final Integer rev, final SrvaEventStateEnum newstate) {
        final SrvaEvent entity = requireEntityService.requireSrvaEvent(id, EntityPermission.UPDATE);

        DtoUtil.assertNoVersionConflict(entity, rev);

        entity.setState(newstate);

        if (newstate == SrvaEventStateEnum.UNFINISHED) {
            entity.setApproverAsUser(null);
            entity.setApproverAsPerson(null);
        } else {
            entity.setApproverAsUser(activeUserService.getActiveUser());
            entity.setApproverAsPerson(Optional.ofNullable(activeUserService.getActiveUser().getPerson()).orElse(null));
        }

        getRepository().saveAndFlush(entity);

        return srvaEventDTOTransformer.apply(entity);
    }

    @Transactional(readOnly = true)
    public long countUnfinishedSrvaEvents(final long rhyId) {
        final Person person = activeUserService.getActiveUser().getPerson();
        if (person == null) {
            return 0;
        }

        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId,
                RiistanhoitoyhdistysAuthorization.Permission.LIST_SRVA);

        return getRepository().count(JpaSpecs.and(
                SrvaSpecs.equalRhy(rhy),
                SrvaSpecs.equalState(SrvaEventStateEnum.UNFINISHED)
        ));
    }

    @Transactional(readOnly = true)
    public Page<SrvaEventDTO> searchPage(final SrvaEventSearchDTO dto, final Pageable pageRequest) {
        Objects.requireNonNull(dto.getCurrentRhyId(), "currentRhyId cannot be null");
        Objects.requireNonNull(dto.getRhyId(), "rhyId cannot be null");

        final Page<SrvaEvent> page = getRepository().findAll(getSpecs(dto), pageRequest);

        return srvaEventDTOTransformer.apply(page, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<SrvaEventDTO> search(final SrvaEventSearchDTO dto) {
        Objects.requireNonNull(dto.getCurrentRhyId(), "currentRhyId cannot be null");

        final List<SrvaEvent> srvaEvents = getRepository().findAll(getSpecs(dto));

        return srvaEventDTOTransformer.apply(srvaEvents);
    }

    @Transactional(readOnly = true)
    public List<SrvaEventExportExcelDTO> searchExcel(final SrvaEventSearchDTO dto) {
        Objects.requireNonNull(dto.getCurrentRhyId(), "currentRhyId cannot be null");

        final List<SrvaEvent> srvaEvents = getRepository()
                .findAll(getSpecs(dto), new JpaSort(Sort.Direction.ASC, SrvaEvent_.id));

        return srvaEvents.stream().map(srvaEvent ->
                SrvaEventExportExcelDTO.create(srvaEvent, enumLocaliser)).collect(Collectors.toList());
    }

    private Specifications<SrvaEvent> getSpecs(final SrvaEventSearchDTO dto) {
        final Riistanhoitoyhdistys currentRhy = requireEntityService.requireRiistanhoitoyhdistys(dto.getCurrentRhyId(),
                RiistanhoitoyhdistysAuthorization.Permission.LIST_SRVA);

        Specifications<SrvaEvent> specs = Specifications
                .where(SrvaSpecs.anyOfEventNames(dto.getEventNames())).and(SrvaSpecs.equalRhy(currentRhy))
                .or(getOtherRhySpecs(dto));

        if (Objects.nonNull(dto.getRhyId())) {
            final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findOne(dto.getRhyId());
            specs = specs.and(SrvaSpecs.equalRhy(rhy));
        } else if (Objects.nonNull(dto.getRkaId())) {
            specs = specs.and(SrvaSpecs.equalRka(dto.getRkaId()));
        }

        if (Objects.nonNull(dto.getStates())) {
            specs = specs.and(SrvaSpecs.anyOfStates(dto.getStates()));
        }

        if (Objects.nonNull(dto.getGameSpeciesCode())) {
            // 0 stands for other species
            if (dto.getGameSpeciesCode() == 0) {
                specs = specs.and(SrvaSpecs.equalSpecies(null));
            } else {
                specs = specs.and(SrvaSpecs.equalSpecies(gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode())));
            }
        }

        if (dto.hasBeginOrEndDate()) {
            specs = specs.and(SrvaSpecs.withinInterval(dto.getBeginDate(), dto.getEndDate()));
        }

        return specs;
    }

    private static Specification<SrvaEvent> getOtherRhySpecs(final SrvaEventSearchDTO dto) {
        if (!Objects.equals(dto.getCurrentRhyId(), dto.getRhyId()) && dto.getEventNames().contains(SrvaEventNameEnum.ACCIDENT)) {
            return SrvaSpecs.equalEventName(SrvaEventNameEnum.ACCIDENT);
        }

        return JpaSpecs.disjunction();
    }
}
