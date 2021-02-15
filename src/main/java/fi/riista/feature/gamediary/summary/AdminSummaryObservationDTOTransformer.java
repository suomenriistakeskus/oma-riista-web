package fi.riista.feature.gamediary.summary;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformerBase;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class AdminSummaryObservationDTOTransformer extends ObservationDTOTransformerBase<ObservationDTO> {

    @Override
    protected List<ObservationDTO> transform(@Nonnull final List<Observation> observations) {
        final Function<Observation, GameSpecies> observationToSpecies =
                getObservationToSpeciesMapping(observations);

        final Map<Observation, List<ObservationSpecimen>> groupedSpecimens =
                getSpecimensGroupedByObservations(observations);

        return observations.stream()
                .filter(Objects::nonNull)
                .map(observation -> createDTO(observation, observationToSpecies.apply(observation),
                        groupedSpecimens.get(observation)))
                .collect(toList());
    }

    private static ObservationDTO createDTO(final Observation observation,
                                            final GameSpecies species,
                                            final List<ObservationSpecimen> specimens) {

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

                .build();

        dto.setPack(ObservationSpecimenOps.isPack(species.getOfficialCode(), observation.getAmount()));
        dto.setLitter(ObservationSpecimenOps.isLitter(species.getOfficialCode(), specimens));

        dto.setRhyId(F.getId(observation.getRhy()));
        dto.setHuntingDayId(F.getId(observation.getHuntingDayOfGroup()));
        dto.setPointOfTimeApprovedToHuntingDay(
                DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTimeApprovedToHuntingDay()));

        dto.setModeratorOverride(observation.isModeratorOverride());
        dto.setUpdateableOnlyByCarnivoreAuthority(observation.isAnyLargeCarnivoreFieldPresent());

        return dto;
    }
}
