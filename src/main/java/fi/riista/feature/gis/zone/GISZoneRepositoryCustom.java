package fi.riista.feature.gis.zone;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.huntingclub.area.zone.HuntingClubAreaFeatureDTO;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface GISZoneRepositoryCustom {
    <E extends AreaEntity<Long>> Function<E, GISZoneWithoutGeometryDTO> getAreaMapping(final Iterable<E> iterable);

    Map<Long, GISZoneWithoutGeometryDTO> fetchWithoutGeometry(final Collection<Long> zoneIds);

    FeatureCollection getPalstaFeatures(long zoneId, GISUtils.SRID srid);

    void updatePalstaFeatures(long zoneId, FeatureCollection featureCollection);

    FeatureCollection getCombinedFeatures(Set<Long> zoneId, GISUtils.SRID srid);

    FeatureCollection getCombinedPolygonFeatures(long zoneId, GISUtils.SRID srid);

    GISBounds getBounds(long zoneId, GISUtils.SRID srid);

    Geometry getSimplifiedGeometry(long zoneId, GISUtils.SRID srid);

    Geometry getInvertedSimplifiedGeometry(long zoneId, GISUtils.SRID srid);

    void updateFeatures(long zoneId, GISUtils.SRID srid, List<HuntingClubAreaFeatureDTO> features);

    GISZoneSizeDTO getAreaSize(long zoneId);

    GISZoneSizeDTO getAdjustedAreaSize(long zoneId);

    void calculateAreaSize(long zoneId);

    List<GISZoneSizeRhyDTO> calculateRhyAreaSize(long zoneId);

    Map<String, Double> calculateHtaAreaSize(long zoneId);

    GISZone copyZone(GISZone from, GISZone to);

    List<Geometry> loadSplicedGeometries(final Collection<Long> zoneIds);

    void removeZonePalstaAndFeatures(GISZone zone);

    Set<Integer> getUniqueMetsahallitusYears(long zoneId);
}
