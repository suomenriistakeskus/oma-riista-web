package fi.riista.api;

import fi.riista.feature.harvestpermit.area.HarvestPermitAreaDTO;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/harvestpermitarea")
public class HarvestPermitAreaApiResource {

    @Resource
    private HarvestPermitAreaFeature harvestPermitAreaFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitAreaDTO> list(@RequestParam long clubId) {
        return harvestPermitAreaFeature.listByClub(clubId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitAreaDTO read(@PathVariable Long id) {
        return harvestPermitAreaFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HarvestPermitAreaDTO createArea(@RequestBody @Validated HarvestPermitAreaDTO dto) {
        return harvestPermitAreaFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitAreaDTO updateArea(@RequestBody @Validated HarvestPermitAreaDTO dto, @PathVariable Long id) {
        dto.setId(id);
        return harvestPermitAreaFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/addPartner/{clubAreaId:\\d+}")
    public void addPartner(@PathVariable Long id, @PathVariable Long clubAreaId) {
        harvestPermitAreaFeature.addArea(id, clubAreaId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/geometry")
    public FeatureCollection getGeometry(@PathVariable Long id) {
        return harvestPermitAreaFeature.getGeometry(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/geometry")
    public void updateGeometry(@PathVariable Long id) {
        harvestPermitAreaFeature.updateGeometry(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/ready")
    public void ready(@PathVariable Long id) {
        harvestPermitAreaFeature.setCompleteStatus(id, true);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/incomplete")
    public void incomplete(@PathVariable Long id) {
        harvestPermitAreaFeature.setCompleteStatus(id, false);
    }
}
