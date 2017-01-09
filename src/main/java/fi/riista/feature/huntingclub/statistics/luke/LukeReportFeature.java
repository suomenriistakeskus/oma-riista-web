package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Component
public class LukeReportFeature {
    private static final Logger LOG = LoggerFactory.getLogger(LukeReportFeature.class);

    @Resource
    private LukeReportParams lukeReportParams;

    @Resource
    private RequireEntityService entityService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public LukeReportParamsDTO getLukeReportParams(@Nullable Long clubId, long permitId) {
        final HuntingClub club = clubId != null ? entityService.requireHuntingClub(clubId, EntityPermission.READ) : null;
        final HarvestPermit permit = entityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return new LukeReportParamsDTO(
                lukeReportParams.checkReportsAvailability(club, permit),
                club == null ? LukeReportParams.Organisation.valuesWithoutClub() : LukeReportParams.Organisation.values());
    }

    @Transactional(readOnly = true, rollbackFor = MalformedURLException.class)
    public URL getLukeReportUrl(final Long clubId,
                                final long permitId,
                                final LukeReportParams.Organisation org,
                                final LukeReportParams.Presentation presentation,
                                final String fileName) throws MalformedURLException {
        final HuntingClub club = clubId == null ? null : entityService.requireHuntingClub(clubId, EntityPermission.READ);
        final HarvestPermit permit = entityService.requireHarvestPermit(permitId, EntityPermission.READ);

        Preconditions.checkNotNull(permit.getMooseArea(), "permit.mooseArea should not be null, permitId:" + permitId);
        String htaNumber = permit.getMooseArea().getNumber();

        final String fullUrl = lukeReportParams.composeUrl(club, permit, htaNumber, org, presentation, fileName);

        LOG.info("userId:{} permitId:{} clubId:{} url:{}", activeUserService.getActiveUserId(), permitId, clubId, fullUrl);

        return new URL(fullUrl);
    }

    public void getLukeReport(final URL lukeReportUrl, final HttpServletResponse response) throws IOException {
        if (lukeReportUrl.getPath().endsWith(".png")) {
            response.setContentType("image/png");
        }

        if (lukeReportUrl.getPath().endsWith(".html")) {
            response.setContentType("text/html");
        }

        final URLConnection connection = lukeReportUrl.openConnection();
        connection.connect();

        try (InputStream is = connection.getInputStream()) {
            IOUtils.copy(is, response.getOutputStream());
        }
    }
}
