package fi.riista.feature.organization.rhy.huntingcontrolevent;

import com.google.common.collect.Sets;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.security.EntityPermission;
import java.util.Set;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import static java.util.stream.Collectors.toList;

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
    private HuntingControlEventDTOTransformer huntingControlEventDtoTransformer;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingControlEventChangeService changeService;

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
            entity.setStatus(HuntingControlEventStatus.PROPOSED);
        }

        entity.setEventType(dto.getEventType());
        // Note! Title is not set for a purpose.
        entity.setCooperationTypes(dto.getCooperationTypes());
        entity.setInspectorCount(dto.getInspectorCount());
        entity.setWolfTerritory(dto.getWolfTerritory());
        entity.setOtherParticipants(dto.getOtherParticipants());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setLocationDescription(dto.getLocationDescription());
        entity.setDate(dto.getDate());
        entity.setBeginTime(dto.getBeginTime());
        entity.setEndTime(dto.getEndTime());
        entity.setCustomers(dto.getCustomers());
        entity.setProofOrders(dto.getProofOrders());
        entity.setDescription(dto.getDescription());

        final List<Long> inspectorsPersonIds = dto.getInspectors().stream()
                .map(HuntingControlInspectorDTO::getId)
                .collect(toList());
        final List<Person> inspectors = personRepository.findAllById(inspectorsPersonIds);
        entity.setInspectors(Sets.newHashSet(inspectors));
    }

    @Override
    protected HuntingControlEventDTO toDTO(@Nonnull final HuntingControlEvent entity) {
        return dtoTransformer.apply(entity);
    }

    @Override
    protected void afterCreate(final HuntingControlEvent event, final HuntingControlEventDTO dto) {
        final List<MultipartFile> attachments = dto.getNewAttachments();
        attachmentService.addAttachments(event, attachments);
        changeService.addCreate(event);
        changeService.addNewAttachments(event, attachments);
    }

    @Override
    protected void afterUpdate(final HuntingControlEvent event, final HuntingControlEventDTO dto) {
        attachmentService.addAttachments(event, dto.getNewAttachments());
        changeService.addNewAttachments(event, dto.getNewAttachments());
        changeService.addModify(event, dto.getReasonForChange());
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public void delete(final Long id) {
        attachmentService.deleteAttachmentsFromEvent(id);
        super.delete(id);
    }

    @Transactional(readOnly = true)
    public List<HuntingControlEventDTO> listHuntingControlEvents(final long rhyId, final int year) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);
        final LocalDate startTime = new LocalDate(year, 1, 1);
        final LocalDate endTime = new LocalDate(year, 12, 31);

        return huntingControlEventDtoTransformer.apply(eventRepository.findByRhyAndDateBetweenOrderByDateDesc(rhy, startTime, endTime));
    }

    @Transactional(readOnly = true)
    public List<Integer> listAvailableYears(final long rhyId) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);
        return eventRepository.listEventYears(rhy);
    }

    @Transactional(readOnly = true)
    public List<Integer> listAvailableYearsForActiveUser(final long rhyId) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_HUNTING_CONTROL_EVENTS);
        final Person person = activeUserService.requireActivePerson();

        return eventRepository.listEventYears(rhy, person);
    }

    @Transactional(readOnly = true)
    public List<HuntingControlEventDTO> listHuntingControlEventsForActiveUser(final long rhyId, final int year) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_HUNTING_CONTROL_EVENTS);
        final Person person = activeUserService.requireActivePerson();

        final List<HuntingControlEvent> events = eventRepository.findByRhyAndYearAndInspectorOrderByDateDesc(rhy, year, person);
        return huntingControlEventDtoTransformer.apply(events);
    }

    @Transactional(readOnly = true)
    public ActiveGameWardensDTO listAllActiveGameWardens(final long rhyId, final LocalDate date) {

        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_HUNTING_CONTROL_EVENTS);

        final List<Person> activeGameWardensAtDate  = occupationRepository.findActiveByOrganisationAndOccupationTypeAndDate(rhy, OccupationType.METSASTYKSENVALVOJA, date).stream()
                .map(Occupation::getPerson)
                .collect(toList());

        final boolean isModeratorOrAdminOrCoordinator = activeUserService.isModeratorOrAdmin()
                || userAuthorizationHelper.isCoordinator(rhy);

        if (!isModeratorOrAdminOrCoordinator && !userAuthorizationHelper.isGameWardenValidOn(rhy, date)) {
            // Game warden must have active nomination at the time for listing others
            return ActiveGameWardensDTO.createWithNoActiveNomination();
        }

        return ActiveGameWardensDTO.create(
                activeGameWardensAtDate.stream()
                        .map(HuntingControlInspectorDTO::create)
                        .collect(toList()));
    }

    @Transactional(rollbackFor = IOException.class)
    public HuntingControlEventDTO changeStatus(final Long id, final HuntingControlEventStatus newStatus) {
        final HuntingControlEvent entity = eventRepository.findById(id).orElseThrow(NotFoundException::new);
        requireEntityService.requireRiistanhoitoyhdistys(entity.getRhy().getId(), EntityPermission.UPDATE);
        entity.setStatus(newStatus);
        changeService.addChangeStatus(entity, newStatus.name());
        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('VIEW_HUNTING_CONTROL_EVENTS')")
    public List<HuntingControlEventDTO> searchModerator(final HuntingControlEventSearchParametersDTO params) {
        final List<HuntingControlEvent> events = eventRepository.findReportEvents(params);
        return huntingControlEventDtoTransformer.apply(events);
    }
}
