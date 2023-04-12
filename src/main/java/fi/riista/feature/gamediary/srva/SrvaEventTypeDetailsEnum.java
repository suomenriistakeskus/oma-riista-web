package fi.riista.feature.gamediary.srva;

import fi.riista.util.LocalisedEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

public enum SrvaEventTypeDetailsEnum implements LocalisedEnum {

    CARED_HOUSE_AREA(SrvaEventTypeEnum.ANIMAL_NEAR_HOUSES_AREA),
    FARM_ANIMAL_BUILDING(SrvaEventTypeEnum.ANIMAL_NEAR_HOUSES_AREA),
    URBAN_AREA(SrvaEventTypeEnum.ANIMAL_NEAR_HOUSES_AREA),

    CARCASS_AT_FOREST(SrvaEventTypeEnum.ANIMAL_AT_FOOD_DESTINATION),
    CARCASS_NEAR_HOUSES_AREA(SrvaEventTypeEnum.ANIMAL_AT_FOOD_DESTINATION),
    GARBAGE_CAN(SrvaEventTypeEnum.ANIMAL_AT_FOOD_DESTINATION),
    BEEHIVE(SrvaEventTypeEnum.ANIMAL_AT_FOOD_DESTINATION),

    OTHER(SrvaEventTypeEnum.ANIMAL_NEAR_HOUSES_AREA, SrvaEventTypeEnum.ANIMAL_AT_FOOD_DESTINATION);

    private final List<SrvaEventTypeEnum> eventTypeLinks;

    SrvaEventTypeDetailsEnum(final SrvaEventTypeEnum... eventTypes) {
        this.eventTypeLinks = Collections.unmodifiableList(Arrays.asList(eventTypes));
    }

    public static Map<SrvaEventTypeEnum, List<SrvaEventTypeDetailsEnum>> getBySrvaEventType(final List<SrvaEventTypeEnum> types) {
        return types.stream()
                .filter(t -> !getDetailsForType(t).isEmpty())
                .collect(Collectors.toMap(t -> t, t -> getDetailsForType(t)));
    }

    private static List<SrvaEventTypeDetailsEnum> getDetailsForType(final SrvaEventTypeEnum type) {
        return Stream.of(SrvaEventTypeDetailsEnum.values())
                .filter(t -> t.matchesEventType(type))
                .collect(toList());
    }

    private boolean matchesEventType(final SrvaEventTypeEnum type) {
        return this.eventTypeLinks.contains(type);
    }
}
