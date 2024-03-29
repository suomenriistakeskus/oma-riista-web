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
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.feature.gamediary.srva.specimen.QSrvaSpecimen;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.integration.common.export.srva.CEV_GameAge;
import fi.riista.integration.common.export.srva.CEV_GameGender;
import fi.riista.integration.common.export.srva.CEV_GeoLocation;
import fi.riista.integration.common.export.srva.CEV_SRVAEvent;
import fi.riista.integration.common.export.srva.CEV_SRVAEventName;
import fi.riista.integration.common.export.srva.CEV_SRVAEventResult;
import fi.riista.integration.common.export.srva.CEV_SRVAEventResultDetailsEnum;
import fi.riista.integration.common.export.srva.CEV_SRVAEventType;
import fi.riista.integration.common.export.srva.CEV_SRVAEventTypeDetailsEnum;
import fi.riista.integration.common.export.srva.CEV_SRVAMethod;
import fi.riista.integration.common.export.srva.CEV_SRVASpecimen;
import fi.riista.integration.common.export.srva.CEV_SrvaEvents;
import fi.riista.util.DateUtil;
import fi.riista.util.EnumUtils;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import org.joda.time.DateTime;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

import static fi.riista.feature.gamediary.srva.SrvaEventStateEnum.APPROVED;

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
    public String exportSrvaEventsAsXml(final int year, final int month, final boolean includeV2Parameters) {
        return JaxbUtils.marshalToString(exportSrvaEvents(year, month, includeV2Parameters), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_COMMON')")
    public CEV_SrvaEvents exportSrvaEvents(final int year, final int month, final boolean includeV2Parameters) {
        Preconditions.checkArgument(0 < month && month < 13, "Month value must be valid");

        final Collection<CEV_SRVAEvent> events = findEventsInMonth(year, month, includeV2Parameters);

        return new CEV_SrvaEvents()
                .withSrvaEvent(findEventsInMonth(year, month, includeV2Parameters))
                .withSrvaSpecimen(findSpecimenForEvents(
                        F.mapNonNullsToList(events, ev -> ev.getSrvaEventId())));
    }

    private Collection<CEV_SRVAEvent> findEventsInMonth(final int year, final int month, final boolean includeV2Parameters) {
        final Range<DateTime> range = DateUtil.monthAsRange(year, month);

        final Map<Long, CEV_SRVAEvent> eventMap = fetchEventsToMapById(range, includeV2Parameters);
        fetchAndAssignMethodsToEvents(eventMap, includeV2Parameters);

        return eventMap.values();

    }

    private Map<Long, CEV_SRVAEvent> fetchEventsToMapById(final Range<DateTime> range, final boolean includeV2Parameters) {
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
                        EVENT.timeSpent,
                        EVENT.deportationOrderNumber,
                        EVENT.eventTypeDetail,
                        EVENT.otherEventTypeDetailDescription,
                        EVENT.eventResultDetail
                )
                .from(EVENT)
                .join(EVENT.rhy, RHY)
                .leftJoin(EVENT.species, SPECIES)
                .where(EVENT.pointOfTime.between(
                        range.lowerEndpoint(),
                        range.upperEndpoint()),
                        EVENT.state.eq(APPROVED))
                .fetch().stream()
                .map(tuple -> createSrvaEventFromTuple(tuple, includeV2Parameters))
                .collect(Collectors.toMap(e -> e.getSrvaEventId(), Function.identity()));
    }

    private static CEV_SRVAEvent createSrvaEventFromTuple(final Tuple tuple, final boolean includeV2Parameters) {
        CEV_SRVAEvent event = new CEV_SRVAEvent()
                .withSrvaEventId(tuple.get(EVENT.id))
                .withRhyNumber(tuple.get(RHY.officialCode))
                .withPointOfTime(tuple.get(EVENT.pointOfTime).toLocalDateTime())
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
                .withNumberOfWorkHours(tuple.get(EVENT.timeSpent))
                .withDeportationOrderNumber(tuple.get(EVENT.deportationOrderNumber));

        if (includeV2Parameters) {
            event = event
                    .withEventTypeDetail(EnumUtils.convertNullableByEnumName(
                            CEV_SRVAEventTypeDetailsEnum.class,
                            tuple.get(EVENT.eventTypeDetail)))
                    .withOtherEventTypeDetailDescription(tuple.get(EVENT.otherEventTypeDetailDescription))
                    .withEventResultDetail(EnumUtils.convertNullableByEnumName(
                            CEV_SRVAEventResultDetailsEnum.class,
                            tuple.get(EVENT.eventResultDetail)));
        }
        return event;
    }


    private void fetchAndAssignMethodsToEvents(final Map<Long, CEV_SRVAEvent> eventMap, final boolean includeV2Parameters) {
        Iterables.partition(eventMap.keySet(), 4096).forEach(partition -> queryFactory
                .select(METHOD.event.id,
                        METHOD.name)
                .from(METHOD)
                .where(METHOD.event.id.in(partition))
                .where(METHOD.isChecked.isTrue())
                .fetch()
                .forEach(t -> eventMap
                        .get(t.get(METHOD.event.id))
                        .withMethod(getMethod(t.get(METHOD.name), includeV2Parameters))));
    }

    private CEV_SRVAMethod getMethod(SrvaMethodEnum method, final boolean includeV2Parameters) {
        if (!includeV2Parameters && (method == SrvaMethodEnum.CHASING_WITH_PEOPLE || method == SrvaMethodEnum.VEHICLE)) {
            return EnumUtils.convertNullableByEnumName(CEV_SRVAMethod.class, SrvaMethodEnum.OTHER);
        }
        return EnumUtils.convertNullableByEnumName(CEV_SRVAMethod.class, method);
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


    private static CEV_GeoLocation convertLocation(final GeoLocation location) {
        return new CEV_GeoLocation().withLatitude(location.getLatitude()).withLongitude(location.getLongitude());
    }

}
