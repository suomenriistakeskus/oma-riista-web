package fi.riista.feature.gis.zone;

import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.OnlyStateAreaService;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.metsahallitus.MetsahallitusHirviRepository;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fi.riista.feature.gis.zone.GISZoneConstants.AREA_SIZE_CALCULATION_FAILED;
import static fi.riista.feature.gis.zone.GISZoneConstants.AREA_SIZE_NOT_AVAILABLE;
import static java.util.Collections.singletonList;

@Service
public class GISZoneEditService {
    private static final GISUtils.SRID EDITOR_SRID = GISUtils.SRID.WGS84;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private MetsahallitusHirviRepository metsahallitusRepository;

    @Resource
    private OnlyStateAreaService onlyStateAreaService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection getFeatures(final GISZone zone) {
        final List<Feature> palstaFeatures = zoneRepository.getPalstaFeatures(zone.getId(), EDITOR_SRID);
        final List<Feature> otherFeatures = zoneRepository.getOtherFeatures(zone.getId(), EDITOR_SRID);
        final List<Feature> metsahallitusFeatures = metsahallitusRepository.findByZoneAsFeatures(zone.getId(),
                EDITOR_SRID);
        final Feature excludedFeature = zone.getExcludedAsGeoJSON(GeoJSONConstants.ID_EXCLUDED).orElse(null);
        final GISBounds bounds = zoneRepository.getBounds(zone.getId(), EDITOR_SRID);

        return createFeatureCollection(palstaFeatures, otherFeatures, metsahallitusFeatures, excludedFeature, bounds);
    }

    @Transactional(noRollbackFor = RuntimeException.class, propagation = Propagation.MANDATORY)
    public void storeFeatures(final FeatureCollection featureCollection, final GISZone zone) {
        zone.setExcludedGeom(extractExcludedGeometry(featureCollection));
        zone.setMetsahallitusHirvi(extractMetsahallitusHirviIdSet(featureCollection));

        // Reset area size before calculation
        zone.setComputedAreaSize(AREA_SIZE_NOT_AVAILABLE);
        zone.setWaterAreaSize(AREA_SIZE_NOT_AVAILABLE);
        zone.setStateLandAreaSize(null);
        zone.setPrivateLandAreaSize(null);

        // Make sure modification time is updated
        zone.setModificationTimeToCurrentTime();

        zoneRepository.saveAndFlush(zone);
        zoneRepository.updatePalstaFeatures(zone.getId(), featureCollection);
        zoneRepository.updateOtherFeatures(zone.getId(), featureCollection, EDITOR_SRID);
        zoneRepository.calculateCombinedGeometry(zone.getId());
    }

    // Open new transaction in order to be able to mark calculation failed in case of failure
    @Transactional(timeout = 1200, propagation = Propagation.REQUIRES_NEW)
    public void updateAreaSize(final long zoneId) {
        final boolean onlyStateLand = onlyStateAreaService.shouldContainOnlyStateLand(singletonList(zoneId));
        zoneRepository.calculateAreaSize(zoneId, onlyStateLand);
        zoneRepository.getOne(zoneId).forceRevisionUpdate();
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void markCalculationFailed(final long zoneId) {
        final GISZone zone = zoneRepository.getOne(zoneId);
        zone.setComputedAreaSize(AREA_SIZE_CALCULATION_FAILED);
        zone.setWaterAreaSize(AREA_SIZE_CALCULATION_FAILED);
        zone.setStateLandAreaSize(null);
        zone.setPrivateLandAreaSize(null);
    }

    private static FeatureCollection createFeatureCollection(final List<Feature> palstaFeatures,
                                                             final List<Feature> otherFeatures,
                                                             final List<Feature> metsahallitusFeatures,
                                                             final Feature excludedFeature,
                                                             final GISBounds bounds) {
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setBbox(bounds != null ? bounds.toBBox() : null);

        if (palstaFeatures != null) {
            featureCollection.addAll(palstaFeatures);
        }

        if (otherFeatures != null) {
            featureCollection.addAll(otherFeatures);
        }

        if (metsahallitusFeatures != null) {
            featureCollection.addAll(metsahallitusFeatures);
        }

        if (excludedFeature != null) {
            featureCollection.add(excludedFeature);
        }

        return featureCollection;
    }

    private static Geometry extractExcludedGeometry(final FeatureCollection featureCollection) {
        return findExcludedFeature(featureCollection)
                .map(Feature::getGeometry)
                .map(jsonGeom -> PolygonConversionUtil.geoJsonToJava(jsonGeom, EDITOR_SRID))
                .orElse(null);
    }

    private static Optional<Feature> findExcludedFeature(final @Nonnull FeatureCollection featureCollection) {
        return featureCollection.getFeatures().stream().filter(GISZoneEditService::isExcludedFeature).findAny();
    }

    private static boolean isExcludedFeature(final @Nonnull Feature feature) {
        return feature.getGeometry() != null && GeoJSONConstants.ID_EXCLUDED.equals(feature.getId());
    }

    private static Set<Integer> extractMetsahallitusHirviIdSet(final @Nonnull FeatureCollection featureCollection) {
        return F.mapNonNullsToSet(featureCollection.getFeatures(), GISZoneEditService::getMetsahallitusFeatureId);
    }

    private static Integer getMetsahallitusFeatureId(final @Nonnull Feature feature) {
        return Optional.ofNullable(feature.getId())
                .map(GISZoneEditService::parseMetsahallitusFeatureSuffix)
                .map(Integer::parseInt)
                .orElse(null);
    }

    private static String parseMetsahallitusFeatureSuffix(final @Nonnull String id) {
        return id.startsWith(GeoJSONConstants.ID_PREFIX_MH_HIRVI)
                ? id.substring(GeoJSONConstants.ID_PREFIX_MH_HIRVI.length())
                : null;
    }
}
