package fi.riista.api.admin;

import fi.riista.feature.metrics.AdminMetricsFeature;
import fi.riista.feature.metrics.AdminHarvestReportMetricsDTO;
import fi.riista.feature.metrics.AdminMetricsDTO;
import fi.riista.feature.metrics.AdminRhyEditMetricsDTO;
import fi.riista.feature.metrics.AdminMetricsHarvestReportExcelView;
import fi.riista.feature.metrics.AdminRhyEditMetricsExcelView;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
public class AdminMetricsController {

    @Resource
    private AdminMetricsFeature adminMetricsFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/api/v1/admin/metrics", method = RequestMethod.GET)
    public AdminMetricsDTO get() {
        return adminMetricsFeature.getBasicMetrics();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/api/v1/admin/harvestreportmetrics", method = RequestMethod.GET)
    public List<AdminHarvestReportMetricsDTO> getHarvestReportMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return adminMetricsFeature.getHarvestReportMetrics(beginToDate(begin), endToDate(end));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/api/v1/admin/harvestreportmetrics/excel", method = RequestMethod.POST)
    public ModelAndView searchRhysExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<AdminHarvestReportMetricsDTO> results =
                adminMetricsFeature.getHarvestReportMetrics(beginToDate(begin), endToDate(end));
        return new ModelAndView(new AdminMetricsHarvestReportExcelView(results));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/api/v1/admin/rhyeditmetrics", method = RequestMethod.GET)
    public List<AdminRhyEditMetricsDTO> getRhyEditMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return adminMetricsFeature.getRhyEditMetrics(beginToDate(begin), endToDate(end));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/api/v1/admin/rhyeditmetrics/excel", method = RequestMethod.POST)
    public ModelAndView getRhyEditMetricsExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        final List<AdminRhyEditMetricsDTO> results = adminMetricsFeature.getRhyEditMetrics(beginToDate(begin), endToDate(end));
        return new ModelAndView(new AdminRhyEditMetricsExcelView(results));
    }

    private static Date beginToDate(LocalDate begin) {
        if (begin == null) {
            begin = new LocalDate(2014, 8, 1);
        }
        return begin.toDate();
    }

    private static Date endToDate(LocalDate end) {
        if (end == null) {
            end = DateUtil.today().plusDays(1);
        }
        return end.toDate();
    }
}
