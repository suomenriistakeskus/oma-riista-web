package fi.riista.feature.organization.rhy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Range;
import com.google.common.collect.Streams;
import io.vavr.Tuple;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class MergedRhyMapping {

    public static final String OLD_HAUKIVUORI_054 = "054";
    public static final String OLD_HEINÄVESI_056 = "056";
    public static final String OLD_KANGASLAMMI_060 = "060";
    public static final String OLD_PUNKAHARJU_067 = "067";
    public static final String OLD_SAVONLINNA_074 = "074";
    public static final String OLD_VIRTASALMI_075 = "075";
    public static final String OLD_NURMO_325 = "325";
    public static final String OLD_SEINÄJOKI_328 = "328";
    public static final String OLD_HALSUA_304 = "304";
    public static final String OLD_KAUSTINEN_314 = "314";
    public static final String OLD_VETELI_331 = "331";
    public static final String OLD_TENHOLA_631 = "631";
    public static final String OLD_INKOO_SNAPPERTUNA_623 = "623";
    public static final String OLD_TAMMISAARI_619 = "619";
    public static final String OLD_HANKONIEMI_621 = "621";
    public static final String OLD_KARJAA_624 = "624";
    public static final String OLD_POHJA_628 = "628";
    public static final String OLD_SIUNTIO_630 = "630";
    public static final String OLD_HÄMEENLINNA_006 = "006";
    public static final String OLD_RENKO_013 = "013";
    public static final String OLD_TAMPERE_376 = "376";
    public static final String OLD_TEISKO_378 = "378";
    public static final String OLD_TUUSNIEMI_470 = "470";
    public static final String OLD_RIISTAVESI_465 = "465";
    public static final String OLD_NURMES_408 = "408";
    public static final String OLD_VALTIMO_415 = "415";
    public static final String OLD_JÄPPILÄ_059 = "059";
    public static final String OLD_PIEKSÄMÄKI_066 = "066";
    public static final String OLD_KESÄLAHTI_405 = "405";
    public static final String OLD_KITEE_406 = "406";

    public static final String NEW_HAUKIVUORI_VIRTASALMI_076 = "076";
    public static final String NEW_SAVONLINNA_077 = "077";
    public static final String NEW_ETELÄ_SOISALO_078 = "078";
    public static final String NEW_LAKEUS_334 = "334";
    public static final String NEW_PERHOJOKILAAKSO_335 = "335";
    public static final String NEW_LÄNSI_UUSIMAA_632 = "632";
    public static final String NEW_RENGON_SEUTU_017 = "017";
    public static final String NEW_TAMPERE_382 = "382";
    public static final String NEW_KOILLIS_SAVO_475 = "475";
    public static final String NEW_YLÄ_KARJALA_416 = "416";
    public static final String NEW_PIEKSÄMÄKI_079 = "079";
    public static final String NEW_KESKI_KARJALA_417 = "417";

    /* package */ static final List<RhyMerge> OFFICIAL_MERGES = asList(
            new RhyMerge(2014, OLD_HAUKIVUORI_054, NEW_HAUKIVUORI_VIRTASALMI_076),
            new RhyMerge(2014, OLD_VIRTASALMI_075, NEW_HAUKIVUORI_VIRTASALMI_076),
            new RhyMerge(2014, OLD_PUNKAHARJU_067, NEW_SAVONLINNA_077),
            new RhyMerge(2014, OLD_SAVONLINNA_074, NEW_SAVONLINNA_077),
            new RhyMerge(2014, OLD_NURMO_325, NEW_LAKEUS_334),
            new RhyMerge(2014, OLD_SEINÄJOKI_328, NEW_LAKEUS_334),
            new RhyMerge(2019, OLD_HEINÄVESI_056, NEW_ETELÄ_SOISALO_078),
            new RhyMerge(2019, OLD_KANGASLAMMI_060, NEW_ETELÄ_SOISALO_078),
            new RhyMerge(2020, OLD_HALSUA_304, NEW_PERHOJOKILAAKSO_335),
            new RhyMerge(2020, OLD_KAUSTINEN_314, NEW_PERHOJOKILAAKSO_335),
            new RhyMerge(2020, OLD_VETELI_331, NEW_PERHOJOKILAAKSO_335),
            new RhyMerge(2020, OLD_TENHOLA_631, NEW_LÄNSI_UUSIMAA_632),
            new RhyMerge(2020, OLD_INKOO_SNAPPERTUNA_623, NEW_LÄNSI_UUSIMAA_632),
            new RhyMerge(2020, OLD_TAMMISAARI_619, NEW_LÄNSI_UUSIMAA_632),
            new RhyMerge(2020, OLD_HANKONIEMI_621, NEW_LÄNSI_UUSIMAA_632),
            new RhyMerge(2020, OLD_KARJAA_624, NEW_LÄNSI_UUSIMAA_632),
            new RhyMerge(2020, OLD_POHJA_628, NEW_LÄNSI_UUSIMAA_632),
            new RhyMerge(2020, OLD_SIUNTIO_630, NEW_LÄNSI_UUSIMAA_632),
            new RhyMerge(2020, OLD_HÄMEENLINNA_006, NEW_RENGON_SEUTU_017),
            new RhyMerge(2020, OLD_RENKO_013, NEW_RENGON_SEUTU_017),
            new RhyMerge(2020, OLD_TAMPERE_376, NEW_TAMPERE_382),
            new RhyMerge(2020, OLD_TEISKO_378, NEW_TAMPERE_382),
            new RhyMerge(2020, OLD_TUUSNIEMI_470, NEW_KOILLIS_SAVO_475),
            new RhyMerge(2020, OLD_RIISTAVESI_465, NEW_KOILLIS_SAVO_475),
            new RhyMerge(2020, OLD_NURMES_408, NEW_YLÄ_KARJALA_416),
            new RhyMerge(2020, OLD_VALTIMO_415, NEW_YLÄ_KARJALA_416),
            new RhyMerge(2023, OLD_JÄPPILÄ_059, NEW_PIEKSÄMÄKI_079),
            new RhyMerge(2023, OLD_PIEKSÄMÄKI_066, NEW_PIEKSÄMÄKI_079),
            new RhyMerge(2023, OLD_KESÄLAHTI_405, NEW_KESKI_KARJALA_417),
            new RhyMerge(2023, OLD_KITEE_406, NEW_KESKI_KARJALA_417));

    private static List<RhyMerge> MERGES;
    private static Map<String, String> MERGE_MAPPINGS;

    private static List<RhyExistence> MERGED_RHY_EXISTENCIES;

    static {
        initMerges(OFFICIAL_MERGES);
    }

    // For testing
    /*package*/ static void initMerges(final List<RhyMerge> merges) {
        MERGES = merges;
        MERGE_MAPPINGS = MERGES.stream().collect(toImmutableMap(m -> m.oldRhyCode, m -> m.newRhyCode));
        final Stream<RhyExistence> oldExistencies = MERGES
                .stream()
                .map(m -> new RhyExistence(m.oldRhyCode, Range.lessThan(m.yearOfMerge)));

        final Stream<RhyExistence> newExistencies = MERGES
                .stream()
                .map(m -> Tuple.of(m.newRhyCode, m.yearOfMerge))
                .distinct()
                .map(pair -> pair.map2(Range::atLeast).apply(RhyExistence::new));

        MERGED_RHY_EXISTENCIES = Streams
                .concat(oldExistencies, newExistencies)
                .sorted(comparing(e -> e.rhyCode))
                .collect(toImmutableList());
    }

    public static String translateIfMerged(final String rhyCode) {
        return MERGE_MAPPINGS.getOrDefault(rhyCode, rhyCode);
    }

    public static Set<String> getOldRhyCodes() {
        return MERGE_MAPPINGS.keySet();
    }

    public static Set<String> getOfficialCodesOfRhysNotExistingAtYear(final int year) {
        return MERGED_RHY_EXISTENCIES
                .stream()
                .filter(e -> !e.existsAtYear(year))
                .map(e -> e.rhyCode)
                .collect(toSet());
    }

    // Merge function takes two parameters: (1) new RHY code and (2) collection of objects to merge.
    public static <T> ImmutableMap<String, T> transformMerged(@Nonnull final Map<String, T> preMergeIndexByRhyCode,
                                                              final int year,
                                                              @Nonnull final BiFunction<String, Collection<T>, T> mergeFunction) {
        requireNonNull(preMergeIndexByRhyCode);
        requireNonNull(mergeFunction);

        final HashMap<String, T> results = new HashMap<>();
        final HashSet<String> allOldRhyCodes = new HashSet<>();

        getInverseMappingFromNewRhyCodeToOldOnes(year).forEach((newRhyCode, oldRhyCodes) -> {

            final List<T> objectsAssociatedWithOldRhyCodes = oldRhyCodes
                    .stream()
                    .map(preMergeIndexByRhyCode::get)
                    .filter(Objects::nonNull)
                    .collect(toList());

            results.put(newRhyCode, mergeFunction.apply(newRhyCode, objectsAssociatedWithOldRhyCodes));

            allOldRhyCodes.addAll(oldRhyCodes);
        });

        preMergeIndexByRhyCode.forEach((rhyCode, object) -> {

            if (!allOldRhyCodes.contains(rhyCode)) {
                results.put(rhyCode, object);
            }
        });

        return ImmutableSortedMap.copyOf(results);
    }

    public static List<Integer> getExistenceRangeOrDefault(final String rhyCode, final int min, final int max) {
        final Range<Integer> existenceRange = getExistenceRange(rhyCode);

        final int lowerBound = Optional.ofNullable(existenceRange)
                .filter(Range::hasLowerBound)
                .map(Range::lowerEndpoint)
                .filter(lowerEndpoint -> lowerEndpoint > min)
                .orElse(min);

        final int upperBound = Optional.ofNullable(existenceRange)
                .filter(Range::hasUpperBound)
                .map(range -> range.upperEndpoint() - 1)
                .filter(upperEndpoint -> upperEndpoint < max)
                .orElse(max);

        return lowerBound <= upperBound && lowerBound <= max && upperBound >= min ?
                IntStream.rangeClosed(lowerBound, upperBound).boxed().collect(toList()) :
                emptyList();
    }

    private static Range<Integer> getExistenceRange(final String rhyCode) {
        return MERGED_RHY_EXISTENCIES.stream()
                .filter(rhyExistence -> rhyExistence.rhyCode.equals(rhyCode))
                .findFirst()
                .map(rhyExistence -> rhyExistence.yearRangeOfExistence)
                .orElse(null);
    }

    private static Map<String, Set<String>> getInverseMappingFromNewRhyCodeToOldOnes(final int year) {
        return MERGES
                .stream()
                .filter(m -> m.yearOfMerge == year)
                .collect(groupingBy(m -> m.newRhyCode, mapping(m -> m.oldRhyCode, toSet())));
    }

    public static class RhyMerge {

        public static RhyMerge create(final int yearOfMerge, final String oldRhyCode, final String newRhyCode) {
            return new RhyMerge(yearOfMerge, oldRhyCode, newRhyCode);
        }

        public final int yearOfMerge;
        public final String oldRhyCode;
        public final String newRhyCode;

        RhyMerge(final int yearOfMerge, final String oldRhyCode, final String newRhyCode) {
            this.yearOfMerge = yearOfMerge;
            this.oldRhyCode = requireNonNull(oldRhyCode);
            this.newRhyCode = requireNonNull(newRhyCode);
        }
    }

    private static class RhyExistence {

        public final String rhyCode;
        public final Range<Integer> yearRangeOfExistence;

        public RhyExistence(final String rhyCode, final Range<Integer> yearRangeOfExistence) {
            this.rhyCode = requireNonNull(rhyCode);
            this.yearRangeOfExistence = requireNonNull(yearRangeOfExistence);
        }

        public boolean existsAtYear(final int year) {
            return yearRangeOfExistence.contains(year);
        }
    }

    private MergedRhyMapping() {
        throw new AssertionError();
    }
}
