package fi.riista.feature.harvestpermit.download;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.DecisionUtil;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
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
import java.util.Objects;
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
    private HarvestPermitLatestDecisionRevisionService harvestPermitLatestDecisionRevisionService;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Transactional(readOnly = true, rollbackFor = MalformedURLException.class)
    public HarvestPermitDownloadDecisionDTO getDecisionPdf(final String permitNumber) {
        final HarvestPermit harvestPermit = harvestPermitRepository.findByPermitNumber(permitNumber);

        if (harvestPermit == null) {
            // could be permit number of decision for cancelled or ignored application
            final HarvestPermitApplication application = findApplication(permitNumber);
            final PermitDecision decision = permitDecisionRepository.findOneByApplication(application);

            decision.assertStatus(DecisionStatus.PUBLISHED);

            return getDecisionPdfForDecision(permitNumber, decision);
        }

        activeUserService.assertHasPermission(harvestPermit, EntityPermission.READ);

        final PermitDecision permitDecision = harvestPermit.getPermitDecision();
        if (permitDecision != null) {
            if (permitDecision.getStatus() == DecisionStatus.PUBLISHED) {
                return getDecisionPdfForDecision(permitNumber, permitDecision);
            } else {
                throw new IllegalArgumentException("Decision is not published.");
            }
        }

        if (StringUtils.isBlank(harvestPermit.getPrintingUrl())) {
            throw new IllegalArgumentException("Decision is not available");
        }

        LOG.info("userId:{} loading permitNumber:{} pdf", activeUserService.requireActiveUserId(), permitNumber);
        final String filename = DecisionUtil.getPermitDecisionFileName(Locales.FI, permitNumber);

        return new HarvestPermitDownloadDecisionDTO(filename, harvestPermit.getPrintingUrl());
    }

    private HarvestPermitApplication findApplication(final String permitNumber) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository
                .findByApplicationNumber(DocumentNumberUtil.extractOrderNumber(permitNumber))
                .orElseThrow(() -> new HarvestPermitNotFoundException(permitNumber));

        activeUserService.assertHasPermission(application, EntityPermission.READ);
        return application;
    }

    private HarvestPermitDownloadDecisionDTO getDecisionPdfForDecision(final String permitNumber, final PermitDecision decision) {
        Objects.requireNonNull(permitNumber);
        Objects.requireNonNull(decision);

        final UUID latestRevisionArchivePdfId = getLatestRevisionArchivePdfId(decision);
        if (latestRevisionArchivePdfId == null) {
            throw new IllegalArgumentException("Decision is not available");
        }
        LOG.info("userId:{} loading permitNumber:{} pdf", activeUserService.requireActiveUserId(), permitNumber);
        final String filename = DecisionUtil.getPermitDecisionFileName(decision.getLocale(), permitNumber);
        return new HarvestPermitDownloadDecisionDTO(filename, latestRevisionArchivePdfId);
    }

    private UUID getLatestRevisionArchivePdfId(final PermitDecision permitDecision) {
        return harvestPermitLatestDecisionRevisionService.getLatestRevisionArchivePdfId(permitDecision)
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
