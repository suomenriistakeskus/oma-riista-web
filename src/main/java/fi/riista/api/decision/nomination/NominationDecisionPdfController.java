package fi.riista.api.decision.nomination;

import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.common.decision.nomination.pdf.NominationDecisionPdfFeature;
import fi.riista.feature.common.decision.nomination.pdf.NominationDecisionPdfFileDTO;
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
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Controller
@RequestMapping(value = "/api/v1/nominationdecision")
public class NominationDecisionPdfController {
    private static final Logger LOG = LoggerFactory.getLogger(NominationDecisionPdfController.class);

    @Nonnull
    public static String getHtmlPath(final long decisionId) {
        return String.format("/api/v1/nominationdecision/%d/print/html", decisionId);
    }

    @Resource
    private NominationDecisionPdfFeature nominationDecisionPdfFeature;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/print/html", produces = MediaType.TEXT_HTML_VALUE)
    public String html(final @PathVariable long decisionId, final Model model) {
        model.addAttribute("model", nominationDecisionPdfFeature.getModel(decisionId));

        return "pdf/nominationdecision";
    }

    @ResponseBody
    @PostMapping(value = "{decisionId:\\d+}/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long decisionId,
                    final HttpServletResponse httpServletResponse) {
        final NominationDecisionPdfFileDTO dto = nominationDecisionPdfFeature.getDecisionFileName(decisionId);

        ContentDispositionUtil.addHeader(httpServletResponse, dto.getFilename());

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create()
                    .withHeaderRight(dto.getHeaderText())
                    .withHtmlPath(getHtmlPath(decisionId))
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }
}
