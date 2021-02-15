package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Consumer;

import static fi.riista.test.Asserts.assertThat;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

public class HarvestSpecimenAssertionBuilder {

    private final ArrayList<Consumer<? super HarvestSpecimenBusinessFields>> assertionVerifiers = new ArrayList<>();

    public static HarvestSpecimenAssertionBuilder builder() {
        return new HarvestSpecimenAssertionBuilder();
    }

    public void verify(@Nonnull final HarvestSpecimenBusinessFields specimen) {
        requireNonNull(specimen);
        assertionVerifiers.forEach(verifier -> verifier.accept(specimen));
    }

    public HarvestSpecimenAssertionBuilder ageAndGenderPresent() {
        return and(specimen -> {
            assertThat(specimen.getAge(), is(notNullValue()), HarvestSpecimenFieldName.AGE.name());
            assertThat(specimen.getGender(), is(notNullValue()), HarvestSpecimenFieldName.GENDER.name());
        });
    }

    public HarvestSpecimenAssertionBuilder weightPresent() {
        return and(specimen -> mustNotBeNull(HarvestSpecimenFieldName.WEIGHT, specimen.getWeight()));
    }

    public HarvestSpecimenAssertionBuilder weightAbsent() {
        return and(specimen -> mustBeNull(HarvestSpecimenFieldName.WEIGHT, specimen.getWeight()));
    }

    public HarvestSpecimenAssertionBuilder onlyCommonFieldsPresent() {
        return ageAndGenderPresent().weightPresent().extensionFieldsAbsent();
    }

    public HarvestSpecimenAssertionBuilder extendedWeightFieldsPresent() {
        return and(specimen -> {
            if (F.allNull(specimen.getWeightEstimated(), specimen.getWeightMeasured())) {
                fail("both estimated and measured weight are null");
            }
        });
    }

    public HarvestSpecimenAssertionBuilder antlerFieldsAbsent() {
        return antlerDetailFieldsAbsent()
                .and(specimen -> mustBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost()));
    }

    public HarvestSpecimenAssertionBuilder antlerDetailFieldsAbsent() {
        return and(specimen -> {
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
            mustBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
            mustBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_GIRTH, specimen.getAntlersGirth());
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_LENGTH, specimen.getAntlersLength());
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH, specimen.getAntlersInnerWidth());
            mustBeNull(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH, specimen.getAntlerShaftWidth());
        });
    }

    public HarvestSpecimenAssertionBuilder antlersLostPresent() {
        return antlerDetailFieldsAbsent()
                .and(specimen -> mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost()));
    }

    public HarvestSpecimenAssertionBuilder extensionFieldsAbsent() {
        return extensionFieldsAbsentExceptEstimatedWeight()
                .and(specimen -> mustBeNull(HarvestSpecimenFieldName.WEIGHT_ESTIMATED, specimen.getWeightEstimated()));
    }

    public HarvestSpecimenAssertionBuilder weightEstimatedPresentButOtherExtensionFieldsAbsent() {
        return extensionFieldsAbsentExceptEstimatedWeight()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.WEIGHT_ESTIMATED, specimen.getWeightEstimated());
                });
    }

    private HarvestSpecimenAssertionBuilder extensionFieldsAbsentExceptEstimatedWeight() {
        return antlerFieldsAbsent()
                .aloneAbsent()
                .and(specimen -> {
                    mustBeNull(HarvestSpecimenFieldName.WEIGHT_MEASURED, specimen.getWeightMeasured());

                    mustBeNull(HarvestSpecimenFieldName.FITNESS_CLASS, specimen.getFitnessClass());
                    mustBeNull(HarvestSpecimenFieldName.NOT_EDIBLE, specimen.getNotEdible());
                    mustBeNull(HarvestSpecimenFieldName.ADDITIONAL_INFO, specimen.getAdditionalInfo());
                });
    }

    public HarvestSpecimenAssertionBuilder onlyAgeGenderAndWeightEstimatedPresent() {
        return ageAndGenderPresent()
                .weightAbsent()
                .weightEstimatedPresentButOtherExtensionFieldsAbsent();
    }

    public HarvestSpecimenAssertionBuilder ageGenderAndExtendedWeightFieldsPresent() {
        return ageAndGenderPresent().weightAbsent().extendedWeightFieldsPresent();
    }

    // Moose, fallow deer, white-tailed deer, wild forest reindeer
    public HarvestSpecimenAssertionBuilder permitBasedMooselikeCommonFieldsPresent() {
        return ageGenderAndExtendedWeightFieldsPresent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.NOT_EDIBLE, specimen.getNotEdible());
                    mustNotBeNull(HarvestSpecimenFieldName.ADDITIONAL_INFO, specimen.getAdditionalInfo());
                });
    }

    public HarvestSpecimenAssertionBuilder mooseCommonFieldsPresent() {
        return permitBasedMooselikeCommonFieldsPresent()
                .nonMooseFieldsAbsent()
                .and(specimen -> mustNotBeNull(HarvestSpecimenFieldName.FITNESS_CLASS, specimen.getFitnessClass()));
    }

    public HarvestSpecimenAssertionBuilder mooseAdultMaleFields2015Present() {
        return mooseCommonFieldsPresent()
                .aloneAbsent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());

                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_GIRTH, specimen.getAntlersGirth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_LENGTH, specimen.getAntlersLength());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH, specimen.getAntlersInnerWidth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH, specimen.getAntlerShaftWidth());
                });
    }

    public HarvestSpecimenAssertionBuilder mooseAdultMaleFields2020Present() {
        return mooseCommonFieldsPresent()
                .aloneAbsent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_GIRTH, specimen.getAntlersGirth());
                });
    }

    public HarvestSpecimenAssertionBuilder mooseAntlersLostFieldsPresent() {
        return mooseCommonFieldsPresent().antlersLostPresent().aloneAbsent();
    }

    public HarvestSpecimenAssertionBuilder mooseAdultFemaleFieldsPresent() {
        return mooseCommonFieldsPresent().antlerFieldsAbsent().aloneAbsent();
    }

    public HarvestSpecimenAssertionBuilder mooseYoungFieldsPresent() {
        return mooseCommonFieldsPresent()
                .antlerFieldsAbsent()
                .and(specimen -> mustNotBeNull(HarvestSpecimenFieldName.ALONE, specimen.getAlone()));
    }

    public HarvestSpecimenAssertionBuilder mooseFieldsAbsent() {
        return aloneAbsent()
                .and(specimen -> {
                    mustBeNull(HarvestSpecimenFieldName.FITNESS_CLASS, specimen.getFitnessClass());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());
                });
    }

    public HarvestSpecimenAssertionBuilder aloneAbsent() {
        return and(specimen -> mustBeNull(HarvestSpecimenFieldName.ALONE, specimen.getAlone()));
    }

    public HarvestSpecimenAssertionBuilder roeDeerCommonFieldsPresent() {
        return ageGenderAndExtendedWeightFieldsPresent().nonRoeDeerFieldsAbsent();
    }

    public HarvestSpecimenAssertionBuilder roeDeerAdultMaleFieldsPresent() {
        return roeDeerCommonFieldsPresent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_LENGTH, specimen.getAntlersLength());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH, specimen.getAntlerShaftWidth());
                });
    }

    public HarvestSpecimenAssertionBuilder roeDeerAntlersLostFieldsPresent() {
        return roeDeerCommonFieldsPresent().antlersLostPresent();
    }

    public HarvestSpecimenAssertionBuilder whiteTailedDeerCommonFieldsPresent() {
        return permitBasedMooselikeCommonFieldsPresent().nonWhiteTailedDeerFieldsAbsent();
    }

    public HarvestSpecimenAssertionBuilder whiteTailedDeerAdultMaleFieldsPresent() {
        return whiteTailedDeerCommonFieldsPresent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_GIRTH, specimen.getAntlersGirth());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_LENGTH, specimen.getAntlersLength());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH, specimen.getAntlersInnerWidth());
                });
    }

    public HarvestSpecimenAssertionBuilder whiteTailedDeerAntlersLostFieldsPresent() {
        return whiteTailedDeerCommonFieldsPresent().antlersLostPresent();
    }

    // Fallow deer, wild forest reindeer
    public HarvestSpecimenAssertionBuilder otherDeerCommonFieldsPresent() {
        return permitBasedMooselikeCommonFieldsPresent().nonOtherDeerFieldsAbsent();
    }

    // Fallow deer, wild forest reindeer
    public HarvestSpecimenAssertionBuilder otherDeerAdultMaleFieldsPresent() {
        return otherDeerCommonFieldsPresent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());
                });
    }

    // Fallow deer, wild forest reindeer
    public HarvestSpecimenAssertionBuilder otherDeerAntlersLostFieldsPresent() {
        return otherDeerCommonFieldsPresent().antlersLostPresent();
    }

    // Fallow deer, white-tailed deer, wild forest reindeer
    public HarvestSpecimenAssertionBuilder permitBasedDeerAdultMaleFields2016Present() {
        return permitBasedMooselikeCommonFieldsPresent()
                .and(specimen -> {
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT, specimen.getAntlerPointsLeft());
                    mustNotBeNull(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT, specimen.getAntlerPointsRight());

                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_LOST, specimen.getAntlersLost());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_TYPE, specimen.getAntlersType());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_GIRTH, specimen.getAntlersGirth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_LENGTH, specimen.getAntlersLength());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH, specimen.getAntlersInnerWidth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH, specimen.getAntlerShaftWidth());
                });
    }

    public HarvestSpecimenAssertionBuilder wildBoarFieldsPresent() {
        return ageGenderAndExtendedWeightFieldsPresent().nonWildBoarFieldsAbsent();
    }

    public HarvestSpecimenAssertionBuilder nonMooseFieldsAbsent() {
        return and(specimen -> {
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_LENGTH, specimen.getAntlersLength());
            mustBeNull(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH, specimen.getAntlersInnerWidth());
            mustBeNull(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH, specimen.getAntlerShaftWidth());
        });
    }

    public HarvestSpecimenAssertionBuilder nonRoeDeerFieldsAbsent() {
        return mooseFieldsAbsent()
                .and(specimen -> {
                    mustBeNull(HarvestSpecimenFieldName.NOT_EDIBLE, specimen.getNotEdible());
                    mustBeNull(HarvestSpecimenFieldName.ADDITIONAL_INFO, specimen.getAdditionalInfo());

                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_WIDTH, specimen.getAntlersWidth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_GIRTH, specimen.getAntlersGirth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH, specimen.getAntlersInnerWidth());
                });
    }

    public HarvestSpecimenAssertionBuilder nonWhiteTailedDeerFieldsAbsent() {
        return mooseFieldsAbsent()
                .and(specimen -> mustBeNull(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH, specimen.getAntlerShaftWidth()));
    }

    public HarvestSpecimenAssertionBuilder nonOtherDeerFieldsAbsent() {
        return mooseFieldsAbsent()
                .and(specimen -> {
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_GIRTH, specimen.getAntlersGirth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_LENGTH, specimen.getAntlersLength());
                    mustBeNull(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH, specimen.getAntlersInnerWidth());
                    mustBeNull(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH, specimen.getAntlerShaftWidth());
                });
    }

    public HarvestSpecimenAssertionBuilder nonWildBoarFieldsAbsent() {
        return antlerFieldsAbsent()
                .aloneAbsent()
                .and(specimen -> {
                    mustBeNull(HarvestSpecimenFieldName.FITNESS_CLASS, specimen.getFitnessClass());
                    mustBeNull(HarvestSpecimenFieldName.NOT_EDIBLE, specimen.getNotEdible());
                    mustBeNull(HarvestSpecimenFieldName.ADDITIONAL_INFO, specimen.getAdditionalInfo());
                });
    }

    public HarvestSpecimenAssertionBuilder mooseFields2017EqualTo(@Nonnull final HarvestSpecimenBusinessFields that,
                                                                  @Nonnull final HarvestSpecVersion specVersion) {

        return mooseFields2017EqualTo(that, specVersion, true);
    }

    public HarvestSpecimenAssertionBuilder mooseFields2017EqualTo(@Nonnull final HarvestSpecimenBusinessFields that,
                                                                  @Nonnull final HarvestSpecVersion specVersion,
                                                                  final boolean includeAntlerFields) {
        requireNonNull(that);
        requireNonNull(specVersion);

        final HarvestSpecimenAssertionBuilder assertionBuilder = notSameInstance(that)
                .withAgeAndGender(that.getAge(), that.getGender())
                .withWeightEstimated(that.getWeightEstimated())
                .withWeightMeasured(that.getWeightMeasured())
                .withFitnessClass(that.getFitnessClass())
                .withNotEdible(that.getNotEdible())
                .withAdditionalInfo(that.getAdditionalInfo());

        if (specVersion.supportsSolitaryMooseCalves()) {
            assertionBuilder.withAlone(that.getAlone());
        }

        if (!includeAntlerFields) {
            return assertionBuilder;
        }

        return assertionBuilder
                .withAntlersType(that.getAntlersType())
                .withAntlersWidth(that.getAntlersWidth())
                .withAntlerPointsLeft(that.getAntlerPointsLeft())
                .withAntlerPointsRight(that.getAntlerPointsRight());
    }

    public HarvestSpecimenAssertionBuilder mooseFields2020EqualTo(@Nonnull final HarvestSpecimenBusinessFields that) {
        requireNonNull(that);

        return notSameInstance(that)
                .withAgeAndGender(that.getAge(), that.getGender())
                .withWeightEstimated(that.getWeightEstimated())
                .withWeightMeasured(that.getWeightMeasured())
                .withFitnessClass(that.getFitnessClass())
                .withNotEdible(that.getNotEdible())
                .withAdditionalInfo(that.getAdditionalInfo())

                .withAlone(that.getAlone())

                .withAntlersLost(that.getAntlersLost())
                .withAntlersType(that.getAntlersType())
                .withAntlersWidth(that.getAntlersWidth())
                .withAntlerPointsLeft(that.getAntlerPointsLeft())
                .withAntlerPointsRight(that.getAntlerPointsRight())
                .withAntlersGirth(that.getAntlersGirth());
    }

    public HarvestSpecimenAssertionBuilder permitBasedDeerFields2016EqualTo(@Nonnull final HarvestSpecimenBusinessFields that) {
        return permitBasedDeerFields2016EqualTo(that, true);
    }

    public HarvestSpecimenAssertionBuilder permitBasedDeerFields2016EqualTo(@Nonnull final HarvestSpecimenBusinessFields that,
                                                                            final boolean includeAntlerFields) {
        requireNonNull(that);

        final HarvestSpecimenAssertionBuilder assertionBuilder = notSameInstance(that)
                .withAgeAndGender(that.getAge(), that.getGender())
                .withWeightEstimated(that.getWeightEstimated())
                .withWeightMeasured(that.getWeightMeasured())
                .withNotEdible(that.getNotEdible())
                .withAdditionalInfo(that.getAdditionalInfo());

        if (!includeAntlerFields) {
            return assertionBuilder;
        }

        return assertionBuilder
                .withAntlersWidth(that.getAntlersWidth())
                .withAntlerPointsLeft(that.getAntlerPointsLeft())
                .withAntlerPointsRight(that.getAntlerPointsRight());
    }

    public HarvestSpecimenAssertionBuilder roeDeerFields2020EqualTo(@Nonnull final HarvestSpecimenBusinessFields that) {
        requireNonNull(that);

        return notSameInstance(that)
                .withAgeAndGender(that.getAge(), that.getGender())
                .withWeightEstimated(that.getWeightEstimated())
                .withWeightMeasured(that.getWeightMeasured())

                .withAntlersLost(that.getAntlersLost())
                .withAntlerPointsLeft(that.getAntlerPointsLeft())
                .withAntlerPointsRight(that.getAntlerPointsRight())
                .withAntlersLength(that.getAntlersLength())
                .withAntlerShaftWidth(that.getAntlerShaftWidth());
    }

    public HarvestSpecimenAssertionBuilder whiteTailedDeerFields2020EqualTo(@Nonnull final HarvestSpecimenBusinessFields that) {
        requireNonNull(that);

        return notSameInstance(that)
                .withAgeAndGender(that.getAge(), that.getGender())
                .withWeightEstimated(that.getWeightEstimated())
                .withWeightMeasured(that.getWeightMeasured())
                .withNotEdible(that.getNotEdible())
                .withAdditionalInfo(that.getAdditionalInfo())

                .withAntlersLost(that.getAntlersLost())
                .withAntlerPointsLeft(that.getAntlerPointsLeft())
                .withAntlerPointsRight(that.getAntlerPointsRight())
                .withAntlersGirth(that.getAntlersGirth())
                .withAntlersLength(that.getAntlersLength())
                .withAntlersInnerWidth(that.getAntlersInnerWidth());
    }

    public HarvestSpecimenAssertionBuilder otherDeerFields2020EqualTo(@Nonnull final HarvestSpecimenBusinessFields that) {
        requireNonNull(that);

        return notSameInstance(that)
                .withAgeAndGender(that.getAge(), that.getGender())
                .withWeightEstimated(that.getWeightEstimated())
                .withWeightMeasured(that.getWeightMeasured())
                .withNotEdible(that.getNotEdible())
                .withAdditionalInfo(that.getAdditionalInfo())

                .withAntlersLost(that.getAntlersLost())
                .withAntlersWidth(that.getAntlersWidth())
                .withAntlerPointsLeft(that.getAntlerPointsLeft())
                .withAntlerPointsRight(that.getAntlerPointsRight());
    }

    public HarvestSpecimenAssertionBuilder wildBoarFields2020EqualTo(@Nonnull final HarvestSpecimenBusinessFields that) {
        requireNonNull(that);

        return notSameInstance(that)
                .withAgeAndGender(that.getAge(), that.getGender())
                .withWeightEstimated(that.getWeightEstimated())
                .withWeightMeasured(that.getWeightMeasured());
    }

    public HarvestSpecimenAssertionBuilder notSameInstance(@Nonnull final HarvestSpecimenBusinessFields obj) {
        requireNonNull(obj);
        return and(specimen -> assertThat(specimen, not(sameInstance(obj)), "Should not compare object to itself"));
    }

    public HarvestSpecimenAssertionBuilder withAgeAndGender(@Nullable final GameAge expectedAge,
                                                            @Nullable final GameGender expectedGender) {
        return and(specimen -> {
            assertFieldValue(expectedAge, specimen.getAge(), HarvestSpecimenFieldName.AGE);
            assertFieldValue(expectedGender, specimen.getGender(), HarvestSpecimenFieldName.GENDER);
        });
    }

    public HarvestSpecimenAssertionBuilder withWeight(@Nullable final Double expected) {
        return and(specimen -> {
            assertDoubleFieldValue(expected, specimen.getWeight(), HarvestSpecimenFieldName.WEIGHT);
        });
    }

    public HarvestSpecimenAssertionBuilder withWeightEstimated(@Nullable final Double expected) {
        return and(specimen -> {
            assertDoubleFieldValue(expected, specimen.getWeightEstimated(), HarvestSpecimenFieldName.WEIGHT_ESTIMATED);
        });
    }

    public HarvestSpecimenAssertionBuilder withWeightMeasured(@Nullable final Double expected) {
        return and(specimen -> {
            assertDoubleFieldValue(expected, specimen.getWeightMeasured(), HarvestSpecimenFieldName.WEIGHT_MEASURED);
        });
    }

    public HarvestSpecimenAssertionBuilder withFitnessClass(@Nullable final GameFitnessClass expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getFitnessClass(), HarvestSpecimenFieldName.FITNESS_CLASS);
        });
    }

    public HarvestSpecimenAssertionBuilder withNotEdible(@Nullable final Boolean expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getNotEdible(), HarvestSpecimenFieldName.NOT_EDIBLE);
        });
    }

    public HarvestSpecimenAssertionBuilder withAdditionalInfo(@Nullable final String expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAdditionalInfo(), HarvestSpecimenFieldName.ADDITIONAL_INFO);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlersLost(@Nullable final Boolean expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlersLost(), HarvestSpecimenFieldName.ANTLERS_LOST);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlersType(@Nullable final GameAntlersType expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlersType(), HarvestSpecimenFieldName.ANTLERS_TYPE);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlersWidth(@Nullable final Integer expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlersWidth(), HarvestSpecimenFieldName.ANTLERS_WIDTH);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlerPointsLeft(@Nullable final Integer expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlerPointsLeft(), HarvestSpecimenFieldName.ANTLER_POINTS_LEFT);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlerPointsRight(@Nullable final Integer expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlerPointsRight(), HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlersGirth(@Nullable final Integer expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlersGirth(), HarvestSpecimenFieldName.ANTLERS_GIRTH);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlersLength(@Nullable final Integer expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlersLength(), HarvestSpecimenFieldName.ANTLERS_LENGTH);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlersInnerWidth(@Nullable final Integer expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlersInnerWidth(), HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH);
        });
    }

    public HarvestSpecimenAssertionBuilder withAntlerShaftWidth(@Nullable final Integer expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAntlerShaftWidth(), HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH);
        });
    }

    public HarvestSpecimenAssertionBuilder withAlone(@Nullable final Boolean expected) {
        return and(specimen -> {
            assertFieldValue(expected, specimen.getAlone(), HarvestSpecimenFieldName.ALONE);
        });
    }

    private static <T> void assertFieldValue(@Nullable final T expected,
                                             @Nullable final T actual,
                                             @Nonnull final HarvestSpecimenFieldName field) {
        if (expected != null) {
            assertThat(actual, equalTo(expected), field.name());
        } else {
            assertThat(actual, is(nullValue()), field.name());
        }
    }

    private static void assertDoubleFieldValue(@Nullable final Double expected,
                                               @Nullable final Double actual,
                                               @Nonnull final HarvestSpecimenFieldName field) {
        if (expected != null) {
            assertThat(actual, closeTo(expected, 0.001), field.name());
        } else {
            assertThat(actual, is(nullValue()), field.name());
        }
    }

    private HarvestSpecimenAssertionBuilder and(@Nonnull final Consumer<? super HarvestSpecimenBusinessFields> verifier) {
        requireNonNull(verifier);
        assertionVerifiers.add(verifier);
        return this;
    }

    private static void mustBeNull(final HarvestSpecimenFieldName fieldName, final Object value) {
        assertThat(value, is(nullValue()), '"' + fieldName.name() + "\": ");
    }

    private static void mustNotBeNull(final HarvestSpecimenFieldName fieldName, final Object value) {
        assertThat(value, is(notNullValue()), "expected \"" + fieldName.name() + "\" to be non-null but was null");
    }
}
