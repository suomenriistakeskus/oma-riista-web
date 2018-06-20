package fi.riista.api;

import com.newrelic.api.agent.NewRelic;
import fi.riista.feature.account.certificate.HunterPdfExportFeature;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import io.sentry.Sentry;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Controller
@RequestMapping(value = HunterCertificateApiResource.URL_PREFIX + "{hunterNumber:\\d+}")
public class HunterCertificateApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(HunterCertificateApiResource.class);

    static final String URL_PREFIX = "/api/v1/certificate/";

    private static final String JSP_FOREIGN_EN = "pdf/foreign-hunter-certificate-en";
    private static final String JSP_FOREIGN_DE = "pdf/foreign-hunter-certificate-de";
    private static final String JSP_HUNTER_CARD_FI = "pdf/hunting-card-fi";
    private static final String JSP_HUNTER_CARD_SV = "pdf/hunting-card-sv";

    @Resource
    private HunterPdfExportFeature hunterPdfExportFeature;

    @Resource
    private PdfExportFactory pdfExportFactory;

    // PDF

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/huntingCard.pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdfHuntingCard(@PathVariable String hunterNumber,
                               @RequestParam String lang,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        final String uri = URL_PREFIX + hunterNumber + "/huntingCard.html";
        final String filename = String.format("hunting-card-%s-%s.pdf", hunterNumber, lang);
        generatePdf(uri, filename, lang, httpServletRequest, httpServletResponse);
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/foreign.pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void foreignCertificatePdf(@PathVariable String hunterNumber,
                                      @RequestParam String lang,
                                      HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse) {
        final String uri = URL_PREFIX + hunterNumber + "/foreign.html";
        final String filename = String.format("foreign-certificate-%s-%s.pdf", hunterNumber, lang);
        generatePdf(uri, filename, lang, httpServletRequest, httpServletResponse);
    }

    private void generatePdf(final String uri, final String filename, final String lang,
                             final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) {
        try {
            ContentDispositionUtil.addHeader(httpServletResponse, filename);

            try (final OutputStream os = httpServletResponse.getOutputStream()) {
                pdfExportFactory.create(httpServletRequest)
                        .withHtmlPath(uri)
                        .withLanguage(lang)
                        .withMargin(0,0,0,0)
                        .build()
                        .export(os);
            }
        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);

            NewRelic.noticeError(ex, false);
            Sentry.capture(ex);
        }
    }

    // HTML

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/huntingCard.html")
    public String htmlHuntingCard(@PathVariable String hunterNumber,
                                  @RequestParam(required = false, defaultValue = "fi") String lang,
                                  Model model) {
        model.addAttribute("model", hunterPdfExportFeature.huntingCard(hunterNumber, lang));

        switch (lang) {
            case "fi":
                return JSP_HUNTER_CARD_FI;
            case "sv":
                return JSP_HUNTER_CARD_SV;
            default:
                throw new IllegalArgumentException("Invalid languageCode");
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/foreign.html")
    public String foreignCertificateHtml(@PathVariable String hunterNumber,
                                         @RequestParam(required = false, defaultValue = "en") String lang,
                                         Model model) {
        model.addAttribute("model", hunterPdfExportFeature.foreignHuntingCertificateModel(hunterNumber));

        switch (lang) {
            case "en":
                return JSP_FOREIGN_EN;
            case "de":
                return JSP_FOREIGN_DE;
            default:
                throw new IllegalArgumentException("Invalid languageCode");
        }
    }

}
