package fi.riista.test.matchers;

import fi.riista.feature.common.entity.HasID;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class CommonMatchers {
    public static <T extends HasID<Long>> Matcher<T> hasId(Matcher<Long> childMatcher) {
        return new FeatureMatcher<T, Long>(childMatcher, "id", "id") {
            @Override
            protected Long featureValueOf(T actual) {
                return actual.getId();
            }
        };
    }

    private CommonMatchers() {
        throw new AssertionError();
    }
}
