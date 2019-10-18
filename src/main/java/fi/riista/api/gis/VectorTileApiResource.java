package fi.riista.api.gis;

import fi.riista.feature.gis.vector.HarvestPermitApplicationZoneCache;
import fi.riista.feature.gis.vector.HuntingClubAreaZoneCache;
import fi.riista.feature.gis.vector.ModeratorAreaZoneCache;
import fi.riista.feature.gis.vector.PersonalAreaUnionZoneCache;
import fi.riista.feature.gis.vector.PersonalAreaZoneCache;
import fi.riista.feature.gis.vector.VectorTileService;
import fi.riista.feature.gis.vector.VectorTileUtil;
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

    @Resource
    private PersonalAreaZoneCache personalAreaZoneCache;

    @Resource
    private PersonalAreaUnionZoneCache personalAreaUnionZoneCache;

    @Resource
    private ModeratorAreaZoneCache moderatorAreaZoneCache;

    @GetMapping("/application/{applicationId:\\d+}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> application(final HttpSession httpSession,
                                              final @PathVariable long applicationId,
                                              final @PathVariable int z,
                                              final @PathVariable int x,
                                              final @PathVariable int y) {
        return createResponse(z, x, y, harvestPermitApplicationZoneCache.getZoneId(applicationId, httpSession));
    }

    @GetMapping("/hunting-club-area/{areaId:\\d+}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> clubArea(final HttpSession httpSession,
                                           final @PathVariable long areaId,
                                           final @PathVariable int z,
                                           final @PathVariable int x,
                                           final @PathVariable int y) {
        return createResponse(z, x, y, huntingClubAreaZoneCache.getZoneId(areaId, httpSession));
    }

    @GetMapping("/personal-area/{areaId:\\d+}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> personalArea(final HttpSession httpSession,
                                               final @PathVariable long areaId,
                                               final @PathVariable int z,
                                               final @PathVariable int x,
                                               final @PathVariable int y) {
        return createResponse(z, x, y, personalAreaZoneCache.getZoneId(areaId, httpSession));
    }

    @GetMapping("/personal-area-union/{areaId:\\d+}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> accountAreaUnion(final HttpSession httpSession,
                                                   final @PathVariable long areaId,
                                                   final @PathVariable int z,
                                                   final @PathVariable int x,
                                                   final @PathVariable int y) {
        return createResponse(z, x, y, personalAreaUnionZoneCache.getZoneId(areaId, httpSession));
    }

    @GetMapping("/moderator-area/{areaId:\\d+}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> moderatorArea(final HttpSession httpSession,
                                                final @PathVariable long areaId,
                                                final @PathVariable int z,
                                                final @PathVariable int x,
                                                final @PathVariable int y) {
        return createResponse(z, x, y, moderatorAreaZoneCache.getZoneId(areaId, httpSession));
    }

    private ResponseEntity<byte[]> createResponse(final int z, final int x, final int y, final Long zoneId) {
        if (zoneId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!VectorTileUtil.isValidTile(z, x, y)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_VECTOR_TILE)
                .cacheControl(CacheControl.maxAge(15, TimeUnit.SECONDS))
                .body(vectorTileService.getZoneVectorTile(zoneId, z, x, y));
    }
}
