package fi.riista.integration.common.export;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.gamediary.observation.specimen.QObservationSpecimen;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.integration.common.export.observations.COBS_FemaleAndCalfs;
import fi.riista.integration.common.export.observations.COBS_GameGender;
import fi.riista.integration.common.export.observations.COBS_GameMarking;
import fi.riista.integration.common.export.observations.COBS_GeoLocation;
import fi.riista.integration.common.export.observations.COBS_Observation;
import fi.riista.integration.common.export.observations.COBS_ObservationSpecimen;
import fi.riista.integration.common.export.observations.COBS_ObservationType;
import fi.riista.integration.common.export.observations.COBS_Observations;
import fi.riista.integration.common.export.observations.COBS_ObservedGameAge;
import fi.riista.integration.common.export.observations.COBS_ObservedGameState;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CommonObservationExportFeature {

    final static QObservation OBSERVATION = QObservation.observation;
    final static QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    final static QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    final static QObservationSpecimen SPECIMEN = QObservationSpecimen.observationSpecimen;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource(name = "commonObservationExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_COMMON')")
    public String exportObservationsAsXml(final int year, final int month) {
        return JaxbUtils.marshalToString(exportObservations(year, month), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_COMMON')")
    public COBS_Observations exportObservations(final int year, final int month) {
        Preconditions.checkArgument(0 < month && month < 13, "Month value must be valid");
        final Range<DateTime> range = DateUtil.monthAsRange(year, month);


        final List<Tuple> observationsResult = fetchObservationsInMonth(range);

        final COBS_Observations observations = new COBS_Observations()
                .withObservation(F.mapNonNullsToList(observationsResult, tuple -> createObservationFromTuple(tuple)));

        final List<Long> ids = observationsResult.stream().map(t->t.get(OBSERVATION.id)).collect(Collectors.toList());


        Lists.partition(ids, 4096).forEach(partition -> {

            final List<Tuple> specimenResult = fetchSpecimensByObservationId(partition);
            observations.withObservationSpecimen(F.mapNonNullsToList(specimenResult,
                                                                     tuple -> createSpecimenFromTuple(tuple)));
        });

        return observations;
    }

    private List<Tuple> fetchSpecimensByObservationId(final List<Long> partition) {
        return queryFactory.select(SPECIMEN.observation.id,
                                   SPECIMEN.gender,
                                   SPECIMEN.age,
                                   SPECIMEN.state,
                                   SPECIMEN.marking,
                                   SPECIMEN.widthOfPaw,
                                   SPECIMEN.lengthOfPaw)
                           .from(SPECIMEN)
                           .where(SPECIMEN.observation.id.in(partition))
                           .fetch();
    }

    private List<Tuple> fetchObservationsInMonth(final Range<DateTime> range) {
        return queryFactory.select(OBSERVATION.id,
                                   RHY.officialCode,
                                   SPECIES.officialCode,
                                   OBSERVATION.pointOfTime,
                                   OBSERVATION.geoLocation,
                                   OBSERVATION.observationType,
                                   OBSERVATION.amount,
                                   OBSERVATION.mooselikeMaleAmount,
                                   OBSERVATION.mooselikeFemaleAmount,
                                   OBSERVATION.mooselikeFemale1CalfAmount,
                                   OBSERVATION.mooselikeFemale2CalfsAmount,
                                   OBSERVATION.mooselikeFemale3CalfsAmount,
                                   OBSERVATION.mooselikeFemale4CalfsAmount,
                                   OBSERVATION.mooselikeCalfAmount,
                                   OBSERVATION.mooselikeUnknownSpecimenAmount)
                           .from(OBSERVATION)
                           .join(OBSERVATION.rhy, RHY)
                           .join(OBSERVATION.species, SPECIES)
                           .where(OBSERVATION.pointOfTime.between(
                                   range.lowerEndpoint(),
                                   range.upperEndpoint()))
                           .fetch();
    }

    private static COBS_Observation createObservationFromTuple(final Tuple tuple) {
        return new COBS_Observation()
                .withObservationId(tuple.get(OBSERVATION.id))
                .withRhyNumber(tuple.get(RHY.officialCode))
                .withGameSpeciesCode(tuple.get(SPECIES.officialCode))
                .withPointOfTime(new LocalDateTime(tuple.get(OBSERVATION.pointOfTime)))
                .withGeoLocation(convertLocation(tuple.get(OBSERVATION.geoLocation)))
                .withObservationType(COBS_ObservationType.fromValue(tuple.get(OBSERVATION.observationType).name()))
                .withAmount(tuple.get(OBSERVATION.amount))
                .withMaleAmount(tuple.get(OBSERVATION.mooselikeMaleAmount))
                .withFemaleAndCalfs(Stream.of(
                        createFemaleAndCalfs(tuple.get(OBSERVATION.mooselikeFemaleAmount), 0),
                        createFemaleAndCalfs(tuple.get(OBSERVATION.mooselikeFemale1CalfAmount), 1),
                        createFemaleAndCalfs(tuple.get(OBSERVATION.mooselikeFemale2CalfsAmount), 2),
                        createFemaleAndCalfs(tuple.get(OBSERVATION.mooselikeFemale3CalfsAmount), 3),
                        createFemaleAndCalfs(tuple.get(OBSERVATION.mooselikeFemale4CalfsAmount), 4))
                                          .filter(Objects::nonNull).collect(Collectors.toList()))
                .withSolitaryCalfAmount(tuple.get(OBSERVATION.mooselikeCalfAmount))
                .withUnknownSpecimenAmount(tuple.get(OBSERVATION.mooselikeUnknownSpecimenAmount));
    }

    private static COBS_ObservationSpecimen createSpecimenFromTuple(final Tuple tuple) {
        return new COBS_ObservationSpecimen()
                .withObservationId(tuple.get(SPECIMEN.observation.id))
                .withGender(EnumUtils.convertNullableByEnumName(COBS_GameGender.class,
                                                                tuple.get(SPECIMEN.gender)))
                .withAge(EnumUtils.convertNullableByEnumName(COBS_ObservedGameAge.class,
                                                             tuple.get(SPECIMEN.age)))
                .withState(EnumUtils.convertNullableByEnumName(COBS_ObservedGameState.class,
                                                               tuple.get(SPECIMEN.state)))
                .withMarking(EnumUtils.convertNullableByEnumName(COBS_GameMarking.class,
                                                                 tuple.get(SPECIMEN.marking)))
                .withWidthOfPaw(tuple.get(SPECIMEN.widthOfPaw))
                .withLengthOfPaw(tuple.get(SPECIMEN.lengthOfPaw));
    }

    private static COBS_GeoLocation convertLocation(final GeoLocation location) {
        return new COBS_GeoLocation().withLatitude(location.getLatitude()).withLongitude(location.getLongitude());
    }

    private static COBS_FemaleAndCalfs createFemaleAndCalfs(final Integer amount, final int calfs) {
        // If amount is zero or null, return null so that xml will not contain empty item
        if (amount == null || amount == 0) {
            return null;
        }
        return new COBS_FemaleAndCalfs().withAmount(amount).withCalfs(calfs);
    }
}
