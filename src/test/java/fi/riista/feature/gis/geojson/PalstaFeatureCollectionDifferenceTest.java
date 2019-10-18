package fi.riista.feature.gis.geojson;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class PalstaFeatureCollectionDifferenceTest {

    @Test
    public void testIgnoreOtherFeatures() {
        final FeatureCollection featureCollection = new FeatureCollection();
        final Feature feature = new Feature();
        feature.setId("other");
        featureCollection.add(feature);

        final PalstaFeatureCollectionDifference difference =
                PalstaFeatureCollectionDifference.create(featureCollection, ImmutableList.of());

        assertEquals(ImmutableList.of(), difference.getInsertable());
        assertEquals(ImmutableList.of(), difference.getRemovable());
    }

    @Test
    public void testInsertToEmpty() {
        final Set<Integer> existing = ImmutableSet.of();
        final Set<Integer> normal = ImmutableSet.of(1, 2, 3, 4);
        final Set<Integer> fixed = ImmutableSet.of();

        final PalstaFeatureCollectionDifference difference = create(existing, normal, fixed);

        assertEquals(ImmutableList.of(1, 2, 3, 4), difference.getInsertable());
        assertEquals(ImmutableList.of(), difference.getRemovable());
    }

    @Test
    public void testInsertToEmpty_WithFixed() {
        final Set<Integer> existing = ImmutableSet.of();
        final Set<Integer> normal = ImmutableSet.of(1, 2);
        final Set<Integer> fixed = ImmutableSet.of(3, 4);

        final PalstaFeatureCollectionDifference difference = create(existing, normal, fixed);

        assertEquals(ImmutableList.of(1, 2, 3, 4), difference.getInsertable());
        assertEquals(ImmutableList.of(3, 4), difference.getRemovable());
    }

    @Test
    public void testUpdate_InsertOneRemoveOne() {
        final Set<Integer> existing = ImmutableSet.of(2, 3);
        final Set<Integer> normal = ImmutableSet.of(1, 2);
        final Set<Integer> fixed = ImmutableSet.of();

        final PalstaFeatureCollectionDifference difference = create(existing, normal, fixed);

        assertEquals(ImmutableList.of(1), difference.getInsertable());
        assertEquals(ImmutableList.of(3), difference.getRemovable());
    }

    @Test
    public void testUpdate_RemoveAll() {
        final Set<Integer> existing = ImmutableSet.of(1, 2, 3, 4);
        final Set<Integer> normal = ImmutableSet.of();
        final Set<Integer> fixed = ImmutableSet.of();

        final PalstaFeatureCollectionDifference difference = create(existing, normal, fixed);

        assertEquals(ImmutableList.of(), difference.getInsertable());
        assertEquals(ImmutableList.of(1, 2, 3, 4), difference.getRemovable());
    }

    @Test
    public void testUpdate_UpdateFixedRemoveOne() {
        final Set<Integer> existing = ImmutableSet.of(1, 2, 3, 4);
        final Set<Integer> normal = ImmutableSet.of(1, 2);
        final Set<Integer> fixed = ImmutableSet.of(3);

        final PalstaFeatureCollectionDifference difference = create(existing, normal, fixed);

        assertEquals(ImmutableList.of(3), difference.getInsertable());
        assertEquals(ImmutableList.of(3, 4), difference.getRemovable());
    }

    private static PalstaFeatureCollectionDifference create(final Set<Integer> existing,
                                                            final Set<Integer> normal,
                                                            final Set<Integer> fixed) {
        final FeatureCollection featureCollection = featureCollection(normal, fixed);

        return PalstaFeatureCollectionDifference.create(featureCollection, ImmutableList.copyOf(existing));
    }

    private static FeatureCollection featureCollection(final Set<Integer> normalPalstaIds,
                                                       final Set<Integer> fixedPalstaIds) {
        final FeatureCollection featureCollection = new FeatureCollection();

        for (final Integer id : normalPalstaIds) {
            final Feature feature = new Feature();
            feature.setId(Integer.toString(id));
            featureCollection.add(feature);
        }

        for (final Integer id : fixedPalstaIds) {
            final Feature feature = new Feature();
            feature.setId(Integer.toString(id));
            feature.setProperty(GeoJSONConstants.PROPERTY_FIXED, true);
            featureCollection.add(feature);
        }

        return featureCollection;
    }
}
