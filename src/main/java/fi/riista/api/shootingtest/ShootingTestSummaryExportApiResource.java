package fi.riista.api.shootingtest;


import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.shootingtest.ShootingTestSummaryExportFeature;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Controller
public class ShootingTestSummaryExportApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(ShootingTestSummaryExportApiResource.class);

    private static final String JSP_SHOOTING_TEST_SUMMARY_FI = "pdf/shootingtest-summary-fi";
    private static final String JSP_SHOOTING_TEST_SUMMARY_SV = "pdf/shootingtest-summary-sv";

    @Resource
    private ShootingTestSummaryExportFeature shootingTestSummaryExportFeature;


    @Resource
    private PdfExportFactory pdfExportFactory;


    // PDF

    @ResponseBody
    @PostMapping(
            value = ShootingTestApiResource.URL_PREFIX + "/summary/event/{calendarEventId:\\d+}/event-summary.pdf",
            produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdfEventSummary(@PathVariable final String calendarEventId,
                                @RequestParam(required = false, defaultValue = "fi") final String lang,
                                final HttpServletResponse httpServletResponse) {
        final String uri = ShootingTestApiResource.URL_PREFIX + "/summary/event/" + calendarEventId + "/event-summary.html";
        httpServletResponse.setContentType(MediaTypeExtras.APPLICATION_PDF_VALUE);
        generatePdf(uri, calendarEventId, lang, httpServletResponse);
    }

    // HTML
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(
            value = ShootingTestApiResource.URL_PREFIX + "/summary/event/{calendarEventId:\\d+}/event-summary.html",
            produces = MediaType.TEXT_HTML_VALUE)
    public String htmlEventSummary(@PathVariable final Long calendarEventId,
                                   @RequestParam(required = false, defaultValue = "fi") final String lang,
                                   final Model model) {

        return generateHtml(calendarEventId, lang, model);
    }

    public String generateHtml(final Long calendarEventId,
                               final String lang,
                               final Model model) {
        model.addAttribute("model", shootingTestSummaryExportFeature.getShootingTestSummary(calendarEventId));
        switch (lang) {
            case "sv":
                return JSP_SHOOTING_TEST_SUMMARY_SV;
            case "fi": // Fall through
            default:
                return JSP_SHOOTING_TEST_SUMMARY_FI;
        }
    }

    public void generatePdf(final String uri, final String calendarEventId, final String lang,
                            final HttpServletResponse httpServletResponse) {
        try {
            final String filename = String.format("event-summary-%s-%s.pdf", calendarEventId, lang);
            ContentDispositionUtil.addHeader(httpServletResponse, filename);

            try (final OutputStream os = httpServletResponse.getOutputStream()) {
                pdfExportFactory.create()
                        .withHtmlPath(uri)
                        .withLanguage(lang)
                        .build()
                        .export(os);
            }
        } catch (final Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }
}
