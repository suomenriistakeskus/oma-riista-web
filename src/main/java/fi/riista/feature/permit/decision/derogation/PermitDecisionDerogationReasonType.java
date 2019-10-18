package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.common.entity.PersistableEnum;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Stream;

import static fi.riista.feature.permit.decision.derogation.HabitatsConstants.HABITATS_REASON_TYPE_POPULATION_PRESERVATION;
import static java.util.Objects.requireNonNull;

public enum PermitDecisionDerogationReasonType implements PersistableEnum {
    REASON_PUBLIC_HEALTH(1),
    REASON_PUBLIC_SAFETY(1),
    REASON_AVIATION_SAFETY(2),
    REASON_CROPS_DAMAMGE(3),
    REASON_DOMESTIC_PETS(3),
    REASON_FOREST_DAMAGE(3),
    REASON_FISHING(3),
    REASON_WATER_SYSTEM(3),
    REASON_FLORA(4),
    REASON_FAUNA(4),
    REASON_RESEARCH(5),
    REASON_POPULATION_PRESERVATION(6);

    private final int lawSectionNumber;

    PermitDecisionDerogationReasonType(final int lawSectionNumber) {
        this.lawSectionNumber = lawSectionNumber;
    }

    @Override
    public String getDatabaseValue() {
        return this.name();
    }


    public int getLawSectionNumber() {
        return lawSectionNumber;
    }

    public static Stream<PermitDecisionDerogationReasonType> streamSelected(
            final @Nonnull BirdPermitApplicationCause cause) {
        requireNonNull(cause);

        return Arrays.stream(PermitDecisionDerogationReasonType.values())
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
            case REASON_POPULATION_PRESERVATION:
                return false;
            default:
                throw new RuntimeException("Implementation is not complete");
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
}
