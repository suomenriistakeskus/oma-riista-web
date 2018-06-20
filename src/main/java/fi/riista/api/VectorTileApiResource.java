package fi.riista.api;

import fi.riista.feature.gis.vector.VectorTileService;
import fi.riista.feature.gis.vector.VectorTileUtil;
import fi.riista.feature.huntingclub.area.HuntingClubAreaZoneCache;
import fi.riista.feature.permit.application.geometry.HarvestPermitApplicationZoneCache;
import fi.riista.util.MediaTypeExtras;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/v1/vector")
public class VectorTileApiResource {
    @Resource
    private VectorTileService vectorTileService;

    @Resource
    private HarvestPermitApplicationZoneCache harvestPermitApplicationZoneCache;

    @Resource
    private HuntingClubAreaZoneCache huntingClubAreaZoneCache;

    @GetMapping(value = "/application/{applicationId:\\d+}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> application(final HttpSession httpSession,
                                              final @PathVariable long applicationId,
                                              final @PathVariable int z,
                                              final @PathVariable int x,
                                              final @PathVariable int y) {
        if (!VectorTileUtil.isValidTile(z, x, y)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        final Long zoneId = harvestPermitApplicationZoneCache.getZoneId(applicationId, httpSession);

        if (zoneId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_VECTOR_TILE)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(vectorTileService.getZoneVectorTile(zoneId, z, x, y));
    }

    @GetMapping(value = "/hunting-club-area/{areaId:\\d+}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> zone(final HttpSession httpSession,
                                       final @PathVariable long areaId,
                                       final @PathVariable int z,
                                       final @PathVariable int x,
                                       final @PathVariable int y) {
        if (!VectorTileUtil.isValidTile(z, x, y)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        final Long zoneId = huntingClubAreaZoneCache.getZoneId(areaId, httpSession);

        if (zoneId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_VECTOR_TILE)
                .cacheControl(CacheControl.noCache())
                .body(vectorTileService.getZoneVectorTile(zoneId, z, x, y));
    }
}
