package fi.riista.feature.gamediary.srva;

import fi.riista.util.LocalisedEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum SrvaEventTypeEnum implements LocalisedEnum {

    TRAFFIC_ACCIDENT(SrvaEventNameEnum.ACCIDENT),
    RAILWAY_ACCIDENT(SrvaEventNameEnum.ACCIDENT),

    ANIMAL_NEAR_HOUSES_AREA(SrvaEventNameEnum.DEPORTATION),
    ANIMAL_AT_FOOD_DESTINATION(SrvaEventNameEnum.DEPORTATION),

    INJURED_ANIMAL(SrvaEventNameEnum.INJURED_ANIMAL),
    ANIMAL_ON_ICE(SrvaEventNameEnum.INJURED_ANIMAL),

    OTHER(SrvaEventNameEnum.DEPORTATION, SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL);

    private final List<SrvaEventNameEnum> eventLinks;

    SrvaEventTypeEnum(final SrvaEventNameEnum... events) {
        this.eventLinks = Collections.unmodifiableList(Arrays.asList(events));
    }

    public static EnumSet<SrvaEventTypeEnum> getBySrvaEvent(final SrvaEventNameEnum event) {
        return EnumSet.copyOf(Stream.of(SrvaEventTypeEnum.values())
                .filter(t -> t.matchesEventName(event))
                .collect(toList()));
    }

    public boolean matchesEventName(final SrvaEventNameEnum event) {
        return this.eventLinks.contains(event);
    }

}
