package fi.riista.api.mobile;

import fi.riista.api.shootingtest.ShootingTestSummaryExportApiResource;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = MobileShootingTestSummaryExportApiResource.URL_PREFIX_MOBILE)
public class MobileShootingTestSummaryExportApiResource {


    /*package*/ final static String URL_PREFIX_MOBILE = "/api/mobile/v2/shootingtest/event/";

    @Resource
    private ShootingTestSummaryExportApiResource shootingTestSummaryExportApiResource;


    // PDF

    @ResponseBody
    @PostMapping(value = "{eventId:\\d+}/event-summary.pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdfEventSummaryMobile(@PathVariable final String eventId,
                                      @RequestParam(required = false, defaultValue = "fi") final String lang,
                                      final HttpServletRequest httpServletRequest,
                                      final HttpServletResponse httpServletResponse) {
        final String uri = URL_PREFIX_MOBILE + eventId + "/event-summary.html";

        shootingTestSummaryExportApiResource.generatePdf(uri, eventId, lang, httpServletResponse);
    }

    // HTML

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{eventId:\\d+}/event-summary.html", produces = MediaType.TEXT_HTML_VALUE)
    public String htmlEventSummaryMobile(@PathVariable final Long eventId,
                                         @RequestParam(required = false, defaultValue = "fi") final String lang,
                                         final Model model) {
        return shootingTestSummaryExportApiResource.generateHtml(eventId, lang, model);

    }
}
