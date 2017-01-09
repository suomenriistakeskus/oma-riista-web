package fi.riista.feature.gamediary.observation;

import fi.riista.feature.gamediary.HuntingDiaryEntryDTOTransformer;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImage_;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.jpa.JpaGroupingUtils;

import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class ObservationDTOTransformerBase<DTO extends ObservationDTOBase>
        extends HuntingDiaryEntryDTOTransformer<Observation, DTO> {

    protected static boolean isObservationEditable(
            final Observation observation,
            final Person requestingPerson,
            final Person author,
            final Person observer) {

        if (observation.getHuntingDayOfGroup() != null) {
            return false;
        }

        return Objects.equals(author, requestingPerson) || Objects.equals(observer, requestingPerson);
    }

    @Resource
    protected ObservationSpecimenRepository specimenRepo;

    @Resource
    protected GameDiaryImageRepository gameDiaryImageRepo;

    @Nonnull
    protected Function<Observation, Person> getObservationToObserverMapping(
            final Iterable<Observation> observations) {

        return createGameDiaryEntryToPersonMapping(observations, Observation::getObserver);
    }

    @Nonnull
    protected Map<Observation, List<ObservationSpecimen>> getSpecimensGroupedByObservations(
            final Collection<Observation> observations) {

        return JpaGroupingUtils.groupRelations(
                observations, ObservationSpecimen_.observation, specimenRepo, new JpaSort(ObservationSpecimen_.id));
    }

    @Nonnull
    protected Map<Observation, List<GameDiaryImage>> getImagesGroupedByObservations(
            final Collection<Observation> observations) {

        return JpaGroupingUtils.groupRelations(observations, GameDiaryImage_.observation, gameDiaryImageRepo);
    }

}
