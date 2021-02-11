package fi.riista.api.external;

import fi.riista.integration.luke.LukeExportFeature;
import fi.riista.integration.luke.LukeExportWhiteTailedDeerFeature;
import fi.riista.integration.luke_export.deerharvests.LED_Permits;
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
public class LukeMooselikePermitExportApiResource {

    @Resource
    private LukeExportFeature feature;

    @Resource
    private LukeExportWhiteTailedDeerFeature deerFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/hirvi", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public LEM_Permits getMooselikeHarvests(@RequestParam("huntingYear") final int huntingYear) {
        return feature.exportMoose(huntingYear);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/hirvi/xml", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public String getMooselikeHarvestsXml(@RequestParam("huntingYear") final int huntingYear) {
        return feature.exportMooseXml(huntingYear);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/vhp", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public LED_Permits getWhiteTailedDeerHarvests(@RequestParam("huntingYear") final int huntingYear) {
        return deerFeature.exportDeer(huntingYear);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/vhp/xml", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public String getWhiteTailedDeerHarvestsXml(@RequestParam("huntingYear") final int huntingYear) {
        return deerFeature.exportDeerXml(huntingYear);
    }

}
