package fi.riista.feature.permit.application.create;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.util.DateUtil.today;

@Component
public class HarvestPermitApplicationTypeFeature {

    @Resource
    private RequireEntityService requireEntityService;

    public List<HarvestPermitApplicationTypeDTO> listTypes() {
        return new HarvestPermitApplicationTypeFactory(today()).listAll();
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationTypeDTO findTypeForApplication(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        return new HarvestPermitApplicationTypeFactory(today())
                .resolve(application.getHarvestPermitCategory(), application.getApplicationYear());
    }
}
