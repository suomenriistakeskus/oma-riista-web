package fi.riista.integration.common.export;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.srva.QSrvaEvent;
import fi.riista.feature.gamediary.srva.method.QSrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.QSrvaSpecimen;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.integration.common.export.srva.CEV_GameAge;
import fi.riista.integration.common.export.srva.CEV_GameGender;
import fi.riista.integration.common.export.srva.CEV_GeoLocation;
import fi.riista.integration.common.export.srva.CEV_SRVAEvent;
import fi.riista.integration.common.export.srva.CEV_SRVAEventName;
import fi.riista.integration.common.export.srva.CEV_SRVAEventResult;
import fi.riista.integration.common.export.srva.CEV_SRVAEventType;
import fi.riista.integration.common.export.srva.CEV_SRVAMethod;
import fi.riista.integration.common.export.srva.CEV_SRVASpecimen;
import fi.riista.integration.common.export.srva.CEV_SrvaEvents;
import fi.riista.util.DateUtil;
import fi.riista.util.EnumUtils;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommonSrvaEventExportFeature {

    final static QSrvaEvent EVENT = QSrvaEvent.srvaEvent;
    final static QSrvaMethod METHOD = QSrvaMethod.srvaMethod;
    final static QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    final static QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource(name = "commonSrvaEventExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_COMMON')")
    public String exportSrvaEventsAsXml(final int year, final int month) {
        return JaxbUtils.marshalToString(exportSrvaEvents(year, month), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_COMMON')")
    public CEV_SrvaEvents exportSrvaEvents(final int year, final int month) {
        Preconditions.checkArgument(0 < month && month < 13, "Month value must be valid");

        final Collection<CEV_SRVAEvent> events = findEventsInMonth(year, month);

        return new CEV_SrvaEvents()
                .withSrvaEvent(findEventsInMonth(year, month))
                .withSrvaSpecimen(findSpecimenForEvents(
                        F.mapNonNullsToList(events, ev -> ev.getSrvaEventId())));
    }


    private Collection<CEV_SRVAEvent> findEventsInMonth(final int year, final int month) {
        final Range<DateTime> range = DateUtil.monthAsRange(year, month);

        final Map<Long, CEV_SRVAEvent> eventMap = fetchEventsToMapById(range);
        fetchAndAssignMethodsToEvents(eventMap);

        return eventMap.values();

    }

    private Map<Long, CEV_SRVAEvent> fetchEventsToMapById(final Range<DateTime> range) {
        return queryFactory
                .select(EVENT.id,
                        RHY.officialCode,
                        EVENT.pointOfTime,
                        EVENT.geoLocation,
                        SPECIES.officialCode,
                        EVENT.otherSpeciesDescription,
                        EVENT.eventName,
                        EVENT.eventType,
                        EVENT.otherTypeDescription,
                        EVENT.otherMethodDescription,
                        EVENT.eventResult,
                        EVENT.totalSpecimenAmount,
                        EVENT.personCount,
                        EVENT.timeSpent
                )
                .from(EVENT)
                .join(EVENT.rhy, RHY)
                .leftJoin(EVENT.species, SPECIES)
                .where(EVENT.pointOfTime.between(
                        range.lowerEndpoint().toDate(),
                        range.upperEndpoint().toDate()))
                .fetch().stream()
                .map(tuple -> createSrvaEventFromTuple(tuple))
                .collect(Collectors.toMap(e -> e.getSrvaEventId(), Function.identity()));
    }

    private CEV_SRVAEvent createSrvaEventFromTuple(final Tuple tuple) {
        return new CEV_SRVAEvent()
                .withSrvaEventId(tuple.get(EVENT.id))
                .withRhyNumber(tuple.get(RHY.officialCode))
                .withPointOfTime(new LocalDateTime(tuple.get(EVENT.pointOfTime).getTime()))
                .withGeoLocation(convertLocation(tuple.get(EVENT.geoLocation)))
                .withGameSpeciesCode(tuple.get(SPECIES.officialCode))
                .withOtherSpeciesDescription(tuple.get(EVENT.otherSpeciesDescription))
                .withName(EnumUtils.convertNullableByEnumName(CEV_SRVAEventName.class, tuple.get(EVENT.eventName)))
                .withEventType(EnumUtils.convertNullableByEnumName(CEV_SRVAEventType.class,
                                                                   tuple.get(EVENT.eventType)))
                .withOtherTypeDescription(tuple.get(EVENT.otherTypeDescription))
                .withOtherMethodDescription(tuple.get(EVENT.otherMethodDescription))
                .withResult(EnumUtils.convertNullableByEnumName(CEV_SRVAEventResult.class,
                                                                tuple.get(EVENT.eventResult)))
                .withTotalSpecimenAmount(tuple.get(EVENT.totalSpecimenAmount))
                .withNumberOfParticipants(tuple.get(EVENT.personCount))
                .withNumberOfWorkHours(tuple.get(EVENT.timeSpent));
    }


    private void fetchAndAssignMethodsToEvents(final Map<Long, CEV_SRVAEvent> eventMap) {
        Iterables.partition(eventMap.keySet(), 4096).forEach(partition -> queryFactory
                .select(METHOD.event.id,
                        METHOD.name)
                .from(METHOD)
                .where(METHOD.event.id.in(partition))
                .where(METHOD.isChecked.isTrue())
                .fetch()
                .forEach(t -> eventMap
                        .get(t.get(METHOD.event.id))
                        .withMethod(
                                EnumUtils.convertNullableByEnumName(CEV_SRVAMethod.class, t.get(METHOD.name)))
                ));
    }

    private Collection<CEV_SRVASpecimen> findSpecimenForEvents(final List<Long> ids) {

        final QSrvaSpecimen SPECIMEN = QSrvaSpecimen.srvaSpecimen;
        final List<CEV_SRVASpecimen> specimen = Lists.newArrayListWithExpectedSize(ids.size());

        Lists.partition(ids, 4096).forEach(partition -> {

            final List<Tuple>
                    specimenResult =
                    queryFactory.select(SPECIMEN.event.id,
                                        SPECIMEN.gender,
                                        SPECIMEN.age)
                                .from(SPECIMEN)
                                .where(SPECIMEN.event.id.in(partition))
                                .fetch();

            specimen.addAll(F.mapNonNullsToList(specimenResult, t ->
                    new CEV_SRVASpecimen()
                            .withSRVAEventId(t.get(SPECIMEN.event.id))
                            .withGender(EnumUtils.convertNullableByEnumName(CEV_GameGender.class,
                                                                            t.get(SPECIMEN.gender)))
                            .withAge(EnumUtils.convertNullableByEnumName(CEV_GameAge.class, t.get(SPECIMEN.age)))
            ));
        });

        return specimen;
    }


    private CEV_GeoLocation convertLocation(final GeoLocation location) {
        return new CEV_GeoLocation().withLatitude(location.getLatitude()).withLongitude(location.getLongitude());
    }

}
