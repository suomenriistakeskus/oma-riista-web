package fi.riista.api.decision.permit;

import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.permit.decision.pdf.PermitDecisionPdfFeature;
import fi.riista.feature.permit.decision.pdf.PermitDecisionPdfFileDTO;
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

    @Nonnull
    public static String getPublicHtmlPath(final long decisionId) {
        return String.format("/api/v1/decision/%d/print/public-html", decisionId);
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

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/print/public-html", produces = MediaType.TEXT_HTML_VALUE)
    public String publicHtml(final @PathVariable long decisionId, final Model model) {
        model.addAttribute("model", permitDecisionPrintFeature.getModel(decisionId));

        return "pdf/decision-no-contact-info";
    }

    @ResponseBody
    @PostMapping(value = "{decisionId:\\d+}/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long decisionId,
                    final HttpServletRequest httpServletRequest,
                    final HttpServletResponse httpServletResponse) {
        final PermitDecisionPdfFileDTO dto = permitDecisionPrintFeature.getDecisionPermitNumber(decisionId);

        ContentDispositionUtil.addHeader(httpServletResponse, dto.getFilename());

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create(httpServletRequest)
                    .withHeaderRight(dto.getHeaderText())
                    .withHtmlPath(getHtmlPath(decisionId))
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }
}
