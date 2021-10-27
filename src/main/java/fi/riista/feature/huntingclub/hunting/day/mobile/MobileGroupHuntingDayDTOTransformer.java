package fi.riista.feature.huntingclub.hunting.day.mobile;

import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Component
public class MobileGroupHuntingDayDTOTransformer extends ListTransformer<GroupHuntingDay, MobileGroupHuntingDayDTO> {

    @Override
    @Nonnull
    protected List<MobileGroupHuntingDayDTO> transform(@Nonnull final List<GroupHuntingDay> huntingDays) {
        return huntingDays.stream().filter(Objects::nonNull).map(entity -> {
            final MobileGroupHuntingDayDTO dto = new MobileGroupHuntingDayDTO();
            DtoUtil.copyBaseFields(entity, dto);

            dto.setHuntingGroupId(F.getId(entity.getGroup()));
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
            dto.setCreatedBySystem(entity.isCreatedBySystem());
            return dto;
        }).collect(toList());
    }
}
