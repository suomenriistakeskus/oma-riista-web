package fi.riista.feature.huntingclub.area.zone;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.metsahallitus.GISMetsahallitusRepository;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

@Component
public class HuntingClubAreaZoneFeature {

    private static final int SIMPLIFY_AMOUNT = 1;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private GISMetsahallitusRepository metsahallitusRepository;

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection geoJSON(final long clubAreaId) {
        final HuntingClubArea huntingClubArea = requireEntityService
                .requireHuntingClubArea(clubAreaId, EntityPermission.READ);

        if (huntingClubArea.getZone() != null) {
            final GISZone gisZone = huntingClubArea.getZone();
            final FeatureCollection features = zoneRepository.getPalstaFeatures(gisZone.getId(), GISUtils.SRID.WGS84);
            features.setBbox(zoneRepository.getBounds(gisZone.getId(), GISUtils.SRID.WGS84));

            final Optional<Feature> excludedFeature = gisZone.getExcludedAsGeoJSON(GeoJSONConstants.ID_EXCLUDED);
            excludedFeature.ifPresent(features::add);

            // Metsähallitus hirvialueet
            features.addAll(metsahallitusRepository.listZoneHirviFeatures(gisZone.getId(), GISUtils.SRID.WGS84));

            return features;
        }
        return new FeatureCollection();
    }

    @Transactional(timeout = 60)
    public long updateGeoJSON(final long clubAreaId, final FeatureCollection featureCollection) {
        final HuntingClubArea huntingClubArea = requireEntityService
                .requireHuntingClubArea(clubAreaId, EntityPermission.UPDATE);

        final GISZone zone = huntingClubArea.getZone() != null ? huntingClubArea.getZone() : new GISZone();
        zone.setExcludedGeom(extractExcludedFeature(featureCollection));
        zone.setMetsahallitusHirvi(extractMetsahallitusHirvi(featureCollection));
        zone.setComputedAreaSize(-1);
        zone.setWaterAreaSize(-1);
        huntingClubArea.setZone(zone);

        // Make sure modification time is updated
        zone.setModificationTimeToCurrentTime();
        huntingClubArea.setModificationTimeToCurrentTime();

        zoneRepository.saveAndFlush(zone);

        zoneRepository.updatePalstaFeatures(zone.getId(), featureCollection);

        return zone.getId();
    }

    @Async
    @Transactional(timeout = 120)
    public void updateAreaSize(final long zoneId) {
        zoneRepository.calculateAreaSize(zoneId);
    }

    private static Geometry extractExcludedFeature(
            final FeatureCollection featureCollection) {

        return featureCollection.getFeatures().stream()
                .filter(f -> f.getGeometry() != null && GeoJSONConstants.ID_EXCLUDED.equals(f.getId()))
                .findAny()
                .map(f -> PolygonConversionUtil.geoJsonToJava(f.getGeometry(), GISUtils.SRID.WGS84))
                .orElse(null);
    }

    private static Set<Integer> extractMetsahallitusHirvi(final FeatureCollection featureCollection) {
        return featureCollection.getFeatures().stream()
                .map(Feature::getId)
                .filter(Objects::nonNull)
                .filter(id -> id.startsWith(GeoJSONConstants.ID_PREFIX_MH_HIRVI))
                .map(id -> id.substring(GeoJSONConstants.ID_PREFIX_MH_HIRVI.length()))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection combinedGeoJSON(final long clubAreaId) {
        final HuntingClubArea huntingClubArea = requireEntityService
                .requireHuntingClubArea(clubAreaId, EntityPermission.READ);

        return Optional.ofNullable(huntingClubArea.getZone())
                .map(GISZone::getId)
                .map(zoneId -> zoneRepository.getCombinedFeatures(singleton(zoneId), GISUtils.SRID.WGS84, SIMPLIFY_AMOUNT))
                .orElseGet(FeatureCollection::new);
    }
}
