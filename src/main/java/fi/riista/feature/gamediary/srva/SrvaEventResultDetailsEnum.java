package fi.riista.feature.gamediary.srva;

import fi.riista.util.LocalisedEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

public enum SrvaEventResultDetailsEnum implements LocalisedEnum {
    ANIMAL_CONTACTED_AND_DEPORTED(SrvaResultEnum.ANIMAL_DEPORTED),
    ANIMAL_CONTACTED(SrvaResultEnum.ANIMAL_DEPORTED),
    UNCERTAIN_RESULT(SrvaResultEnum.ANIMAL_DEPORTED);

    private final List<SrvaResultEnum> resultLinks;

    SrvaEventResultDetailsEnum(final SrvaResultEnum... results) {
        this.resultLinks = Collections.unmodifiableList(Arrays.asList(results));
    }

    public static Map<SrvaResultEnum, List<SrvaEventResultDetailsEnum>> getBySrvaResult(final List<SrvaResultEnum> results) {
        return results.stream()
                .filter(t -> !getDetailsForResult(t).isEmpty())
                .collect(Collectors.toMap(t -> t, t -> getDetailsForResult(t)));
    }

    private static List<SrvaEventResultDetailsEnum> getDetailsForResult(final SrvaResultEnum result) {
        return Stream.of(SrvaEventResultDetailsEnum.values())
                .filter(t -> t.matchesEventResult(result))
                .collect(toList());
    }

    private boolean matchesEventResult(final SrvaResultEnum result) {
        return this.resultLinks.contains(result);
    }
}
