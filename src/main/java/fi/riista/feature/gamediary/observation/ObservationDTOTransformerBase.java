package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameDiaryEntryDTOTransformerHelper;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.image.GameDiaryImage_;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class ObservationDTOTransformerBase<DTO extends ObservationDTOBase>
        extends ListTransformer<Observation, DTO> {

    @Resource
    protected ObservationSpecimenRepository specimenRepo;

    @Resource
    protected GameDiaryImageRepository gameDiaryImageRepo;

    @Resource
    protected ActiveUserService activeUserService;

    @Resource
    private GameDiaryEntryDTOTransformerHelper helper;

    @Nullable
    protected Person getAuthenticatedPerson() {
        return activeUserService.requireActiveUser().getPerson();
    }

    @Nonnull
    protected Function<Observation, GameSpecies> getObservationToSpeciesMapping(final Iterable<Observation> observations) {
        return helper.createGameDiaryEntryToSpeciesMapping(observations);
    }

    @Nonnull
    protected Function<Observation, Person> getObservationToAuthorMapping(final Iterable<Observation> observations) {
        return helper.createAuthorMapping(observations);
    }

    @Nonnull
    protected Function<Observation, Person> getObservationToObserverMapping(final Iterable<Observation> observations) {
        return helper.createPersonMapping(observations, Observation::getObserver, true);
    }

    @Nonnull
    protected Function<Observation, Person> getObservationToApproverToHuntingDayMapping(final Iterable<Observation> observations) {
        return helper.createApproverToHuntingDayMapping(observations);
    }

    @Nonnull
    protected Map<Observation, List<ObservationSpecimen>> getSpecimensGroupedByObservations(
            final Collection<Observation> observations) {

        return JpaGroupingUtils.groupRelations(
                observations, ObservationSpecimen_.observation, specimenRepo, JpaSort.of(ObservationSpecimen_.id));
    }

    @Nonnull
    protected Map<Observation, List<GameDiaryImage>> getImagesGroupedByObservations(
            final Collection<Observation> observations) {

        return JpaGroupingUtils.groupRelations(observations, GameDiaryImage_.observation, gameDiaryImageRepo);
    }
}
