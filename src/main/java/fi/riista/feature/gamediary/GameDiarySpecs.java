package fi.riista.feature.gamediary;

import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.Observation_;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson_;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.jpa.JpaPreds;
import fi.riista.util.jpa.JpaSpecs;
import fi.riista.util.jpa.JpaSubQuery;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Objects;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.isNotNull;
import static fi.riista.util.jpa.JpaSpecs.isNull;
import static fi.riista.util.jpa.JpaSpecs.notEqual;
import static fi.riista.util.jpa.JpaSpecs.olderThan;

public final class GameDiarySpecs {

    private GameDiarySpecs() {
        throw new AssertionError();
    }

    @Nonnull
    public static <T extends GameDiaryEntry> Specification<T> entriesByHuntingYear(
            final int firstCalendarYearOfHuntingYear) {

        return JpaSpecs.<T> withinHuntingYear(GameDiaryEntry_.pointOfTime, firstCalendarYearOfHuntingYear);
    }

    @Nonnull
    public static Specification<Harvest> harvestsByHuntingYear(final int firstCalendarYearOfHuntingYear) {
        return entriesByHuntingYear(firstCalendarYearOfHuntingYear);
    }

    @Nonnull
    public static Specification<Observation> observationsByHuntingYear(final int firstCalendarYearOfHuntingYear) {
        return entriesByHuntingYear(firstCalendarYearOfHuntingYear);
    }

    @Nonnull
    public static <T extends GameDiaryEntry> Specification<T> author(@Nullable final Person person) {
        return JpaSpecs.<T, Person>equal(GameDiaryEntry_.author, person);
    }

    @Nonnull
    public static Specification<Harvest> harvestAuthor(@Nullable final Person person) {
        return author(person);
    }

    @Nonnull
    public static Specification<Harvest> shooter(@Nullable final Person person) {
        return equal(Harvest_.actualShooter, person);
    }

    @Nonnull
    public static Specification<Harvest> authorButNotShooter(@Nullable final Person person) {
        return JpaSpecs.and(harvestAuthor(person), notEqual(Harvest_.actualShooter, person));
    }

    @Nonnull
    public static Specification<Harvest> authorOrShooter(@Nullable final Person person) {
        return JpaSpecs.or(harvestAuthor(person), equal(Harvest_.actualShooter, person));
    }

    @Nonnull
    public static Specification<Observation> observationAuthor(@Nullable final Person person) {
        return author(person);
    }

    @Nonnull
    public static Specification<Observation> observer(@Nullable final Person person) {
        return equal(Observation_.observer, person);
    }

    @Nonnull
    public static Specification<Observation> authorButNotObserver(@Nullable final Person person) {
        return JpaSpecs.and(observationAuthor(person), notEqual(Observation_.observer, person));
    }

    @Nonnull
    public static Specification<Observation> authorOrObserver(@Nullable final Person person) {
        return JpaSpecs.or(observationAuthor(person), equal(Observation_.observer, person));
    }

    @Nonnull
    public static Specification<Harvest> authorOrShooterAndHarvestRejected(@Nullable final Person person) {
        return Specifications
                .where(authorOrShooter(person))
                .and(equal(Harvest_.stateAcceptedToHarvestPermit, Harvest.StateAcceptedToHarvestPermit.REJECTED));
    }

    @Nonnull
    public static Specification<Harvest> authorOrShooterAndHarvestReportRequiredAndMissing(
            @Nullable final Person person) {

        return Specifications
                .where(authorOrShooter(person))
                .and(harvestReportRequiredAndMissing());
    }

    @Nonnull
    public static Specification<Harvest> harvestReportRequiredAndMissing() {
        return Specifications
                .where(equal(Harvest_.harvestReportRequired, true))
                .and(isNull(Harvest_.harvestReport));
    }

    @Nonnull
    public static Specification<Harvest> authorAndRejectedForPermit(@Nullable final Person person) {
        return Specifications
                .where(harvestAuthor(person))
                .and(isNull(Harvest_.harvestReport))
                .and(isNotNull(Harvest_.harvestPermit))
                .and(equal(Harvest_.stateAcceptedToHarvestPermit, Harvest.StateAcceptedToHarvestPermit.REJECTED));
    }

    @Nonnull
    public static Specification<Harvest> permitContactPersonAndProposedToPermit(@Nullable final Person person) {
        return Specifications
                .where(isNull(Harvest_.harvestReport))
                .and(equal(Harvest_.stateAcceptedToHarvestPermit, Harvest.StateAcceptedToHarvestPermit.PROPOSED))
                .and(permitContactPerson(person));
    }

    @Nonnull
    public static Specification<Harvest> acceptedToPermit(@Nullable final HarvestPermit permit) {
        return Specifications
                .where(equal(Harvest_.harvestPermit, permit))
                .and(equal(Harvest_.stateAcceptedToHarvestPermit, Harvest.StateAcceptedToHarvestPermit.ACCEPTED));
    }

    private static Specification<Harvest> permitContactPerson(final Person person) {
        return (root, criteriaQuery, criteriaBuilder) -> {

            final Subquery<Long> sub = criteriaQuery.subquery(Long.class);
            final Root<HarvestPermitContactPerson> subRoot = sub.from(HarvestPermitContactPerson.class);
            final Path<Person> contactPerson = subRoot.get(HarvestPermitContactPerson_.contactPerson);
            final Path<HarvestPermit> subPermit = subRoot.get(HarvestPermitContactPerson_.harvestPermit);

            // pseudo SQL:
            // SELECT harvest h WHERE h.harvestPermit IN (
            //   SELECT hpc.harvestPermit FROM HarvestPermitContactPerson hpc WHERE hpc.contactPerson = ?)

            sub.select(subPermit.get(HarvestPermit_.id))
                    .where(criteriaBuilder.equal(contactPerson, person));

            final Path<HarvestPermit> harvestPermit = root.get(Harvest_.harvestPermit);

            return harvestPermit.get(HarvestPermit_.id).in(sub);
        };
    }

    @Nonnull
    public static Specification<Harvest> permitHarvestAsList(final boolean asList) {
        return JpaSubQuery.inverseOf(HarvestPermit_.harvests)
                .exists((root, cb) -> cb.equal(root.get(HarvestPermit_.harvestsAsList), asList));
    }

    @Nonnull
    public static Specification<Harvest> emailReminderSentTimeIsNull() {
        return isNull(Harvest_.emailReminderSentTime);
    }

    @Nonnull
    public static Specification<Harvest> emailReminderSentTimeGt(@Nonnull final DateTime reminderOlderThan) {
        return olderThan(Harvest_.emailReminderSentTime, reminderOlderThan);
    }

    @Nonnull
    public static <T, U> Specification<T> equalValueWithHarvestOrObservation(
            @Nonnull final SingularAttribute<? super T, Harvest> harvestAttribute,
            @Nonnull final SingularAttribute<? super T, Observation> observationAttribute,
            @Nonnull final SingularAttribute<? super GameDiaryEntry, U> valueAttribute,
            @Nullable final U value) {

        Objects.requireNonNull(harvestAttribute, "harvestAttribute must not be null");
        Objects.requireNonNull(observationAttribute, "observationAttribute must not be null");
        Objects.requireNonNull(valueAttribute, "valueAttribute must not be null");

        return (root, query, cb) -> cb.or(
                JpaPreds.equal(cb, root.join(harvestAttribute, JoinType.LEFT).get(valueAttribute), value),
                JpaPreds.equal(cb, root.join(observationAttribute, JoinType.LEFT).get(valueAttribute), value));
    }

    @Nonnull
    public static JpaSort temporalSort(@Nullable final Direction direction) {
        return new JpaSort(direction, GameDiaryEntry_.pointOfTime, Harvest_.id);
    }
}
