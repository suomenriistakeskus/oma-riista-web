package fi.riista.feature.gamediary.srva;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.ACCIDENT;

@Service
public class SrvaCrudFeature extends AbstractSrvaCrudFeature<SrvaEventDTO> {

    @Resource
    private SrvaEventDTOTransformer srvaEventDTOTransformer;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    protected void updateEntity(@Nonnull final SrvaEvent entity, @Nonnull final SrvaEventDTO dto) {
        updateEntityCommonFields(entity, dto);
    }

    @Override
    protected SrvaEventDTO toDTO(@Nonnull final SrvaEvent entity) {
        return srvaEventDTOTransformer.apply(entity);
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
        final SrvaEvent entity = requireEntity(id, EntityPermission.READ);

        return srvaEventDTOTransformer.apply(entity);
    }

    @Transactional
    public SrvaEventDTO changeState(final Long id, final Integer rev, final SrvaEventStateEnum newstate) {
        final SrvaEvent entity = requireEntity(id, EntityPermission.UPDATE);

        DtoUtil.assertNoVersionConflict(entity, rev);

        entity.setState(newstate);

        if (newstate == SrvaEventStateEnum.UNFINISHED) {
            entity.setApproverAsUser(null);
            entity.setApproverAsPerson(null);
        } else {
            final SystemUser activeUser = activeUserService.requireActiveUser();
            entity.setApproverAsUser(activeUser);
            entity.setApproverAsPerson(activeUser.getPerson());
        }

        getRepository().saveAndFlush(entity);

        return srvaEventDTOTransformer.apply(entity);
    }

    @Transactional(readOnly = true)
    public Slice<SrvaEventDTO> searchPage(final SrvaEventSearchDTO dto, final Pageable pageRequest) {
        assertCurrentRhyId(dto);
        Objects.requireNonNull(dto.getRhyCode(), "rhyCode cannot be null");

        final Slice<SrvaEvent> slice = getSlice(dto, pageRequest);
        final List<SrvaEventDTO> dtos = srvaEventDTOTransformer.transform(slice.getContent());
        return new SliceImpl<>(dtos, pageRequest, slice.hasNext());
    }

    private Slice<SrvaEvent> getSlice(final SrvaEventSearchDTO dto, final Pageable pageRequest) {
        final List<SrvaEvent> result = getQuery(dto).offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();
        return BaseRepositoryImpl.toSlice(result, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<SrvaEventDTO> search(final SrvaEventSearchDTO dto) {
        assertCurrentRhyId(dto);

        final List<SrvaEvent> srvaEvents = getQuery(dto).fetch();

        return srvaEventDTOTransformer.apply(srvaEvents);
    }

    @Transactional(readOnly = true)
    public List<SrvaEventExportExcelDTO> searchExcel(final SrvaEventSearchDTO dto) {
        assertCurrentRhyId(dto);

        final List<SrvaEvent> srvaEvents = getQuery(dto).fetch();

        return srvaEvents.stream().map(srvaEvent ->
                SrvaEventExportExcelDTO.create(srvaEvent, enumLocaliser)).collect(Collectors.toList());
    }

    private void assertCurrentRhyId(final SrvaEventSearchDTO dto) {
        if (!(activeUserService.isModeratorOrAdmin() && dto.isModeratorView())) {
            Preconditions.checkArgument(dto.getCurrentRhyId() != null, "currentRhyId cannot be null");
        }
    }

    private JPQLQuery<SrvaEvent> getQuery(final SrvaEventSearchDTO dto) {
        final Riistanhoitoyhdistys currentRhy = dto.getCurrentRhyId() != null
                ? requireEntityService.requireRiistanhoitoyhdistys(dto.getCurrentRhyId(), RhyPermission.LIST_SRVA)
                : null;

        final QSrvaEvent SRVA = QSrvaEvent.srvaEvent;

        final List<SrvaEventNameEnum> eventNames = Optional.ofNullable(dto.getEventNames())
                .orElseGet(ArrayList::new);
        final BooleanExpression eventNamesRestriction = SRVA.eventName.in(eventNames);

        final BooleanExpression rhyRestriction = currentRhy != null ? SRVA.rhy.eq(currentRhy) : null;

        final List<BooleanExpression> eventTypeRestrictions = new ArrayList<>();
        if (dto.getEventTypes() != null) {
            dto.getEventTypes().forEach(eventType ->
                    eventTypeRestrictions.add(SRVA.eventName.eq(ACCIDENT).and(SRVA.eventType.eq(eventType))));
        }

        final BooleanExpression accidentSubtypeRestriction = !F.isNullOrEmpty(dto.getEventTypes()) ?
                eventTypeRestrictions.stream().reduce(BooleanExpression::or).get() :
                null;

        final JPQLQuery<SrvaEvent> q = jpqlQueryFactory.select(SRVA)
                .from(SRVA)
                .where(eventNamesRestriction.and(rhyRestriction).or(accidentSubtypeRestriction))
                .orderBy(SRVA.id.asc());

        if (Objects.nonNull(dto.getRhyCode())) {
            final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findByOfficialCode(dto.getRhyCode());
            q.where(SRVA.rhy.eq(rhy));
        } else if (Objects.nonNull(dto.getRkaCode())) {
            final Organisation rka = organisationRepository.findByTypeAndOfficialCode(OrganisationType.RKA, dto.getRkaCode());
            final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
            q.join(SRVA.rhy, RHY);
            q.where(RHY.parentOrganisation.eq(rka));
        } else if (Objects.nonNull(dto.getHtaCode())) {
            final GISHirvitalousalue hta = hirvitalousalueRepository.findByNumber(dto.getHtaCode());
            q.where(SRVA.geom.intersects(hta.getGeom()));
        }

        if (Objects.nonNull(dto.getStates())) {
            q.where(SRVA.state.in(dto.getStates()));
        }

        if (Objects.nonNull(dto.getGameSpeciesCode())) {
            // 0 stands for other species
            if (dto.getGameSpeciesCode() == 0) {
                q.where(SRVA.species.isNull());
            } else {
                final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
                q.where(SRVA.species.eq(gameSpecies));
            }
        }

        if (dto.hasBeginDate()) {
            q.where(SRVA.pointOfTime.goe(DateUtil.toDateTimeNullSafe(dto.getBeginDate())));
        }
        if (dto.hasEndDate()) {
            q.where(SRVA.pointOfTime.lt(DateUtil.toDateTimeNullSafe(dto.getEndDate().plusDays(1))));
        }

        return q;
    }
}
