package fi.riista.feature.permit.area.mml;

import fi.riista.config.Constants;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
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
@RequestMapping(value = "/api/v1/application/area/mml")
public class HarvestPermitAreaMmlPdfController {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitAreaMmlPdfController.class);

    @Nonnull
    public static String getHtmlPath(final long applicationId) {
        return String.format("/api/v1/application/area/mml/%d/print/html", applicationId);
    }

    private String getFileName(final Locale locale) {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        final String filename = messageSource.getMessage("HarvestPermitAreaMmlPdf.fileName", null, locale);
        return String.format("%s-%s.pdf", filename, timestamp);
    }

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestPermitAreaMmlPdfFeature harvestPermitAreaMmlPdfFeature;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{applicationId:\\d+}/print/html", produces = MediaType.TEXT_HTML_VALUE)
    public String html(final @PathVariable long applicationId,
                       final @RequestParam(required = false, defaultValue = "fi") String lang,
                       final Model model,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) {
        final Locale locale = Locales.getLocaleByLanguageCode(lang);
        new RequestContext(httpServletRequest, httpServletResponse).changeLocale(locale);

        final HarvestPermitAreaMmlPdfFeature.PdfModel pdfModel = harvestPermitAreaMmlPdfFeature.getModel(applicationId);

        model.addAttribute("propertyList", pdfModel.getMmls());

        return pdfModel.getView();
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "{applicationId:\\d+}/print/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long applicationId,
                    final HttpServletResponse httpServletResponse,
                    final Locale locale) {
        ContentDispositionUtil.addHeader(httpServletResponse, getFileName(locale));

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create()
                    .withHtmlPath(getHtmlPath(applicationId))
                    .withLanguage(locale)
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }
}
