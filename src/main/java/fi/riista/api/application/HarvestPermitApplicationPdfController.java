package fi.riista.api.application;

import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.permit.application.pdf.HarvestPermitApplicationPdfDTO;
import fi.riista.feature.permit.application.pdf.HarvestPermitApplicationPdfFeature;
import fi.riista.integration.common.HttpProxyService;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Locale;

@Controller
@RequestMapping("/api/v1/harvestpermit/application")
public class HarvestPermitApplicationPdfController {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationPdfController.class);

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
    public String getHtml(final @PathVariable long applicationId,
                          final @RequestParam(required = false, defaultValue = "fi") String lang,
                          final Model model,
                          final HttpServletRequest httpServletRequest,
                          final HttpServletResponse httpServletResponse) {
        final Locale locale = Locales.getLocaleByLanguageCode(lang);
        new RequestContext(httpServletRequest, httpServletResponse).changeLocale(locale);

        final HarvestPermitApplicationPdfFeature.PdfModel pdfModel =
                harvestPermitApplicationPdfFeature.getPdfModel(applicationId, locale);

        model.addAttribute("model", pdfModel.getModel());
        model.addAttribute("speciesNames", pdfModel.getSpeciesNames());

        return pdfModel.getView();
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "{applicationId:\\d+}/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void getPdf(final @PathVariable long applicationId,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) {
        final HarvestPermitApplicationPdfDTO dto = harvestPermitApplicationPdfFeature.getApplication(applicationId);

        if (dto.getPrintingUrl() != null) {
            // Use Lupahallinta
            httpProxyService.downloadFile(httpServletResponse,
                    HttpProxyService.toUri(dto.getPrintingUrl()), null,
                    dto.getFilename(), MediaTypeExtras.APPLICATION_PDF);

        } else {
            // Generate PDF locally
            httpServletResponse.addHeader(HttpHeaders.CONTENT_TYPE, MediaTypeExtras.APPLICATION_PDF_VALUE);

            ContentDispositionUtil.addHeader(httpServletResponse, dto.getFilename());

            try (final OutputStream os = httpServletResponse.getOutputStream()) {
                pdfExportFactory.create(httpServletRequest)
                        .withHeaderRight(dto.getHeaderText())
                        .withHtmlPath(getHtmlPath(applicationId))
                        .withLanguage(dto.getLocale())
                        .build()
                        .export(os);

            } catch (Exception ex) {
                LOG.error("Could not generate PDF", ex);
            }
        }
    }
}
