package fi.riista.api.mobile;

import fi.riista.feature.gamediary.mobile.MobileDeletedDiaryEntriesDTO;
import fi.riista.feature.gamediary.mobile.MobileDiaryEntryPageDTO;
import fi.riista.feature.gamediary.mobile.srva.MobileSrvaCrudFeature;
import fi.riista.feature.gamediary.mobile.srva.MobileSrvaEventDTO;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaParametersDTO;
import fi.riista.util.Patterns;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping(value = "/api/mobile/v2/srva")
public class MobileSrvaApiResource {

    @Resource
    private MobileSrvaCrudFeature mobileSrvaCrudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/parameters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SrvaParametersDTO getSrvaParameters(@RequestParam final int srvaEventSpecVersion) {
        return mobileSrvaCrudFeature.getSrvaParameters(SrvaEventSpecVersion.fromIntValue(srvaEventSpecVersion));
    }

    @RequestMapping(value = "/srvaevent", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileSrvaEventDTO createSrvaEvent(@RequestBody @Valid final MobileSrvaEventDTO dto) {
        // on create force id and rev to be null
        dto.setId(null);
        dto.setRev(null);
        return mobileSrvaCrudFeature.createSrvaEvent(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/srvaevent/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteSrvaEvent(@PathVariable final Long id) {
        mobileSrvaCrudFeature.deleteSrvaEvent(id);
    }

    @RequestMapping(value = "/srvaevent/{id:\\d+}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileSrvaEventDTO updateSrvaEvent(@PathVariable final Long id, @RequestBody @Valid MobileSrvaEventDTO dto) {
        dto.setId(id);

        return mobileSrvaCrudFeature.updateSrvaEvent(dto);
    }

    @RequestMapping(value = "/image/upload", method = RequestMethod.POST)
    public void addImage(@RequestParam("srvaEventId") final long srvaEventId, @RequestParam("uuid") final UUID uuid,
                         @RequestParam("file") final MultipartFile file) throws IOException {
        mobileSrvaCrudFeature.addImage(srvaEventId, uuid, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/image/{imageUuid:" + Patterns.UUID + "}", method = RequestMethod.DELETE)
    public void deleteImage(@PathVariable final UUID imageUuid) {
        mobileSrvaCrudFeature.deleteImage(imageUuid);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/srvaevents", method = RequestMethod.GET)
    public List<MobileSrvaEventDTO> getSrvaEvents(@RequestParam final int srvaEventSpecVersion) {
        final SrvaEventSpecVersion specVersion = SrvaEventSpecVersion.fromIntValue(srvaEventSpecVersion);

        return mobileSrvaCrudFeature.listSrvaEventsForActiveUser(specVersion);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/srvaevents/page", method = RequestMethod.GET)
    public MobileDiaryEntryPageDTO<MobileSrvaEventDTO> getSrvaEventPage(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime modifiedAfter,
            @RequestParam final int srvaEventSpecVersion) {

        final SrvaEventSpecVersion specVersion = SrvaEventSpecVersion.fromIntValue(srvaEventSpecVersion);
        return mobileSrvaCrudFeature.fetchPageForActiveUser(modifiedAfter, specVersion);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/srvaevents/deleted", produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileDeletedDiaryEntriesDTO getDeletedEvents(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime deletedAfter) {
        return mobileSrvaCrudFeature.getDeletedEvents(deletedAfter);
    }
}
