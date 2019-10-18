package fi.riista.api.moderator;

import com.google.common.base.Preconditions;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.permit.application.AmendApplicationFeature;
import fi.riista.feature.permit.application.HarvestPermitApplicationAmendDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationHandlerDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchFeature;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchResultDTO;
import fi.riista.feature.permit.application.search.excel.HarvestPermitApplicationSearchExcelFeature;
import fi.riista.feature.permit.application.statistics.HarvestPermitApplicationStatisticsExcelFeature;
import fi.riista.feature.permit.application.statistics.HarvestPermitApplicationStatisticsFeature;
import fi.riista.feature.permit.application.statistics.HarvestPermitApplicationStatusTableDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/harvestpermit/application")
public class ModeratorApplicationApiResource {

    @Resource
    private AmendApplicationFeature amendApplicationFeature;

    @Resource
    private HarvestPermitApplicationSearchFeature harvestPermitApplicationSearchFeature;

    @Resource
    private HarvestPermitApplicationStatisticsFeature harvestPermitApplicationStatisticsFeature;

    @Resource
    private HarvestPermitApplicationStatisticsExcelFeature harvestPermitApplicationStatisticsExcelFeature;

    @Resource
    private HarvestPermitApplicationSearchExcelFeature harvestPermitApplicationSearchExcelFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    // STATUS TABLE

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/moderator/statustable/{year:[1-9]\\d{3}}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationStatusTableDTO> statusTable(@PathVariable int year) {
        return harvestPermitApplicationStatisticsFeature.statusTable(year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/moderator/excel/{year:[1-9]\\d{3}}", produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportStatisticsExcel(@PathVariable int year) {
        return new ModelAndView(harvestPermitApplicationStatisticsExcelFeature.export(year));
    }

    // SEARCH

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Slice<HarvestPermitApplicationSearchResultDTO> searchApplications(
            final @RequestBody @Valid HarvestPermitApplicationSearchDTO dto) {
        Preconditions.checkArgument(dto.isPageInfoPresent());
        return harvestPermitApplicationSearchFeature.search(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/search/postalqueue", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationSearchResultDTO> listPostalQueue() {
        return harvestPermitApplicationSearchFeature.listPostalQueue();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/search/excel", produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportApplicationsExcel(@RequestParam(value = "json") @NotBlank final String jsonData,
                                                final Locale locale) throws Exception {
        HarvestPermitApplicationSearchDTO dto = objectMapper.readValue(jsonData,
                HarvestPermitApplicationSearchDTO.class);
        return new ModelAndView(harvestPermitApplicationSearchExcelFeature.export(dto, locale));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/handlers", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationHandlerDTO> listHandlers() {
        return harvestPermitApplicationSearchFeature.listHandlers();
    }


    // AMEND

    @PostMapping("/{applicationId:\\d+}/amend/start")
    public void startAmending(@PathVariable final long applicationId) {
        amendApplicationFeature.startAmendApplication(applicationId);
    }

    @PostMapping("/{applicationId:\\d+}/amend/stop")
    public void stopAmending(@PathVariable final long applicationId,
                             @RequestBody @Valid HarvestPermitApplicationAmendDTO dto) {
        dto.setId(applicationId);
        amendApplicationFeature.stopAmendApplication(dto);
    }
}
