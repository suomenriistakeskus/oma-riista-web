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
                mammalForCalendarYear(calendarYear),
                bearForHuntingYear(calendarYear),
                lynxForHuntingYear(calendarYear),
                lynxPoronhoitoForHuntingYear(calendarYear),
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
        final LocalDate begin = new LocalDate(huntingYear, 4, 1);
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

    HarvestPermitApplicationTypeDTO mammalForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.MAMMAL)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO bearForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_BEAR)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .withActiveOverride(false)
                .build();
    }

    HarvestPermitApplicationTypeDTO lynxForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_LYNX)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .withBegin(new LocalDate(2020, 9, 11))
                .withEnd(new LocalDate(2020, 10, 23))
                .build();
    }

    HarvestPermitApplicationTypeDTO lynxPoronhoitoForHuntingYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .withActiveOverride(false)
                .build();
    }

    HarvestPermitApplicationTypeDTO nestRemovalForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.NEST_REMOVAL)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO lawSectionTenForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.LAW_SECTION_TEN)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO weaponTransportationForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.WEAPON_TRANSPORTATION)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO dogUnleashForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DOG_UNLEASH)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO dogDisturbanceForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DOG_DISTURBANCE)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO disabilityForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DISABILITY)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO deportationForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.DEPORTATION)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO researchForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.RESEARCH)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO importingForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.IMPORTING)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }

    HarvestPermitApplicationTypeDTO gameManagementForCalendarYear(final int calendarYear) {
        return HarvestPermitApplicationTypeDTO.Builder.builder(HarvestPermitCategory.GAME_MANAGEMENT)
                .withHuntingYear(calendarYear)
                .withToday(today)
                .build();
    }
}
