package fi.riista.feature.gamediary.srva;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class SrvaParametersDTO {

    public final GameAge[] ages = GameAge.values();

    public final GameGender[] genders = GameGender.values();

    public final List<GameSpeciesDTO> species;

    public final List<EventForDTO> events = unmodifiableList(Arrays.asList(
            new EventForDTO(SrvaEventNameEnum.ACCIDENT),
            new EventForDTO(SrvaEventNameEnum.DEPORTATION),
            new EventForDTO(SrvaEventNameEnum.INJURED_ANIMAL)));

    public SrvaParametersDTO(final List<GameSpeciesDTO> species) {
        this.species = unmodifiableList(species);
    }

    public class EventForDTO {

        public final String name;
        public final List<SrvaEventTypeEnum> types;
        public final List<SrvaResultEnum> results;
        public final List<SrvaMethodDTO> methods;

        public EventForDTO(final SrvaEventNameEnum event) {
            name = event.name();
            types = ImmutableList.copyOf(SrvaEventTypeEnum.getBySrvaEvent(event));
            results = unmodifiableList(SrvaResultEnum.getBySrvaEvent(event));

            this.methods = unmodifiableList(SrvaMethodEnum.getBySrvaEvent(event).stream()
                    .map(SrvaMethodDTO::new)
                    .collect(toList()));
        }
    }

}
