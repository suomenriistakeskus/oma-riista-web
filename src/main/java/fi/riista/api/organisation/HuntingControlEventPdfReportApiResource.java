package fi.riista.api.organisation;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventPdfFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventReportQueryDTO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping(value = "/api/v1/hunting-control-event/report/", produces = MediaType.APPLICATION_JSON_VALUE)
public class HuntingControlEventPdfReportApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(HuntingControlEventPdfReportApiResource.class);

    @Resource
    private PdfExportFactory pdfExportFactory;

    @Resource
    private HuntingControlEventPdfFeature huntingControlEventPdfFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Nonnull
    public static String getHtmlPath(final long rhyId) {
        return String.format("/api/v1/hunting-control-event/report/%d/html", rhyId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/html")
    public String html(final @PathVariable long rhyId,
                       @RequestParam(value = "filters") final String filters,
                       @RequestParam(required = false, defaultValue = "fi") final String lang,
                       final Model model,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) throws JsonProcessingException {
        final HuntingControlEventReportQueryDTO queryFilters = objectMapper.readValue(filters, HuntingControlEventReportQueryDTO.class);
        final Locale locale = Locales.getLocaleByLanguageCode(lang);
        new RequestContext(httpServletRequest, httpServletResponse).changeLocale(locale);

        final HuntingControlEventPdfFeature.PdfModel pdfModel = huntingControlEventPdfFeature.getPdfModel(rhyId, queryFilters, locale);
        model.addAttribute("model", pdfModel.getModel());

        return pdfModel.getView();
    }

    @ResponseBody
    @PostMapping(value = "{rhyId:\\d+}/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public void pdf(final @PathVariable long rhyId,
                    final @Valid @RequestBody HuntingControlEventReportQueryDTO dto,
                    final HttpServletRequest httpServletRequest,
                    final HttpServletResponse httpServletResponse,
                    final Locale locale) throws JsonProcessingException {
        final String filters = objectMapper.writeValueAsString(dto);
        final Map<String, String> requestParams = new HashMap<>();
        requestParams.put("filters", filters);

        httpServletResponse.addHeader(HttpHeaders.CONTENT_TYPE, MediaTypeExtras.APPLICATION_PDF_VALUE);

        ContentDispositionUtil.addHeader(httpServletResponse, Objects.equals(locale.getLanguage(), "sv") ? "jaktovervakningrapporten.pdf" : "metsastyksenvalvontaraportti.pdf");

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            pdfExportFactory.create(httpServletRequest)
                    .withNoHeaderRight()
                    .withHtmlPath(getHtmlPath(rhyId))
                    .withRequestParams(requestParams)
                    .withLanguage(locale)
                    .build()
                    .export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
        }
    }
}
