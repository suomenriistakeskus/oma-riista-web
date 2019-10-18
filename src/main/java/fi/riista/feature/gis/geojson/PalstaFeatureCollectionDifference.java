package fi.riista.feature.gis.geojson;

import com.google.common.collect.ImmutableList;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class PalstaFeatureCollectionDifference {
    private static final Pattern FEATURE_ID_PATTERN = Pattern.compile("[1-9]\\d*");

    private static boolean isPalstaFeature(final Feature feature) {
        return feature.getId() != null && FEATURE_ID_PATTERN.matcher(feature.getId()).matches();
    }

    private static boolean isFeatureFixed(final Feature feature) {
        return Boolean.TRUE.equals(feature.getProperty(GeoJSONConstants.PROPERTY_FIXED));
    }

    public static PalstaFeatureCollectionDifference create(final FeatureCollection featureCollection,
                                                           final List<Integer> featureCurrentlySelected) {
        final List<Feature> palstaFeatures = featureCollection.getFeatures().stream()
                .filter(PalstaFeatureCollectionDifference::isPalstaFeature)
                .collect(toList());

        final Set<Integer> featureStillIncluded = palstaFeatures.stream()
                .map(Feature::getId)
                .map(Integer::parseInt)
                .collect(toSet());

        // All fixed MML features must be re-created (delete + insert)
        final Set<Integer> featureMustBeReplaced = palstaFeatures.stream()
                .filter(PalstaFeatureCollectionDifference::isFeatureFixed)
                .map(Feature::getId)
                .map(Integer::parseInt)
                .collect(toSet());

        final Set<Integer> toAdd = new HashSet<>(featureStillIncluded);
        toAdd.removeAll(featureCurrentlySelected);
        toAdd.addAll(featureMustBeReplaced);

        final Set<Integer> toRemove = new HashSet<>(featureCurrentlySelected);
        toRemove.removeAll(featureStillIncluded);
        toRemove.addAll(featureMustBeReplaced);

        return new PalstaFeatureCollectionDifference(toRemove, toAdd);
    }

    private final List<Integer> toRemove;
    private final List<Integer> toInsert;

    private PalstaFeatureCollectionDifference(final Iterable<Integer> toRemove, final Iterable<Integer> toInsert) {
        this.toRemove = ImmutableList.copyOf(toRemove);
        this.toInsert = ImmutableList.copyOf(toInsert);
    }

    public List<Integer> getRemovable() {
        return toRemove;
    }

    public List<Integer> getInsertable() {
        return toInsert;
    }
}
