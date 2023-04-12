package fi.riista.api.harvest;


import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.harvest.LegalHarvestCertificatePdfFeature;
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

import static fi.riista.api.harvest.LegalHarvestCertificateApiResource.API_PREFIX;

@Controller
@RequestMapping(value = API_PREFIX)
public class LegalHarvestCertificateApiResource {
    public static final String API_PREFIX = "/api/v1/harvest/certificate";
    private static final Logger LOG = LoggerFactory.getLogger(LegalHarvestCertificateApiResource.class);

    @Nonnull
    public static String getHtmlPath(final long harvestId) {
        return String.format(API_PREFIX + "/%d/print/html", harvestId);
    }

    @Resource
    private LegalHarvestCertificatePdfFeature legalHarvestCertificatePdfFeature;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{harvestId:\\d+}/print/html", produces = MediaType.TEXT_HTML_VALUE)
    public String html(final @PathVariable long harvestId,
                       @RequestParam(required = false, defaultValue = "fi") final String lang,
                       final Model model,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) {
        final Locale locale = Locales.getLocaleByLanguageCode(lang);
        new RequestContext(httpServletRequest, httpServletResponse).changeLocale(locale);
        final LegalHarvestCertificatePdfFeature.PdfModel pdfModel =
                legalHarvestCertificatePdfFeature.getPdfModel(harvestId, locale);
        model.addAttribute("model", pdfModel.getModel());

        return pdfModel.getView();
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "{harvestId:\\d+}/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long harvestId,
                    final HttpServletRequest httpServletRequest,
                    final HttpServletResponse httpServletResponse) {
        httpServletResponse.addHeader(HttpHeaders.CONTENT_TYPE, MediaTypeExtras.APPLICATION_PDF_VALUE);

        final Locale harvestCertificateLocale = legalHarvestCertificatePdfFeature.getHarvestCertificateLocale(harvestId);

        ContentDispositionUtil.addHeader(httpServletResponse, LegalHarvestCertificatePdfFeature.createFileName(harvestCertificateLocale));

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create(httpServletRequest)
                    .withNoHeaderRight()
                    .withHtmlPath(getHtmlPath(harvestId))
                    .withLanguage(harvestCertificateLocale)
                    .withMargin(0, 0, 0, 0)
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }
}
