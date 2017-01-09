package fi.riista.feature.gamediary.srva;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum SrvaEventTypeEnum {

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

    public static List<SrvaEventTypeEnum> getBySrvaEvent(final SrvaEventNameEnum event) {
        return Stream.of(SrvaEventTypeEnum.values())
                .filter(t -> t.eventLinks.contains(event))
                .collect(toList());
    }

}
