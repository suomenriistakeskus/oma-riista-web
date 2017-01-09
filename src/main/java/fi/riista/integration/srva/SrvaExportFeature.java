package fi.riista.integration.srva;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.gamediary.srva.method.SrvaMethodRepository;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenRepository;
import fi.riista.feature.gamediary.srva.SrvaJpaUtils;
import fi.riista.feature.gamediary.srva.SrvaSpecs;
import fi.riista.integration.srva.dto.SrvaPublicExportDTO;
import fi.riista.integration.srva.rvr.RVR_GameAgeEnum;
import fi.riista.integration.srva.rvr.RVR_GameGenderEnum;
import fi.riista.integration.srva.rvr.RVR_GeoLocation;
import fi.riista.integration.srva.rvr.RVR_SourceEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEvent;
import fi.riista.integration.srva.rvr.RVR_SrvaEventNameEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEventTypeEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaEvents;
import fi.riista.integration.srva.rvr.RVR_SrvaMethodEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaResultEnum;
import fi.riista.integration.srva.rvr.RVR_SrvaSpecimen;
import fi.riista.util.JaxbUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SrvaExportFeature {

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

    @Value("#{environment['git.commit.id.describe']}")
    private String version;

    @Transactional(readOnly = true)
    public List<SrvaPublicExportDTO> exportPublic() {
        final List<SrvaEvent> srvaEvents = srvaEventRepository.findAll(Specifications
                .where(SrvaSpecs.equalState(SrvaEventStateEnum.APPROVED))
                .and(SrvaSpecs.equalEventName(SrvaEventNameEnum.ACCIDENT)));

        final Function<SrvaEvent, GameSpecies> srvaEventToSpecies = SrvaJpaUtils.getSrvaEventToSpeciesMapping(srvaEvents, gameSpeciesRepo);
        final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens = SrvaJpaUtils.getSpecimensGroupedBySrvaEvent(srvaEvents, srvaSpecimenRepo);

        return srvaEvents.stream().map(event -> SrvaPublicExportDTO.create(
                    event,
                    Optional.ofNullable(srvaEventToSpecies.apply(event)).map(GameSpecies::getOfficialCode).orElse(null),
                    Optional.ofNullable(groupedSpecimens.get(event)).map(SrvaSpecimenDTO::create).orElse(null)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_SRVA_RVR')")
    public String exportRVR() {
        return JaxbUtils.marshalToString(createRVREventsForXmlMarshal(), jaxbMarshaller);
    }

    // Do not call this method directory, it's not authorized!
    @Transactional(readOnly = true)
    public RVR_SrvaEvents createRVREventsForXmlMarshal() {
        final List<SrvaEvent> srvaEvents = srvaEventRepository.findAll(SrvaSpecs.equalState(SrvaEventStateEnum.APPROVED));

        final Function<SrvaEvent, GameSpecies> srvaEventToSpecies = SrvaJpaUtils.getSrvaEventToSpeciesMapping(srvaEvents, gameSpeciesRepo);
        final Function<SrvaEvent, Riistanhoitoyhdistys> srvaEventToRhy = SrvaJpaUtils.getSrvaEventToRhyMapping(srvaEvents, riistanhoitoyhdistysRepo);
        final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens = SrvaJpaUtils.getSpecimensGroupedBySrvaEvent(srvaEvents, srvaSpecimenRepo);
        final Map<SrvaEvent, List<SrvaMethod>> groupedMethods = SrvaJpaUtils.getMethodsGroupedBySrvaEvent(srvaEvents, srvaMethodRepo);

        return constructExportData(srvaEvents, srvaEventToSpecies, srvaEventToRhy, groupedSpecimens, groupedMethods);
    }

    private RVR_SrvaEvents constructExportData(final List<SrvaEvent> events,
                                       final Function<SrvaEvent, GameSpecies> srvaEventToSpecies,
                                       final Function<SrvaEvent, Riistanhoitoyhdistys> srvaEventToRhy,
                                       final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens,
                                       final Map<SrvaEvent, List<SrvaMethod>> groupedMethods) {

        final RVR_SrvaEvents rvrEvents = new RVR_SrvaEvents();
        rvrEvents.setTimeStamp(DateTime.now());
        rvrEvents.setVersion(version);

        rvrEvents.getSrvaEvent().addAll(events.stream().map(event -> {
            RVR_SrvaEvent rvrEvent = new RVR_SrvaEvent();
            rvrEvent.setEventName(createRvrEnum(RVR_SrvaEventNameEnum.class, event.getEventName()));
            rvrEvent.setEventType(createRvrEnum(RVR_SrvaEventTypeEnum.class, event.getEventType()));
            rvrEvent.setOtherTypeDescription(event.getOtherTypeDescription());
            rvrEvent.setId(event.getId());
            rvrEvent.setRev(event.getConsistencyVersion());
            rvrEvent.setGeoLocation(createRvrGeoLocation(event.getGeoLocation()));
            rvrEvent.setPointOfTime(new DateTime(event.getPointOfTime().getTime()));
            rvrEvent.setOtherSpeciesDescription(event.getOtherSpeciesDescription());
            rvrEvent.setTotalSpecimenAmount(event.getTotalSpecimenAmount());
            rvrEvent.setPersonCount(event.getPersonCount());
            rvrEvent.setTimeSpent(event.getTimeSpent());
            rvrEvent.setOtherMethodDescription(event.getOtherMethodDescription());
            rvrEvent.setEventResult(createRvrEnum(RVR_SrvaResultEnum.class, event.getEventResult()));
            rvrEvent.setDescription(event.getDescription());
            rvrEvent.setMethods(createRvrMethods(groupedMethods.get(event)));
            rvrEvent.setSpecimens(createRvrSpecimens(groupedSpecimens.get(event)));

            final Riistanhoitoyhdistys rhy = srvaEventToRhy.apply(event);
            rvrEvent.setRhyOfficialCode(rhy.getOfficialCode());
            rvrEvent.setRhyHumanReadableName(rhy.getNameFinnish());

            final Optional<GameSpecies> species = Optional.ofNullable(srvaEventToSpecies.apply(event));
            rvrEvent.setGameSpeciesOfficialCode(species.map(GameSpecies::getOfficialCode).orElse(null));
            rvrEvent.setGameSpeciesHumanReadableName(species.map(GameSpecies::getNameFinnish).orElse(null));

            return rvrEvent;
        }).collect(Collectors.toList()));

        return rvrEvents;
    }

    private static RVR_SrvaEvent.RVR_Specimens createRvrSpecimens(final List<SrvaSpecimen> srvaSpecimens) {
        if(srvaSpecimens == null || srvaSpecimens.isEmpty()){
            return null;
        }

        final RVR_SrvaEvent.RVR_Specimens rvrSpecimens = new RVR_SrvaEvent.RVR_Specimens();
        rvrSpecimens.getSpecimen().addAll(srvaSpecimens.stream().map(srvaSpecimen -> {
            RVR_SrvaSpecimen specimen = new RVR_SrvaSpecimen();
            specimen.setGender(createRvrEnum(RVR_GameGenderEnum.class, srvaSpecimen.getGender()));
            specimen.setAge(createRvrEnum(RVR_GameAgeEnum.class, srvaSpecimen.getAge()));
            return specimen;

        }).collect(Collectors.toList()));

        return rvrSpecimens;
    }

    private static RVR_SrvaEvent.RVR_Methods createRvrMethods(final List<SrvaMethod> srvaMethods) {
        if(srvaMethods == null || srvaMethods.isEmpty()){
            return null;
        }

        final RVR_SrvaEvent.RVR_Methods rvrMethods = new RVR_SrvaEvent.RVR_Methods();
        rvrMethods.getMethod().addAll(srvaMethods.stream()
                .filter(SrvaMethod::isChecked)
                .map(srvaMethodDTO -> createRvrEnum(RVR_SrvaMethodEnum.class, srvaMethodDTO.getName()))
                .collect(Collectors.toList()));
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
        loc.setSource(createRvrEnum(RVR_SourceEnum.class,geoLocation.getSource()));
        loc.setAccuracy(geoLocation.getAccuracy());

        return loc;
    }
}
