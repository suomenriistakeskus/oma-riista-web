package fi.riista.feature.permit.application.create;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.schedule.HarvestPermitApplicationScheduleDTO;
import fi.riista.feature.permit.application.schedule.HarvestPermitApplicationScheduleService;
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

    @Resource
    private HarvestPermitApplicationScheduleService scheduleService;

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationTypeDTO> listTypes() {
        final List<HarvestPermitApplicationScheduleDTO> schedules = scheduleService.list();
        return new HarvestPermitApplicationTypeFactory(DateUtil.localDateTime()).listAll(schedules);
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationTypeDTO findTypeForApplication(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);
        final HarvestPermitApplicationScheduleDTO schedule = scheduleService.findByCategory(application.getHarvestPermitCategory());

        return new HarvestPermitApplicationTypeFactory(DateUtil.localDateTime()).resolve(schedule, application.getApplicationYear());
    }
}
