package fi.riista.feature.huntingclub.area.print;

import com.google.common.collect.ImmutableList;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.harvestpermit.area.HarvestPermitArea;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.MissingHuntingClubAreaGeometryException;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

@Component
public class AreaPrintFeature {
    private static final Logger LOG = LoggerFactory.getLogger(AreaPrintFeature.class);

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy HH:mm");
    private static final DateTimeFormatter DTF_FILENAME = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final String FINNISH_BOUNDS = "LINESTRING(50199.4814 6582464.0358, 761274.6247 7799839.8902)";
    private static final String LAYER_NAME = "maasto";

    private static class GeometryAndSize {
        final GeoJsonObject geom;
        final double totalAreaSize;
        final double waterAreaSize;
        final double landAreaSize;

        GeometryAndSize(final GeoJsonObject geom,
                        final double totalAreaSize,
                        final double waterAreaSize) {
            this.geom = geom;
            this.totalAreaSize = totalAreaSize;
            this.waterAreaSize = waterAreaSize;
            this.landAreaSize = totalAreaSize - waterAreaSize;
        }
    }

    private NamedParameterJdbcOperations namedParameterJdbcTemplate;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private MessageSource messageSource;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Autowired
    public void setDataSource(DataSource dataSource, ClientHttpRequestFactory requestFactory) {
        this.restTemplate = new RestTemplate(requestFactory);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    private String i18n(final String key, final Locale locale) {
        return messageSource.getMessage("HuntingClubArea.pdf." + key, null, locale);
    }

    private String formatAreaSize(final Locale locale, final String key, final double areaSize) {
        return i18n(key, locale) + " " + String.format("%.2f", areaSize / 10_000) + " " + i18n("ha", locale);
    }

    @Transactional(readOnly = true)
    public String getClubAreaExportFileName(final long clubAreaId, final Locale locale) {
        final HuntingClubArea huntingClubArea = requireEntityService
                .requireHuntingClubArea(clubAreaId, EntityPermission.READ);

        return getExportFileName(locale, huntingClubArea.getNameLocalisation(), huntingClubArea);
    }

    @Transactional(readOnly = true)
    public String getHarvestPermitAreaExportFileName(final long permitAreaId, final Locale locale) {
        final HarvestPermitArea harvestPermitArea = requireEntityService
                .requireHarvestPermitArea(permitAreaId, EntityPermission.READ);

        return getExportFileName(locale, harvestPermitArea.getNameLocalisation(), harvestPermitArea);
    }

    private String getExportFileName(Locale locale, LocalisedString areaName, LifecycleEntity entity) {
        final Date modificationTime = entity.getModificationTime();
        final LocalDateTime saveDateTime = DateUtil.toLocalDateTimeNullSafe(modificationTime);
        return DTF_FILENAME.print(saveDateTime) + " " + areaName.getAnyTranslation(locale) + ".pdf";
    }

    @Transactional(readOnly = true)
    public FeatureCollection exportClubAreaFeatures(final long clubAreaId, final Locale locale) {
        final HuntingClubArea huntingClubArea = requireEntityService
                .requireHuntingClubArea(clubAreaId, EntityPermission.READ);
        final GISZone zone = huntingClubArea.getZone();

        if (zone == null) {
            throw new MissingHuntingClubAreaGeometryException();
        }

        final LocalisedString areaName = huntingClubArea.getNameLocalisation();
        final LocalisedString clubName = huntingClubArea.getClub().getNameLocalisation();
        final Date modificationTime = huntingClubArea.getModificationTime();
        return exportClubAreaFeatures(locale, zone, areaName, clubName, modificationTime);
    }

    @Transactional(readOnly = true)
    public FeatureCollection exportHarvestPermitAreaFeatures(final long permitAreaId, final Locale locale) {
        final HarvestPermitArea harvestPermitArea = requireEntityService
                .requireHarvestPermitArea(permitAreaId, EntityPermission.READ);
        final GISZone zone = harvestPermitArea.getZone();

        if (zone == null) {
            throw new MissingHuntingClubAreaGeometryException();
        }

        final LocalisedString areaName = harvestPermitArea.getNameLocalisation();
        final LocalisedString clubName = harvestPermitArea.getClub().getNameLocalisation();
        final Date modificationTime = harvestPermitArea.getModificationTime();
        return exportClubAreaFeatures(locale, zone, areaName, clubName, modificationTime);
    }

    private FeatureCollection exportClubAreaFeatures(Locale locale, GISZone zone, LocalisedString areaName, LocalisedString clubName, Date modificationTime) {
        final LocalDateTime saveDateTime = DateUtil.toLocalDateTimeNullSafe(modificationTime);
        final String saveDate = DTF.print(saveDateTime);

        final double[] bbox = gisZoneRepository.getBounds(zone.getId(), GISUtils.SRID.ETRS_TM35FIN);

        final GeometryAndSize geometryAndSize = getZoneFeature(zone.getId());

        final Feature feature = new Feature();
        feature.setGeometry(geometryAndSize.geom);
        feature.setProperty("clubName", clubName.getAnyTranslation(locale));
        feature.setProperty("saveDate", i18n("saveDate", locale) + " " + saveDate);
        feature.setProperty("areaName", i18n("areaName", locale) + " " + areaName.getAnyTranslation(locale));
        feature.setProperty("areaSize", formatAreaSize(locale, "totalAreaSize", geometryAndSize.totalAreaSize) + " " +
                formatAreaSize(locale, "landAreaSize", geometryAndSize.landAreaSize) + " " +
                formatAreaSize(locale, "waterAreaSize", geometryAndSize.waterAreaSize));
        feature.setProperty("fill", "rgb(0, 192, 60)");
        feature.setProperty("fill-opacity", 0.3);
        feature.setProperty("stroke-width", 3.0);
        feature.setProperty("stroke", "rgb(0,0,0)");

        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(GISUtils.SRID.ETRS_TM35FIN.getGeoJsonCrs());
        featureCollection.setBbox(bbox);
        featureCollection.setFeatures(ImmutableList.of(feature));

        return featureCollection;
    }

    private GeometryAndSize getZoneFeature(final Long zoneId) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("worldBounds", FINNISH_BOUNDS)
                .addValue("zoneId", zoneId);

        // Calculate inverted area
        final String sql = "SELECT computed_area_size, water_area_size, ST_AsGeoJSON(" +
                "ST_Difference(ST_Buffer(ST_Envelope(ST_GeomFromText(:worldBounds, 3067)), 0), geom)) AS geom" +
                " FROM zone WHERE zone_id = :zoneId;";

        return namedParameterJdbcTemplate.queryForObject(sql, params, (rs, i) -> {
            final GeoJsonObject geom = GISUtils.parseGeoJSONGeometry(objectMapper, rs.getString("geom"));
            final double totalAreaSize = rs.getDouble("computed_area_size");
            final double waterAreaSize = rs.getDouble("water_area_size");

            return new GeometryAndSize(geom, totalAreaSize, waterAreaSize);
        });
    }

    public byte[] printGeoJson(final AreaPrintRequestDTO dto,
                               final FeatureCollection featureCollection) {
        final long zoomLayer = zoomLayer(featureCollection.getBbox(), dto.getPaperDpi());

        final String url = UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getMapExportEndpoint())
                .path(String.format("/%s%s/%d/%s%d.pdf",
                        dto.getPaperSize().name(),
                        dto.getPaperOrientation().asLetter(),
                        dto.getPaperDpi(),
                        LAYER_NAME,
                        zoomLayer))
                .build()
                .toUri()
                .toASCIIString();

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(MediaTypeExtras.APPLICATION_PDF));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Accept-Encoding", "gzip");

        final HttpEntity<FeatureCollection> requestEntity = new HttpEntity<>(featureCollection, requestHeaders);

        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class).getBody();
    }

    private static long zoomLayer(double[] bbox, final int dpi) {
        if (bbox == null || bbox.length != 4) {
            throw new IllegalArgumentException("Area bounding box is invalid");
        }

        final double areaWidth = Math.abs(bbox[2] - bbox[0]);
        final double areaHeight = Math.abs(bbox[3] - bbox[1]);
        final double maxDimension = Math.max(areaWidth, areaHeight);
        final double paperMaxDimension = 0.21; // metres
        final double inchInMeters = 0.0254;
        final double paperPixels = paperMaxDimension / inchInMeters * dpi;
        final double resolution = maxDimension / paperPixels;

        // For ETRS-TM35FIN following applies
        // res = metre / pixel = pow(2, 13 - zoom)
        // zoom = 13 - ln2(res)
        final double zoom = Math.floor(13 - (Math.log(resolution) / Math.log(2)));

        LOG.info("Area width={} res={} zoom={}", maxDimension, resolution, zoom);

        if (zoom > 13) {
            return 13;
        } else if (zoom < 0) {
            return 0;
        } else {
            return Math.round(zoom);
        }
    }
}
