package fi.riista.feature.permit.application.schedule;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HarvestPermitApplicationScheduleService {

    @Resource
    private HarvestPermitApplicationScheduleRepository scheduleRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestPermitApplicationScheduleDTO> list() {
        final JpaSort sort = JpaSort.of(Sort.Direction.ASC, HarvestPermitApplicationSchedule_.id);
        final List<HarvestPermitApplicationSchedule> schedules = scheduleRepository.findAll(sort);

        return schedules.stream()
                .map(HarvestPermitApplicationScheduleDTO::create)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HarvestPermitApplicationScheduleDTO findByCategory(final HarvestPermitCategory category) {
        return HarvestPermitApplicationScheduleDTO.create(scheduleRepository.findByCategory(category));
    }
}
