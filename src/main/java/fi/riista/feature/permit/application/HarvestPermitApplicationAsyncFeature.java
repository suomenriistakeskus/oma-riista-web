package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveDTO;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveService;
import fi.riista.feature.permit.application.email.HarvestPermitApplicationNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class HarvestPermitApplicationAsyncFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationAsyncFeature.class);

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationModeratorNotificationService harvestPermitApplicationModeratorNotificationService;

    @Resource
    private HarvestPermitApplicationNotificationService harvestPermitApplicationNotificationService;

    @Resource
    private PermitApplicationArchiveService permitApplicationArchiveService;

    @Resource
    private UserRepository userRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncSendModeratorNotification(final long applicationId) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);

        harvestPermitApplicationModeratorNotificationService.sendModeratorNotification(application);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncSendEmailNotification(final long applicationId) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);

        if (createdByModerator(application)) {
            LOG.warn("Not sending notification email for application id={} created by moderator", applicationId);

        } else {
            harvestPermitApplicationNotificationService.sendNotification(application);
        }
    }

    private boolean createdByModerator(final HarvestPermitApplication application) {
        final Long createdByUserId = application.getAuditFields().getCreatedByUserId();
        return createdByUserId != null && userRepository.isModeratorOrAdmin(createdByUserId);
    }

    @Async
    public void asyncCreateArchive(final long applicationId) throws Exception {
        final PermitApplicationArchiveDTO dto = permitApplicationArchiveService.getDataForArchive(applicationId);

        Path archivePath = null;

        try {
            archivePath = permitApplicationArchiveService.createArchive(dto);
            permitApplicationArchiveService.storeArchive(archivePath, applicationId);

        } finally {
            if (archivePath != null) {
                try {
                    Files.deleteIfExists(archivePath);
                } catch (IOException e) {
                    LOG.error("Could not delete temporary file", e);
                }
            }
        }
    }
}
