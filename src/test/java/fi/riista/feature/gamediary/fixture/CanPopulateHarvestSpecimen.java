package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenBusinessFields;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOps;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ANTLERS_LOST;
import static java.util.Objects.requireNonNull;

public interface CanPopulateHarvestSpecimen extends ValueGeneratorMixin {

    HarvestSpecimenOps getSpecimenOps();

    /**
     * Mutates content of given object. ID and revision fields are left intact.
     */
    default void mutateContent(@Nonnull final HarvestSpecimenBusinessFields obj) {
        requireNonNull(obj);

        final HarvestSpecimenOps specimenOps = getSpecimenOps();
        final boolean shouldHaveKnownAgeAndGender = specimenOps.isMooseOrDeerRequiringPermitForHunting();

        final Predicate<HarvestSpecimenType> specimenTypeCondition;

        if (specimenOps.isPresenceOfAntlerFields2020Legitimate()) {
            specimenTypeCondition = shouldHaveKnownAgeAndGender
                    ? HarvestSpecimenType::isAgeAndGenderKnown
                    : HarvestSpecimenType::isAgeAndGenderPresent;
        } else {
            specimenTypeCondition = shouldHaveKnownAgeAndGender
                    ? specimenType -> specimenType.isAgeAndGenderKnown() && !specimenType.isAntlersLost()
                    : specimenType -> specimenType.isAgeAndGenderPresent() && !specimenType.isAntlersLost();
        }

        final HarvestSpecimenType currentSpecimenType = HarvestSpecimenType.fromFields(obj);
        final HarvestSpecimenType newSpecimenType =
                someOtherThan(currentSpecimenType, harvestSpecimenTypes(specimenTypeCondition));

        mutateContent(obj, newSpecimenType);
    }

    /**
     * Mutates content of object based on given specimenType. ID and revision fields are left intact.
     */
    default void mutateContent(@Nonnull final HarvestSpecimenBusinessFields obj,
                               @Nonnull final HarvestSpecimenType specimenType) {

        final HarvestSpecimenOps specimenOps = getSpecimenOps();

        if (specimenType == ANTLERS_LOST) {
            checkArgument(specimenOps.isPresenceOfAntlerFields2020Legitimate(), "antlersLost is not legitimate");
        }

        switch (specimenOps.getGameSpeciesCode()) {
            case OFFICIAL_CODE_MOOSE:
                populateFieldsForMoose(obj, specimenType);
                break;
            case OFFICIAL_CODE_ROE_DEER:
                populateFieldsForRoeDeer(obj, specimenType);
                break;
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
                populateFieldsForWhiteTailedDeer(obj, specimenType);
                break;
            case OFFICIAL_CODE_FALLOW_DEER:
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
                populateFieldsForOtherDeer(obj, specimenType);
                break;
            case OFFICIAL_CODE_WILD_BOAR:
                populateFieldsForWildBoar(obj, specimenType);
                break;
            default: // other species
                populateFieldsForOtherSpecies(obj, specimenType);
                break;
        }
    }

    default void populateAgeAndGender(@Nonnull final HarvestSpecimenBusinessFields obj,
                                      @Nonnull final HarvestSpecimenType specimenType) {
        requireNonNull(obj);
        requireNonNull(specimenType);

        obj.setAge(specimenType.getAge());
        obj.setGender(specimenType.getGender());
    }

    default void populateFieldsForMoose(@Nonnull final HarvestSpecimenBusinessFields obj,
                                        @Nonnull final HarvestSpecimenType specimenType) {

        populateAgeAndGender(obj, specimenType);
        obj.setWeight(null);

        final HarvestSpecVersion specVersion = getSpecimenOps().getVersion();

        switch (specVersion) {
            case _3:
            case _4:
                populateExtensionFieldsForMooseWithSpecVersion3(obj, specimenType);
                break;
            case _5:
            case _6:
            case _7:
                populateExtensionFieldsForMooseWithSpecVersion5(obj, specimenType);
                break;
            case _8:
            case _9:
            case _10:
                if (getSpecimenOps().getHuntingYear() >= 2020) {
                    populateExtensionFieldsForMooseWithSpecVersion8(obj, specimenType);
                } else {
                    populateExtensionFieldsForMooseWithSpecVersion5(obj, specimenType);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported specVersion: " + specVersion);
        }
    }

    default void populateFieldsForRoeDeer(@Nonnull final HarvestSpecimenBusinessFields obj,
                                          @Nonnull final HarvestSpecimenType specimenType) {

        populateAgeAndGender(obj, specimenType);

        final HarvestSpecVersion specVersion = getSpecimenOps().getVersion();

        switch (specVersion) {
            case _3:
            case _4:
            case _5:
            case _6:
            case _7:
                obj.setWeight(weight());
                break;
            case _8:
            case _9:
            case _10:
                if (getSpecimenOps().getHuntingYear() >= 2020) {
                    obj.setWeight(null);
                    populateExtensionFieldsForRoeDeerWithSpecVersion8(obj, specimenType);
                } else {
                    obj.setWeight(weight());
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported specVersion: " + specVersion);
        }
    }

    default void populateFieldsForWhiteTailedDeer(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                  @Nonnull final HarvestSpecimenType specimenType) {
        populateAgeAndGender(obj, specimenType);

        final HarvestSpecVersion specVersion = getSpecimenOps().getVersion();

        switch (specVersion) {
            case _3:
                obj.setWeight(weight());
                break;
            case _4:
            case _5:
            case _6:
            case _7:
                obj.setWeight(null);
                populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(obj, specimenType);
                break;
            case _8:
            case _9:
            case _10:
                obj.setWeight(null);
                if (getSpecimenOps().getHuntingYear() >= 2020) {
                    populateExtensionFieldsForWhiteTailedDeerWithSpecVersion8(obj, specimenType);
                } else {
                    populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(obj, specimenType);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported specVersion: " + specVersion);
        }
    }

    // Fallow deer and wild forest reindeer.
    default void populateFieldsForOtherDeer(@Nonnull final HarvestSpecimenBusinessFields obj,
                                            @Nonnull final HarvestSpecimenType specimenType) {

        populateAgeAndGender(obj, specimenType);

        final HarvestSpecVersion specVersion = getSpecimenOps().getVersion();

        switch (specVersion) {
            case _3:
                obj.setWeight(weight());
                break;
            case _4:
            case _5:
            case _6:
            case _7:
                obj.setWeight(null);
                populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(obj, specimenType);
                break;
            case _8:
            case _9:
            case _10:
                obj.setWeight(null);
                if (getSpecimenOps().getHuntingYear() >= 2020) {
                    populateExtensionFieldsForOtherDeerWithSpecVersion8(obj, specimenType);
                } else {
                    populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(obj, specimenType);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported specVersion: " + specVersion);
        }
    }

    default void populateFieldsForWildBoar(@Nonnull final HarvestSpecimenBusinessFields obj,
                                           @Nonnull final HarvestSpecimenType specimenType) {

        populateAgeAndGender(obj, specimenType);

        final HarvestSpecimenOps ops = getSpecimenOps();

        if (ops.getVersion().supportsExtendedWeightFieldsForRoeDeerAndWildBoar() && ops.getHuntingYear() >= 2020) {
            obj.setWeight(null);
            populateExtendedWeightFields(obj);
        } else {
            obj.setWeight(weight());
        }
    }

    default void populateFieldsForOtherSpecies(@Nonnull final HarvestSpecimenBusinessFields obj,
                                               @Nonnull final HarvestSpecimenType specimenType) {

        populateAgeAndGender(obj, specimenType);
        obj.setWeight(weight());
    }

    default void populateExtendedWeightFields(@Nonnull final HarvestSpecimenBusinessFields obj) {
        requireNonNull(obj);
        obj.setWeightEstimated(weight());
        obj.setWeightMeasured(weight());
    }

    default void populateExtensionFieldsForPermitBasedMooselike(@Nonnull final HarvestSpecimenBusinessFields obj) {
        populateExtendedWeightFields(obj);

        obj.setNotEdible(someBoolean());
        obj.setAdditionalInfo("additional info " + nextPositiveInt());
    }

    default void populateExtensionFieldsForMooseWithSpecVersion3(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                                 @Nonnull final HarvestSpecimenType specimenType) {
        populateExtensionFieldsForPermitBasedMooselike(obj);

        obj.setFitnessClass(some(GameFitnessClass.class));

        if (specimenType.isAdultMale()) {
            obj.setAntlersType(some(GameAntlersType.class));
            obj.setAntlersWidth(nextPositiveIntAtMost(100));
            obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
            obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
        }
    }

    default void populateExtensionFieldsForMooseWithSpecVersion5(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                                 @Nonnull final HarvestSpecimenType specimenType) {
        populateExtensionFieldsForPermitBasedMooselike(obj);

        obj.setFitnessClass(some(GameFitnessClass.class));

        if (specimenType.isAdultMale()) {
            obj.setAntlersType(some(GameAntlersType.class));
            obj.setAntlersWidth(nextPositiveIntAtMost(100));
            obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
            obj.setAntlerPointsRight(nextPositiveIntAtMost(10));

        } else if (specimenType.isYoung()) {
            obj.setAlone(someBoolean());
        }
    }

    default void populateExtensionFieldsForMooseWithSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                                 @Nonnull final HarvestSpecimenType specimenType) {
        populateExtensionFieldsForPermitBasedMooselike(obj);

        obj.setFitnessClass(some(GameFitnessClass.class));

        if (specimenType.isAdultMale()) {
            if (specimenType.isAntlersLost()) {
                obj.setAntlersLost(true);
            } else {
                obj.setAntlersLost(false);
                obj.setAntlersType(some(GameAntlersType.class));
                obj.setAntlersWidth(nextPositiveIntAtMost(100));
                obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
                obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
                obj.setAntlersGirth(nextPositiveIntAtMost(50));
            }

        } else if (specimenType.isYoung()) {
            obj.setAlone(someBoolean());
        }
    }

    default void populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                                           @Nonnull final HarvestSpecimenType specimenType) {
        populateExtensionFieldsForPermitBasedMooselike(obj);

        if (specimenType.isAdultMale()) {
            obj.setAntlersWidth(nextPositiveIntAtMost(100));
            obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
            obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
        }
    }

    default void populateExtensionFieldsForRoeDeerWithSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                                   @Nonnull final HarvestSpecimenType specimenType) {
        populateExtendedWeightFields(obj);

        if (specimenType.isAdultMale()) {
            if (specimenType.isAntlersLost()) {
                obj.setAntlersLost(true);
            } else {
                obj.setAntlersLost(false);
                obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
                obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
                obj.setAntlersLength(nextPositiveIntAtMost(100));
                obj.setAntlerShaftWidth(nextPositiveIntAtMost(10));
            }
        }
    }

    default void populateExtensionFieldsForWhiteTailedDeerWithSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                                           @Nonnull final HarvestSpecimenType specimenType) {
        populateExtensionFieldsForPermitBasedMooselike(obj);

        if (specimenType.isAdultMale()) {
            if (specimenType.isAntlersLost()) {
                obj.setAntlersLost(true);
            } else {
                obj.setAntlersLost(false);
                obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
                obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
                obj.setAntlersGirth(nextPositiveIntAtMost(50));
                obj.setAntlersLength(nextPositiveIntAtMost(100));
                obj.setAntlersInnerWidth(nextPositiveIntAtMost(100));
            }
        }
    }

    // Fallow deer and wild forest reindeer.
    default void populateExtensionFieldsForOtherDeerWithSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields obj,
                                                                     @Nonnull final HarvestSpecimenType specimenType) {
        populateExtensionFieldsForPermitBasedMooselike(obj);

        if (specimenType.isAdultMale()) {
            if (specimenType.isAntlersLost()) {
                obj.setAntlersLost(true);
            } else {
                obj.setAntlersLost(false);
                obj.setAntlersWidth(nextPositiveIntAtMost(100));
                obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
                obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
            }
        }
    }
}
