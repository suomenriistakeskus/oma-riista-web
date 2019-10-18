package fi.riista.api.moderator;

import fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelFeature;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/permitplanning")
public class HirviJyvitysExcelApiResource {

    @Resource
    private JyvitysExcelFeature excelFeature;

    @GetMapping(value = "/{officialCode:\\d{3}}/excel")
    public ModelAndView exportExcel(@PathVariable final String officialCode) {
        return new ModelAndView(excelFeature.export(officialCode));
    }
}
