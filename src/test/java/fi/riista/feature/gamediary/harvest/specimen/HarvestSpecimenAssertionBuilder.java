package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HarvestSpecimenAssertionBuilder {

    private final ArrayList<Consumer<? super HarvestSpecimenBusinessFields>> assertionVerifiers = new ArrayList<>();

    private GameAge age;
    private GameGender gender;

    public static HarvestSpecimenAssertionBuilder builder() {
        return new HarvestSpecimenAssertionBuilder();
    }

    public void verify(@Nonnull final HarvestSpecimenBusinessFields specimen) {
        Objects.requireNonNull(specimen);
        assertionVerifiers.forEach(verifier -> verifier.accept(specimen));
    }

    public HarvestSpecimenAssertionBuilder withAgeAndGender(@Nullable final GameAge expectedAge,
                                                            @Nullable final GameGender expectedGender) {
        this.age = expectedAge;
        this.gender = expectedGender;

        return and(specimen -> {
            assertEquals(HarvestSpecimenFieldName.AGE.name(), this.age, specimen.getAge());
            assertEquals(HarvestSpecimenFieldName.GENDER.name(), this.gender, specimen.getGender());
        });
    }

    public HarvestSpecimenAssertionBuilder weightPresent() {
        return and(specimen -> mustNotBeNull(HarvestSpecimenFieldName.WEIGHT, specimen.getWeight()));
    }

    public HarvestSpecimenAssertionBuilder weightPresentAndEqualTo(@Nonnull final Double weight) {
        Objects.requireNonNull(weight);
        return weightPresent().and(specimen -> assertEquals(weight, specimen.getWeight()));
    }

    public HarvestSpecimenAssertionBuilder weightAbsent() {
        return and(specimen -> mustBeNull(HarvestSpecimenFieldName.WEIGHT, specimen.getWeight()));
    }

    public HarvestSpecimenAssertionBuilder weightAbsentButEstimatedWeightPresentAndEqualTo(@Nonnull final Double weight) {
        Objects.requireNonNull(weight);

        return weightAbsent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.WEIGHT_ESTIMATED, specimen.getWeightEstimated());
                    assertEquals(weight, specimen.getWeightEstimated());
                });
    }

    public HarvestSpecimenAssertionBuilder mooselikeWeightPresent() {
        return and(specimen -> {
            if (F.allNull(specimen.getWeightEstimated(), specimen.getWeightMeasured())) {
                fail("both estimated and measured weight are null");
            }
        });
    }

    public HarvestSpecimenAssertionBuilder allMooselikeFieldsPresent() {
        ageAndGenderMustNotBeNull();
        return mooselikeFieldsPreserved(this.age, this.gender);
    }

    public HarvestSpecimenAssertionBuilder allMooseFieldsPresent(@Nonnull final HarvestSpecVersion specVersion) {
        ageAndGenderMustNotBeNull();
        return mooseFieldsPreserved(this.age, this.gender, specVersion);
    }

    public HarvestSpecimenAssertionBuilder mooselikeFieldsPreserved(final GameAge originalAge,
                                                                    final GameGender originalGender) {

        return mooselikeWeightPresent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.NOT_EDIBLE, specimen.getNotEdible());
                    mustNotBeNull(HarvestSpecimenFieldName.ADDITIONAL_INFO, specimen.getAdditionalInfo());

                    if (originalAge == GameAge.ADULT && originalGender == GameGender.MALE) {
                        mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                        mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                        mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
                    } else {
                        mustBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                        mustBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                        mustBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
                    }
                });
    }

    public HarvestSpecimenAssertionBuilder mooseFieldsPreserved(final GameAge originalAge,
                                                                final GameGender originalGender,
                                                                @Nonnull final HarvestSpecVersion specVersion) {

        requireSupportForMooseFields(specVersion);

        return mooselikeFieldsPreserved(originalAge, originalGender)
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.FITNESS_CLASS, specimen.getFitnessClass());

                    if (originalAge == GameAge.ADULT && originalGender == GameGender.MALE) {
                        mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());
                    } else {
                        mustBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());

                        if (originalAge == GameAge.YOUNG) {
                            if (specVersion.supportsSolitaryMooseCalves()) {
                                mustNotBeNull(HarvestSpecimenFieldName.ALONE, specimen.getAlone());
                            }
                        } else {
                            mustBeNull(HarvestSpecimenFieldName.ALONE, specimen.getAlone());
                        }
                    }
                });
    }

    public HarvestSpecimenAssertionBuilder mooseFieldsAbsent() {
        return mooseFieldsAbsentExceptEstimatedWeight()
                .and(specimen -> {
                    mustBeNull(HarvestSpecimenFieldName.WEIGHT_ESTIMATED, specimen.getWeightEstimated());
                });
    }

    public HarvestSpecimenAssertionBuilder mooseFieldsAbsentExceptEstimatedWeight() {
        return mooseOnlyFieldsAbsent()
                .and(specimen -> {
                    mustBeNull(HarvestSpecimenFieldName.WEIGHT_MEASURED, specimen.getWeightMeasured());
                    mustBeNull(HarvestSpecimenFieldName.NOT_EDIBLE, specimen.getNotEdible());
                    mustBeNull(HarvestSpecimenFieldName.ADDITIONAL_INFO, specimen.getAdditionalInfo());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                    mustBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
                });
    }

    public HarvestSpecimenAssertionBuilder mooseOnlyFieldsAbsent() {
        return and(specimen -> {
            mustBeNull(HarvestSpecimenFieldName.FITNESS_CLASS, specimen.getFitnessClass());
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());
        });
    }

    public HarvestSpecimenAssertionBuilder mooselikeFieldsEqualTo(@Nonnull final HasMooselikeFields that) {
        Objects.requireNonNull(that);
        return and(specimen -> assertTrue(specimen.hasEqualMooselikeFields(that)));
    }

    public HarvestSpecimenAssertionBuilder mooseFieldsEqualTo(@Nonnull final HasMooseFields that,
                                                              @Nonnull final HarvestSpecVersion specVersion) {

        Objects.requireNonNull(that);
        requireSupportForMooseFields(specVersion);

        return and(specimen -> {
            assertTrue(new HarvestSpecimenOps(OFFICIAL_CODE_MOOSE, specVersion).hasEqualMooseFields(specimen, that));
        });
    }

    private HarvestSpecimenAssertionBuilder and(final Consumer<? super HarvestSpecimenBusinessFields> verifier) {
        Objects.requireNonNull(verifier);
        assertionVerifiers.add(verifier);
        return this;
    }

    private void ageAndGenderMustNotBeNull() {
        checkState(this.age != null, "age is null");
        checkState(this.gender != null, "gender is null");
    }

    private static void requireSupportForMooseFields(@Nonnull final HarvestSpecVersion specVersion) {
        Objects.requireNonNull(specVersion, "specVersion is null");
        checkArgument(specVersion.supportsExtendedFieldsForMoose(), "specVersion does not support moose fields");
    }

    private static void mustBeNull(final HarvestSpecimenFieldName fieldName, final Object value) {
        assertNull('"' + fieldName.name() + "\": ", value);
    }

    private static void mustNotBeNull(final HarvestSpecimenFieldName fieldName, final Object value) {
        assertNotNull("expected \"" + fieldName.name() + "\" to be non-null but was null", value);
    }
}
