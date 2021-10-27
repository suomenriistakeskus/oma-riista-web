package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.mobile.MobileObservationDTOTransformerBase;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationLockChecker;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Functions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class MobileGroupObservationDTOTransformer extends MobileObservationDTOTransformerBase<MobileGroupObservationDTO> {

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public List<MobileGroupObservationDTO> apply(@Nullable final List<Observation> list,
                                            @Nonnull final ObservationSpecVersion specVersion) {

        return list == null ? null : transform(list, specVersion);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public MobileGroupObservationDTO apply(@Nullable final Observation object,
                                      @Nonnull final ObservationSpecVersion specVersion) {

        if (object == null) {
            return null;
        }

        final List<MobileGroupObservationDTO> singletonList = apply(singletonList(object), specVersion);

        if (singletonList.size() != 1) {
            throw new IllegalStateException(
                    "Expected list containing exactly one element but has: " + singletonList.size());
        }

        return singletonList.get(0);
    }

    @Override
    protected List<MobileGroupObservationDTO> transform(final List<Observation> observations) {
        throw new UnsupportedOperationException("No transformation without observationSpecVersion supported");
    }

    protected List<MobileGroupObservationDTO> transform(@Nonnull final List<Observation> observations,
                                                   @Nonnull final ObservationSpecVersion specVersion) {

        requireNonNull(observations, "observations is null");
        requireNonNull(specVersion, "specVersion is null");

        final Function<Observation, GameSpecies> observationToSpecies = getObservationToSpeciesMapping(observations);
        final Map<Long, ObservationBaseFields> groupedBaseFields =
                getBaseFieldsOfObservations(observations, specVersion);

        final Function<Observation, Person> observationToAuthor = getObservationToAuthorMapping(observations);
        final Function<Observation, Person> observationToObserver = getObservationToObserverMapping(observations);

        final Map<Observation, List<ObservationSpecimen>> groupedSpecimens =
                getSpecimensGroupedByObservations(observations);

        final Map<Observation, List<GameDiaryImage>> groupedImages = getImagesGroupedByObservations(observations);

        final Person authenticatedPerson = getAuthenticatedPerson();

        final Function<Observation, Person> observationToHuntingDayApprover =
                getObservationToApproverToHuntingDayMapping(observations);

        return observations.stream().filter(Objects::nonNull).map(observation -> {

            final GameSpecies species = observationToSpecies.apply(observation);

            // This is the only way to know, should 'withinMooseHunting' field be in the DTO or not for
            // older observation versions.
            final ObservationBaseFields baseFields = groupedBaseFields.get(observation.getId());

            final Person author = observationToAuthor.apply(observation);
            final Person observer = observationToObserver.apply(observation);

            final Person approverToHuntingDay = observationToHuntingDayApprover.apply(observation);

            return createDTO(
                    observation,
                    species,
                    baseFields,
                    groupedSpecimens.computeIfAbsent(observation, o -> o.getAmount() == null ? null : emptyList()),
                    groupedImages.get(observation),
                    author,
                    observer,
                    authenticatedPerson,
                    approverToHuntingDay);

        }).collect(toList());
    }

    private static MobileGroupObservationDTO createDTO(final Observation observation,
                                                       final GameSpecies species,
                                                       final ObservationBaseFields baseFields,
                                                       final List<ObservationSpecimen> specimens,
                                                       final Iterable<GameDiaryImage> images,
                                                       final Person author,
                                                       final Person observer,
                                                       final Person authenticatedPerson,
                                                       final Person approverToHuntingDay) {

        final ObservationSpecVersion specVersion = ObservationSpecVersion.fromIntValue(baseFields.getMetadataVersion());

        final boolean authorOrObserver = author.equals(authenticatedPerson) || observer.equals(authenticatedPerson);
        final boolean isLockedOutOfPersonalDiaryEdits =
                ObservationLockChecker.isLockedOutOfPersonalDiaryEdits(observation, authorOrObserver, specVersion);

        final MobileGroupObservationDTO dto = MobileGroupObservationDTO.builder(baseFields)
                .withIdAndRev(observation)
                .withMobileClientRefId(observation.getMobileClientRefId())

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

                .withDescription(observation.getDescription())

                .withCanEdit(!isLockedOutOfPersonalDiaryEdits)

                .withHuntingDayId(F.getId(observation.getHuntingDayOfGroup()))

                .withAuthorInfo(author)
                .withActorInfo(observer)

                .withApproverToHuntingDay(approverToHuntingDay)

                .build();

        dto.setLinkedToGroupHuntingDay(observation.getHuntingDayOfGroup() != null);

        dto.setPointOfTimeApprovedToHuntingDay(
                DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTimeApprovedToHuntingDay()));

        dto.setPack(ObservationSpecimenOps.isPack(species.getOfficialCode(), observation.getAmount()));
        dto.setLitter(ObservationSpecimenOps.isLitter(species.getOfficialCode(), specimens));

        if (specVersion.supportsLargeCarnivoreFields()) {
            dto.setVerifiedByCarnivoreAuthority(observation.getVerifiedByCarnivoreAuthority());
            dto.setObserverName(observation.getObserverName());
            dto.setObserverPhoneNumber(observation.getObserverPhoneNumber());
            dto.setOfficialAdditionalInfo(observation.getOfficialAdditionalInfo());
        }

        if (authorOrObserver && images != null) {
            F.mapNonNulls(images, dto.getImageIds(), Functions.idOf(GameDiaryImage::getFileMetadata));
        }

        return dto;
    }
}
