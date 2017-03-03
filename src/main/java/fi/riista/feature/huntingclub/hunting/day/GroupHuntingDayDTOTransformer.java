package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.util.DtoUtil;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Component
public class GroupHuntingDayDTOTransformer extends ListTransformer<GroupHuntingDay, GroupHuntingDayDTO> {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Nonnull
    protected List<GroupHuntingDayDTO> transform(@Nonnull final List<GroupHuntingDay> huntingDays) {
        return huntingDays.stream().filter(Objects::nonNull).map(entity -> {
            final GroupHuntingDayDTO dto = new GroupHuntingDayDTO();
            DtoUtil.copyBaseFields(entity, dto);

            dto.setHuntingGroupId(entity.getGroup() != null ? entity.getGroup().getId() : null);
            dto.setStartDate(entity.getStartDate());
            dto.setStartTime(entity.getStartTime());
            dto.setEndDate(entity.getEndDate());
            dto.setEndTime(entity.getEndTime());
            dto.setDurationInMinutes(entity.calculateHuntingDayDurationInMinutes());
            dto.setBreakDurationInMinutes(entity.getBreakDurationInMinutes());
            dto.setSnowDepth(entity.getSnowDepth());
            dto.setHuntingMethod(entity.getHuntingMethod());
            dto.setNumberOfHunters(entity.getNumberOfHunters());
            dto.setNumberOfHounds(entity.getNumberOfHounds());

            return dto;
        }).collect(toList());
    }

}
