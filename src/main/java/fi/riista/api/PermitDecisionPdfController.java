package fi.riista.api;

import com.newrelic.api.agent.NewRelic;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.permit.decision.pdf.PermitDecisionPdfFeature;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import io.sentry.Sentry;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Controller
@RequestMapping(value = "/api/v1/decision")
public class PermitDecisionPdfController {
    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionPdfController.class);

    @Nonnull
    public static String getHtmlPath(final long decisionId) {
        return String.format("/api/v1/decision/%d/print/html", decisionId);
    }

    @Resource
    private PermitDecisionPdfFeature permitDecisionPrintFeature;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/print/html", produces = MediaType.TEXT_HTML_VALUE)
    public String html(final @PathVariable long decisionId, final Model model) {
        model.addAttribute("model", permitDecisionPrintFeature.getModel(decisionId));

        return "pdf/decision";
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long decisionId,
                    final HttpServletRequest httpServletRequest,
                    final HttpServletResponse httpServletResponse) {
        final String permitNumber = permitDecisionPrintFeature.getDecisionPermitNumber(decisionId);
        final String filename = permitNumber != null
                ? String.format("Päätös_%s.pdf", permitNumber)
                : String.format("Päätös_%d.pdf", decisionId);

        ContentDispositionUtil.addHeader(httpServletResponse, filename);

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create(httpServletRequest)
                    .withHeaderRight(permitNumber != null ? permitNumber : "")
                    .withHtmlPath(getHtmlPath(decisionId))
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
            NewRelic.noticeError(ex, false);
            Sentry.capture(ex);
        }
    }
}
