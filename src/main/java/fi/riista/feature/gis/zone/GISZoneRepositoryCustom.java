package fi.riista.feature.gis.zone;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.huntingclub.area.zone.HuntingClubAreaFeatureDTO;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GISZoneRepositoryCustom {
    List<GISZoneWithoutGeometryDTO> fetchWithoutGeometry(final Collection<Long> zoneIds);

    FeatureCollection getPalstaFeatures(Long zoneId, GISUtils.SRID srid);

    void updatePalstaFeatures(long zoneId, FeatureCollection featureCollection);

    FeatureCollection getCombinedFeatures(Set<Long> zoneId, GISUtils.SRID srid, double simplifyAmount);

    double[] getBounds(long zoneId, GISUtils.SRID srid);

    FeatureCollection getFeatures(long zoneId, GISUtils.SRID srid);

    void updateFeatures(long zoneId, GISUtils.SRID srid, List<HuntingClubAreaFeatureDTO> features);

    void calculateAreaSize(long zoneId);

    Map<String, Double> calculateRhyAreaSize(long zoneId);

    Map<String, Double> calculateHtaAreaSize(long zoneId);

    GISZone copyZone(GISZone from, GISZone to);

    List<Geometry> loadSplicedGeometries(final Collection<Long> zoneIds);

    void removeZonePalstaAndFeatures(GISZone zone);
}
