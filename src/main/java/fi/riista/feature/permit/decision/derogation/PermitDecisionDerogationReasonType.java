package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.common.entity.PersistableEnum;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41A;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41B;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41C;
import static fi.riista.feature.permit.decision.derogation.HabitatsConstants.HABITATS_REASON_TYPE_POPULATION_PRESERVATION;
import static java.util.Objects.requireNonNull;

public enum PermitDecisionDerogationReasonType implements PersistableEnum {


    // ML 41b§ Linnut
    REASON_PUBLIC_HEALTH(SECTION_41B, 1),
    REASON_PUBLIC_SAFETY(SECTION_41B, 1),
    REASON_AVIATION_SAFETY(SECTION_41B, 2),
    REASON_CROPS_DAMAMGE(SECTION_41B, 3),
    REASON_DOMESTIC_PETS(SECTION_41B, 3),
    REASON_FOREST_DAMAGE(SECTION_41B, 3),
    REASON_FISHING(SECTION_41B, 3),
    REASON_WATER_SYSTEM(SECTION_41B, 3),
    REASON_FLORA(SECTION_41B, 4),
    REASON_FAUNA(SECTION_41B, 4),
    REASON_RESEARCH(SECTION_41B, 5),
    REASON_POPULATION_PRESERVATION(SECTION_41A, 6),

    REASON_FLORA_41A(SECTION_41A, 1),
    REASON_FAUNA_41A(SECTION_41A, 1),
    REASON_CROPS_DAMAGE_41A(SECTION_41A, 2),
    REASON_CATTLE_DAMAGE_41A(SECTION_41A, 2),
    REASON_FOREST_DAMAGE_41A(SECTION_41A, 2),
    REASON_FISHING_41A(SECTION_41A, 2),
    REASON_REINDEER_HUSBANDRY_41A(SECTION_41A, 2),
    REASON_WATER_SYSTEM_41A(SECTION_41A, 2),
    REASON_OTHER_PROPERTY_DAMAGE_41A(SECTION_41A, 2),
    REASON_PUBLIC_HEALTH_41A(SECTION_41A, 3),
    REASON_PUBLIC_SAFETY_41A(SECTION_41A, 3),
    REASON_OTHER_COMMON_INTEREST_41A(SECTION_41A, 3),
    REASON_RESEARCH_41A(SECTION_41A, 4),

    REASON_FLORA_41C(SECTION_41C, 1),
    REASON_FAUNA_41C(SECTION_41C, 1),
    REASON_CROPS_DAMAGE_41C(SECTION_41C, 2),
    REASON_CATTLE_DAMAGE_41C(SECTION_41C, 2),
    REASON_FOREST_DAMAGE_41C(SECTION_41C, 2),
    REASON_FISHING_41C(SECTION_41C, 2),
    REASON_REINDEER_HUSBANDRY_41C(SECTION_41C, 2),
    REASON_GAME_HUSBANDRY_41C(SECTION_41C, 2),
    REASON_WATER_SYSTEM_41C(SECTION_41C, 2),
    REASON_OTHER_PROPERTY_DAMAGE_41C(SECTION_41C, 2),
    REASON_PUBLIC_HEALTH_41C(SECTION_41C, 3),
    REASON_PUBLIC_SAFETY_41C(SECTION_41C, 3),
    REASON_OTHER_COMMON_INTEREST_41C(SECTION_41C, 3),
    REASON_RESEARCH_41C(SECTION_41C, 4);

    private final DerogationLawSection lawSection;
    private final int lawSectionNumber;

    PermitDecisionDerogationReasonType(final DerogationLawSection lawSection, final int lawSectionNumber) {
        this.lawSection = lawSection;
        this.lawSectionNumber = lawSectionNumber;
    }

    @Override
    public String getDatabaseValue() {
        return this.name();
    }

    public DerogationLawSection getLawSection() {
        return lawSection;
    }

    public int getLawSectionNumber() {
        return lawSectionNumber;
    }

    public static Stream<PermitDecisionDerogationReasonType> streamSelectedForBird(
            final @Nonnull BirdPermitApplicationCause cause) {
        requireNonNull(cause);

        return Arrays.stream(PermitDecisionDerogationReasonType.values())
                .filter(reasonType -> reasonType.getLawSection() == SECTION_41B)
                .filter(reasonType -> reasonType.isSelected(cause));
    }

    public boolean isSelected(final @Nonnull BirdPermitApplicationCause cause) {
        requireNonNull(cause);

        switch (this) {
            case REASON_PUBLIC_HEALTH:
                return cause.isCausePublicHealth();
            case REASON_PUBLIC_SAFETY:
                return cause.isCausePublicSafety();
            case REASON_AVIATION_SAFETY:
                return cause.isCauseAviationSafety();
            case REASON_CROPS_DAMAMGE:
                return cause.isCauseCropsDamage();
            case REASON_DOMESTIC_PETS:
                return cause.isCauseDomesticPets();
            case REASON_FOREST_DAMAGE:
                return cause.isCauseForestDamage();
            case REASON_FISHING:
                return cause.isCauseFishing();
            case REASON_WATER_SYSTEM:
                return cause.isCauseWaterSystem();
            case REASON_FLORA:
                return cause.isCauseFlora();
            case REASON_FAUNA:
                return cause.isCauseFauna();
            case REASON_RESEARCH:
                return cause.isCauseResearch();
            default:
                return false;
        }
    }

    public String getHabidesCodeForCarnivore() {
        switch (this) {
            case REASON_POPULATION_PRESERVATION:
                return HABITATS_REASON_TYPE_POPULATION_PRESERVATION;
            default:
                throw new IllegalArgumentException("Invalid reason for carnivore: " + this.name());
        }
    }

    public static List<PermitDecisionDerogationReasonType> valuesForLawSection(final DerogationLawSection lawSection) {
        return Arrays.stream(values()).filter(t -> t.getLawSection() == lawSection).collect(Collectors.toList());
    }
}
