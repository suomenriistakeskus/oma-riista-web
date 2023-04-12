package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.rhy.GISRiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.util.DateUtil;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.joda.time.LocalDate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static fi.riista.security.EntityPermission.READ;
import static java.util.Objects.requireNonNull;

@Service
public class HuntingControlEventPdfFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HuntingControlEventPdfFeature.class);
    private static final String JSP_HUNTING_CONTROL_REPORT = "pdf/hunting-control-report";

    @Resource
    private HuntingControlEventRepository huntingControlEventRepository;

    @Resource
    private HuntingControlEventDTOTransformer dtoTransformer;

    @Resource
    private GISRiistanhoitoyhdistysRepository gisRiistanhoitoyhdistysRepository;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private RestTemplate restTemplate;

    public static class PdfModel {
        private final String view;
        private final Object model;

        PdfModel(final String view, final Object model) {
            this.view = requireNonNull(view);
            this.model = requireNonNull(model);
        }

        public String getView() {
            return view;
        }

        public Object getModel() {
            return model;
        }
    }

    @Transactional(readOnly = true)
    public PdfModel getPdfModel(final long rhyId, final HuntingControlEventReportQueryDTO filters, final Locale locale) {
        // A4 paper width
        final double EVENT_MAP_WIDTH = 0.21;
        final double EVENT_MAP_HEIGHT = 0.15;

        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, READ);
        final Organisation rka = rhy.getParentOrganisation();


        final LocalDate currentDate = DateUtil.today();
        final LocalDate reportStartDate = new LocalDate(filters.getYear(), 1, 1);
        final LocalDate reportEndDate = currentDate.getYear() == filters.getYear() ?
                currentDate : new LocalDate(filters.getYear(), 12, 31);

        final LocalisedString rkaName = LocalisedString.of(rka.getNameFinnish(), rka.getNameSwedish());
        final LocalisedString rhyName = LocalisedString.of(rhy.getNameFinnish(), rhy.getNameSwedish());

        final List<HuntingControlEvent> events = huntingControlEventRepository.findReportEvents(rhy, filters);

        final String map64Encoded = calculateBboxAndRenderMap(EVENT_MAP_WIDTH, EVENT_MAP_HEIGHT, rhy, events);

        final HuntingControlEventReportDTO reportDTO = new HuntingControlEventReportDTO(
                rkaName.getTranslation(locale),
                rhyName.getTranslation(locale),
                dtoTransformer.apply(events),
                map64Encoded,
                currentDate,
                reportStartDate,
                reportEndDate);

        return new PdfModel(JSP_HUNTING_CONTROL_REPORT, reportDTO);
    }

    private String calculateBboxAndRenderMap(final double EVENT_MAP_WIDTH, final double EVENT_MAP_HEIGHT,
                                             final Riistanhoitoyhdistys rhy, final List<HuntingControlEvent> events) {
        if (events.isEmpty()) {
            return null;
        }

        final List<GeoLocation> geoLocations = events.stream()
                .map(HuntingControlEvent::getGeoLocation)
                .collect(Collectors.toList());
        final List<GISBounds> rhyBoundsList =
                gisRiistanhoitoyhdistysRepository.queryRhyBounds(rhy.getOfficialCode(), GISUtils.SRID.ETRS_TM35FIN);
        final GISBounds rhyBounds = rhyBoundsList.get(0);
        final GISBounds markerBounds = getMarkerBounds(geoLocations);
        final GISBounds bounds = getCombinedBounds(rhyBounds, markerBounds);
        final int zoomLayer = MapPdfRemoteService.zoomLayer(bounds.toBBox(), EVENT_MAP_WIDTH, EVENT_MAP_HEIGHT);
        updateBounds(bounds, markerBounds, zoomLayer);

        final Geometry rhyGeometry =
                gisRiistanhoitoyhdistysRepository.queryInvertedRhyGeom(rhy.getOfficialCode(), GISUtils.SRID.ETRS_TM35FIN);

        final byte[] map = renderPdf(
                bounds.toBBox(),
                (int) MapPdfRemoteService.calculateMapWidthInPixels(EVENT_MAP_WIDTH),
                (int) MapPdfRemoteService.calculateMapHeightInPixels(EVENT_MAP_HEIGHT),
                "maasto" + zoomLayer,
                rhyGeometry,
                events);
        final String map64Encoded = Base64.getEncoder().encodeToString(map);
        return map64Encoded;
    }

    private static String getRemoteUri(final URI baseUri, final int width, final int height, final String background) {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/get-map/" + width + "x" + height + "/" + background)
                .build()
                .toUri()
                .toASCIIString();
    }

    private GISBounds getMarkerBounds(final List<GeoLocation> geoLocations) {
        final double minLat = Collections.min(geoLocations, Comparator.comparing(GeoLocation::getLatitude)).getLatitude();
        final double maxLat = Collections.max(geoLocations, Comparator.comparing(GeoLocation::getLatitude)).getLatitude();
        final double minLng = Collections.min(geoLocations, Comparator.comparing(GeoLocation::getLongitude)).getLongitude();
        final double maxLng = Collections.max(geoLocations, Comparator.comparing(GeoLocation::getLongitude)).getLongitude();

        return new GISBounds(minLng, minLat, maxLng, maxLat);
    }

    private GISBounds getCombinedBounds(final GISBounds rhyBounds, final GISBounds markerBounds) {
        final GISBounds bounds =
                new GISBounds(rhyBounds.getMinLng(), rhyBounds.getMinLat(), rhyBounds.getMaxLng(), rhyBounds.getMaxLat());

        if (rhyBounds.getMinLng() > markerBounds.getMinLng()) {
            bounds.setMinLng(markerBounds.getMinLng());
        }
        if (rhyBounds.getMaxLng() < markerBounds.getMaxLng()) {
            bounds.setMaxLng(markerBounds.getMaxLng());
        }
        if (rhyBounds.getMinLat() > markerBounds.getMinLat()) {
            bounds.setMinLat(markerBounds.getMinLat());
        }
        if (rhyBounds.getMaxLat() < markerBounds.getMaxLat()) {
            bounds.setMaxLat(markerBounds.getMaxLat());
        }

        return bounds;
    }

    final void updateBounds(final GISBounds bounds, final GISBounds markerBounds, final int zoomLayer) {
        // Make sure the whole marker is also in visible map area

        // For ETRS-TM35FIN following applies
        // res = metre / pixel = pow(2, 13 - zoom)
        // zoom = 13 - ln2(res)
        // Hard coded marker height and width from mapexport
        final double markerHeight = 32.1 * Math.pow(2, 13 - zoomLayer);
        final double markerWidth = 24.1 / 2 * Math.pow(2, 13 - zoomLayer);

        // Increase bounding box if marker is closer than marker size to border
        final double minLatWithMargin = bounds.getMinLat() + markerHeight;
        final double maxLatWithMargin = bounds.getMaxLat() - markerHeight;
        final double minLngWithMargin = bounds.getMinLng() + markerWidth;
        final double maxLngWithMargin = bounds.getMaxLng() - markerWidth;

        if (markerBounds.getMinLat() < minLatWithMargin) {
            bounds.setMinLat(bounds.getMinLat() - markerHeight);
        }
        if (markerBounds.getMaxLat() > maxLatWithMargin) {
            bounds.setMaxLat(bounds.getMaxLat() + markerHeight);
        }
        if (markerBounds.getMinLng() < minLngWithMargin) {
            bounds.setMinLng(bounds.getMinLng() - markerWidth);
        }
        if (markerBounds.getMaxLng() > maxLngWithMargin) {
            bounds.setMaxLng(bounds.getMaxLng() + markerWidth);
        }
    }

    private byte[] renderPdf(final double[] bbox,
                             final int width,
                             final int height,
                             final String background,
                             final Geometry rhyGeometry,
                             final List<HuntingControlEvent> events) {
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(GISUtils.SRID.ETRS_TM35FIN.getGeoJsonCrs());
        featureCollection.setBbox(bbox);

        if (rhyGeometry != null) {
            final GeoJsonObject rhyGeometryGeoJson = PolygonConversionUtil.javaToGeoJSON(rhyGeometry);
            final Feature area = new Feature();
            area.setGeometry(rhyGeometryGeoJson);
            area.setBbox(bbox);
            area.setProperty("fill", "rgb(125, 125, 125)");
            area.setProperty("fill-opacity", 0.4);
            area.setProperty("stroke-width", 2.0);
            area.setProperty("stroke", "rgb(0,0,0)");
            featureCollection.add(area);
        }

        if (events != null && !events.isEmpty()) {
            events.forEach(event -> {
                final Feature feature = new Feature();
                final Point point = GISUtils.createPoint(event.getGeoLocation());
                feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(point));
                feature.setProperty("markerType", "hunting-control");
                feature.setProperty("eventStatus", event.getStatus());

                featureCollection.add(feature);
            });
        }

        final String url = getRemoteUri(runtimeEnvironmentUtil.getMapExportEndpoint(), width, height, background);

        try {
            return callRemoteService(url, featureCollection);

        } catch (final Exception ex) {
            if (ex instanceof ResourceAccessException) {
                LOG.warn("Resource access exception occurred, retrying once.");
                delayBeforeRetry();
                return callRemoteService(url, featureCollection);
            }
            LOG.error("Failed to get map", ex);
            throw new RuntimeException("Failed to get map");
        }
    }

    private byte[] callRemoteService(final String url, final FeatureCollection features) {
            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.IMAGE_PNG));
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            requestHeaders.set("Accept-Encoding", "gzip");

            final HttpEntity<FeatureCollection> requestEntity = new HttpEntity<>(features, requestHeaders);
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class).getBody();
    }

    private void delayBeforeRetry() {
        try {
            Thread.sleep(500);
        } catch (final InterruptedException ie) {
            LOG.warn("Sleep interrupted.");
        }
    }

}
