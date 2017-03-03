package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@Component
public class LukeReportFeature {
    private static final Logger LOG = LoggerFactory.getLogger(LukeReportFeature.class);

    @Nonnull
    private static RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(750)
                .setSocketTimeout(5000)
                .build();
    }

    @Resource
    private RequireEntityService entityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HttpClient httpClient;

    @Value("${luke.moose.reports.url.prefix}")
    private URI lukeReportPrefix;

    @Value("${luke.moose.reports.user}")
    private String lukeMooseReportUsername;

    @Value("${luke.moose.reports.pass}")
    private String lukeMooseReportPassword;

    private URI createFullUri(final String reportPath) {
        return UriComponentsBuilder.fromUri(lukeReportPrefix).replacePath(reportPath).build().toUri();
    }

    @Transactional(readOnly = true)
    public LukeReportParamsDTO getLukeReportParams(@Nullable Long clubId, long permitId) {
        final HuntingClub club = clubId != null ? entityService.requireHuntingClub(clubId, EntityPermission.READ) : null;
        final HarvestPermit permit = entityService.requireHarvestPermit(permitId, EntityPermission.READ);

        return new LukeReportParamsDTO(
                checkClubReportsExist(club, permit),
                club == null ? LukeReportParams.Organisation.valuesWithoutClub() : LukeReportParams.Organisation.values());
    }

    @Transactional(readOnly = true)
    public URI getLukeReportUrl(final Long clubId,
                                final long permitId,
                                final LukeReportParams.Organisation org,
                                final LukeReportParams.Presentation presentation,
                                final String fileName) {
        final HuntingClub club = clubId == null ? null : entityService.requireHuntingClub(clubId, EntityPermission.READ);
        final HarvestPermit permit = entityService.requireHarvestPermit(permitId, EntityPermission.READ);

        Preconditions.checkNotNull(permit.getMooseArea(), "permit.mooseArea should not be null, permitId:" + permitId);

        final String htaNumber = permit.getMooseArea().getNumber();
        final String reportPath = LukeReportParams.getReportPath(
                club, permit, htaNumber, org, presentation, fileName);

        LOG.info("userId:{} permitId:{} clubId:{} url:{}", activeUserService.getActiveUserId(), permitId, clubId, reportPath);

        return createFullUri(reportPath);
    }

    private boolean checkClubReportsExist(final HuntingClub club, final HarvestPermit permit) {
        final String path = LukeReportParams.getCheckExistsPath(club, permit);
        return path != null && doTestUrlExists(createFullUri(path));
    }

    private boolean doTestUrlExists(final URI requestUri) {
        try {
            final HttpHead request = new HttpHead(requestUri);
            request.addHeader(new BasicScheme().authenticate(createCredentials(), request, new BasicHttpContext()));
            request.setConfig(createRequestConfig());
            return httpClient.execute(request).getStatusLine().getStatusCode() == HttpStatus.OK.value();
        } catch (final Exception e) {
            LOG.error("Exception, message: " + e.getMessage());
        }
        return false;
    }

    public void getLukeReport(final URI requestUri, final HttpServletResponse response) {
        final UsernamePasswordCredentials credentials = createCredentials();

        try {
            final HttpGet request = new HttpGet(requestUri);
            request.setConfig(createRequestConfig());
            request.addHeader(new BasicScheme().authenticate(credentials, request, new BasicHttpContext()));

            final HttpResponse lukeResponse = httpClient.execute(request);
            int lukeResponseStatusCode = lukeResponse.getStatusLine().getStatusCode();

            if (lukeResponseStatusCode == HttpStatus.OK.value()) {
                if (requestUri.getPath().endsWith(".png")) {
                    response.setContentType("image/png");
                }

                if (requestUri.getPath().endsWith(".html")) {
                    response.setContentType("text/html");
                }

                lukeResponse.getEntity().writeTo(response.getOutputStream());
            } else {
                LOG.warn("Fetching Luke report failed to statusCode:" + lukeResponseStatusCode);
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            LOG.error(String.format("Fetching LUKE report failed, url:%s msg:%s", requestUri, e.getMessage()));
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nonnull
    private UsernamePasswordCredentials createCredentials() {
        return new UsernamePasswordCredentials(
                lukeMooseReportUsername, lukeMooseReportPassword);
    }
}
