package fi.riista.feature.huntingclub.statistics.luke;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.integration.common.HttpProxyService;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class LukeReportFeature {
    private static final Logger LOG = LoggerFactory.getLogger(LukeReportFeature.class);

    private final ActiveUserService activeUserService;
    private final HttpProxyService httpProxyService;
    private final LukeReportUriBuilderFactory lukeReportUriBuilderFactory;
    private final UsernamePasswordCredentials credentials;

    @Autowired
    public LukeReportFeature(final ActiveUserService activeUserService,
                             final HttpProxyService httpProxyService,
                             final LukeReportUriBuilderFactory lukeReportUriBuilderFactory,
                             final LukeReportEndpoint lukeReportEndpoint) {
        this.activeUserService = activeUserService;
        this.httpProxyService = httpProxyService;
        this.lukeReportUriBuilderFactory = lukeReportUriBuilderFactory;
        this.credentials = lukeReportEndpoint.getHttpClientCredentials();
    }

    @Nonnull
    @Transactional(readOnly = true, timeout = 5)
    public LukeReportUriBuilder getUriBuilder(final long permitId, final Long clubId, final HttpSession session) {
        return lukeReportUriBuilderFactory.getUriBuilder(permitId, clubId, session);
    }

    public void getReport(final LukeReportUriBuilder uriBuilder,
                          final HttpServletResponse httpServletResponse,
                          final LukeReportParams.Organisation org,
                          final LukeReportParams.Presentation presentation,
                          final String fileName) {
        final URI lukeReportUrl = uriBuilder.getReportUri(org, presentation, fileName);

        LOG.info("userId:{} permitId:{} clubId:{} url:{}", activeUserService.requireActiveUserId(),
                uriBuilder.getPermitId(), uriBuilder.getClubId(), lukeReportUrl.getPath());

        httpProxyService.downloadFile(httpServletResponse, lukeReportUrl, credentials,
                null, presentation.getContentType());
    }

    public LukeReportParamsDTO getReportParameters(final LukeReportUriBuilder uriBuilder) {
        final URI checkClubReportExistsUri = uriBuilder.getCheckClubReportExistsUri();
        final List<Map<String, Object>> parameters = checkClubReportExistsUri != null
                ? LukeReportParams.Organisation.allValues()
                : LukeReportParams.Organisation.valuesWithoutClub();

        return new LukeReportParamsDTO(parameters, checkClubReportExistsUri != null &&
                httpProxyService.checkHeadResponseOk(checkClubReportExistsUri, credentials));
    }
}
