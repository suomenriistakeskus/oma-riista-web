package fi.riista.api.moderator;

import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitStatisticsDTO;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitStatisticsFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/moderator/mh/statistics")
public class MetsahallitusPermitStatisticsApiResource {

    @Resource
    private MetsahallitusPermitStatisticsFeature metsahallitusPermitStatisticsFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping
    public MetsahallitusPermitStatisticsDTO statistics(@RequestParam(required = false) final Integer huntingYear,
                                                       final Locale locale) {
        return metsahallitusPermitStatisticsFeature.listStatistics(locale, huntingYear);
    }
}
