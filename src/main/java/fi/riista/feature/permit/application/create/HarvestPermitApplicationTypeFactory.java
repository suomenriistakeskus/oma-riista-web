package fi.riista.feature.permit.application.create;

import fi.riista.feature.permit.application.schedule.HarvestPermitApplicationScheduleDTO;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class HarvestPermitApplicationTypeFactory {
    private final LocalDateTime now;

    HarvestPermitApplicationTypeFactory(final LocalDateTime now) {
        this.now = requireNonNull(now);
    }

    public List<HarvestPermitApplicationTypeDTO> listAll(final List<HarvestPermitApplicationScheduleDTO> schedules) {
        return schedules.stream()
                .map(this::resolve)
                .collect(Collectors.toList());
    }

    public HarvestPermitApplicationTypeDTO resolve(final HarvestPermitApplicationScheduleDTO schedule) {
        return resolve(schedule, now.getYear());
    }

    public HarvestPermitApplicationTypeDTO resolve(final HarvestPermitApplicationScheduleDTO schedule,
                                                   final int year) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(schedule.getCategory())
                .withHuntingYear(year)
                .withBegin(schedule.getBeginTime())
                .withEnd(schedule.getEndTime())
                .withNow(now)
                .withActiveOverride(schedule.getActiveOverride())
                .withInstructions(F.mapNullable(schedule.getInstructions(), LocalisedString::fromMap))
                .build();
    }
}
