package fi.riista.feature.gamediary.srva;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static fi.riista.util.jpa.JpaSpecs.joinPathToId;

public final class SrvaSpecs {

    private SrvaSpecs() {
        throw new AssertionError();
    }

    @Nonnull
    public static Specification<SrvaEvent> anyOfStates(@Nonnull List<SrvaEventStateEnum> states) {
        return inCollection(SrvaEvent_.state, states);
    }

    @Nonnull
    public static Specification<SrvaEvent> anyOfEventNames(@Nonnull List<SrvaEventNameEnum> eventNames) {
        return inCollection(SrvaEvent_.eventName, eventNames);
    }

    @Nonnull
    public static Specification<SrvaEvent> equalEventName(@Nullable SrvaEventNameEnum eventName) {
        return equal(SrvaEvent_.eventName, eventName);
    }

    @Nonnull
    public static Specification<SrvaEvent> equalRhy(@Nullable Riistanhoitoyhdistys rhy) {
        return equal(SrvaEvent_.rhy, rhy);
    }

    @Nonnull
    public static Specification<SrvaEvent> equalSpecies(@Nullable GameSpecies species) {
        return equal(SrvaEvent_.species, species);
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
    public static Specification<SrvaEvent> withinInterval(@Nullable LocalDate beginDate, @Nullable LocalDate endDate) {
        return JpaSpecs.withinInterval(SrvaEvent_.pointOfTime, beginDate, endDate);
    }

    @Nonnull
    public static Specification<SrvaEvent> withinInterval(@Nonnull Interval interval) {
        return JpaSpecs.withinInterval(SrvaEvent_.pointOfTime, interval);
    }

    @Nonnull
    public static Specification<SrvaEvent> equalRka(@Nonnull final Long areaId) {
        return joinPathToId(SrvaEvent_.rhy, Organisation_.parentOrganisation, Organisation_.id, areaId);
    }
}
