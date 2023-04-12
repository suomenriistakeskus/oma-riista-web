package fi.riista.feature.huntingclub.poi.geojson;

import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationGroup;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationRepository;
import fi.riista.feature.huntingclub.poi.gpx.GpxPoiLocationDTO;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.util.Collect.nullSafeGroupingBy;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
public class HuntingClubPoiGeoJsonExportFeature {

    @Resource
    private PoiLocationGroupRepository poiLocationGroupRepository;

    @Resource
    private HuntingClubAreaRepository areaRepository;

    @Resource
    private PoiLocationRepository poiLocationRepository;

    @Transactional(readOnly = true)
    public List<Feature> getFeatures(final String externalId) {

        final Optional<HuntingClubArea> areaOptional = areaRepository.findByExternalId(externalId);
        if (!areaOptional.isPresent()) {
            return emptyList();
        }
        final HuntingClubArea area = areaOptional.get();

        final List<Long> poiIds = areaRepository.listPois(area.getId());

        final List<PoiLocationGroup> poiGroups = poiLocationGroupRepository.findAllById(poiIds);
        // GPX points are already in WSG84 coordinates (PoiLocation is in ETRS_TM35FIN)
        final Map<Long, List<GpxPoiLocationDTO>> gpxPointsByPoiId =
                poiLocationRepository.getGpxPointsByPoiIn(poiGroups).stream()
                        .collect(nullSafeGroupingBy(GpxPoiLocationDTO::getPoiId));

        return poiGroups.stream()
                .map(e -> asGeoJsonFeature(e, gpxPointsByPoiId.get(e.getId())))
                .collect(toList());
    }

    private Feature asGeoJsonFeature(final PoiLocationGroup poiGroup, final List<GpxPoiLocationDTO> gpxPoints) {
        final Feature feature = new Feature();

        feature.setProperty("id", poiGroup.getVisibleId());
        feature.setProperty("poiType", poiGroup.getType().name());
        feature.setProperty("name", poiGroup.getDescription());
        feature.setProperty("pointProperties", getPointProperties(gpxPoints));
        final Geometry geom = getGeometry(gpxPoints);
        feature.setBbox(GISUtils.getGeoJsonBBox(geom));
        feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(geom));

        return feature;
    }

    private List<Map<String, String>> getPointProperties(final List<GpxPoiLocationDTO> poiLocations) {

        final List<Map<String, String>> pointProperties = new ArrayList<>();
        poiLocations.forEach(location -> {
            final Map<String, String> pointData = new HashMap<>();
            pointData.put("name", location.getVisibleId());
            pointData.put("notes", location.getLocationComment());
            pointProperties.add(pointData);
        });
        return pointProperties;
    }

    private Geometry getGeometry(final List<GpxPoiLocationDTO> gpxPoints) {
        final GeometryFactory geometryFactory = GISUtils.getGeometryFactory(GISUtils.SRID.WGS84);
        final Point[] poiPoints = gpxPoints.stream()
                .map(loc -> geometryFactory.createPoint(new Coordinate(loc.getLongitude(), loc.getLatitude())))
                .toArray(Point[] ::new);
        return geometryFactory.createMultiPoint(poiPoints);
    }
}
