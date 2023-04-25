package fi.riista.api;

import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.gamediary.srva.SrvaPdfFeature;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/v1/srva")
public class SrvaPdfReportApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(SrvaPdfReportApiResource.class);

    @Resource
    private SrvaPdfFeature srvaPdfFeature;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @Nonnull
    public static String getHtmlPath(final long id) {
        return String.format("/api/v1/srva/%d/report/html", id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/report/html")
    public String html(final @PathVariable long id,
                       @RequestParam(required = false, defaultValue = "fi") final String lang,
                       final Model model,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) {
        final Locale locale = Locales.getLocaleByLanguageCode(lang);
        new RequestContext(httpServletRequest, httpServletResponse).changeLocale(locale);

        final SrvaPdfFeature.PdfModel pdfModel = srvaPdfFeature.getPdfModel(id, locale);
        model.addAttribute("model", pdfModel.getModel());

        return pdfModel.getView();
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "{id:\\d+}/report/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long id,
                    final HttpServletResponse httpServletResponse,
                    final Locale locale) {
        httpServletResponse.addHeader(HttpHeaders.CONTENT_TYPE, MediaTypeExtras.APPLICATION_PDF_VALUE);

        ContentDispositionUtil.addHeader(httpServletResponse, "srva-raportti.pdf");

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create()
                    .withNoHeaderRight()
                    .withHtmlPath(getHtmlPath(id))
                    .withLanguage(locale)
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }

}
