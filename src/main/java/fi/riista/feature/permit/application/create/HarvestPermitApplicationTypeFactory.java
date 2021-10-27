package fi.riista.feature.permit.application.create;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.List;

import static java.util.Objects.requireNonNull;

class HarvestPermitApplicationTypeFactory {
    private final LocalDateTime now;

    HarvestPermitApplicationTypeFactory(final LocalDateTime now) {
        this.now = requireNonNull(now);
    }

    public List<HarvestPermitApplicationTypeDTO> listAll() {
        final int calendarYear = now.getYear();

        return ImmutableList.of(
                mooselikeForHuntingYear(calendarYear),
                birdForCalendarYear(calendarYear),
                mammalForCalendarYear(calendarYear),
                bearForHuntingYear(calendarYear),
                lynxForHuntingYear(calendarYear),
                lynxPoronhoitoForHuntingYear(calendarYear),
                wolfForHuntingYear(calendarYear),
                wolfPoronhoitoForHuntingYear(calendarYear),
                nestRemovalForCalendarYear(calendarYear),
                lawSectionTenForCalendarYear(calendarYear),
                weaponTransportationForCalendarYear(calendarYear),
                disabilityForCalendarYear(calendarYear),
                dogUnleashForCalendarYear(calendarYear),
                dogDisturbanceForCalendarYear(calendarYear),
                deportationForCalendarYear(calendarYear),
                researchForCalendarYear(calendarYear),
                importingForCalendarYear(calendarYear),
                gameManagementForCalendarYear(calendarYear));
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
            case LARGE_CARNIVORE_WOLF:
                return wolfForHuntingYear(huntingYear);
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return wolfPoronhoitoForHuntingYear(huntingYear);
            case MAMMAL:
                return mammalForCalendarYear(huntingYear);
            case NEST_REMOVAL:
                return nestRemovalForCalendarYear(huntingYear);
            case LAW_SECTION_TEN:
                return lawSectionTenForCalendarYear(huntingYear);
            case WEAPON_TRANSPORTATION:
                return weaponTransportationForCalendarYear(huntingYear);
            case DISABILITY:
                return disabilityForCalendarYear(huntingYear);
            case DOG_UNLEASH:
                return dogUnleashForCalendarYear(huntingYear);
            case DOG_DISTURBANCE:
                return dogDisturbanceForCalendarYear(huntingYear);
            case DEPORTATION:
                return deportationForCalendarYear(huntingYear);
            case RESEARCH:
                return researchForCalendarYear(huntingYear);
            case IMPORTING:
                return importingForCalendarYear(huntingYear);
            case GAME_MANAGEMENT:
                return gameManagementForCalendarYear(huntingYear);
            default:
                throw new IllegalArgumentException("Unknown permit category:" + category);
        }

    }

    HarvestPermitApplicationTypeDTO mooselikeForHuntingYear(final int huntingYear) {
        final LocalDateTime begin = new LocalDate(huntingYear, 4, 1).toLocalDateTime(new LocalTime(0,0));
        final LocalDateTime end = new LocalDate(huntingYear, 4, 30).toLocalDateTime(new LocalTime(16, 15));

        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.MOOSELIKE)
                .withHuntingYear(huntingYear)
                .withBegin(begin)
                .withEnd(end)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO birdForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.BIRD)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO mammalForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.MAMMAL)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO bearForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_BEAR)
                .withHuntingYear(calendarYear)
                .withNow(now)
//                .withActiveOverride(false) // Uncomment when period ends
                .build();
    }

    HarvestPermitApplicationTypeDTO lynxForHuntingYear(final int calendarYear) {
        if (calendarYear == 2021) {
            // TODO: When disabling lynx for 2021, remember to remove static info text from frontend code.
            final LocalDateTime begin = new LocalDate(2021, 9, 14).toLocalDateTime(new LocalTime(0, 0));
            final LocalDateTime end = new LocalDate(2021, 10, 10).toLocalDateTime(new LocalTime(23, 59));
            return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_LYNX)
                    .withHuntingYear(calendarYear)
                    .withNow(now)
                    .withBegin(begin)
                    .withEnd(end)
                    .build();

        }

        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_LYNX)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .withActiveOverride(false)
                .build();
    }

    HarvestPermitApplicationTypeDTO lynxPoronhoitoForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO wolfForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_WOLF)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .withActiveOverride(false)
                .build();
    }

    HarvestPermitApplicationTypeDTO wolfPoronhoitoForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_WOLF_PORONHOITO)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO nestRemovalForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.NEST_REMOVAL)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO lawSectionTenForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LAW_SECTION_TEN)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO weaponTransportationForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.WEAPON_TRANSPORTATION)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO dogUnleashForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DOG_UNLEASH)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO dogDisturbanceForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DOG_DISTURBANCE)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO disabilityForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DISABILITY)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO deportationForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DEPORTATION)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO researchForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.RESEARCH)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO importingForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.IMPORTING)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }

    HarvestPermitApplicationTypeDTO gameManagementForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.GAME_MANAGEMENT)
                .withHuntingYear(calendarYear)
                .withNow(now)
                .build();
    }
}
