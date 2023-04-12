package fi.riista.integration.srva;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.srva.QSrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.gamediary.srva.SrvaJpaUtils;
import fi.riista.feature.gamediary.srva.SrvaSpecs;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethodRepository;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.integration.srva.dto.SrvaPublicExportDTO;
import fi.riista.integration.srva.rvr.RVR_GameAgeEnum;
import fi.riista.integration.srva.rvr.RVR_GameGenderEnum;
import fi.riista.integration.srva.rvr.RVR_GeoLocation;
import fi.riista.integration.srva.rvr.RVR_SourceEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEvent;
import fi.riista.integration.srva.rvr.RVR_SrvaEventNameEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEventResultDetailsEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEventTypeDetailsEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEventTypeEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEvents;
import fi.riista.integration.srva.rvr.RVR_SrvaMethodEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaResultEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaSpecimen;
import fi.riista.util.DateUtil;
import fi.riista.util.JaxbUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.security.access.prepost.PreAuthorize;

import static java.util.stream.Collectors.toList;

@Service
public class SrvaExportFeature {

    private static final int MIN_YEAR = 2017;

    @Resource(name = "srvaRvrExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepo;

    @Resource
    private SrvaSpecimenRepository srvaSpecimenRepo;

    @Resource
    private SrvaMethodRepository srvaMethodRepo;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepo;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Value("#{environment['git.commit.id.describe']}")
    private String version;

    private final LoadingCache<Integer, List<SrvaPublicExportDTO>> publicDtoCache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(1, TimeUnit.MINUTES)
                    .build(new CacheLoader<Integer, List<SrvaPublicExportDTO>>() {
                        @Override
                        public List<SrvaPublicExportDTO> load(final Integer calendarYear) {
                            return doLoadPublicResult(calendarYear);
                        }
                    });

    // PUBLIC EXPORT

    @Transactional(readOnly = true)
    public List<SrvaPublicExportDTO> exportPublic(final int calendarYear) {
        if (calendarYear < MIN_YEAR || calendarYear > DateUtil.today().getYear()) {
            return Collections.emptyList();
        }

        try {
            return publicDtoCache.get(calendarYear);
        } catch (final ExecutionException ee) {
            throw new RuntimeException(ee);
        }

    }

    // For testing
    /*package*/ void invalidatePublicDtoCache() {
        publicDtoCache.invalidateAll();
    }

    private List<SrvaPublicExportDTO> doLoadPublicResult(final int calendarYear) {
        final QSrvaEvent SRVA_EVENT = QSrvaEvent.srvaEvent;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final DateTime yearStart = new LocalDate(calendarYear, 1, 1).toDateTimeAtStartOfDay();
        final DateTime yearEnd = new LocalDate(calendarYear + 1, 1, 1).toDateTimeAtStartOfDay();
        final List<SrvaEvent> srvaEvents = jpqlQueryFactory.selectFrom(SRVA_EVENT)
                .join(SRVA_EVENT.species, SPECIES)
                .where(SRVA_EVENT.state.eq(SrvaEventStateEnum.APPROVED))
                .where(SRVA_EVENT.eventName.eq(SrvaEventNameEnum.ACCIDENT))
                .where(SRVA_EVENT.eventType.in(SrvaEventTypeEnum.TRAFFIC_ACCIDENT,
                        SrvaEventTypeEnum.RAILWAY_ACCIDENT))
                .where(SRVA_EVENT.pointOfTime.between(yearStart, yearEnd))
                .where(SPECIES.officialCode.in(
                        GameSpecies.OFFICIAL_CODE_MOOSE,
                        GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER,
                        GameSpecies.OFFICIAL_CODE_FALLOW_DEER,
                        GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER,
                        GameSpecies.OFFICIAL_CODE_ROE_DEER,
                        GameSpecies.OFFICIAL_CODE_WILD_BOAR))
                .fetch();

        // To prevent too many parameters (2^15 is max) to SQL IN clause, process in smaller batches
        return Lists.partition(srvaEvents, 4096).stream()
                .flatMap(this::transformEventsToPublicDtoStream)
                .collect(toList());

    }

    private Stream<SrvaPublicExportDTO> transformEventsToPublicDtoStream(final List<SrvaEvent> srvaEvents) {
        final Function<SrvaEvent, GameSpecies> srvaEventToSpecies =
                SrvaJpaUtils.getSrvaEventToSpeciesMapping(srvaEvents, gameSpeciesRepo);
        final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens =
                SrvaJpaUtils.getSpecimensGroupedBySrvaEvent(srvaEvents, srvaSpecimenRepo);

        return srvaEvents.stream()
                .map(event -> SrvaPublicExportDTO.create(
                        event,
                        Optional.ofNullable(srvaEventToSpecies.apply(event)).map(GameSpecies::getOfficialCode).orElse(null),
                        Optional.ofNullable(groupedSpecimens.get(event)).map(SrvaSpecimenDTO::create).orElse(null)));
    }

    // RVR EXPORT

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_SRVA_RVR')")
    public String exportRVRV1Xml(final Optional<Integer> calendarYear) {
        return JaxbUtils.marshalToString(exportRVRV1(calendarYear), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_SRVA_RVR')")
    public String exportRVRV2Xml(final Optional<Integer> calendarYear) {
        return JaxbUtils.marshalToString(exportRVRV2(calendarYear), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_SRVA_RVR')")
    public RVR_SrvaEvents exportRVRV1(final Optional<Integer> calendarYear) {
        final List<SrvaEvent> srvaEvents = calendarYear
                .map(this::getApprovedWithinCalendarYear)
                .orElseGet(this::getAllApproved);
        return mapEntities(srvaEvents, false);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_SRVA_RVR')")
    public RVR_SrvaEvents exportRVRV2(final Optional<Integer> calendarYear) {
        final List<SrvaEvent> srvaEvents = calendarYear
                .map(this::getApprovedWithinCalendarYear)
                .orElseGet(this::getAllApproved);
        return mapEntities(srvaEvents, true);
    }

    private RVR_SrvaEvents mapEntities(final List<SrvaEvent> srvaEvents, final boolean includeV2Parameters) {
        // To prevent too many parameters (2^15 is max) to SQL IN clause, process in smaller batches
        final List<RVR_SrvaEvent> rvrEventList = Lists.partition(srvaEvents, 4096).stream()
                .flatMap(events -> transformToRVREventStream(events, includeV2Parameters))
                .collect(toList());

        final RVR_SrvaEvents rvrEvents = new RVR_SrvaEvents();
        rvrEvents.setTimeStamp(DateTime.now());
        rvrEvents.setVersion(version);
        rvrEvents.setSrvaEvent(rvrEventList);
        return rvrEvents;
    }


    private List<SrvaEvent> getAllApproved() {
        return srvaEventRepository.findAll(SrvaSpecs.equalState(SrvaEventStateEnum.APPROVED));
    }

    private List<SrvaEvent> getApprovedWithinCalendarYear(final int year) {
        final DateTime yearStart = new LocalDate(year, 1, 1).toDateTimeAtStartOfDay();
        final DateTime yearEnd = new LocalDate(year + 1, 1, 1).toDateTimeAtStartOfDay();

        final List<SrvaEvent> srvaEvents =
                srvaEventRepository.findAll(SrvaSpecs.equalState(SrvaEventStateEnum.APPROVED)
                        .and(SrvaSpecs.withinInterval(new Interval(yearStart, yearEnd))));
        return srvaEvents;
    }

    private Stream<? extends RVR_SrvaEvent> transformToRVREventStream(final List<SrvaEvent> srvaEvents,
                                                                      final boolean includeV2Parameters) {
        final Function<SrvaEvent, GameSpecies> srvaEventToSpecies =
                SrvaJpaUtils.getSrvaEventToSpeciesMapping(srvaEvents, gameSpeciesRepo);
        final Function<SrvaEvent, Riistanhoitoyhdistys> srvaEventToRhy =
                SrvaJpaUtils.getSrvaEventToRhyMapping(srvaEvents, riistanhoitoyhdistysRepo);
        final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens =
                SrvaJpaUtils.getSpecimensGroupedBySrvaEvent(srvaEvents, srvaSpecimenRepo);
        final Map<SrvaEvent, List<SrvaMethod>> groupedMethods = SrvaJpaUtils.getMethodsGroupedBySrvaEvent(srvaEvents,
                srvaMethodRepo);

        return constructExportData(srvaEvents, srvaEventToSpecies, srvaEventToRhy, groupedSpecimens, groupedMethods, includeV2Parameters);
    }

    private static Stream<RVR_SrvaEvent> constructExportData(final List<SrvaEvent> events,
                                                             final Function<SrvaEvent, GameSpecies> srvaEventToSpecies,
                                                             final Function<SrvaEvent, Riistanhoitoyhdistys> srvaEventToRhy,
                                                             final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens,
                                                             final Map<SrvaEvent, List<SrvaMethod>> groupedMethods,
                                                             final boolean includeV2Parameters) {

        return events.stream().map(event -> {
            RVR_SrvaEvent rvrEvent = new RVR_SrvaEvent();
            rvrEvent.setEventName(createRvrEnum(RVR_SrvaEventNameEnum.class, event.getEventName()));
            rvrEvent.setEventType(createRvrEnum(RVR_SrvaEventTypeEnum.class, event.getEventType()));
            rvrEvent.setOtherTypeDescription(event.getOtherTypeDescription());
            rvrEvent.setId(event.getId());
            rvrEvent.setRev(event.getConsistencyVersion());
            rvrEvent.setGeoLocation(createRvrGeoLocation(event.getGeoLocation()));
            rvrEvent.setPointOfTime(event.getPointOfTime());
            rvrEvent.setOtherSpeciesDescription(event.getOtherSpeciesDescription());
            rvrEvent.setTotalSpecimenAmount(event.getTotalSpecimenAmount());
            rvrEvent.setPersonCount(event.getPersonCount());
            rvrEvent.setTimeSpent(event.getTimeSpent());
            rvrEvent.setOtherMethodDescription(event.getOtherMethodDescription());
            rvrEvent.setEventResult(createRvrEnum(RVR_SrvaResultEnum.class, event.getEventResult()));
            rvrEvent.setDescription(event.getDescription());
            rvrEvent.setSpecimens(createRvrSpecimens(groupedSpecimens.get(event)));
            if (includeV2Parameters) {
                rvrEvent.setMethods(createRvrMethods(groupedMethods.get(event)));
            } else {
                rvrEvent.setMethods(getV1Methods(groupedMethods.get(event)));
            }

            final Riistanhoitoyhdistys rhy = srvaEventToRhy.apply(event);
            rvrEvent.setRhyOfficialCode(rhy.getOfficialCode());
            rvrEvent.setRhyHumanReadableName(rhy.getNameFinnish());

            final Optional<GameSpecies> species = Optional.ofNullable(srvaEventToSpecies.apply(event));
            rvrEvent.setGameSpeciesOfficialCode(species.map(GameSpecies::getOfficialCode).orElse(null));
            rvrEvent.setGameSpeciesHumanReadableName(species.map(GameSpecies::getNameFinnish).orElse(null));

            if (includeV2Parameters) {
                rvrEvent.setDeportationOrderNumber(event.getDeportationOrderNumber());
                rvrEvent.setEventTypeDetail(createRvrEnum(RVR_SrvaEventTypeDetailsEnum.class, event.getEventTypeDetail()));
                rvrEvent.setOtherEventTypeDetailDescription(event.getOtherEventTypeDetailDescription());
                rvrEvent.setEventResultDetail(createRvrEnum(RVR_SrvaEventResultDetailsEnum.class, event.getEventResultDetail()));
            }
            return rvrEvent;
        });
    }

    private static RVR_SrvaEvent.RVR_Methods getV1Methods(final List<SrvaMethod> methods) {
        if (methods == null) {
            return null;
        }

        // Count how many V2 methods are enabled
        final long v2Methods = methods.stream().filter(
                method -> (method.getName() == SrvaMethodEnum.CHASING_WITH_PEOPLE && method.isChecked()) ||
                        (method.getName() == SrvaMethodEnum.VEHICLE && method.isChecked())
        ).count();

        final long otherMethods = methods.stream().filter(
                method -> (method.getName() == SrvaMethodEnum.OTHER)).count();

        // If there are v2 methods and no OTHER method then add OTHER
        if (v2Methods > 0 && otherMethods == 0) {
            final SrvaMethod method = new SrvaMethod();
            method.setName(SrvaMethodEnum.OTHER);
            methods.add(method);
        }

        // Filter V2 methods away and if any of them was set, then enable OTHER method
        return createRvrMethods(
                methods.stream()
                        .filter(method -> method.getName() != SrvaMethodEnum.CHASING_WITH_PEOPLE &&
                                method.getName() != SrvaMethodEnum.VEHICLE)
                        .map(method -> {
                            if (method.getName() == SrvaMethodEnum.OTHER && v2Methods > 0) {
                                method.setChecked(true);
                                return method;
                            } else {
                                return method;
                            }
                        }).collect(toList()));
    }

    private static RVR_SrvaEvent.RVR_Specimens createRvrSpecimens(final List<SrvaSpecimen> srvaSpecimens) {
        if (srvaSpecimens == null || srvaSpecimens.isEmpty()) {
            return null;
        }

        final RVR_SrvaEvent.RVR_Specimens rvrSpecimens = new RVR_SrvaEvent.RVR_Specimens();
        rvrSpecimens.setSpecimen(srvaSpecimens.stream().map(srvaSpecimen -> {
            RVR_SrvaSpecimen specimen = new RVR_SrvaSpecimen();
            specimen.setGender(createRvrEnum(RVR_GameGenderEnum.class, srvaSpecimen.getGender()));
            specimen.setAge(createRvrEnum(RVR_GameAgeEnum.class, srvaSpecimen.getAge()));
            return specimen;

        }).collect(toList()));

        return rvrSpecimens;
    }

    private static RVR_SrvaEvent.RVR_Methods createRvrMethods(final List<SrvaMethod> srvaMethods) {
        if (srvaMethods == null || srvaMethods.isEmpty()) {
            return null;
        }

        final RVR_SrvaEvent.RVR_Methods rvrMethods = new RVR_SrvaEvent.RVR_Methods();
        rvrMethods.setMethod(srvaMethods.stream()
                .filter(SrvaMethod::isChecked)
                .map(srvaMethodDTO -> createRvrEnum(RVR_SrvaMethodEnum.class, srvaMethodDTO.getName()))
                .collect(toList()));
        // if all methods are filtered(isChecked = false) return null to avoid empty methods element
        return rvrMethods.getMethod().isEmpty() ? null : rvrMethods;
    }

    private static <E extends Enum<E>> E createRvrEnum(final Class<E> clazz, final Enum<?> e) {
        return e == null ? null : Enum.valueOf(clazz, e.name());
    }

    private static RVR_GeoLocation createRvrGeoLocation(final GeoLocation geoLocation) {
        final RVR_GeoLocation loc = new RVR_GeoLocation();
        loc.setLatitude(geoLocation.getLatitude());
        loc.setLongitude(geoLocation.getLongitude());
        loc.setSource(createRvrEnum(RVR_SourceEnum.class, geoLocation.getSource()));
        loc.setAccuracy(geoLocation.getAccuracy());

        return loc;
    }
}
