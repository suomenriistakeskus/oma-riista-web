package fi.riista.api.harvestseason;

import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeasonCrudFeature;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = HarvestSeasonApiResource.URL_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
public class HarvestSeasonApiResource {
    public static final String URL_PREFIX = "/api/v1/harvestseason";

    @Resource
    private HarvestSeasonCrudFeature harvestSeasonFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/list/{huntingYear:\\d+}")
    public List<HarvestSeasonDTO> list(@PathVariable final int huntingYear) {
        return harvestSeasonFeature.listHarvestSeasons(huntingYear);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/list-quota-areas")
    public Map<HarvestArea.HarvestAreaType, List<HarvestAreaDTO>> listQuotaAreas() {
        return harvestSeasonFeature.listQuotaAreas();
    }

    @PostMapping
    public HarvestSeasonDTO create(@RequestBody @Valid final HarvestSeasonDTO dto) {
        return harvestSeasonFeature.create(dto);
    }

    @PutMapping("/{id:\\d+}")
    public HarvestSeasonDTO update(@PathVariable final long id, @RequestBody @Valid final HarvestSeasonDTO dto) {
        return harvestSeasonFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id:\\d+}")
    public void deleteEvent(@PathVariable final long id) {
        harvestSeasonFeature.delete(id);
    }

    @PostMapping("/copy/{huntingYear:\\d+}")
    public void copy(@PathVariable final int huntingYear) {
        harvestSeasonFeature.copyHarvestSeasons(huntingYear);
    }
}
