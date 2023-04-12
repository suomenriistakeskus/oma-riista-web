package fi.riista.api.mobile;

import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventAttachmentFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.mobile.MobileHuntingControlEventAttachmentFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.mobile.MobileHuntingControlEventDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.mobile.MobileHuntingControlEventFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.mobile.MobileHuntingControlHunterInfoDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.mobile.MobileHuntingControlRhyDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.mobile.MobileHuntingControlSpecVersion;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/mobile/v2/huntingcontrol", produces = MediaType.APPLICATION_JSON_VALUE)
public class MobileHuntingControlEventApiResource {

    @Resource
    private HuntingControlEventAttachmentFeature attachmentFeature;

    @Resource
    private MobileHuntingControlEventFeature mobileEventFeature;

    @Resource
    private MobileHuntingControlEventAttachmentFeature mobileAttachmentFeature;

    /*
        event
     */

    // List all events on given RHY and year, which the user is an inspector
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/event")
    public List<MobileHuntingControlRhyDTO> getEvents(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime modifiedAfter,
            @RequestParam final int requestedSpecVersion) {
        final MobileHuntingControlSpecVersion specVersion = MobileHuntingControlSpecVersion.fromIntValue(requestedSpecVersion);
        return mobileEventFeature.getEvents(modifiedAfter, specVersion);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/event/{rhyId:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHuntingControlEventDTO createEvent(final @PathVariable long rhyId,
                                                    final @RequestParam int requestedSpecVersion,
                                                    final @RequestBody @Validated MobileHuntingControlEventDTO dto) {
        final MobileHuntingControlSpecVersion specVersion = MobileHuntingControlSpecVersion.fromIntValue(requestedSpecVersion);
        return mobileEventFeature.createEvent(rhyId, dto, specVersion);
    }

    @PutMapping(value = "/event/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHuntingControlEventDTO updateEvent(final @PathVariable long id,
                                                    final @RequestParam int requestedSpecVersion,
                                                    final @RequestBody @Validated MobileHuntingControlEventDTO dto) {
        dto.setId(id);
        final MobileHuntingControlSpecVersion specVersion = MobileHuntingControlSpecVersion.fromIntValue(requestedSpecVersion);
        return mobileEventFeature.updateEvent(dto, specVersion);
    }

    /*
        attachment
     */

    @PostMapping(value = "/attachment/{eventId:\\d+}/upload")
    public Long uploadAttachment(final @PathVariable long eventId,
                                 final @RequestParam UUID uuid,
                                 final @RequestParam MultipartFile file) throws IOException {
        return mobileAttachmentFeature.addAttachment(eventId, uuid, file);
    }

    @GetMapping(value = "/attachment/{attachmentId:\\d+}/download")
    public ResponseEntity<byte[]> downloadAttachment(final @PathVariable long attachmentId) throws IOException {
        return attachmentFeature.getAttachment(attachmentId);
    }

    // Note! Cache max-age set when response entity is being constructed.
    @GetMapping(value = "attachment/{attachmentId:\\d+}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(final @PathVariable long attachmentId) throws IOException {
        return attachmentFeature.getThumbnail(attachmentId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/attachment/{attachmentId:\\d+}")
    public void deleteAttachment(final @PathVariable long attachmentId) {
        attachmentFeature.deleteAttachment(attachmentId);
    }

    /*
        hunter check
    */
    @GetMapping(value = "/hunterInfo")
    public MobileHuntingControlHunterInfoDTO getHunterInfoByHunterNumber(
            @Valid @ModelAttribute MobileHunterInfoRequestParam reqParam) {
        if (reqParam.getHunterNumber() != null) {
            return mobileEventFeature.getHunterInfoByHunterNumber(reqParam.getHunterNumber());
        } else {
            return mobileEventFeature.getHunterInfoBySsn(reqParam.getSsn());
        }
    }
}
