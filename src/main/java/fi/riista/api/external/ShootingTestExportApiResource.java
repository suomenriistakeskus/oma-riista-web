package fi.riista.api.external;

import fi.riista.integration.metsastajarekisteri.shootingtest.ShootingTestExportFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/export/shootingtest")
public class ShootingTestExportApiResource {

    @Resource
    private ShootingTestExportFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> exportShootingTestRegistry() {
        return feature.exportShootingTestRegistry();
    }
}
