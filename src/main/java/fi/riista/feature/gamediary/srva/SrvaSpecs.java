package fi.riista.feature.gamediary.srva;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.Interval;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static fi.riista.util.jpa.JpaSpecs.equal;

public final class SrvaSpecs {

    private SrvaSpecs() {
        throw new AssertionError();
    }

    @Nonnull
    public static Specification<SrvaEvent> equalRhy(@Nullable Riistanhoitoyhdistys rhy) {
        return equal(SrvaEvent_.rhy, rhy);
    }

    @Nonnull
    public static Specification<SrvaEvent> equalState(@Nullable SrvaEventStateEnum state) {
        return equal(SrvaEvent_.state, state);
    }

    @Nonnull
    public static Specification<SrvaEvent> author(@Nullable Person person) {
        return equal(SrvaEvent_.author, person);
    }

    @Nonnull
    public static Specification<SrvaEvent> withinInterval(@Nonnull Interval interval) {
        return JpaSpecs.withinInterval(SrvaEvent_.pointOfTime, interval);
    }
}
