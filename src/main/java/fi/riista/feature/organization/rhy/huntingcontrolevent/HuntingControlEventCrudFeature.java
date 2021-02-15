package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;

@Service
public class HuntingControlEventCrudFeature extends AbstractCrudFeature<Long, HuntingControlEvent, HuntingControlEventDTO> {

    @Resource
    private HuntingControlEventRepository eventRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private HuntingControlEventDTOTransformer dtoTransformer;

    @Resource
    private HuntingControlEventAttachmentService attachmentService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingControlEventRepository huntingControlEventRepository;

    @Resource
    private HuntingControlEventDTOTransformer huntingControlEventDtoTransformer;

    @Override
    protected JpaRepository<HuntingControlEvent, Long> getRepository() {
        return eventRepository;
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public HuntingControlEventDTO create(final HuntingControlEventDTO dto) {
        return super.create(dto);
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public HuntingControlEventDTO update(final HuntingControlEventDTO dto) {
        return super.update(dto);
    }

    @Override
    protected void updateEntity(final HuntingControlEvent entity, final HuntingControlEventDTO dto) {

        final LocalDate eventDate = dto.getDate();
        final LocalTime beginTime = dto.getBeginTime();
        final LocalTime endTime = dto.getEndTime();

        RhyEventTimeException.assertEventNotTooFarInPast(eventDate, activeUserService.isModeratorOrAdmin());
        RhyEventTimeException.assertEventNotInFuture(eventDate);
        RhyEventTimeException.assertBeginTimeNotAfterEndTime(beginTime, endTime);

        if (entity.isNew()) {
            entity.setRhy(rhyRepository.getOne(dto.getRhy().getId()));
        }

        entity.setTitle(dto.getTitle());
        entity.setInspectorCount(dto.getInspectorCount());
        entity.setCooperationType(dto.getCooperationType());
        entity.setWolfTerritory(dto.getWolfTerritory());
        entity.setInspectors(dto.getInspectors());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setDate(dto.getDate());
        entity.setBeginTime(dto.getBeginTime());
        entity.setEndTime(dto.getEndTime());
        entity.setCustomers(dto.getCustomers());
        entity.setProofOrders(dto.getProofOrders());
        entity.setDescription(dto.getDescription());

        if (!entity.isNew()) {
            attachmentService.addAttachments(entity, dto.getNewAttachments());
        }
    }

    @Override
    protected HuntingControlEventDTO toDTO(@Nonnull final HuntingControlEvent entity) {
        return dtoTransformer.apply(entity);
    }

    @Override
    protected void afterCreate(final HuntingControlEvent event, final HuntingControlEventDTO dto) {
        attachmentService.addAttachments(event, dto.getNewAttachments());
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public void delete(final Long id) {
        attachmentService.deleteAttachmentsFromEvent(id);
        super.delete(id);
    }

    @Transactional(readOnly = true)
    public List<HuntingControlEventDTO> listHuntingControlEvents(final long rhyId, final int year) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_HUNTING_CONTROL_EVENTS);
        final LocalDate startTime = new LocalDate(year, 1, 1);
        final LocalDate endTime = new LocalDate(year, 12, 31);

        return huntingControlEventDtoTransformer.apply(huntingControlEventRepository.findByRhyAndDateBetweenOrderByDateDesc(rhy, startTime, endTime));
    }
}
