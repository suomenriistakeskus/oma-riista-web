package fi.riista.api.organisation;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.annualreport.RhyAnnualReportFeature;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@Controller
@RequestMapping(value = RhyAnnualReportApiResource.URL_MAPPING)
public class RhyAnnualReportApiResource {
    static final String URL_MAPPING = "/api/v1/riistanhoitoyhdistys/{rhyId:\\d+}/annualreport/{year:\\d+}";

    @Resource
    private RhyAnnualReportFeature annualReportFeature;

    @Resource
    private MessageSource messageSource;

    // Word
    @ResponseBody
    @PostMapping(produces = MediaTypeExtras.APPLICATION_WORD_VALUE)
    public void annualReport(final Locale locale,
                             final @PathVariable long rhyId,
                             final @PathVariable int year,
                             final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) {
        httpServletResponse.setContentType(MediaTypeExtras.APPLICATION_WORD_VALUE);

        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final String filename = String.format("%s-%s-%s-%s.docx",
                localiser.getTranslation("OrganisationType.RHY"),
                rhyId,
                localiser.getTranslation("RhyAnnualReport.annualReport"),
                year);
        ContentDispositionUtil.addHeader(httpServletResponse, filename);

        try {
            final byte[] document = annualReportFeature.createAnnualReport(rhyId, year, locale, localiser);
            httpServletResponse.getOutputStream().write(document);

        } catch (IOException e) {
            throw new RuntimeException("Error creating annual report");
        }
    }
}
