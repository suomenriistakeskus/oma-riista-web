package fi.riista.feature.permit.application.create;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Objects.requireNonNull;

class HarvestPermitApplicationTypeFactory {
    private final LocalDate today;

    HarvestPermitApplicationTypeFactory(final LocalDate today) {
        this.today = requireNonNull(today);
    }

    public List<HarvestPermitApplicationTypeDTO> listAll() {
        final int calendarYear = today.getYear();

        return ImmutableList.of(
                mooselikeForHuntingYear(calendarYear),
                birdForCalendarYear(calendarYear),
                bearForHuntingYear(calendarYear),
                lynxForHuntingYear(calendarYear),
                lynxPoronhoitoForHuntingYear(calendarYear));
    }

    public HarvestPermitApplicationTypeDTO resolve(final HarvestPermitCategory category,
                                                   final int huntingYear) {
        switch (category) {
            case MOOSELIKE:
                return mooselikeForHuntingYear(huntingYear);
            case BIRD:
                return birdForCalendarYear(huntingYear);
            case LARGE_CARNIVORE_BEAR:
                return bearForHuntingYear(huntingYear);
            case LARGE_CARNIVORE_LYNX:
                return lynxForHuntingYear(huntingYear);
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
                return lynxPoronhoitoForHuntingYear(huntingYear);
            default:
                throw new IllegalArgumentException("Unknown permit category:" + category);
        }

    }

    HarvestPermitApplicationTypeDTO mooselikeForHuntingYear(final int huntingYear) {
        final LocalDate begin = new LocalDate(huntingYear, 4, 3);
        final LocalDate end = new LocalDate(huntingYear, 4, 30);

        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.MOOSELIKE)
                .withHuntingYear(huntingYear)
                .withBegin(begin)
                .withEnd(end)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO birdForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.BIRD)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO bearForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_BEAR)
                .withHuntingYear(calendarYear)
                .withActiveOverride(false)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO lynxForHuntingYear(final int calendarYear) {
        final LocalDate begin = new LocalDate(calendarYear, 9, 10);
        final LocalDate end = new LocalDate(calendarYear, 9, 30);
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_LYNX)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .withBegin(begin)
                .withEnd(end)
                .build();
    }

    HarvestPermitApplicationTypeDTO lynxPoronhoitoForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .withActiveOverride(false)
                .build();
    }
}
