package fi.riista.feature.gamediary.srva;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

public class SrvaParametersDTO {

    public final GameAge[] ages = GameAge.values();

    public final GameGender[] genders = GameGender.values();

    public final List<GameSpeciesDTO> species;

    private final List<EventForDTO> events;

    public SrvaParametersDTO(final List<GameSpeciesDTO> species, final SrvaEventSpecVersion specVersion) {
        this.species = unmodifiableList(species);
        this.events = unmodifiableList(Arrays.asList(
            new EventForDTO(SrvaEventNameEnum.ACCIDENT, specVersion),
            new EventForDTO(SrvaEventNameEnum.DEPORTATION, specVersion),
            new EventForDTO(SrvaEventNameEnum.INJURED_ANIMAL, specVersion)));
    }

    public class TypeDetailDTO {
        public final SrvaEventTypeDetailsEnum detailType;
        public final List<Integer> speciesCodes;

        public TypeDetailDTO(final SrvaEventTypeDetailsEnum detailType, final List<Integer> speciesCodes) {
            this.detailType = detailType;
            this.speciesCodes = speciesCodes;
        }
    }

    public class EventForDTO {

        public final String name;
        public final List<SrvaEventTypeEnum> types;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public final Map<SrvaEventTypeEnum, List<TypeDetailDTO>> typeDetails;
        public final List<SrvaResultEnum> results;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public final Map<SrvaResultEnum, List<SrvaEventResultDetailsEnum>> resultDetails;
        public final List<SrvaMethodDTO> methods;

        public EventForDTO(final SrvaEventNameEnum event, final SrvaEventSpecVersion specVersion) {
            name = event.name();
            types = ImmutableList.copyOf(SrvaEventTypeEnum.getBySrvaEvent(event));
            results = unmodifiableList(SrvaResultEnum.getBySrvaEvent(event));
            if (specVersion.supportsSrvaDetails()) {
                typeDetails = toTypeDetailDTOMap(SrvaEventTypeDetailsEnum.getBySrvaEventType(types));
                resultDetails = unmodifiableMap(SrvaEventResultDetailsEnum.getBySrvaResult(results));
            } else {
                typeDetails = null;
                resultDetails = null;
            }

            this.methods = unmodifiableList(SrvaMethodEnum.getBySrvaEvent(event, specVersion).stream()
                    .map(SrvaMethodDTO::new)
                    .collect(toList()));
        }

        private Map<SrvaEventTypeEnum, List<TypeDetailDTO>> toTypeDetailDTOMap(final Map<SrvaEventTypeEnum, List<SrvaEventTypeDetailsEnum>> details) {
            return details.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                k -> k.getKey(),
                                v -> v.getValue().stream().map(t -> toTypeDetailDTO(t)).collect(Collectors.toList())
                        ));
        }

        private TypeDetailDTO toTypeDetailDTO(final SrvaEventTypeDetailsEnum detailType) {
            // Beehive is only for bears, every other type is for every species
            if (detailType == SrvaEventTypeDetailsEnum.BEEHIVE) {
                return new TypeDetailDTO(detailType, Collections.singletonList(GameSpecies.OFFICIAL_CODE_BEAR));
            } else {
                return new TypeDetailDTO(detailType, null);
            }
        }
    }
}
