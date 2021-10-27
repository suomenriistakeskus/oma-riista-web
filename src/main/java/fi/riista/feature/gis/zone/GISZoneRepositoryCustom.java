package fi.riista.feature.gis.zone;

import fi.riista.feature.gis.GISBounds;
import fi.riista.integration.koiratutka.HuntingClubAreaImportFeatureDTO;
import fi.riista.util.GISUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GISZoneRepositoryCustom {
    Map<Long, GISZoneWithoutGeometryDTO> fetchWithoutGeometry(final Collection<Long> zoneIds);

    List<Feature> getPalstaFeatures(long zoneId, GISUtils.SRID srid);

    List<Feature> getOtherFeatures(long zoneId, GISUtils.SRID srid);

    Geometry getStateGeometry(long zoneId, GISUtils.SRID srid);

    Geometry getStateGeometry(Geometry geom, GISUtils.SRID srid);

    void updatePalstaFeatures(long zoneId, FeatureCollection featureCollection);

    void updateOtherFeatures(long zoneId, FeatureCollection featureCollection, final GISUtils.SRID srid);

    void updateExternalFeatures(long zoneId, GISUtils.SRID srid, List<HuntingClubAreaImportFeatureDTO> features);

    FeatureCollection getCombinedFeatures(Set<Long> zoneId, GISUtils.SRID srid);

    FeatureCollection getCombinedPolygonFeatures(long zoneId, GISUtils.SRID srid);

    GISBounds getBounds(long zoneId, GISUtils.SRID srid);

    Map<Long, GISBounds> getBounds(Collection<Long> zoneIds, GISUtils.SRID srid);

    Geometry getSimplifiedGeometry(long zoneId, GISUtils.SRID srid);

    Geometry getInvertedSimplifiedGeometry(long zoneId, GISUtils.SRID srid);

    void calculateCombinedGeometry(long zoneId);

    GISZoneSizeDTO getAreaSize(long zoneId);

    GISZoneSizeDTO getAdjustedAreaSize(long zoneId);

    void calculateAreaSize(long zoneId, boolean onlyStateLand);

    List<GISZoneSizeByOfficialCodeDTO> calculateRhyAreaSize(long zoneId);

    List<GISZoneSizeByOfficialCodeDTO> calculateVerotusLohkoAreaSize(int huntingYear, long zoneId);

    Map<String, Double> calculateHtaAreaSize(long zoneId);

    GISZone copyZone(GISZone from, GISZone to);

    GISZone addAreas(final GISZone orig, final GISZone added);

    List<Geometry> loadSplicedGeometries(final Collection<Long> zoneIds);

    void removeZonePalstaAndFeatures(GISZone zone);

    Set<Integer> getUniqueMetsahallitusYears(long zoneId);

    List<GISZoneMmlPropertyIntersectionDTO> findIntersectingPalsta(final long zoneId);
}
