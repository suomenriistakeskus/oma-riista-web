package fi.riista.feature.permit.application.create;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class HarvestPermitApplicationTypeFeature {

    @Resource
    private RequireEntityService requireEntityService;

    public List<HarvestPermitApplicationTypeDTO> listTypes() {
        return new HarvestPermitApplicationTypeFactory(DateUtil.localDateTime()).listAll();
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationTypeDTO findTypeForApplication(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        return new HarvestPermitApplicationTypeFactory(DateUtil.localDateTime())
                .resolve(application.getHarvestPermitCategory(), application.getApplicationYear());
    }
}
