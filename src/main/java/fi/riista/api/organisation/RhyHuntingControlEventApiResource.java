package fi.riista.api.organisation;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlAttachmentDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventAttachmentFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventCrudFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventDTO;
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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys", produces = MediaType.APPLICATION_JSON_VALUE)
public class RhyHuntingControlEventApiResource {

    @Resource
    private HuntingControlEventCrudFeature huntingControlEventCrudFeature;

    @Resource
    private HuntingControlEventAttachmentFeature attachmentFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/huntingcontrolevents/{year:\\d+}")
    public List<HuntingControlEventDTO> listEvents(final @PathVariable long rhyId,
                                                   final @PathVariable int year) {
        return huntingControlEventCrudFeature.listHuntingControlEvents(rhyId, year);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{rhyId:\\d+}/huntingcontroleventswithattachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HuntingControlEventDTO addAttachment(final @PathVariable long rhyId,
                                                final @RequestParam(value = "dto") String dtoData,
                                                final @RequestParam MultipartFile[] file) throws IOException {
        final HuntingControlEventDTO dto = objectMapper.readValue(dtoData, HuntingControlEventDTO.class);
        dto.setRhy(new RiistanhoitoyhdistysDTO());
        dto.getRhy().setId(rhyId);
        dto.setNewAttachments(Arrays.asList(file));

        final HuntingControlEventDTO outputDto;
        if (dto.getId() == null) {
            outputDto = huntingControlEventCrudFeature.create(dto);
        } else {
            outputDto = huntingControlEventCrudFeature.update(dto);
        }

        return outputDto;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "{rhyId:\\d+}/huntingcontrolevents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingControlEventDTO createEvent(final @PathVariable long rhyId,
                                              final @RequestBody @Validated HuntingControlEventDTO dto) {

        dto.setRhy(new RiistanhoitoyhdistysDTO());
        dto.getRhy().setId(rhyId);
        return huntingControlEventCrudFeature.create(dto);
    }

    @PutMapping(value = "huntingcontrolevents/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingControlEventDTO updateEvent(final @RequestBody @Validated HuntingControlEventDTO dto,
                                              final @PathVariable long id) {

        dto.setId(id);
        return huntingControlEventCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "huntingcontrolevents/{id:\\d+}")
    public void deleteEvent(final @PathVariable long id) {
        huntingControlEventCrudFeature.delete(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "huntingcontrolevents/attachment/{id:\\d+}")
    public void deleteAttachment(final @PathVariable long id) {
        attachmentFeature.deleteAttachment(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "huntingcontrolevents/{eventId:\\d+}/attachments")
    public List<HuntingControlAttachmentDTO> listAttachments(final @PathVariable long eventId) {
        return attachmentFeature.listAttachments(eventId);
    }

    @PostMapping(value = "huntingcontrolevents/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(final @PathVariable long attachmentId) throws IOException {
        return attachmentFeature.getAttachment(attachmentId);
    }

}
