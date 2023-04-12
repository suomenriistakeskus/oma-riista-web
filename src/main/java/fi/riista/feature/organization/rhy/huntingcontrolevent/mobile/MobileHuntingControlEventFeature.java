package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import com.google.common.collect.Sets;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.account.AccountShootingTestService;
import fi.riista.feature.account.mobile.MobileOrganisationDTO;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventChangeService;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventRepository;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;

import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_HUNTING_CONTROL_EVENTS;
import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.stream.Collectors.toList;

@Service
public class MobileHuntingControlEventFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MobileHuntingControlEventFeature.class);


    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingControlEventRepository huntingControlEventRepository;

    @Resource
    private MobileHuntingControlEventDTOTransformer eventDTOTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private HuntingControlEventChangeService changeService;

    @Resource
    private MobileHuntingControlHelper helper;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private AccountShootingTestService shootingTestService;

    // Event

    @Transactional(readOnly = true)
    public List<MobileHuntingControlRhyDTO> getEvents(final LocalDateTime modifiedAfter,
                                                      final MobileHuntingControlSpecVersion specVersion) {

        final Person authenticatedPerson = activeUserService.requireActivePerson();
        final List<Occupation> occupations = occupationRepository.findActiveByPerson(authenticatedPerson);
        final Function<Occupation, Organisation> organisationMapper =
                singleQueryFunction(occupations, Occupation::getOrganisation, organisationRepository, true);

        return occupations.stream()
                .filter(occ -> occ.getOccupationType().equals(OccupationType.METSASTYKSENVALVOJA))
                .map(occ -> MobileHuntingControlRhyDTO.create(
                        specVersion,
                        MobileOrganisationDTO.create(organisationMapper.apply(occ)),
                        findRhyGameWardens(organisationMapper.apply(occ), occ),
                        findEvents(occ, authenticatedPerson, limitSearchDate(modifiedAfter), specVersion)))
                .collect(toList());
    }

    @Transactional
    public MobileHuntingControlEventDTO createEvent(final long rhyId,
                                                    final MobileHuntingControlEventDTO dto,
                                                    final MobileHuntingControlSpecVersion requestedSpecVersion) {

        final Person creator = activeUserService.requireActivePerson();
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, LIST_HUNTING_CONTROL_EVENTS);

        helper.assertValidGameWarden(rhy, dto.getDate());
        assertValidSpecVersion(dto);
        assertValidRefId(dto);

        final HuntingControlEvent event =  huntingControlEventRepository.findByMobileClientRefId(dto.getMobileClientRefId());

        return event == null
                ? eventDTOTransformer.transform(createEvent(dto, rhy, creator), requestedSpecVersion)
                : eventDTOTransformer.transform(event, requestedSpecVersion);

    }

    @Transactional
    public MobileHuntingControlEventDTO updateEvent(final MobileHuntingControlEventDTO dto,
                                                    final MobileHuntingControlSpecVersion requestedSpecVersion) {

        final Person authenticatedPerson = activeUserService.requireActivePerson();

        final HuntingControlEvent event =  huntingControlEventRepository.findById(dto.getId()).get();
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(event.getRhy().getId(), LIST_HUNTING_CONTROL_EVENTS);

        helper.assertValidGameWarden(rhy, dto.getDate());
        DtoUtil.assertNoVersionConflict(event, dto);
        helper.assertPersonIsEventInspector(event, authenticatedPerson);
        helper.assertEventIsEditable(event);

        updateEntity(event, dto);
        changeService.addModify(event, null);
        final HuntingControlEvent persisted = huntingControlEventRepository.saveAndFlush(event);

        return eventDTOTransformer.transform(persisted, requestedSpecVersion);
    }

    @Transactional(readOnly = true)
    public MobileHuntingControlHunterInfoDTO getHunterInfoByHunterNumber(final String hunterNumber) {
        final Person authenticatedPerson = activeUserService.requireActivePerson();
        if (!userAuthorizationHelper.isGameWardenOnAnyRhy(authenticatedPerson)) {
            throw new AccessDeniedException("No permission");
        }

        final Person person = personLookupService.findByHunterNumber(hunterNumber, true)
                .orElseThrow(() -> PersonNotFoundException.byHunterNumber(hunterNumber));
        return createHunterInfo(person);
    }

    @Transactional(readOnly = true)
    public MobileHuntingControlHunterInfoDTO getHunterInfoBySsn(final String ssn) {
        final Person authenticatedPerson = activeUserService.requireActivePerson();
        if (!userAuthorizationHelper.isGameWardenOnAnyRhy(authenticatedPerson)) {
            throw new AccessDeniedException("No permission");
        }

        final Person person = personLookupService.findBySsnNoFallback(ssn)
                .orElseThrow(() -> PersonNotFoundException.bySsn(ssn));
        return createHunterInfo(person);
    }

    private MobileHuntingControlHunterInfoDTO createHunterInfo(final Person person) {
        final List<AccountShootingTestDTO> shootingTests = shootingTestService.listQualifiedShootingTests(
                person,
                LocaleContextHolder.getLocale());

        final LocalDate paymentDate = person.getHuntingPaymentDateForHuntingYear(DateUtil.huntingYear()).orElse(null);

        final MobileHuntingControlHunterInfoDTO dto = MobileHuntingControlHunterInfoDTO.create(
                person.getFullName(),
                person.parseDateOfBirth(),
                person.getHomeMunicipalityName().asMap(),
                person.getHunterNumber(),
                isHuntingCardActive(person),
                paymentDate,
                shootingTests);
        return dto;
    }

    private LocalDateTime limitSearchDate(final LocalDateTime searchDate) {
        final LocalDateTime minSearchDateLimit = DateUtil.toLocalDateTimeNullSafe(minSearchDateTime());
        return searchDate == null || searchDate.isBefore(minSearchDateLimit)
                ? minSearchDateLimit
                : searchDate;
    }

    private boolean isHuntingCardActive(final Person person) {
        return DateUtil.rangeFrom(person.getHuntingCardStart(), person.getHuntingCardEnd()).contains(DateUtil.today());
    }

    private DateTime minSearchDateTime() {
        return DateUtil.beginOfCalendarYear(DateUtil.now().minusYears(1).getYear());
    }

    private List<MobileHuntingControlEventDTO> findEvents(final Occupation occ,
                                                          final Person inspector,
                                                          final LocalDateTime modifiedAfter,
                                                          final MobileHuntingControlSpecVersion specVersion) {

        final Organisation org = occ.getOrganisation();
        requireEntityService.requireRiistanhoitoyhdistys(org.getId(), LIST_HUNTING_CONTROL_EVENTS);

        final DateTime after = DateUtil.toDateTimeNullSafe(modifiedAfter);
        final List<HuntingControlEvent> events =
                huntingControlEventRepository.findByRhyAndInspectorAndModifiedAfterOrder(org, inspector, after);

        return eventDTOTransformer.transform(events, specVersion);
    }

    private List<MobileGameWardenDTO> findRhyGameWardens(final Organisation rhy, final Occupation inspector) {
        final LocalDate beginDate = inspector.getBeginDate() == null
                ? DateUtil.toLocalDateNullSafe(minSearchDateTime())
                : inspector.getBeginDate();
        final LocalDate endDate = inspector.getEndDate() == null
                ? DateUtil.today().plusMonths(6)
                : inspector.getEndDate();
        final List<Occupation> gameWardens = occupationRepository.findByOrganisationAndTypeAndBetweenDates(
                rhy, OccupationType.METSASTYKSENVALVOJA, beginDate, endDate)
                .stream()
                .filter(occ -> occ.getLifecycleFields().getDeletionTime() == null
                        || !beginDate.isAfter(occ.getLifecycleFields().getDeletionTime().toLocalDate()))
                .collect(toList());
        final Function<Occupation, Person> personMapper =
                singleQueryFunction(gameWardens, Occupation::getPerson, personRepository, true);

        return gameWardens.stream()
                .map(gameWarden -> MobileGameWardenDTO.create(gameWarden, personMapper.apply(gameWarden), inspector))
                .collect(toList());
    }

    private HuntingControlEvent createEvent(final MobileHuntingControlEventDTO dto,
                                            final Riistanhoitoyhdistys rhy,
                                            final Person creator) {

        LOG.debug("Mobile createEvent: refId={}, rhyId={}", dto.getMobileClientRefId(), rhy.getId());

        final HuntingControlEvent entity = new HuntingControlEvent();

        final LocalDate eventDate = dto.getDate();
        final LocalTime beginTime = dto.getBeginTime();
        final LocalTime endTime = dto.getEndTime();

        RhyEventTimeException.assertEventNotTooFarInPast(eventDate, false);
        RhyEventTimeException.assertEventNotInFuture(eventDate);
        RhyEventTimeException.assertBeginTimeNotAfterEndTime(beginTime, endTime);

        entity.setMobileClientRefId(dto.getMobileClientRefId());
        entity.setRhy(rhy);
        entity.setStatus(HuntingControlEventStatus.PROPOSED);

        updateEntity(entity, dto);
        entity.getInspectors().add(creator); // Make sure that event creator is an inspector

        final HuntingControlEvent persisted = huntingControlEventRepository.saveAndFlush(entity);

        changeService.addCreate(persisted);
        return persisted;
    }

    private void updateEntity(final HuntingControlEvent entity, final MobileHuntingControlEventDTO dto) {
        entity.setEventType(dto.getEventType());
        entity.setCooperationTypes(dto.getCooperationTypes());
        entity.setInspectorCount(dto.getInspectors().size());
        entity.setWolfTerritory(dto.isWolfTerritory());
        entity.setOtherParticipants(dto.getOtherParticipants());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setLocationDescription(dto.getLocationDescription());
        entity.setDate(dto.getDate());
        entity.setBeginTime(dto.getBeginTime());
        entity.setEndTime(dto.getEndTime());
        entity.setCustomers(dto.getCustomers());
        entity.setProofOrders(dto.getProofOrders());
        entity.setDescription(dto.getDescription());

        // TODO: Check that persons really are game warden at the time in the RHY
        final List<Long> inspectorsPersonIds = dto.getInspectors().stream()
                .map(MobileHuntingControlInspectorDTO::getId)
                .collect(toList());
        final List<Person> inspectors = personRepository.findAllById(inspectorsPersonIds);
        entity.setInspectors(Sets.newHashSet(inspectors));
    }

    private static void assertValidSpecVersion(final MobileHuntingControlEventDTO dto) {
        Objects.requireNonNull(dto);
        if (dto.getSpecVersion() == null) {
            throw new MessageExposableValidationException("specVersion must not be null");
        }
    }

    private static void assertValidRefId(final MobileHuntingControlEventDTO dto) {
        Objects.requireNonNull(dto);
        if (dto.getMobileClientRefId() == null) {
            throw new MessageExposableValidationException("mobileClientRefId must not be null");
        }
    }

}
