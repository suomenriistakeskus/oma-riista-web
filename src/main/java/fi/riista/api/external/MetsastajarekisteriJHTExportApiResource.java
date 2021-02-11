package fi.riista.api.external;

import fi.riista.integration.metsastajarekisteri.jht.MR_JHT_Jht;
import fi.riista.integration.metsastajarekisteri.jht.MrJhtExportFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/export/metsastajarekisteri")
public class MetsastajarekisteriJHTExportApiResource {

    @Resource
    private MrJhtExportFeature mrJhtExportFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/jht", produces = MediaType.APPLICATION_JSON_VALUE)
    public MR_JHT_Jht exportJson() {
        return mrJhtExportFeature.export();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/jht/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String exportXml() {
        return mrJhtExportFeature.exportAsXml();
    }
}
