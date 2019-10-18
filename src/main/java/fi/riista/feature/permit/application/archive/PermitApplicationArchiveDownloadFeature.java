package fi.riista.feature.permit.application.archive;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Component
public class PermitApplicationArchiveDownloadFeature {

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitApplicationArchiveRepository permitApplicationArchiveRepository;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadOriginalArchive(final long applicationId, final HttpServletResponse response) throws IOException {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        final PermitApplicationArchive archive = findOldestArchive(application)
                .orElseThrow(() -> new NotFoundException("No archive is available"));

        downloadArchive(response, application, archive.getFileMetadata());
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadArchiveWithoutAuthorization(final UUID uuid, final HttpServletResponse response) throws IOException {
        final Tuple2<HarvestPermitApplication, PersistentFileMetadata> applicationAndFile = Optional
                .ofNullable(harvestPermitApplicationRepository.findByUuid(uuid))
                .flatMap(application -> {
                    Preconditions.checkArgument(application.getStatus() == HarvestPermitApplication.Status.ACTIVE);

                    return findOldestArchive(application).map(PermitApplicationArchive::getFileMetadata)
                            .map(fileMetadata -> Tuple.of(application, fileMetadata));
                }).orElseThrow(() -> new NotFoundException("Could not find archive"));

        downloadArchive(response, applicationAndFile._1, applicationAndFile._2);
    }

    private void downloadArchive(final HttpServletResponse response,
                                 final HarvestPermitApplication application,
                                 final PersistentFileMetadata fileMetadata) throws IOException {
        final String prefix = HarvestPermitApplication.FILENAME_PREFIX.getAnyTranslation(LocaleContextHolder.getLocale());
        final String fileName = String.format("%s-%d.zip", prefix, application.getApplicationNumber());
        fileDownloadService.downloadUsingTemporaryFile(fileMetadata, fileName, response);
    }

    @Nonnull
    private Optional<PermitApplicationArchive> findOldestArchive(final HarvestPermitApplication application) {
        return permitApplicationArchiveRepository
                .findByHarvestPermitApplication(application).stream()
                .min(Comparator.comparing(PermitApplicationArchive::getCreationTime));
    }
}
