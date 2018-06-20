package fi.riista.feature.gamediary.srva;

import fi.riista.util.LocalisedEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum SrvaResultEnum implements LocalisedEnum {

    ANIMAL_FOUND_DEAD(SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL),
    ANIMAL_FOUND_AND_TERMINATED(SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL),
    ANIMAL_FOUND_AND_NOT_TERMINATED(SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL),
    ACCIDENT_SITE_NOT_FOUND(SrvaEventNameEnum.ACCIDENT),

    ANIMAL_TERMINATED(SrvaEventNameEnum.DEPORTATION),
    ANIMAL_DEPORTED(SrvaEventNameEnum.DEPORTATION),

    ANIMAL_NOT_FOUND(SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL, SrvaEventNameEnum.DEPORTATION),
    UNDUE_ALARM(SrvaEventNameEnum.ACCIDENT, SrvaEventNameEnum.INJURED_ANIMAL, SrvaEventNameEnum.DEPORTATION);

    private List<SrvaEventNameEnum> eventLinks;

    SrvaResultEnum(final SrvaEventNameEnum... events) {
        this.eventLinks = Collections.unmodifiableList(Arrays.asList(events));
    }

    public static List<SrvaResultEnum> getBySrvaEvent(final SrvaEventNameEnum event) {
        return Stream.of(SrvaResultEnum.values())
                .filter(t -> t.eventLinks.contains(event))
                .collect(toList());
    }

}
