package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveDTO;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveService;
import fi.riista.feature.permit.application.email.HarvestPermitApplicationNotificationService;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.ACTIVE;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.AMENDING;

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

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    @Transactional(rollbackFor = Exception.class)
    public String createArchiveIfMissing(final long applicationId) throws Exception {
        if (permitApplicationArchiveService.isArchiveMissing(applicationId)) {
            doCreateArchive(applicationId);
            return "created";
        } else {
            return "exists";
        }
    }

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
        doCreateArchive(applicationId);
    }

    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void recreateArchivesForYear(final int year) {
        final List<HarvestPermitApplication> applications =
                harvestPermitApplicationRepository.findByApplicationYearAndStatusInAndHarvestPermitCategory(year,
                        Arrays.asList(ACTIVE, AMENDING), HarvestPermitCategory.MOOSELIKE);
        final List<Long> ids = new ArrayList<>(F.getUniqueIds(applications));
        doRecreate(ids);
    }

    @Async
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void recreateArchives(final List<Long> ids) {
        doRecreate(ids);
    }

    private void doCreateArchive(final long applicationId) throws Exception {
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

    private void doRecreate(final List<Long> applicationIds) {
        final List<Long> failures = new ArrayList<>();

        for (final Long id : applicationIds) {
            Path originalPath = null;
            Path archivePath = null;
            try {
                originalPath = permitApplicationArchiveService.getOriginalZipArchive(id);
                archivePath = permitApplicationArchiveService.appendPartnersMapToArchive(originalPath, id);
                permitApplicationArchiveService.updateArchive(archivePath, id);
            } catch (final Exception e) {
                LOG.error("Failed to recreate archive", e);
                failures.add(id);
            } finally {
                if (originalPath != null) {
                    try {
                        Files.deleteIfExists(originalPath);
                    } catch (final IOException e) {
                        LOG.error("Could not delete temporary original file", e);
                    }
                }
                if (archivePath != null) {
                    try {
                        Files.deleteIfExists(archivePath);
                    } catch (final IOException e) {
                        LOG.error("Could not delete temporary archive file", e);
                    }
                }
            }
        }

        if (failures.isEmpty()) {
            LOG.info("Successfully recreated archives");
        } else {
            LOG.error("Failed to recreate archives: {}", failures);
        }
    }
}
