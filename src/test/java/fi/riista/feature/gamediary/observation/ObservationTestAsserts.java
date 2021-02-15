package fi.riista.feature.gamediary.observation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class ObservationTestAsserts {

    public static void assertMooseAmountFieldsNotNull(final HasMooselikeObservationAmounts obj,
                                                      final boolean includeCalf) {

        assertMooselikeAmountFieldsNotNull(obj, includeCalf, false);
    }

    public static void assertMooselikeAmountFieldsNotNull(final HasMooselikeObservationAmounts obj,
                                                          final boolean includeCalf,
                                                          final boolean includeFemaleWith4Calfs) {

        assertThat(obj.getMooselikeMaleAmount(), is(notNullValue()));
        assertThat(obj.getMooselikeFemaleAmount(), is(notNullValue()));

        if (includeCalf) {
            assertThat(obj.getMooselikeCalfAmount(), is(notNullValue()));
        }

        assertThat(obj.getMooselikeFemale1CalfAmount(), is(notNullValue()));
        assertThat(obj.getMooselikeFemale2CalfsAmount(), is(notNullValue()));
        assertThat(obj.getMooselikeFemale3CalfsAmount(), is(notNullValue()));

        if (includeFemaleWith4Calfs) {
            assertThat(obj.getMooselikeFemale4CalfsAmount(), is(notNullValue()));
        }

        assertThat(obj.getMooselikeUnknownSpecimenAmount(), is(notNullValue()));
    }

    public static void assertMooselikeAmounts(final HasMooselikeObservationAmounts expected,
                                              final HasMooselikeObservationAmounts actual) {

        assertMooselikeAmounts(expected, actual, true, true);
    }

    public static void assertMooseAmounts(final HasMooselikeObservationAmounts expected,
                                          final HasMooselikeObservationAmounts actual,
                                          final boolean includeCalf) {

        assertMooselikeAmounts(expected, actual, includeCalf, false);
    }

    public static void assertMooselikeAmounts(final HasMooselikeObservationAmounts expected,
                                              final HasMooselikeObservationAmounts actual,
                                              final boolean includeCalf,
                                              final boolean includeFemaleWith4Calfs) {

        assertThat(actual.getMooselikeMaleAmount(), equalTo(expected.getMooselikeMaleAmount()));
        assertThat(actual.getMooselikeFemaleAmount(), equalTo(expected.getMooselikeFemaleAmount()));

        if (includeCalf) {
            assertThat(actual.getMooselikeCalfAmount(), equalTo(expected.getMooselikeCalfAmount()));
        }

        assertThat(actual.getMooselikeFemale1CalfAmount(), equalTo(expected.getMooselikeFemale1CalfAmount()));
        assertThat(actual.getMooselikeFemale2CalfsAmount(), equalTo(expected.getMooselikeFemale2CalfsAmount()));
        assertThat(actual.getMooselikeFemale3CalfsAmount(), equalTo(expected.getMooselikeFemale3CalfsAmount()));

        if (includeFemaleWith4Calfs) {
            assertThat(actual.getMooselikeFemale4CalfsAmount(), equalTo(expected.getMooselikeFemale4CalfsAmount()));
        }

        assertThat(actual.getMooselikeUnknownSpecimenAmount(), equalTo(expected.getMooselikeUnknownSpecimenAmount()));
    }

    public static void assertMooselikeAmountFieldsAreNull(final HasMooselikeObservationAmounts obj) {
        assertThat(obj.getMooselikeMaleAmount(), is(nullValue()));
        assertThat(obj.getMooselikeFemaleAmount(), is(nullValue()));
        assertThat(obj.getMooselikeCalfAmount(), is(nullValue()));
        assertThat(obj.getMooselikeFemale1CalfAmount(), is(nullValue()));
        assertThat(obj.getMooselikeFemale2CalfsAmount(), is(nullValue()));
        assertThat(obj.getMooselikeFemale3CalfsAmount(), is(nullValue()));
        assertThat(obj.getMooselikeFemale4CalfsAmount(), is(nullValue()));
        assertThat(obj.getMooselikeUnknownSpecimenAmount(), is(nullValue()));
    }

    public static void assertLargeCarnivoreFieldsNotNull(final Observation observation) {
        // inYardDistanceToResidence property is not asserted because it would need mocking!

        assertThat(observation.getVerifiedByCarnivoreAuthority(), is(notNullValue()));
        assertThat(observation.getObserverName(), is(notNullValue()));
        assertThat(observation.getObserverPhoneNumber(), is(notNullValue()));
        assertThat(observation.getOfficialAdditionalInfo(), is(notNullValue()));
    }

    public static void assertLargeCarnivoreFieldsAreNull(final Observation observation) {
        assertThat(observation.getInYardDistanceToResidence(), is(nullValue()));
        assertThat(observation.getVerifiedByCarnivoreAuthority(), is(nullValue()));
        assertThat(observation.getObserverName(), is(nullValue()));
        assertThat(observation.getObserverPhoneNumber(), is(nullValue()));
        assertThat(observation.getOfficialAdditionalInfo(), is(nullValue()));
    }
}
