package fi.riista.api.external;

import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportDTO;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/moosedatacard")
public class MooseDataCardApiResource {

    @Resource
    private MooseDataCardImportFeature importFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/listforgroup/{groupId:\\d+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MooseDataCardImportDTO> listForGroup(@PathVariable final long groupId) {
        return importFeature.getListOfMooseDataCardImportsForGroup(groupId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/pdf", method = RequestMethod.POST)
    public ResponseEntity<byte[]> getMooseDataCardPdf(@PathVariable final long id) throws IOException {
        return importFeature.getPdfFile(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/xml", method = RequestMethod.POST)
    public ResponseEntity<byte[]> getMooseDataCardXml(@PathVariable final long id) throws IOException {
        return importFeature.getXmlFile(id);
    }


}
