package fi.riista.api.external;

import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedExportFeature;
import fi.riista.integration.common.export.otherwisedeceased.ODA_DeceasedAnimals;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/export/deceasedanimal")
public class OtherwiseDeceasedExportApiResource {

    @Resource
    private OtherwiseDeceasedExportFeature exportFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/json")
    public ODA_DeceasedAnimals getDeceasedAnimals(@RequestParam("year") final int year) {
        return exportFeature.export(year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/xml")
    public String getDeceasedAnimalsXml(@RequestParam("year") final int year) {
        return exportFeature.exportXml(year);
    }

}
