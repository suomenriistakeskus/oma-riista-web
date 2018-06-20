package fi.riista.feature.common;

import com.newrelic.api.agent.NewRelic;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.PdfExport;
import io.sentry.Sentry;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Component
public class PdfExportFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PdfExportFactory.class);

    private static final Duration PDF_JWT_TTL = Duration.standardMinutes(2);

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    public PdfExport.Builder create() {
        return create(UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri()));
    }

    public PdfExport.Builder create(final HttpServletRequest request) {
        return create(ServletUriComponentsBuilder.fromRequestUri(request));
    }

    public PdfExport.Builder create(final UriComponentsBuilder uriBuilder) {
        return new PdfExport.Builder(uriBuilder, runtimeEnvironmentUtil.isProductionEnvironment())
                .withDpi(runtimeEnvironmentUtil.isDevelopmentEnvironment() ? 300 : 72)
                .withImageDpi(runtimeEnvironmentUtil.isDevelopmentEnvironment() ? 300 : 72)
                .withAuthenticationToken(activeUserService.createLoginTokenForActiveUser(PDF_JWT_TTL));
    }

    public void exportPdf(final String htmlPath,
                          final String filename,
                          final HttpServletRequest httpServletRequest,
                          final HttpServletResponse httpServletResponse) {
        ContentDispositionUtil.addHeader(httpServletResponse, filename);

        try (final OutputStream os = httpServletResponse.getOutputStream()) {
            create(httpServletRequest).withHtmlPath(htmlPath).build().export(os);

        } catch (Exception ex) {
            LOG.error("Could not generate PDF", ex);
            NewRelic.noticeError(ex, false);
            Sentry.capture(ex);
        }
    }
}
