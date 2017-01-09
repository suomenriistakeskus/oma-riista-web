package fi.riista.api;

import fi.riista.feature.account.certificate.HunterPdfExportFeature;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.util.PdfExport;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import fi.riista.feature.RuntimeEnvironmentUtil;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Controller
@RequestMapping(value = HunterCertificateApiResource.URL_PREFIX + "{hunterNumber:\\d+}")
public class HunterCertificateApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(HunterCertificateApiResource.class);

    public static final Duration PDF_JWT_TTL = Duration.standardMinutes(2);

    public static final String URL_PREFIX = "/api/v1/certificate/";

    public static final String JSP_FOREIGN_EN = "pdf/foreign-hunter-certificate-en";
    public static final String JSP_FOREIGN_DE = "pdf/foreign-hunter-certificate-de";
    public static final String JSP_HUNTER_CARD_FI = "pdf/hunting-card-fi";
    public static final String JSP_HUNTER_CARD_SV = "pdf/hunting-card-sv";

    @Resource
    private HunterPdfExportFeature hunterPdfExportFeature;

    @Resource
    private RuntimeEnvironmentUtil environmentUtil;

    @Resource
    private ActiveUserService activeUserService;

    @ExceptionHandler(Exception.class)
    public String handleAllErrors(@SuppressWarnings("unused") Exception ex) {
        return "error/error";
    }

    static String foreignCertificateFilename(final String hunterNumber, final String lang) {
        Objects.requireNonNull(hunterNumber);
        Objects.requireNonNull(lang);

        return "foreign-certificate-" + hunterNumber + "-" + lang + ".pdf";
    }

    static String huntingCardFilename(final String hunterNumber, final String lang) {
        Objects.requireNonNull(hunterNumber);
        Objects.requireNonNull(lang);

        return "hunting-card-" + hunterNumber + "-" + lang + ".pdf";
    }

    // PDF

    private PdfExport getPdfBuilder(final HttpServletRequest httpServletRequest) {
        final String jwtToken = activeUserService.createLoginTokenForActiveUser(PDF_JWT_TTL);

        return new PdfExport(httpServletRequest, jwtToken, environmentUtil.isProductionEnvironment());
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/huntingCard.pdf", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdfHuntingCard(@PathVariable String hunterNumber,
                               @RequestParam String lang,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) throws IOException {
        try {
            getPdfBuilder(httpServletRequest)
                    .withFileName(huntingCardFilename(hunterNumber, lang))
                    .withHtmlPath(URL_PREFIX + hunterNumber + "/huntingCard.html")
                    .withLanguage(lang)
                    .export(httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            if (!httpServletResponse.isCommitted()) {
                throw ex;
            }
            LOG.error("Could not generate PDF", ex);
        }
    }

    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/foreign.pdf", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void foreignCertificatePdf(@PathVariable String hunterNumber,
                                      @RequestParam String lang,
                                      HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse) throws IOException {
        try {
            getPdfBuilder(httpServletRequest)
                    .withFileName(foreignCertificateFilename(hunterNumber, lang))
                    .withHtmlPath(URL_PREFIX + hunterNumber + "/foreign.html")
                    .withLanguage(lang)
                    .export(httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            if (!httpServletResponse.isCommitted()) {
                throw ex;
            }
            LOG.error("Could not generate PDF", ex);
        }
    }

    // HTML

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/huntingCard.html", method = RequestMethod.GET)
    public String htmlHuntingCard(@PathVariable String hunterNumber,
                                  @RequestParam(required = false, defaultValue = "fi") String lang,
                                  Model model) {
        model.addAttribute("model", hunterPdfExportFeature.huntingCard(hunterNumber, lang));

        if ("fi".equals(lang)) {
            return JSP_HUNTER_CARD_FI;
        } else if ("sv".equals(lang)) {
            return JSP_HUNTER_CARD_SV;
        } else {
            throw new IllegalArgumentException("Invalid languageCode");
        }
    }


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/foreign.html", method = RequestMethod.GET)
    public String foreignCertificateHtml(@PathVariable String hunterNumber,
                                         @RequestParam(required = false, defaultValue = "en") String lang,
                                         Model model) {
        model.addAttribute("model", hunterPdfExportFeature.foreignHuntingCertificateModel(hunterNumber));

        if ("en".equals(lang)) {
            return JSP_FOREIGN_EN;
        } else if ("de".equals(lang)) {
            return JSP_FOREIGN_DE;
        } else {
            throw new IllegalArgumentException("Invalid languageCode");
        }
    }

}
