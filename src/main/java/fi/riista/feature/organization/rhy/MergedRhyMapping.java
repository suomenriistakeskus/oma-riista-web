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
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.asList;
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

    public static final String NEW_HAUKIVUORI_VIRTASALMI_076 = "076";
    public static final String NEW_SAVONLINNA_077 = "077";
    public static final String NEW_ETELÄ_SOISALO_078 = "078";
    public static final String NEW_LAKEUS_334 = "334";

    private static final List<RhyMerge> MERGES = asList(
            new RhyMerge(2014, OLD_HAUKIVUORI_054, NEW_HAUKIVUORI_VIRTASALMI_076),
            new RhyMerge(2014, OLD_VIRTASALMI_075, NEW_HAUKIVUORI_VIRTASALMI_076),
            new RhyMerge(2014, OLD_PUNKAHARJU_067, NEW_SAVONLINNA_077),
            new RhyMerge(2014, OLD_SAVONLINNA_074, NEW_SAVONLINNA_077),
            new RhyMerge(2014, OLD_NURMO_325, NEW_LAKEUS_334),
            new RhyMerge(2014, OLD_SEINÄJOKI_328, NEW_LAKEUS_334),
            new RhyMerge(2019, OLD_HEINÄVESI_056, NEW_ETELÄ_SOISALO_078),
            new RhyMerge(2019, OLD_KANGASLAMMI_060, NEW_ETELÄ_SOISALO_078));

    private static final Map<String, String> MERGE_MAPPINGS =
            MERGES.stream().collect(toImmutableMap(m -> m.oldRhyCode, m -> m.newRhyCode));

    private static final List<RhyExistence> MERGED_RHY_EXISTENCIES;

    static {
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

    private static Map<String, Set<String>> getInverseMappingFromNewRhyCodeToOldOnes(final int year) {
        return MERGES
                .stream()
                .filter(m -> m.yearOfMerge == year)
                .collect(groupingBy(m -> m.newRhyCode, mapping(m -> m.oldRhyCode, toSet())));
    }

    private static class RhyMerge {

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
