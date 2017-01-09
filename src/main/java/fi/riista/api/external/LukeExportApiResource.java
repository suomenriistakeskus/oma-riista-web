package fi.riista.api.external;

import fi.riista.integration.luke.LukeExportFeature;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Permits;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/export/luke")
public class LukeExportApiResource {

    @Resource
    private LukeExportFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/hirvi", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LEM_Permits getMooselikeHarvests(@RequestParam("huntingYear") int huntingYear) {
        return feature.exportMoose(huntingYear);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/hirvi/xml", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public String getMooselikeHarvestsXml(@RequestParam("huntingYear") int huntingYear) {
        return feature.exportMooseXml(huntingYear);
    }
}
