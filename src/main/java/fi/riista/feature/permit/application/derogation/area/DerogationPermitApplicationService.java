package fi.riista.feature.permit.application.derogation.area;

import com.google.common.base.Preconditions;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Service
public class DerogationPermitApplicationService {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;


    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public DerogationPermitApplicationAreaInfo findForRead(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final HarvestPermitCategory category = application.getHarvestPermitCategory();
        if (category.isLargeCarnivore()) {
            return carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
        } else if (category == HarvestPermitCategory.MAMMAL) {
            return mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
        } else {
            throw new UnsupportedOperationException("Unsupported category: " + category);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void update(final long applicationId,
                       final @NotNull DerogationPermitApplicationAreaDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        application.setRhy(gisQueryService.findRhyByLocation(dto.getGeoLocation()));

        final HarvestPermitCategory category = application.getHarvestPermitCategory();
        if (category.isLargeCarnivore()) {
            final CarnivorePermitApplication existing =
                    carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
            Preconditions.checkState(existing != null, "Carnivore application not found.");
            udpateFields(existing, dto);
            carnivorePermitApplicationRepository.save(existing);

        } else if (category == HarvestPermitCategory.MAMMAL) {
            final MammalPermitApplication existing =
                    mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
            Preconditions.checkState(existing != null, "Mammal application not found.");
            udpateFields(existing, dto);
            mammalPermitApplicationRepository.save(existing);
        } else {
            throw new UnsupportedOperationException("Unsupported category: " + category);
        }
    }

    private void udpateFields(DerogationPermitApplicationAreaInfo entity,
                              final @NotNull DerogationPermitApplicationAreaDTO dto) {
        entity.setAreaSize(dto.getAreaSize());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setAreaDescription(dto.getAreaDescription());

    }
}
