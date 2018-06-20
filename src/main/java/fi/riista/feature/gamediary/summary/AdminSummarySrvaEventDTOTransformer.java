package fi.riista.feature.gamediary.summary;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.SrvaEventDTOTransformerBase;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class AdminSummarySrvaEventDTOTransformer extends SrvaEventDTOTransformerBase<SrvaEventDTO> {

    @Nonnull
    @Override
    protected List<SrvaEventDTO> transform(@Nonnull final List<SrvaEvent> srvaEvents) {
        final Function<SrvaEvent, GameSpecies> srvaEventToSpecies = getSrvaEventToSpeciesMapping(srvaEvents);
        final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens = getSpecimensGroupedBySrvaEvent(srvaEvents);
        final Map<SrvaEvent, List<SrvaMethod>> groupedMethods = getMethodsGroupedBySrvaEvent(srvaEvents);

        return srvaEvents.stream()
                .filter(Objects::nonNull)
                .map(srvaEvent -> createDTO(srvaEvent, srvaEventToSpecies.apply(srvaEvent),
                        groupedSpecimens.get(srvaEvent), groupedMethods.get(srvaEvent)))
                .collect(toList());
    }

    private SrvaEventDTO createDTO(final SrvaEvent srvaEvent,
                                   final GameSpecies species,
                                   final List<SrvaSpecimen> specimens,
                                   final List<SrvaMethod> methods) {
        final SrvaEventDTO dto = SrvaEventDTO.create(srvaEvent);

        setCommonFields(
                dto,
                null,
                species,
                specimens != null ? specimens : Collections.emptyList(),
                methods != null ? methods : Collections.emptyList(),
                null,
                null,
                null);

        return dto;
    }
}
