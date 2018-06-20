package fi.riista.feature.permit.application.archive;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.util.PDFUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.file.Path;

@Service
public class PermitApplicationArchiveValidationService {

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Transactional(readOnly = true)
    public void validateApplicationPdf(final long applicationId,
                                       final Path applicationPdf) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);
        final String textContent = PDFUtil.extractAllText(applicationPdf.toFile());

        final String applicationNumber = Integer.toString(application.getApplicationNumber());

        if (!textContent.contains(applicationNumber)) {
            throw new PermitApplicationArchiveValidationException("Application number is missing: "
                    + applicationNumber);
        }

        if (!textContent.contains("SUOMEN") || !textContent.contains("RIISTAKESKUS")) {
            throw new PermitApplicationArchiveValidationException("Riistakeskus text is missing");
        }
    }

    @Transactional(readOnly = true)
    public void validateMapPdf(final Path mapPdf) {
        final String textContent = PDFUtil.extractAllText(mapPdf.toFile());

        if (!textContent.contains("Maastokartta") || !textContent.contains("Maanmittauslaitos")) {
            throw new PermitApplicationArchiveValidationException("Map source missing");
        }
    }
}
