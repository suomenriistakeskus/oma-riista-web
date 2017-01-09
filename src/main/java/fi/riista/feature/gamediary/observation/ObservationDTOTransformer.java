package fi.riista.feature.gamediary.observation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.Functions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class ObservationDTOTransformer extends ObservationDTOTransformerBase<ObservationDTO> {

    @Resource
    private JPAQueryFactory queryFactory;

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nonnull
    @Override
    public List<ObservationDTO> transform(@Nonnull final List<Observation> observations) {
        Objects.requireNonNull(observations);

        final Person authenticatedPerson = getAuthenticatedPerson();

        final Function<Observation, GameSpecies> observationToSpecies =
                getGameDiaryEntryToSpeciesMapping(observations);
        final Function<Observation, Person> observationToAuthor = getGameDiaryEntryToAuthorMapping(observations);
        final Function<Observation, Person> observationToObserver =
                getObservationToObserverMapping(observations);

        final Map<Observation, List<ObservationSpecimen>> groupedSpecimens =
                getSpecimensGroupedByObservations(observations);
        final Map<Observation, List<GameDiaryImage>> groupedImages = getImagesGroupedByObservations(observations);

        final Map<Observation, Organisation> observationToGroupOfHuntingDay = getGroupOfHuntingDay(observations);
        final Function<Observation, Person> observationToHuntingDayApprover = getApproverToHuntingDay(observations);

        return observations.stream().filter(Objects::nonNull).map(observation -> {
            final Person author = observationToAuthor.apply(observation);
            final Person observer = observationToObserver.apply(observation);
            final Organisation groupOfHuntingDay = observationToGroupOfHuntingDay.get(observation);
            final Person approverToHuntingDay = observationToHuntingDayApprover.apply(observation);

            return createDTO(
                    observation,
                    observationToSpecies.apply(observation),
                    groupedSpecimens.get(observation),
                    groupedImages.get(observation),
                    author,
                    observer,
                    authenticatedPerson,
                    groupOfHuntingDay,
                    approverToHuntingDay
            );

        }).collect(toList());
    }

    private static ObservationDTO createDTO(
            final Observation observation,
            final GameSpecies species,
            final List<ObservationSpecimen> specimens,
            final Iterable<GameDiaryImage> images,
            final Person author,
            final Person observer,
            final Person authenticatedPerson,
            final Organisation groupOfHuntingDay,
            final Person approverToHuntingDay) {

        final ObservationDTO dto = ObservationDTO.builder()
                .populateWith(observation)
                .populateWith(species)
                .withAuthorInfo(author)
                .withActorInfo(observer)
                .withCanEdit(isObservationEditable(observation, authenticatedPerson, author, observer))
                .withGroupOfHuntingDay(groupOfHuntingDay)
                .withApproverToHuntingDay(approverToHuntingDay)
                .build();

        if (observation.getAmount() != null && !F.isNullOrEmpty(specimens)) {
            dto.setSpecimens(ObservationSpecimenDTO.transformList(specimens));
        }

        final boolean authorOrObserver = author.equals(authenticatedPerson) || observer.equals(authenticatedPerson);

        if (authorOrObserver) {
            if (images != null) {
                F.mapNonNulls(images, dto.getImageIds(), Functions.idOf(GameDiaryImage::getFileMetadata));
            }
        } else {
            dto.setDescription(null);
        }

        return dto;
    }

    private Function<Observation, Person> getApproverToHuntingDay(final Iterable<Observation> observations) {
        return createGameDiaryEntryToPersonMapping(observations, Observation::getApproverToHuntingDay, false);
    }

    private Map<Observation, Organisation> getGroupOfHuntingDay(final List<Observation> observations) {
        final QObservation observation = QObservation.observation;
        final QGroupHuntingDay day = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        return queryFactory.select(observation, group)
                .from(observation)
                .join(observation.huntingDayOfGroup, day)
                .join(day.group, group)
                .where(observation.in(observations))
                .fetch()
                .stream().collect(toMap(t -> t.get(0, Observation.class), t -> t.get(1, HuntingClubGroup.class)));
    }
}
