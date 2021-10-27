package fi.riista.feature.gamediary.mobile;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformerBase;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.QObservationBaseFields;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public abstract class MobileObservationDTOTransformerBase<DTO extends MobileObservationDTO> extends ObservationDTOTransformerBase<DTO> {

    @Resource
    private JPAQueryFactory queryFactory;

    protected Map<Long, ObservationBaseFields> getBaseFieldsOfObservations(final List<Observation> observations,
                                                                         final ObservationSpecVersion specVersion) {
        final QObservation observation = QObservation.observation;
        final QGameSpecies species = QGameSpecies.gameSpecies;
        final QObservationBaseFields baseFields = QObservationBaseFields.observationBaseFields;

        return queryFactory.select(observation.id, baseFields)
                .from(observation)
                .join(observation.species, species)
                .join(species.observationBaseFields, baseFields)
                .where(observation.in(observations).and(baseFields.metadataVersion.eq(specVersion.toIntValue())))
                .fetch()
                .stream()
                .collect(toMap(t -> t.get(0, Long.class), t -> t.get(1, ObservationBaseFields.class)));
    }

}
