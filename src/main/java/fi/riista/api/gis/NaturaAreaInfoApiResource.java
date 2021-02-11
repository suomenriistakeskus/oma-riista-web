package fi.riista.api.gis;

import fi.riista.feature.natura.NaturaAreaInfoDTO;
import fi.riista.feature.natura.NaturaAreaInfoFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1/gis")
public class NaturaAreaInfoApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(NaturaAreaInfoApiResource.class);

    @Resource
    private NaturaAreaInfoFeature naturaAreaInfoFeature;

    @GetMapping(value = "/naturainfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getNaturaAreaInfo(@RequestParam final int zoomLevel,
                                            @RequestParam final int tileX,
                                            @RequestParam final int tileY,
                                            @RequestParam final int pixelX,
                                            @RequestParam final int pixelY) {

        try {

            final NaturaAreaInfoDTO response = naturaAreaInfoFeature.getNaturaAreaInfo(zoomLevel, tileX, tileY, pixelX, pixelY);

            if (response == null) {
                return ResponseEntity.noContent().build();
            }

            final HttpHeaders httpHeaders= new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity(response, httpHeaders, HttpStatus.OK);

        } catch (final IOException e) {
            LOG.error("", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
