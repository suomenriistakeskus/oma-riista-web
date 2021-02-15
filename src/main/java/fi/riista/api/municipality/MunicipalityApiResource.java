package fi.riista.api.municipality;

import fi.riista.feature.common.MunicipalityFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/municipality", produces = MediaType.APPLICATION_JSON_VALUE)
public class MunicipalityApiResource {

    @Resource
    private MunicipalityFeature municipalityFeature;

    @GetMapping("/list")
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<MunicipalityDTO> listMunicipalities() {
        return municipalityFeature.listMunicipalities();
    }

}
