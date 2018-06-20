package fi.riista.feature.permit.application.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.security.EntityPermission;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.URL;

@Component
public class HarvestPermitApplicationPdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Transactional(readOnly = true)
    public HarvestPermitApplicationPdfDTO getPdfModel(final long applicationId) {
        final HarvestPermitApplication application = readApplication(applicationId);
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(application.getArea().getZone().getId());
        return new HarvestPermitApplicationPdfDTO(application, areaSize);
    }

    @Transactional(readOnly = true)
    public Tuple2<URL, Integer> getApplicationNumberAndPrintUri(final long applicationId) {
        final HarvestPermitApplication application = readApplication(applicationId);

        return Tuple.of(application.getPrintingUrl(), application.getApplicationNumber());
    }

    private HarvestPermitApplication readApplication(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);
        application.assertStatus(HarvestPermitApplication.Status.ACTIVE);
        return application;
    }
}
