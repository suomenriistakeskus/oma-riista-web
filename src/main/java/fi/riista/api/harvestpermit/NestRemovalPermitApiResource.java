package fi.riista.api.harvestpermit;

import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestRemovalDetailsFeature;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestRemovalUsageDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/nestremovalpermit", produces = MediaType.APPLICATION_JSON_VALUE)
public class NestRemovalPermitApiResource {

    @Resource
    private HarvestPermitNestRemovalDetailsFeature harvestPermitNestRemovalDetailsFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{id:\\d+}/usage")
    public List<HarvestPermitNestRemovalUsageDTO> getPermitUsage(@PathVariable long id) {
        return harvestPermitNestRemovalDetailsFeature.getPermitUsage(id);
    }

    static class HarvestPermitNestRemovalUsageList {

        @Valid
        private List<HarvestPermitNestRemovalUsageDTO> usageList;

        public List<HarvestPermitNestRemovalUsageDTO> getUsageList() {
            return usageList;
        }

        public void setUsageList(final List<HarvestPermitNestRemovalUsageDTO> usageList) {
            this.usageList = usageList;
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id:\\d+}/usage")
    public void savePermitUsage(@PathVariable long id,
                                @RequestBody @Valid HarvestPermitNestRemovalUsageList usages) {
        harvestPermitNestRemovalDetailsFeature.savePermitUsage(id, usages.usageList);
    }
}
