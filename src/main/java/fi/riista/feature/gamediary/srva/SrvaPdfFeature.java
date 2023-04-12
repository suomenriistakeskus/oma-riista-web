package fi.riista.feature.gamediary.srva;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodRepository;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenRepository;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import io.vavr.Tuple2;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class SrvaPdfFeature {

    private static final Logger LOG = LoggerFactory.getLogger(SrvaPdfFeature.class);

    private static final String JSP_SRVA_REPORT = "pdf/srva-report";

    private static final int ATTACHMENT_SIZE = 600;

    private static final int MAP_LATITUDE_OFFSET = 12500;
    private static final int MAP_LONGITUDE_OFFSET = 12500;

    private static final int EVENT_MAP_WIDTH = 2000;
    private static final int EVENT_MAP_HEIGHT = 2000;

    private static final int FINLAND_MAP_WIDTH = 400;
    private static final int FINLAND_MAP_HEIGHT = 500;

    private static final double FINLAND_WEST = 61687;
    private static final double FINLAND_EAST = 732908;
    private static final double FINLAND_SOUTH = 6609490;
    private static final double FINLAND_NORTH = 7776450;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private SrvaMethodRepository srvaMethodRepository;

    @Resource
    private SrvaSpecimenRepository srvaSpecimenRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

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
    public PdfModel getPdfModel(final long srvaEventId, final Locale locale) {
        final SrvaEvent event = requireEntityService.requireSrvaEvent(srvaEventId, EntityPermission.READ);
        final Set<SrvaMethodDTO> methods = srvaMethodRepository.findByEventAndIsChecked(event, true).stream()
                .map(SrvaMethodDTO::create)
                .collect(Collectors.toSet());
        final PersonWithNameDTO author = PersonWithNameDTO.create(event.getAuthor());
        final GameSpeciesDTO species = GameSpeciesDTO.create(event.getSpecies());
        final Set<SrvaSpecimenDTO> specimens = srvaSpecimenRepository.findByEventOrderById(event).stream()
                .map(SrvaSpecimenDTO::create)
                .collect(Collectors.toSet());
        final String rhy = locale.getLanguage().equals("sv") ? event.getRhy().getNameSwedish() : event.getRhy().getNameFinnish();
        final PersonWithNameDTO approver = F.mapNullable(event.getApproverAsPerson(), PersonWithNameDTO::create);
        final String activeUser =
                F.mapNullable(activeUserService.requireActiveUser(), p -> p.getFirstName() + " " + p.getLastName());
        final boolean isModerator = activeUserService.isModeratorOrAdmin();

        final Set<String> imageURLs = event.getImages().stream()
                .map(i -> "/api/v1/gamediary/image/" +
                        i.getFileMetadata().getId() +
                        "/resize/" + ATTACHMENT_SIZE + "x" + ATTACHMENT_SIZE + "x1")
                .collect(Collectors.toSet());

        final GeoLocation geoLocation = event.getGeoLocation();
        final double minLat = geoLocation.getLatitude() - MAP_LATITUDE_OFFSET;
        final double maxLat = geoLocation.getLatitude() + MAP_LATITUDE_OFFSET;
        final double minLng = geoLocation.getLongitude() - MAP_LONGITUDE_OFFSET;
        final double maxLng = geoLocation.getLongitude() + MAP_LONGITUDE_OFFSET;
        final byte[] map = renderPdf(
                new double[]{minLng, minLat, maxLng, maxLat},
                EVENT_MAP_WIDTH,
                EVENT_MAP_HEIGHT,
                "maasto8",
                null,
                geoLocation);
        final String map64Encoded = Base64.getEncoder().encodeToString(map);

        final Polygon area = GISUtils.createPolygon(geoLocation, Arrays.asList(
                new Tuple2<>(-MAP_LATITUDE_OFFSET, -MAP_LONGITUDE_OFFSET),
                new Tuple2<>(MAP_LATITUDE_OFFSET, -MAP_LONGITUDE_OFFSET),
                new Tuple2<>(MAP_LATITUDE_OFFSET, MAP_LONGITUDE_OFFSET),
                new Tuple2<>(-MAP_LATITUDE_OFFSET, MAP_LONGITUDE_OFFSET)));
        final double finlandMiddle = FINLAND_SOUTH + (FINLAND_NORTH - FINLAND_SOUTH) / 2;
        double southBoundary;
        double northBoundary;
        if (minLat > finlandMiddle && maxLat > finlandMiddle) {
            southBoundary = finlandMiddle;
            northBoundary = FINLAND_NORTH;
        } else if (minLat < finlandMiddle && maxLat < finlandMiddle) {
            southBoundary = FINLAND_SOUTH;
            northBoundary = finlandMiddle;
        } else {
            southBoundary = FINLAND_SOUTH;
            northBoundary = maxLat + 2 * MAP_LATITUDE_OFFSET;
        }

        final byte[] mapFinland = renderPdf(
                new double[]{FINLAND_WEST, southBoundary, FINLAND_EAST, northBoundary},
                FINLAND_MAP_WIDTH,
                FINLAND_MAP_HEIGHT,
                "maasto2",
                area,
                null);
        final String mapFinland64Encoded = Base64.getEncoder().encodeToString(mapFinland);

        final SrvaEventReportDTO reportDTO = SrvaEventReportDTO.create(event, methods, author, species, specimens, rhy,
                approver, activeUser, isModerator, imageURLs, map64Encoded, mapFinland64Encoded, locale.getLanguage());
        return new PdfModel(JSP_SRVA_REPORT, reportDTO);
    }

    private static String getRemoteUri(final URI baseUri, final int width, final int height, final String background) {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/get-map/" + width + "x" + height + "/" + background)
                .build()
                .toUri()
                .toASCIIString();
    }

    private byte[] renderPdf(final double[] bbox, final int width, final int height, final String background, final Polygon area, final GeoLocation markerPoint) {
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(GISUtils.SRID.ETRS_TM35FIN.getGeoJsonCrs());
        featureCollection.setBbox(bbox);

        if (area != null) {
            final Feature feature = new Feature();
            feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(area));
            feature.setBbox(bbox);

            feature.setProperty("fill", "rgb(255, 0, 0)");
            feature.setProperty("fill-opacity", 1.0);
            feature.setProperty("stroke-width", 2.0);
            feature.setProperty("stroke", "rgb(0,0,0)");

            featureCollection.add(feature);
        }

        if (markerPoint != null) {
            final Feature feature = new Feature();
            final Point point = GISUtils.createPoint(markerPoint);
            feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(point));
            feature.setProperty("markerType", "srva");

            featureCollection.add(feature);
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
