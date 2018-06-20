package fi.riista.feature.harvestpermit.download;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionRepository;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.integration.common.HttpProxyService;
import fi.riista.security.EntityPermission;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class HarvestPermitDownloadDecisionFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitDownloadDecisionFeature.class);

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private HttpProxyService httpProxyService;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private PermitDecisionRevisionRepository permitDecisionRevisionRepository;

    @Transactional(readOnly = true, rollbackFor = MalformedURLException.class)
    public HarvestPermitDownloadDecisionDTO getDecisionPdf(final String permitNumber) {
        final HarvestPermit harvestPermit = harvestPermitRepository.findByPermitNumber(permitNumber);
        activeUserService.assertHasPermission(harvestPermit, EntityPermission.READ);

        final Locale locale = harvestPermit.getPermitDecision() != null
                ? harvestPermit.getPermitDecision().getLocale()
                : Locales.FI;
        final String filename = PermitDecision.getFileName(locale, permitNumber);
        final UUID latestRevisionArchivePdfId = getLatestRevisionArchivePdfId(harvestPermit);

        if (latestRevisionArchivePdfId == null && StringUtils.isBlank(harvestPermit.getPrintingUrl())) {
            throw new IllegalArgumentException("Decision is not available");
        }

        LOG.info("userId:{} loading permitNumber:{} pdf", activeUserService.requireActiveUserId(), permitNumber);

        return latestRevisionArchivePdfId != null
                ? new HarvestPermitDownloadDecisionDTO(filename, latestRevisionArchivePdfId)
                : new HarvestPermitDownloadDecisionDTO(filename, harvestPermit.getPrintingUrl());
    }

    private UUID getLatestRevisionArchivePdfId(final HarvestPermit harvestPermit) {
        return harvestPermit.getPermitDecision() != null
                ? getLatestRevisionArchivePdfId(harvestPermit.getPermitDecision())
                : null;
    }

    private UUID getLatestRevisionArchivePdfId(final PermitDecision permitDecision) {
        final List<PermitDecisionRevision> all = permitDecisionRevisionRepository.findByPermitDecision(permitDecision);
        return all.stream().max(Comparator.comparingLong(PermitDecisionRevision::getId))
                .map(PermitDecisionRevision::getPdfMetadata)
                .map(PersistentFileMetadata::getId)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No revision available for decisionId: %d", permitDecision.getId())));
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadLocalPdf(final HarvestPermitDownloadDecisionDTO dto,
                                 final HttpServletResponse httpServletResponse) throws IOException {
        fileDownloadService.downloadUsingTemporaryFile(
                dto.getLocalDecisionFileId(), dto.getFilename(),
                MediaTypeExtras.APPLICATION_PDF_VALUE, httpServletResponse);
    }

    public void downloadRemotePdf(final HarvestPermitDownloadDecisionDTO dto,
                                  final HttpServletResponse httpServletResponse) {
        httpProxyService.downloadFile(httpServletResponse,
                URI.create(dto.getRemoteUri()), null,
                dto.getFilename(), MediaTypeExtras.APPLICATION_PDF);
    }
}
