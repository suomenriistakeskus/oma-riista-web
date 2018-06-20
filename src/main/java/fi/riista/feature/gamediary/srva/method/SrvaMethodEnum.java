package fi.riista.feature.gamediary.srva.method;

import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.util.LocalisedEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum SrvaMethodEnum implements LocalisedEnum {

    DOG(SrvaEventNameEnum.DEPORTATION),
    PAIN_EQUIPMENT(SrvaEventNameEnum.DEPORTATION),
    SOUND_EQUIPMENT(SrvaEventNameEnum.DEPORTATION),

    TRACED_WITH_DOG(SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL),
    TRACED_WITHOUT_DOG(SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL),

    OTHER(SrvaEventNameEnum.DEPORTATION, SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL);

    private final List<SrvaEventNameEnum> eventLinks;

    SrvaMethodEnum(final SrvaEventNameEnum... events) {
        this.eventLinks = Collections.unmodifiableList(Arrays.asList(events));
    }

    public static List<SrvaMethodEnum> getBySrvaEvent(final SrvaEventNameEnum event) {
        return Stream.of(SrvaMethodEnum.values())
                .filter(t -> t.eventLinks.contains(event))
                .collect(toList());
    }
}
