package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOps;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HasMooseFields;
import fi.riista.feature.gamediary.harvest.specimen.HasMooselikeFields;
import fi.riista.util.ValueGeneratorMixin;

import javaslang.Tuple;
import javaslang.Tuple2;

import javax.annotation.Nonnull;

import java.util.EnumSet;
import java.util.Objects;

public interface CanPopulateHarvestSpecimen extends ValueGeneratorMixin {

    HarvestSpecimenOps getSpecimenOps();

    static Tuple2<EnumSet<GameAge>, EnumSet<GameGender>> getAgeAndGenderRange(final boolean allowUnknownAgeAndGender) {
        final EnumSet<GameAge> allowedAges = allowUnknownAgeAndGender
                ? EnumSet.allOf(GameAge.class)
                : EnumSet.complementOf(EnumSet.of(GameAge.UNKNOWN));

        final EnumSet<GameGender> allowedGenders = allowUnknownAgeAndGender
                ? EnumSet.allOf(GameGender.class)
                : EnumSet.complementOf(EnumSet.of(GameGender.UNKNOWN));

        return Tuple.of(allowedAges, allowedGenders);
    }

    default HarvestSpecimenDTO newHarvestSpecimenDTO(final boolean allowUnknownAgeAndGender) {
        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
        mutateContent(dto, allowUnknownAgeAndGender);
        return dto;
    }

    /**
     * Mutates content of given HarvestSpecimen. ID and revision fields are left
     * intact.
     */
    default void mutateContent(@Nonnull final HarvestSpecimen entity, final boolean allowUnknownAgeAndGender) {
        Objects.requireNonNull(entity);
        final HarvestSpecimenOps ops = getSpecimenOps();

        getAgeAndGenderRange(allowUnknownAgeAndGender).transform((allowedAges, allowedGenders) -> {
            entity.setAge(someOtherThan(entity.getAge(), allowedAges));
            entity.setGender(someOtherThan(entity.getGender(), allowedGenders));
            // Called only for side-effect.
            return null;
        });

        if (ops.supportsExtendedMooselikeFields()) {
            if (ops.supportsExtendedMooseFields()) {
                populateMooseFields(entity);
            } else {
                populateMooselikeFields(entity);
            }
            entity.setWeight(null);
        } else {
            entity.setWeight(weight());
        }
    }

    /**
     * Mutates content of given HarvestSpecimenDTO. ID and revision fields are
     * left intact.
     */
    default void mutateContent(@Nonnull final HarvestSpecimenDTO dto, final boolean allowUnknownAgeAndGender) {
        Objects.requireNonNull(dto);
        final HarvestSpecimenOps ops = getSpecimenOps();

        getAgeAndGenderRange(allowUnknownAgeAndGender).transform((allowedAges, allowedGenders) -> {
            dto.setAge(someOtherThan(dto.getAge(), allowedAges));
            dto.setGender(someOtherThan(dto.getGender(), allowedGenders));
            // Called only for side-effect.
            return null;
        });

        if (ops.supportsExtendedMooselikeFields()) {
            if (ops.supportsExtendedMooseFields()) {
                populateMooseFields(dto);
            } else {
                populateMooselikeFields(dto);
            }
            dto.setWeight(null);
        } else {
            dto.setWeight(weight());
        }
    }

    default void populateMooselikeFields(@Nonnull final HasMooselikeFields obj) {
        Objects.requireNonNull(obj);

        obj.setWeightEstimated(weight());
        obj.setWeightMeasured(weight());
        obj.setAntlersWidth(nextPositiveIntAtMost(100));
        obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
        obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
        obj.setNotEdible(false);
        obj.setAdditionalInfo("additional info " + nextPositiveInt());
    }

    default void populateMooseFields(@Nonnull final HasMooseFields obj) {
        populateMooselikeFields(obj);
        obj.setFitnessClass(some(GameFitnessClass.class));
        obj.setAntlersType(some(GameAntlersType.class));
    }

}
