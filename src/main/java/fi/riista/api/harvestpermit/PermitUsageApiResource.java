package fi.riista.api.harvestpermit;

import fi.riista.feature.harvestpermit.usage.PermitUsageDTO;
import fi.riista.feature.harvestpermit.usage.PermitUsageFeature;
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
@RequestMapping(value = "/api/v1/permit", produces = MediaType.APPLICATION_JSON_VALUE)
public class PermitUsageApiResource {

   @Resource
   private PermitUsageFeature permitUsageFeature;

   @CacheControl(policy = CachePolicy.NO_CACHE)
   @GetMapping("/{id:\\d+}/usage")
   public List<PermitUsageDTO> getPermitUsage(@PathVariable long id) {
       return permitUsageFeature.getPermitUsage(id);
   }

   static class PermitUsageList {
       @Valid
       private List<PermitUsageDTO> usageList;

       public List<PermitUsageDTO> getUsageList() {
            return usageList;
       }

       public void setUsageList(final List<PermitUsageDTO> usageList) {
           this.usageList = usageList;
       }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id:\\d+}/usage")
    public void savePermitUsage(@PathVariable long id,
                                @RequestBody @Valid PermitUsageList usages) {
        permitUsageFeature.savePermitUsage(id, usages.usageList);
    }

}
