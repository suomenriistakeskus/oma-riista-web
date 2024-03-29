package fi.riista.api.external;

import fi.riista.integration.srva.SrvaExportFeature;
import fi.riista.integration.srva.dto.SrvaPublicExportDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/")
public class SrvaExportApiResource {

    @Resource
    private SrvaExportFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "v1/export/srva/rvr", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public String getSrvaRvrExportDataV1(final @RequestParam(required = false) Integer calendarYear) {
        return feature.exportRVRV1Xml(Optional.ofNullable(calendarYear));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "v2/export/srva/rvr", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public String getSrvaRvrExportDataV2(final @RequestParam(required = false) Integer calendarYear) {
        return feature.exportRVRV2Xml(Optional.ofNullable(calendarYear));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "v1/anon/srva", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SrvaPublicExportDTO> getSrvaPublicExportData(final @RequestParam int calendarYear) {
        return feature.exportPublic(calendarYear);
    }
}
