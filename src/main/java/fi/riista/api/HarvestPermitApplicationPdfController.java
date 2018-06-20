package fi.riista.api;

import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.pdf.HarvestPermitApplicationPdfFeature;
import fi.riista.integration.common.HttpProxyService;
import fi.riista.util.MediaTypeExtras;
import io.vavr.Tuple2;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
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
import java.net.URL;
import java.util.Locale;

@Controller
@RequestMapping("/api/v1/harvestpermit/application")
public class HarvestPermitApplicationPdfController {
    @Nonnull
    public static String getHtmlPath(final long applicationId) {
        return String.format("/api/v1/harvestpermit/application/%d/print/html", applicationId);
    }

    @Resource
    private PdfExportFactory pdfExportFactory;

    @Resource
    private HttpProxyService httpProxyService;

    @Resource
    private HarvestPermitApplicationPdfFeature harvestPermitApplicationPdfFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{applicationId:\\d+}/print/html", produces = MediaType.TEXT_HTML_VALUE)
    public String getHtml(final @PathVariable long applicationId, Model model) {
        model.addAttribute("model", harvestPermitApplicationPdfFeature.getPdfModel(applicationId));

        return "pdf/application";
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "{applicationId:\\d+}/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void getPdf(final @PathVariable long applicationId,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) {
        final Tuple2<URL, Integer> urlAndApplicationNumber = harvestPermitApplicationPdfFeature
                .getApplicationNumberAndPrintUri(applicationId);
        final Locale locale = LocaleContextHolder.getLocale();
        final String fileName = HarvestPermitApplication.getPdfFileName(locale, urlAndApplicationNumber._2);

        if (urlAndApplicationNumber._1 != null) {
            // Use Lupahallinta
            httpProxyService.downloadFile(httpServletResponse,
                    HttpProxyService.toUri(urlAndApplicationNumber._1), null,
                    fileName, MediaTypeExtras.APPLICATION_PDF);

        } else {
            // Generate PDF locally
            httpServletResponse.addHeader(HttpHeaders.CONTENT_TYPE, MediaTypeExtras.APPLICATION_PDF_VALUE);
            pdfExportFactory.exportPdf(getHtmlPath(applicationId), fileName, httpServletRequest, httpServletResponse);
        }
    }

}
