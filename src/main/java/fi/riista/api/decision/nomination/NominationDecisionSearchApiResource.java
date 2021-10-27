package fi.riista.api.decision.nomination;

import fi.riista.feature.common.decision.DecisionHandlerDTO;
import fi.riista.feature.common.decision.nomination.NominationDecisionDTO;
import fi.riista.feature.common.decision.nomination.NominationDecisionFeature;
import fi.riista.feature.common.decision.nomination.NominationDecisionSearchDTO;
import fi.riista.feature.common.decision.nomination.excel.NominationDecisionExcelFeature;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/nominationdecision/search")
public class NominationDecisionSearchApiResource {

    @Resource
    private NominationDecisionFeature nominationDecisionFeature;

    @Resource
    private NominationDecisionExcelFeature excelFeature;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Slice<NominationDecisionDTO> listDecisions(final @RequestBody @Valid NominationDecisionSearchDTO dto) {
        return nominationDecisionFeature.search(dto);
    }

    @PostMapping(value = "/excel", produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportApplicationsExcel(@Valid @RequestBody NominationDecisionSearchDTO dto, final Locale locale) {

        return new ModelAndView(excelFeature.export(dto, locale));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/handlers")
    public List<DecisionHandlerDTO> listHandlers() {
        return nominationDecisionFeature.listHandlers();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/statistics/{year:\\d+}")
    public List<NominationDecisionHandlingStatisticsDTO> getStatistics(@PathVariable final int year) {
        return nominationDecisionFeature.getStatistics(year);
    }
}
