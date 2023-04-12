package fi.riista.api.organisation;

import com.google.common.collect.ImmutableList;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.ActiveGameWardensDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlAttachmentDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventAttachmentFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventCrudFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventExcelView;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventExportDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventExportFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys", produces = MediaType.APPLICATION_JSON_VALUE)
public class RhyHuntingControlEventApiResource {

    @Resource
    private HuntingControlEventCrudFeature huntingControlEventCrudFeature;

    @Resource
    private HuntingControlEventAttachmentFeature attachmentFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private HuntingControlEventExportFeature exportFeature;

    @Resource
    private MessageSource messageSource;

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

    @PutMapping(value = "huntingcontrolevents/{id:\\d+}/accept")
    public HuntingControlEventDTO acceptEvent(final @PathVariable long id) {
        return huntingControlEventCrudFeature.changeStatus(id, HuntingControlEventStatus.ACCEPTED);
    }

    @PutMapping(value = "huntingcontrolevents/{id:\\d+}/acceptsubsidized")
    public HuntingControlEventDTO acceptSubsidizedEvent(final @PathVariable long id) {
        return huntingControlEventCrudFeature.changeStatus(id, HuntingControlEventStatus.ACCEPTED_SUBSIDIZED);
    }

    @PutMapping(value = "huntingcontrolevents/{id:\\d+}/reject")
    public HuntingControlEventDTO rejectEvent(final @PathVariable long id) {
        return huntingControlEventCrudFeature.changeStatus(id, HuntingControlEventStatus.REJECTED);
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

    // Note! Cache max-age set when response entity is being constructed.
    @GetMapping(value = "huntingcontrolevents/attachment/{attachmentId:\\d+}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(final @PathVariable long attachmentId) throws IOException {
        return attachmentFeature.getThumbnail(attachmentId);
    }

    @PostMapping(value = "{rhyId:\\d+}/huntingcontrolevents/excel/{year:\\d+}", produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportExcel(final @PathVariable long rhyId,
                                    final @PathVariable int year) {
        final HuntingControlEventExportDTO exported = exportFeature.export(rhyId, year);

        return new ModelAndView(new HuntingControlEventExcelView(new EnumLocaliser(messageSource), ImmutableList.of(exported)));
    }

    @PostMapping(value = "{rhyId:\\d+}/huntingcontrolevents/my/excel/{year:\\d+}", produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportMyExcel(final @PathVariable long rhyId,
                                      final @PathVariable int year) {
        final HuntingControlEventExportDTO exported = exportFeature.exportForActiveUser(rhyId, year);

        return new ModelAndView(new HuntingControlEventExcelView(new EnumLocaliser(messageSource), ImmutableList.of(exported)));
    }

    @PostMapping(value = "huntingcontrolevents/excel/all/{year:\\d+}", produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportAllExcel(final @PathVariable int year) {
        final List<HuntingControlEventExportDTO> events = exportFeature.exportAll(year);

        return new ModelAndView(new HuntingControlEventExcelView(new EnumLocaliser(messageSource), events));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/huntingcontrolevents/inspectors")
    public ActiveGameWardensDTO getAllInspectors(
            final @PathVariable long rhyId,
            final @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return huntingControlEventCrudFeature.listAllActiveGameWardens(rhyId, date);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/huntingcontrolevents/my/{year:\\d+}")
    public List<HuntingControlEventDTO> listMyEvents(final @PathVariable long rhyId,
                                                     final @PathVariable int year) {
        return huntingControlEventCrudFeature.listHuntingControlEventsForActiveUser(rhyId, year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/huntingcontrolevents/years")
    public List<Integer> listEventYears(final @PathVariable long rhyId) {
        return huntingControlEventCrudFeature.listAvailableYears(rhyId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/huntingcontrolevents/my/years")
    public List<Integer> listMyEventYears(final @PathVariable long rhyId) {
        return huntingControlEventCrudFeature.listAvailableYearsForActiveUser(rhyId);
    }

}
