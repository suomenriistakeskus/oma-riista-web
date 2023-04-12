package fi.riista.api.club;

import fi.riista.feature.common.ContentTypeChecker;
import fi.riista.feature.huntingclub.deercensus.DeerCensusFeature;
import fi.riista.feature.huntingclub.deercensus.DeerCensusDTO;
import fi.riista.feature.huntingclub.deercensus.attachment.DeerCensusAttachment;
import fi.riista.feature.huntingclub.deercensus.attachment.DeerCensusAttachmentDTO;
import fi.riista.feature.huntingclub.deercensus.attachment.DeerCensusAttachmentFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/deercensus", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeerCensusApiResource {

    @Resource
    private DeerCensusFeature deerCensusFeature;

    @Resource
    private DeerCensusAttachmentFeature deerCensusAttachmentFeature;

    @Resource
    private ContentTypeChecker contentTypeChecker;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeerCensusDTO createDeerCensus(@RequestBody @Validated final DeerCensusDTO dto) {
        return deerCensusFeature.create(dto);
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeerCensusDTO updateDeerCensus(
            @PathVariable final long id, @RequestBody @Validated final DeerCensusDTO dto) {
        dto.setId(id);
        return deerCensusFeature.update(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/club/{clubId:\\d+}")
    public List<DeerCensusDTO> findDeerCensusesByClubId(@PathVariable final long clubId) {
        return deerCensusFeature.findDeerCensusesByClubId(clubId);
    }


    @GetMapping(value = "/{deerCensusId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DeerCensusAttachmentDTO> listAttachments(final @PathVariable long deerCensusId) {
        return deerCensusAttachmentFeature.listAttachments(deerCensusId);
    }

    @GetMapping(value = "/attachment/tmp", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DeerCensusAttachmentDTO> listTmpAttachments(final @RequestParam List<Long> attachmentIds) {
        return deerCensusAttachmentFeature.listAttachmentsByIds(attachmentIds);
    }

    @PostMapping(value = "/{deerCensusId:\\d+}/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addAttachment(final @PathVariable long deerCensusId,
                                                final @RequestParam DeerCensusAttachment.Type attachmentType,
                                                final @RequestParam("file") MultipartFile file) throws IOException {
        if (!contentTypeChecker.isValidApplicationAttachmentContent(file)) {
            return ResponseEntity.badRequest().build();
        }

        final Long attachmentId = deerCensusAttachmentFeature.addAttachment(
                deerCensusId, attachmentType, file);

        return ResponseEntity.ok(Collections.singletonMap("id", attachmentId));
    }

    @PostMapping(value = "/attachment/tmp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addTemporaryDeerCensusAttachment(final @RequestParam DeerCensusAttachment.Type attachmentType,
                                                   final @RequestParam("file") MultipartFile file) throws IOException {
        if (!contentTypeChecker.isValidApplicationAttachmentContent(file)) {
            return ResponseEntity.badRequest().build();
        }

        final Long attachmentId = deerCensusAttachmentFeature.addAttachmentWithoutDeerCensusAssociation(attachmentType, file);
        return ResponseEntity.ok(Collections.singletonMap("id", attachmentId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/attachment/{attachmentId:\\d+}")
    public void deleteAttachment(@PathVariable final long attachmentId) {
        deerCensusAttachmentFeature.deleteAttachment(attachmentId);
    }

    @PostMapping(value = "/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable final long attachmentId) throws IOException {
        return deerCensusAttachmentFeature.getAttachment(attachmentId);
    }
}
