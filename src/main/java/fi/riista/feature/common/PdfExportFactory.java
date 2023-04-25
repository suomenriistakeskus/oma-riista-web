package fi.riista.feature.common;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.util.PdfExport;
import org.joda.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;

@Component
public class PdfExportFactory {
    private static final Duration PDF_JWT_TTL = Duration.standardMinutes(2);

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    public PdfExport.Builder create() {
        return create(UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri()));
    }

    public PdfExport.Builder create(final UriComponentsBuilder uriBuilder) {
        return new PdfExport.Builder(uriBuilder, runtimeEnvironmentUtil.isAwsEnvironment())
                .withDpi(runtimeEnvironmentUtil.isDevelopmentEnvironment() ? 300 : 72)
                .withImageDpi(runtimeEnvironmentUtil.isDevelopmentEnvironment() ? 300 : 72)
                .withAuthenticationToken(activeUserService.createLoginTokenForActiveUser(PDF_JWT_TTL));
    }
}
