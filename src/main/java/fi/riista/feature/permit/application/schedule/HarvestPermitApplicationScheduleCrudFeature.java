package fi.riista.feature.permit.application.schedule;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class HarvestPermitApplicationScheduleCrudFeature
        extends AbstractCrudFeature<Long, HarvestPermitApplicationSchedule, HarvestPermitApplicationScheduleDTO> {

    @Resource
    private HarvestPermitApplicationScheduleRepository scheduleRepository;

    @Resource
    private HarvestPermitApplicationScheduleService scheduleService;

    @Override
    protected JpaRepository<HarvestPermitApplicationSchedule, Long> getRepository() {
        return scheduleRepository;
    }

    @Override
    protected void updateEntity(final HarvestPermitApplicationSchedule schedule, final HarvestPermitApplicationScheduleDTO dto) {
        schedule.setBeginTime(DateUtil.toDateTimeNullSafe(dto.getBeginTime()));
        schedule.setEndTime(DateUtil.toDateTimeNullSafe(dto.getEndTime()));
        final Map<String, String> instructionsMap = dto.getInstructions();
        if (instructionsMap != null) {
            final LocalisedString instructions = LocalisedString.fromMap(instructionsMap);
            schedule.setInstructionsFi(instructions.getFinnish());
            schedule.setInstructionsSv(instructions.getSwedish());
        } else {
            schedule.setInstructionsFi(null);
            schedule.setInstructionsSv(null);
        }
        schedule.setActiveOverride(dto.getActiveOverride());
    }

    @Override
    protected HarvestPermitApplicationScheduleDTO toDTO(@Nonnull final HarvestPermitApplicationSchedule schedule) {
        return HarvestPermitApplicationScheduleDTO.create(schedule);
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationScheduleDTO> list() {
        return scheduleService.list();
    }
}
