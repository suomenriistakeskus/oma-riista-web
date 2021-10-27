package fi.riista.api.moderator;

import fi.riista.feature.organization.rka.AreaMeetingRepresentativeExcelFeature;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/rka")
public class AreaMeetingApiResource {

    @Resource
    private AreaMeetingRepresentativeExcelFeature excelFeature;

    @PostMapping(value = "/{rkaId:\\d+}/meeting/representatives/excel")
    public ModelAndView exportExcel(final @PathVariable long rkaId,
                                    final Locale locale) {
        return new ModelAndView(excelFeature.export(rkaId, locale));
    }
}
