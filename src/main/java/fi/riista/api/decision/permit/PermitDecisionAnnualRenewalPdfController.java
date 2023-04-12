package fi.riista.api.decision.permit;

import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.harvestpermit.report.paper.PermitHarvestReportFeature;
import fi.riista.feature.harvestpermit.report.paper.PermitHarvestReportPdf;
import fi.riista.feature.permit.decision.derogation.pdf.AnnualRenewalPermitPdfFeature;
import fi.riista.feature.permit.decision.derogation.pdf.AnnualRenewalPermitPdfModelDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping(value = "/api/v1/decision/annual/{permitId:\\d+}")
public class PermitDecisionAnnualRenewalPdfController {
    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionAnnualRenewalPdfController.class);

    private static LocalisedString FILENAME =
            LocalisedString.of("jatkolupa-%s.pdf", "fortsattningsdispens-%s.pdf");

    @Nonnull
    public static String getHtmlPath(final long permitId) {
        return String.format("/api/v1/decision/annual/%d/print/html", permitId);
    }

    @Resource
    private AnnualRenewalPermitPdfFeature pdfFeature;

    @Resource
    private PermitHarvestReportFeature permitHarvestReportFeature;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/print/html", produces = MediaType.TEXT_HTML_VALUE)
    public String html(final @PathVariable long permitId, final Model model) {
        final AnnualRenewalPermitPdfModelDTO pdfModel = pdfFeature.getModel(permitId);
        model.addAttribute("model", pdfModel);

        return Locales.isSwedish(pdfModel.getDecisionLocale())
                ? "pdf/annual-renewal-permit-sv"
                : "pdf/annual-renewal-permit-fi";
    }

    @ResponseBody
    @PostMapping(value = "/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long permitId,
                    final HttpServletRequest httpServletRequest,
                    final HttpServletResponse httpServletResponse) {
        final AnnualRenewalPermitPdfModelDTO model = pdfFeature.getModel(permitId);

        final String filename = String.format(
                FILENAME.getAnyTranslation(model.getDecisionLocale()), model.getPermitNumber());

        ContentDispositionUtil.addHeader(httpServletResponse, filename);

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create(httpServletRequest)
                    .withHtmlPath(getHtmlPath(permitId))
                    .withMargin(0, 0, 0, 0)
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }


    // HARVEST REPORT

    @PostMapping(value = "/print/harvest-report", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printHarvestReport(final @PathVariable long permitId) throws IOException {
        final PermitHarvestReportPdf pdf = permitHarvestReportFeature.getRenewedPermitHarvestReportPdf(permitId);
        return pdf.asResponseEntity();
    }
}
