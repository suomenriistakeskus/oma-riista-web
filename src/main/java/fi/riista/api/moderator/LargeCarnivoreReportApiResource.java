package fi.riista.api.moderator;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.largecarnivorereport.LargeCarnivoreExcelExportDTO;
import fi.riista.feature.largecarnivorereport.LargeCarnivoreReportExcelFeature;
import fi.riista.feature.largecarnivorereport.LargeCarnivoreReportExcelView;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/large-carnivore-report/{huntingYear:\\d+}")
public class LargeCarnivoreReportApiResource {

    @Resource
    private LargeCarnivoreReportExcelFeature excelFeature;

    @Resource
    private MessageSource messageSource;

    @GetMapping
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public ModelAndView exportExcel(final Locale locale,
                                    @PathVariable final int huntingYear) {
        final LargeCarnivoreExcelExportDTO dto = excelFeature.export(huntingYear);
        return new ModelAndView(new LargeCarnivoreReportExcelView(new EnumLocaliser(messageSource, locale), dto));
    }

}
