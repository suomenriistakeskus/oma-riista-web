package fi.riista.api.mobile;

import fi.riista.feature.gis.metsahallitus.MetsahallitusGeometryLookupFeature;
import fi.riista.feature.gis.metsahallitus.MetsahallitusHirviDTO;
import fi.riista.feature.gis.metsahallitus.MetsahallitusPienriistaDTO;
import fi.riista.feature.gis.metsahallitus.MetsahallitusProperties;
import fi.riista.feature.gis.vector.VectorTileService;
import fi.riista.feature.gis.vector.VectorTileUtil;
import fi.riista.feature.huntingclub.area.mobile.MobileHuntingClubAreaDTO;
import fi.riista.feature.huntingclub.area.mobile.MobileMapFeature;
import fi.riista.feature.huntingclub.area.mobile.MobileMapZoneCache;
import fi.riista.util.MediaTypeExtras;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/mobile/v2/area")
public class MobileMapApiResource {

    @Resource
    private MobileMapFeature mobileMapFeature;

    @Resource
    private VectorTileService vectorTileService;

    @Resource
    private MobileMapZoneCache mobileMapZoneCache;

    @Resource
    private MetsahallitusGeometryLookupFeature metsahallitusGeometryLookupFeature;

    @Resource
    private MetsahallitusProperties metsahallitusProperties;

    @GetMapping("/club")
    public List<MobileHuntingClubAreaDTO> listClubMaps(@RequestParam(required = false) Integer huntingYear) {
        return mobileMapFeature.listClubMaps(huntingYear);
    }

    @GetMapping("/mh/hirvi")
    public List<MetsahallitusHirviDTO> listMetsahallitusHirvi() {
        return metsahallitusGeometryLookupFeature.listHirvi(metsahallitusProperties.getLatestMetsahallitusYear());
    }

    @GetMapping("/mh/hirvi/{year}")
    public List<MetsahallitusHirviDTO> listMetsahallitusHirvi(final @PathVariable int year) {
        return metsahallitusGeometryLookupFeature.listHirvi(year);
    }

    @GetMapping("/mh/pienriista")
    public List<MetsahallitusPienriistaDTO> listMetsahallitusPienriista() {
        return metsahallitusGeometryLookupFeature.listPienriista(metsahallitusProperties.getLatestMetsahallitusYear());
    }

    @GetMapping("/mh/pienriista/{year}")
    public List<MetsahallitusPienriistaDTO> listMetsahallitusPienriista(final @PathVariable int year) {
        return metsahallitusGeometryLookupFeature.listPienriista(year);
    }

    @GetMapping("/code/{externalId}")
    public ResponseEntity<MobileHuntingClubAreaDTO> findByExternalId(@PathVariable String externalId) {
        final MobileHuntingClubAreaDTO dto = mobileMapFeature.findByExternalId(externalId);

        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/vector/{externalId}/{z:\\d+}/{x:\\d+}/{y:\\d+}")
    public ResponseEntity<byte[]> vector(final @PathVariable int z,
                                         final @PathVariable int x,
                                         final @PathVariable int y,
                                         final @PathVariable String externalId) {
        if (!VectorTileUtil.isValidTile(z, x, y)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        final Long zoneId = mobileMapZoneCache.findByExternalId(externalId);

        if (zoneId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_VECTOR_TILE)
                .cacheControl(CacheControl.noCache())
                .body(vectorTileService.getZoneVectorTile(zoneId, z, x, y));
    }
}
