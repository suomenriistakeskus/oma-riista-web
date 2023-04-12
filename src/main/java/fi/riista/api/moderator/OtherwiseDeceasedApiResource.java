package fi.riista.api.moderator;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedAttachmentFeature;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedBriefDTO;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedDTO;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedExcelFeature;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedFeature;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedFilterDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/deceased")
public class OtherwiseDeceasedApiResource {

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private OtherwiseDeceasedFeature otherwiseDeceasedFeature;

    @Resource
    private OtherwiseDeceasedAttachmentFeature attachmentFeature;

    @Resource
    private OtherwiseDeceasedExcelFeature excelFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/list")
    public Slice<OtherwiseDeceasedBriefDTO> search(final @Validated @RequestBody OtherwiseDeceasedFilterDTO dto,
                                                   final Pageable pageable) {
        return otherwiseDeceasedFeature.searchPage(dto, pageable);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/save")
    public OtherwiseDeceasedDTO save(@RequestBody @Validated final OtherwiseDeceasedDTO dto) {
        return otherwiseDeceasedFeature.save(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/savewithattachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OtherwiseDeceasedDTO save(final @Validated @RequestParam(value = "dto") String dtoData,
                                     final @RequestParam MultipartFile[] files) throws IOException {
        final OtherwiseDeceasedDTO dto = objectMapper.readValue(dtoData, OtherwiseDeceasedDTO.class);
        return otherwiseDeceasedFeature.save(dto, Arrays.asList(files));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}")
    public OtherwiseDeceasedDTO getDetails(@PathVariable final long id) {
        return otherwiseDeceasedFeature.getDetails(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{id:\\d+}/reject")
    public void reject(@PathVariable final long id) {
        otherwiseDeceasedFeature.reject(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{id:\\d+}/restore")
    public void restore(@PathVariable final long id) {
        otherwiseDeceasedFeature.restore(id);
    }

    /** Attachment API */

    // Result can be cached due its content cannot be changed.
    @GetMapping(value = "/attachment/{id:\\d+}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable final long id) throws IOException {
        return attachmentFeature.getAttachment(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/attachment/{id:\\d+}")
    public void deleteAttachment(@PathVariable final long id) throws IOException {
        attachmentFeature.deleteAttachment(id);
    }

    /** Excel API */

    @PostMapping(value = "/excel", produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportToExcel(@RequestBody @Validated final OtherwiseDeceasedFilterDTO filter,
                                      final Locale locale) {
        return new ModelAndView(excelFeature.exportToExcel(filter, locale));
    }


}