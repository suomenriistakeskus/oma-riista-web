package fi.riista.api;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.HarvestChangeHistoryDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportModeratorFeature;
import fi.riista.feature.harvestpermit.report.HarvestReportStateChangeDTO;
import fi.riista.feature.harvestpermit.report.category.HarvestReportCategoryDTO;
import fi.riista.feature.harvestpermit.report.category.HarvestReportCategoryFeature;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportExcelDTO;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportListExcelView;
import fi.riista.feature.harvestpermit.report.search.HarvestReportSearchDTO;
import fi.riista.feature.harvestpermit.report.search.HarvestReportSearchFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/harvestreport", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class HarvestReportApiResource {

    @Resource
    private HarvestReportCategoryFeature harvestReportCategoryFeature;

    @Resource
    private HarvestReportSearchFeature harvestReportSearchFeature;

    @Resource
    private HarvestReportModeratorFeature harvestReportModeratorFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/categories")
    public List<HarvestReportCategoryDTO> getFields(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean excludePermitNotRequiredWithoutSeason) {
        return harvestReportCategoryFeature.list(activeOnly, excludePermitNotRequiredWithoutSeason);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/harvest/{harvestId:\\d+}/state")
    public void changeHarvestState(@PathVariable long harvestId,
                                   @RequestBody @Valid HarvestReportStateChangeDTO dto) {
        dto.setHarvestId(harvestId);
        harvestReportModeratorFeature.changeHarvestReportState(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/harvest/{harvestId:\\d+}/history")
    public List<HarvestChangeHistoryDTO> changeHistory(@PathVariable long harvestId) {
        return harvestReportModeratorFeature.getChangeHistory(harvestId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/admin")
    public Map<String, Object> admin() {
        return harvestReportModeratorFeature.admin();
    }

    // Search

    @PostMapping("/rhy/search")
    public List<HarvestDTO> searchRhy(@RequestBody @Valid HarvestReportSearchDTO dto) {
        dto.setSearchType(HarvestReportSearchDTO.SearchType.COORDINATOR);
        return harvestReportSearchFeature.searchCoordinator(dto);
    }

    @PostMapping("/admin/search")
    public Slice<HarvestDTO> search(@RequestBody @Valid HarvestReportSearchDTO dto, Pageable pageRequest) {
        dto.setSearchType(HarvestReportSearchDTO.SearchType.MODERATOR);
        return harvestReportSearchFeature.searchModerator(dto, pageRequest);
    }

    // Excel export

    @PostMapping("/rhy/search/excel")
    public ModelAndView searchRhyExcel(
            @RequestParam(value = "json") @NotBlank final String jsonData) throws IOException {
        final HarvestReportSearchDTO dto = objectMapper.readValue(jsonData, HarvestReportSearchDTO.class);
        dto.setSearchType(HarvestReportSearchDTO.SearchType.COORDINATOR);

        final EnumLocaliser localiser = new EnumLocaliser(messageSource, LocaleContextHolder.getLocale());
        final List<HarvestReportExcelDTO> data = harvestReportSearchFeature.searchCoordinatorExcel(dto);

        return new ModelAndView(HarvestReportListExcelView.create(localiser, data));
    }

    @PostMapping("/admin/search/excel")
    public ModelAndView searchExcel(
            @RequestParam(value = "json") @NotBlank final String jsonData) throws IOException {
        final HarvestReportSearchDTO dto = objectMapper.readValue(jsonData, HarvestReportSearchDTO.class);
        dto.setSearchType(HarvestReportSearchDTO.SearchType.MODERATOR);

        final EnumLocaliser localiser = new EnumLocaliser(messageSource, LocaleContextHolder.getLocale());
        final List<HarvestReportExcelDTO> data = harvestReportSearchFeature.searchModeratorExcel(dto);

        return new ModelAndView(HarvestReportListExcelView.create(localiser, data));
    }

}
