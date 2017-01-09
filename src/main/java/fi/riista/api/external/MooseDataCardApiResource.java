package fi.riista.api.external;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportDTO;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportFeature;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/moosedatacard")
public class MooseDataCardApiResource {

    @Resource
    private MooseDataCardImportFeature importFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/import", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> importMooseDataCard(
            @RequestParam final MultipartFile xmlFile, @RequestParam final MultipartFile pdfFile) {

        try {
            return ResponseEntity.ok(toMap(importFeature.importMooseDataCardAsModerator(xmlFile, pdfFile)));
        } catch (final MooseDataCardImportException e) {
            return ResponseEntity.badRequest().body(toMap(e.getMessages()));
        }
    }

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

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
    public void revokeMooseDataCardImport(@PathVariable final long id) {
        importFeature.revokeMooseDataCardImport(id);
    }

    private static ImmutableMap<String, List<String>> toMap(final List<String> messages) {
        return ImmutableMap.of("messages", messages);
    }

}
