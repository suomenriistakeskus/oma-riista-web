package fi.riista.feature.gamediary.observation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
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

import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class ObservationDTOTransformer extends ObservationDTOTransformerBase<ObservationDTO> {

    @Resource
    private JPAQueryFactory queryFactory;

    @Override
    protected List<ObservationDTO> transform(@Nonnull final List<Observation> observations) {
        return transform(observations, true);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nonnull
    public List<ObservationDTO> transform(@Nonnull final List<Observation> observations,
                                          final boolean includeLargeCarnivoreFieldsOnlyIfAuthorOrObserver) {
        requireNonNull(observations);

        final Person authenticatedPerson = getAuthenticatedPerson();

        final Function<Observation, GameSpecies> observationToSpecies = getObservationToSpeciesMapping(observations);
        final Function<Observation, Person> observationToAuthor = getObservationToAuthorMapping(observations);
        final Function<Observation, Person> observationToObserver = getObservationToObserverMapping(observations);

        final Map<Observation, List<ObservationSpecimen>> groupedSpecimens =
                getSpecimensGroupedByObservations(observations);
        final Map<Observation, List<GameDiaryImage>> groupedImages = getImagesGroupedByObservations(observations);

        final Map<Observation, Organisation> observationToGroupOfHuntingDay = getGroupOfHuntingDay(observations);
        final Function<Observation, Person> observationToHuntingDayApprover =
                getObservationToApproverToHuntingDayMapping(observations);

        return observations.stream().filter(Objects::nonNull).map(observation -> {

            final Person author = observationToAuthor.apply(observation);
            final Person observer = observationToObserver.apply(observation);
            final Organisation groupOfHuntingDay = observationToGroupOfHuntingDay.get(observation);
            final Person approverToHuntingDay = observationToHuntingDayApprover.apply(observation);

            return createDTO(
                    observation,
                    observationToSpecies.apply(observation),
                    groupedSpecimens.computeIfAbsent(observation, o -> o.getAmount() == null ? null : emptyList()),
                    groupedImages.get(observation),
                    author,
                    observer,
                    authenticatedPerson,
                    groupOfHuntingDay,
                    approverToHuntingDay,
                    includeLargeCarnivoreFieldsOnlyIfAuthorOrObserver);

        }).collect(toList());
    }

    private static ObservationDTO createDTO(final Observation observation,
                                            final GameSpecies species,
                                            final List<ObservationSpecimen> specimens,
                                            final Iterable<GameDiaryImage> images,
                                            final Person author,
                                            final Person observer,
                                            final Person authenticatedPerson,
                                            final Organisation groupOfHuntingDay,
                                            final Person approverToHuntingDay,
                                            final boolean includeLargeCarnivoreFieldsOnlyIfAuthorOrObserver) {

        final boolean authorOrObserver = author.equals(authenticatedPerson) || observer.equals(authenticatedPerson);

        final boolean isLockedOutOfPersonalDiaryEdits = ObservationLockChecker
                .isLockedOutOfPersonalDiaryEdits(observation, authorOrObserver, MOST_RECENT);

        final ObservationDTO dto = ObservationDTO.builder()
                .withIdAndRev(observation)

                .withGeoLocation(observation.getGeoLocation())
                .withPointOfTime(DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTime()))

                .withGameSpeciesCode(species.getOfficialCode())
                .withObservationCategory(observation.getObservationCategory())
                .withObservationType(observation.getObservationType())
                .withDeerHuntingType(observation.getDeerHuntingType())
                .withDeerHuntingTypeDescription(observation.getDeerHuntingTypeDescription())

                .withAmount(observation.getAmount())
                .withMooselikeAmountsFrom(observation)
                .populateSpecimensWith(specimens)

                .withAuthorInfo(author)
                .withActorInfo(observer)

                .withDescription(authorOrObserver ? observation.getDescription() : null)

                .withGroupOfHuntingDay(groupOfHuntingDay)
                .withApproverToHuntingDay(approverToHuntingDay)

                // Indicates whether observation is editable in personal diary.
                // Note! The logic for determining editability is different in club hunting view.
                .withCanEdit(!isLockedOutOfPersonalDiaryEdits)

                .build();

        dto.setRhyId(F.getId(observation.getRhy()));
        dto.setHuntingDayId(F.getId(observation.getHuntingDayOfGroup()));
        dto.setPointOfTimeApprovedToHuntingDay(
                DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTimeApprovedToHuntingDay()));

        dto.setModeratorOverride(observation.isModeratorOverride());
        dto.setUpdateableOnlyByCarnivoreAuthority(observation.isAnyLargeCarnivoreFieldPresent());

        dto.setPack(ObservationSpecimenOps.isPack(species.getOfficialCode(), observation.getAmount()));
        dto.setLitter(ObservationSpecimenOps.isLitter(species.getOfficialCode(), specimens));

        if (authorOrObserver || !includeLargeCarnivoreFieldsOnlyIfAuthorOrObserver) {
            dto.setVerifiedByCarnivoreAuthority(observation.getVerifiedByCarnivoreAuthority());
            dto.setObserverName(observation.getObserverName());
            dto.setObserverPhoneNumber(observation.getObserverPhoneNumber());
            dto.setOfficialAdditionalInfo(observation.getOfficialAdditionalInfo());
        }

        if (authorOrObserver) {
            if (images != null) {
                F.mapNonNulls(images, dto.getImageIds(), Functions.idOf(GameDiaryImage::getFileMetadata));
            }
        }

        return dto;
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
