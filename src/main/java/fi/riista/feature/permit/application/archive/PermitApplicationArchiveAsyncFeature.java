package fi.riista.feature.permit.application.archive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PermitApplicationArchiveAsyncFeature {
    private static final Logger LOG = LoggerFactory.getLogger(PermitApplicationArchiveAsyncFeature.class);

    @Resource
    private PermitApplicationArchiveService permitApplicationArchiveService;

    @Async
    public void asyncCreateArchive(final long applicationId) throws Exception {
        createArchiveInternal(applicationId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public String createArchiveIfMissing(final long applicationId) throws Exception {
        if (permitApplicationArchiveService.isArchiveMissing(applicationId)) {
            createArchiveInternal(applicationId);
            return "created";
        } else {
            return "exists";
        }
    }

    private void createArchiveInternal(final long applicationId) throws Exception {
        Path archivePath = null;

        try {
            archivePath = permitApplicationArchiveService.createArchive(applicationId);
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
