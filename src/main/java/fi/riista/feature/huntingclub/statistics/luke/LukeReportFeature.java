package fi.riista.feature.huntingclub.statistics.luke;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.integration.common.HttpProxyService;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.huntingclub.statistics.luke.LukeReportParams.Presentation.MOOSE_TABLE_FULL;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class LukeReportFeature {
    private static final Logger LOG = LoggerFactory.getLogger(LukeReportFeature.class);

    private final ActiveUserService activeUserService;
    private final HttpProxyService httpProxyService;
    private final LukeReportUriBuilderFactory lukeReportUriBuilderFactory;
    private final UsernamePasswordCredentials credentials;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private OccupationRepository occupationRepository;

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(3000)
            .setSocketTimeout(5000)
            // Do not follow redirects and treat anything else than 200 as 404 Not Found.
            .setRedirectsEnabled(false)
            .build();

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

    @Transactional(readOnly = true)
    public void getReport(final LukeReportUriBuilder uriBuilder,
                          final HttpServletResponse httpServletResponse,
                          final LukeReportParams.LukeArea area,
                          final LukeReportParams.Presentation presentation,
                          final String fileName,
                          final long activeOccupationId) {
        // Only Moderator or Coordinator can see "Seurakohtaiset tiheysindeksit" for moose.
        if (presentation == MOOSE_TABLE_FULL && fileName.equals("s2") && !isModeratorOrCoordinator(uriBuilder.getRhyOfficialCode(), activeOccupationId)) {
            return;
        }

        final URI lukeReportUrl = uriBuilder.getReportUri(area, presentation, fileName);

        LOG.info("userId:{} permitId:{} clubId:{} url:{}", activeUserService.requireActiveUserId(),
                uriBuilder.getPermitId(), uriBuilder.getClubId(), lukeReportUrl.getPath());

        httpProxyService.downloadFile(httpServletResponse, lukeReportUrl, credentials,
                null, presentation.getContentType(), REQUEST_CONFIG);
    }

    @Transactional(readOnly = true)
    public LukeReportParamsDTO getReportParameters(final LukeReportUriBuilder uriBuilder,
                                                   final int species,
                                                   final long activeOccupationId) {
        final URI checkClubReportExistsUri = uriBuilder.getCheckClubReportExistsUri();
        final int huntingYear = uriBuilder.getHuntingYear();
        final boolean isModeratorOrCoordinator = isModeratorOrCoordinator(uriBuilder.getRhyOfficialCode(), activeOccupationId);

        final List<Map<String, Object>> parameters = checkClubReportExistsUri != null
                ? LukeReportParams.Organisation.allValues(species, huntingYear, isModeratorOrCoordinator)
                : LukeReportParams.Organisation.valuesWithoutClub(species, huntingYear, isModeratorOrCoordinator);

        return new LukeReportParamsDTO(parameters, checkClubReportExistsUri != null &&
                httpProxyService.checkHeadResponseOk(checkClubReportExistsUri, credentials));
    }

    private boolean isModeratorOrCoordinator(final String rhyOfficialCode, final long activeOccupationId) {
        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findByOfficialCode(rhyOfficialCode);
        final Occupation activeOcc = occupationRepository.getOne(activeOccupationId);
        return activeUserService.isModeratorOrAdmin() ||
                (userAuthorizationHelper.isCoordinator(rhy) &&
                        activeOcc.getOccupationType() == TOIMINNANOHJAAJA &&
                        activeOcc.getOrganisation().getId() == rhy.getId());
    }
}
