package fi.riista.feature.huntingclub.area.transfer;

import com.google.common.collect.ImmutableMap;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.zone.AreaEntity;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.huntingclub.area.MissingHuntingClubAreaGeometryException;
import fi.riista.integration.gis.ExternalHuntingClubAreaExportRequest;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.RandomStringUtil;
import io.vavr.control.Either;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class HuntingClubAreaExportFeature {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy HH:mm");
    private static final String FILENAME_GEOJSON = "area.json";
    private static final String FILENAME_METADATA = "README.txt";

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private AuditService auditService;

    @PreAuthorize("hasPrivilege('EXPORT_HUNTINGCLUB_AREA')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> exportCombinedGeoJson(final ExternalHuntingClubAreaExportRequest body,
                                                   final WebRequest request) {
        final String externalId = body.getExternalId().toUpperCase();

        if (externalId.length() < RandomStringUtil.EXTERNAL_ID_LENGTH) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        final Optional<Either<HuntingClubArea, HarvestPermitArea>> areaOpt = resolveArea(externalId);
        final AreaEntity<Long> baseEntity = areaOpt.map(F::reduceToCommonBase).orElse(null);

        if (baseEntity == null || baseEntity.getZone() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        final String etag = Integer.toHexString(baseEntity.getConsistencyVersion());
        final Date saveDate = baseEntity.getLatestCombinedModificationTime();

        if (request.checkNotModified(saveDate.getTime())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).lastModified(saveDate.getTime()).build();
        }

        // Audit (only if content is actually returned to ignore refresh attempts)
        auditService.log("exportClubMap", baseEntity, ImmutableMap.<String, Object> builder()
                .put("remoteUser", body.getRemoteUser())
                .put("remoteAddress", body.getRemoteAddress())
                .build());

        final FeatureCollection featureCollection = areaOpt.get().fold(
                area -> toFeatureCollectionWithMetadata(area, area.getClub(), area.getHuntingYear()),
                area -> toFeatureCollectionWithMetadata(area, area.getClub(), area.getHuntingYear()));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePrivate().mustRevalidate())
                .eTag(etag)
                .lastModified(saveDate.getTime())
                .contentType(MediaType.parseMediaType(MediaTypeExtras.APPLICATION_GEOJSON_VALUE))
                .body(featureCollection);
    }

    private Optional<Either<HuntingClubArea, HarvestPermitArea>> resolveArea(final String externalId) {
        return F.optionallyEither(
                huntingClubAreaRepository.findByExternalId(externalId),
                () -> harvestPermitAreaRepository.findByExternalId(externalId));
    }

    @Transactional(readOnly = true)
    public byte[] exportCombinedGeoJsonAsArchive(final long clubAreaId, final Locale locale) {
        final HuntingClubArea clubArea = requireEntityService.requireHuntingClubArea(clubAreaId, EntityPermission.READ);

        final FeatureCollection featureCollection =
                toFeatureCollectionWithMetadata(clubArea, clubArea.getClub(), clubArea.getHuntingYear());

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final ZipOutputStream zip = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
            zip.setComment("Exported from oma.riista.fi on " + DTF.print(DateUtil.now()));
            zip.setLevel(9);

            // GeoJSON
            zip.putNextEntry(new ZipEntry(FILENAME_GEOJSON));
            final OutputStreamWriter gos = new OutputStreamWriter(new CloseShieldOutputStream(zip), StandardCharsets.UTF_8);
            objectMapper.writeValue(gos, featureCollection);
            zip.closeEntry();

            // Metadata
            final OutputStreamWriter mos = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
            zip.putNextEntry(new ZipEntry(FILENAME_METADATA));
            mos.write(exportMetadataString(clubArea, locale));
            mos.flush();
            zip.closeEntry();

            zip.flush();
            zip.close();

            return bos.toByteArray();

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static String exportMetadataString(final HuntingClubArea huntingClubArea, final Locale locale) {
        final LocalisedString clubName = huntingClubArea.getClub().getNameLocalisation();
        final LocalisedString areaName = huntingClubArea.getNameLocalisation();
        final Date saveDate = huntingClubArea.getLatestCombinedModificationTime();
        final LocalDateTime saveDateTime = DateUtil.toLocalDateTimeNullSafe(saveDate);

        return new StringBuilder()
                .append(clubName.getAnyTranslation(locale))
                .append('\n')
                .append(areaName.getAnyTranslation(locale))
                .append('\n')
                .append(String.format("%.2f", huntingClubArea.getZone().getComputedAreaSize() / 10_000))
                .append(" ha\n")
                .append(DTF.print(saveDateTime))
                .append('\n')
                .toString();
    }

    @Transactional(readOnly = true)
    public FeatureCollection exportCombinedGeoJsonForGarmin(final long clubAreaId) {
        return toFeatureCollection(requireEntityService.requireHuntingClubArea(clubAreaId, EntityPermission.READ));
    }

    private FeatureCollection toFeatureCollection(final AreaEntity<?> area) {
        return area.getZoneIdSet()
                .map(zoneIds -> zoneRepository.getCombinedFeatures(zoneIds, GISUtils.SRID.WGS84))
                .orElseThrow(MissingHuntingClubAreaGeometryException::new);
    }

    private FeatureCollection toFeatureCollectionWithMetadata(final AreaEntity<?> area,
                                                              final HuntingClub club,
                                                              final int huntingYear) {

        return area.getZoneIdSet()
                .map(zoneIds -> zoneRepository.getCombinedFeatures(zoneIds, GISUtils.SRID.WGS84))
                .map(featureCollection -> {
                    final Date saveDate = area.getLatestCombinedModificationTime();
                    final DateTime saveDateTime = new DateTime(saveDate).withZone(DateTimeZone.UTC);

                    for (final Feature feature : featureCollection) {
                        feature.setId(null);
                        if (club != null) {
                            feature.setProperty(GeoJSONConstants.PROPERTY_CLUB_NAME, club.getNameLocalisation());
                        }
                        feature.setProperty(GeoJSONConstants.PROPERTY_AREA_NAME, area.getNameLocalisation());
                        feature.setProperty(GeoJSONConstants.PROPERTY_AREA_SIZE, Math.round(area.getZone().getComputedAreaSize()));
                        feature.setProperty(GeoJSONConstants.PROPERTY_WATER_AREA_SIZE, Math.round(area.getZone().getWaterAreaSize()));
                        feature.setProperty(GeoJSONConstants.PROPERTY_SAVE_DATE, ISODateTimeFormat.basicDateTimeNoMillis().print(saveDateTime));
                        feature.setProperty(GeoJSONConstants.PROPERTY_HUNTING_YEAR, huntingYear);
                    }

                    return featureCollection;
                })
                .orElseThrow(MissingHuntingClubAreaGeometryException::new);
    }
}
