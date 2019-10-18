package fi.riista.integration.koiratutka.export;

import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.PersonalAreaRepository;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.moderatorarea.ModeratorArea;
import fi.riista.feature.moderatorarea.ModeratorAreaRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.util.GISUtils;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.RandomStringUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class HuntingClubAreaExportFeature {

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private ModeratorAreaRepository moderatorAreaRepository;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private AuditService auditService;

    private static Date getLatest(final @Nonnull Date first, final @Nonnull Date second) {
        return first.after(second) ? first : second;
    }

    @PreAuthorize("hasPrivilege('EXPORT_HUNTINGCLUB_AREA')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> export(final ExportRequestDTO request,
                                    final WebRequest webRequest) {

        return resolveArea(request)
                .filter(area -> area.getZoneId() != null)
                .map(area -> {
                    // Check ETAG
                    if (webRequest.checkNotModified(area.getResponseEtag())) {
                        return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                                .eTag(area.getResponseEtag())
                                .build();
                    }

                    final long zoneId = area.getZoneId();
                    final GISZoneWithoutGeometryDTO zoneDTO = getZoneDTO(zoneId);
                    final Date latestModificationTime = getLatest(
                            area.getAreaModificationTime(),
                            zoneDTO.getModificationTime());

                    // Check HTTP modification time header
                    if (webRequest.checkNotModified(latestModificationTime.getTime())) {
                        return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                                .lastModified(latestModificationTime.getTime())
                                .build();
                    }

                    // Audit (only if content is actually returned to ignore refresh attempts)
                    auditService.log("exportClubMap", request.getExternalId(), request.getAuditExtraInfo());

                    final GeoJsonMetadata geoJsonMetadata = new GeoJsonMetadata(zoneDTO.getSize(), area, latestModificationTime);
                    final FeatureCollection featureCollection = getFeatures(zoneId, geoJsonMetadata);

                    return ResponseEntity.ok()
                            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePrivate().mustRevalidate())
                            .eTag(area.getResponseEtag())
                            .lastModified(latestModificationTime.getTime())
                            .contentType(MediaTypeExtras.APPLICATION_GEOJSON)
                            .body(featureCollection);

                }).orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .cacheControl(CacheControl.noCache())
                        .contentType(MediaTypeExtras.TEXT_PLAIN_UTF8)
                        .body("No such area: " + request.getExternalId()));
    }

    @Nonnull
    private Optional<AreaExportDTO> resolveArea(final ExportRequestDTO dto) {
        final String externalId = dto.getExternalId().toUpperCase();

        if (externalId.length() < RandomStringUtil.EXTERNAL_ID_LENGTH) {
            return Optional.empty();
        }

        final Optional<HuntingClubArea> clubAreaOptional = huntingClubAreaRepository.findByExternalId(externalId);

        if (clubAreaOptional.isPresent()) {
            return Optional.of(AreaExportDTO.create(clubAreaOptional.get()));
        }

        final Optional<HarvestPermitArea> permitAreaOptional = harvestPermitAreaRepository.findByExternalId(externalId);

        if (permitAreaOptional.isPresent()) {
            return Optional.of(AreaExportDTO.create(permitAreaOptional.get()));
        }

        final Optional<PersonalArea> personalAreaOptional = personalAreaRepository.findByExternalId(externalId);

        if (personalAreaOptional.isPresent()) {
            return Optional.of(AreaExportDTO.create(personalAreaOptional.get()));
        }

        final Optional<ModeratorArea> moderatorAreaOptional = moderatorAreaRepository.findByExternalId(externalId);

        if (moderatorAreaOptional.isPresent()) {
            return Optional.of(AreaExportDTO.create(moderatorAreaOptional.get()));
        }

        return Optional.empty();
    }

    @Nonnull
    private GISZoneWithoutGeometryDTO getZoneDTO(final long zoneId) {
        return zoneRepository.fetchWithoutGeometry(Collections.singleton(zoneId)).get(zoneId);
    }

    @Nonnull
    private FeatureCollection getFeatures(final long zoneId, final GeoJsonMetadata geoJsonMetadata) {
        final FeatureCollection featureCollection = zoneRepository.getCombinedFeatures(Collections.singleton(zoneId), GISUtils.SRID.WGS84);

        for (Feature feature : featureCollection) {
            geoJsonMetadata.updateFeature(feature);
        }

        return featureCollection;
    }
}
