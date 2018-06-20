package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformerBase;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.Functions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class MobileObservationDTOTransformer extends ObservationDTOTransformerBase<MobileObservationDTO> {

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public List<MobileObservationDTO> apply(@Nullable final List<Observation> list,
                                            @Nonnull final ObservationSpecVersion specVersion) {

        return list == null ? null : transform(list, specVersion);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public MobileObservationDTO apply(@Nullable final Observation object,
                                      @Nonnull final ObservationSpecVersion specVersion) {

        if (object == null) {
            return null;
        }

        final List<MobileObservationDTO> singletonList = apply(Collections.singletonList(object), specVersion);

        if (singletonList.size() != 1) {
            throw new IllegalStateException(
                    "Expected list containing exactly one element but has: " + singletonList.size());
        }

        return singletonList.get(0);
    }

    @Override
    protected List<MobileObservationDTO> transform(final List<Observation> observations) {
        throw new UnsupportedOperationException("No transformation without observationSpecVersion supported");
    }

    protected List<MobileObservationDTO> transform(@Nonnull final List<Observation> observations,
                                                   @Nonnull final ObservationSpecVersion specVersion) {

        Objects.requireNonNull(observations, "observations is null");
        Objects.requireNonNull(specVersion, "specVersion is null");

        final Function<Observation, GameSpecies> observationToSpecies = getGameDiaryEntryToSpeciesMapping(observations);

        final Function<Observation, Person> observationToAuthor = getGameDiaryEntryToAuthorMapping(observations);
        final Function<Observation, Person> observationToObserver = getObservationToObserverMapping(observations);

        final Map<Observation, List<ObservationSpecimen>> groupedSpecimens =
                getSpecimensGroupedByObservations(observations);

        final Map<Observation, List<GameDiaryImage>> groupedImages = getImagesGroupedByObservations(observations);

        final Person authenticatedPerson = getAuthenticatedPerson();

        return observations.stream().filter(Objects::nonNull).map(observation -> {
            final Person author = observationToAuthor.apply(observation);
            final Person observer = observationToObserver.apply(observation);
            final boolean authorOrObserver = author.equals(authenticatedPerson) || observer.equals(authenticatedPerson);

            return createDTO(
                    observation,
                    specVersion,
                    observationToSpecies.apply(observation),
                    groupedSpecimens.computeIfAbsent(observation, o -> o.getAmount() == null ? null : emptyList()),
                    groupedImages.get(observation),
                    isObservationEditable(observation, authorOrObserver));
        }).collect(toList());
    }

    private static MobileObservationDTO createDTO(final Observation observation,
                                                  final ObservationSpecVersion specVersion,
                                                  final GameSpecies species,
                                                  final List<ObservationSpecimen> specimens,
                                                  final Iterable<GameDiaryImage> images,
                                                  final boolean isEditable) {

        final MobileObservationDTO dto = MobileObservationDTO.builder(specVersion)
                .populateWith(observation, specVersion.supportsLargeCarnivoreFields())
                .populateWith(species)
                .populateSpecimensWith(specimens)
                .withCanEdit(isEditable)
                .build();

        if (images != null) {
            F.mapNonNulls(images, dto.getImageIds(), Functions.idOf(GameDiaryImage::getFileMetadata));
        }

        dto.setPack(ObservationSpecimenOps.isPack(species.getOfficialCode(), observation.getAmount()));
        dto.setLitter(ObservationSpecimenOps.isLitter(species.getOfficialCode(), specimens));

        return dto;
    }
}
