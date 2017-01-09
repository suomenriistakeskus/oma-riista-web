package fi.riista.test;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.F;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class OccupationMatchers {
    public static Matcher<Occupation> hasOccupationType(final Matcher<OccupationType> childMatcher) {
        return new FeatureMatcher<Occupation, OccupationType>(childMatcher, "occupationType", "occupationType") {
            @Override
            protected OccupationType featureValueOf(Occupation actual) {
                return actual.getOccupationType();
            }
        };
    }

    public static Matcher<Occupation> hasPersonId(final Matcher<Long> childMatcher) {
        return new FeatureMatcher<Occupation, Long>(childMatcher, "personId", "personId") {
            @Override
            protected Long featureValueOf(Occupation actual) {
                return F.getId(actual.getPerson());
            }
        };
    }

    public static Matcher<Occupation> hasCallOrder(final Matcher<Integer> childMatcher) {
        return new FeatureMatcher<Occupation, Integer>(childMatcher, "callOrder", "callOrder") {
            @Override
            protected Integer featureValueOf(Occupation actual) {
                return actual.getCallOrder();
            }
        };
    }

    private OccupationMatchers() {
        throw new AssertionError();
    }
}
