package fi.riista.feature.permit.application.archive;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.security.EntityPermission;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class PermitApplicationArchiveDownloadFeature {

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitApplicationArchiveRepository permitApplicationArchiveRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadOriginalArchive(final long applicationId, final HttpServletResponse response) throws IOException {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        final List<PermitApplicationArchive> archiveList = permitApplicationArchiveRepository
                .findByHarvestPermitApplication(application);

        final Optional<PermitApplicationArchive> oldestArchive = archiveList.stream()
                .min(Comparator.comparing(PermitApplicationArchive::getCreationTime));

        if (!oldestArchive.isPresent()) {
            throw new NotFoundException("No archive is available");
        }

        final Locale locale = LocaleContextHolder.getLocale();
        final String archiveFileName = HarvestPermitApplication.getArchiveFileName(locale, application.getApplicationNumber());

        fileDownloadService.downloadUsingTemporaryFile(oldestArchive.get().getFileMetadata(), archiveFileName, response);
    }
}
