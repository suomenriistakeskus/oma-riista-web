package fi.riista.feature.gis.geojson;

import com.google.common.base.CharMatcher;
import com.google.common.primitives.Ints;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class PalstaFeatureCollection {
    private static final CharMatcher DIGIT_MATCHER = CharMatcher.inRange('0', '9');

    private final int[] current;
    private final int[] toRetain;
    private final int[] toReplace;

    public PalstaFeatureCollection(final FeatureCollection featureCollection,
                                   final List<Integer> currentList) {
        this.current = Ints.toArray(currentList);

        final List<Feature> palstaFeatures = featureCollection.getFeatures().stream()
                .filter(f -> f.getId() != null && DIGIT_MATCHER.matchesAllOf(f.getId()))
                .collect(toList());

        this.toRetain = palstaFeatures.stream()
                .mapToInt(f -> Integer.parseInt(f.getId()))
                .toArray();

        this.toReplace = palstaFeatures.stream()
                .filter(f -> Boolean.TRUE.equals(f.getProperty(GeoJSONConstants.PROPERTY_FIXED)))
                .mapToInt(f -> Integer.parseInt(f.getId()))
                .toArray();
    }

    public int[] getToRemove() {
        final Set<Integer> result = new HashSet<>(Ints.asList(current));
        result.removeAll(Ints.asList(toRetain));
        result.addAll(Ints.asList(toReplace));
        return Ints.toArray(result);
    }

    public int[] getToAdd() {
        final Set<Integer> result = new HashSet<>(Ints.asList(toRetain));
        result.removeAll(Ints.asList(current));
        result.addAll(Ints.asList(toReplace));
        return Ints.toArray(result);
    }
}
