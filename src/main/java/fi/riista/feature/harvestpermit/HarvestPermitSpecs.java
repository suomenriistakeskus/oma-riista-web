package fi.riista.feature.harvestpermit;

import fi.riista.feature.gamediary.GameDiaryEntry_;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.HarvestReport_;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaSubQuery;
import org.joda.time.Interval;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;

import static fi.riista.util.jpa.JpaPreds.beginsWithIgnoreCase;
import static fi.riista.util.jpa.JpaPreds.overlapsInterval;
import static fi.riista.util.jpa.JpaSpecs.and;
import static fi.riista.util.jpa.JpaSpecs.disjunction;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.hasRelationWithId;
import static fi.riista.util.jpa.JpaSpecs.joinPathToId;
import static fi.riista.util.jpa.JpaSpecs.notEqual;
import static fi.riista.util.jpa.JpaSpecs.pathToValueExists;

public final class HarvestPermitSpecs {

    public static final Specification<HarvestPermit> IS_MOOSELIKE_PERMIT =
            equal(HarvestPermit_.permitTypeCode, HarvestPermit.MOOSELIKE_PERMIT_TYPE);

    public static final Specification<HarvestPermit> IS_NOT_ANY_MOOSELIKE_PERMIT = and(
            notEqual(HarvestPermit_.permitTypeCode, HarvestPermit.MOOSELIKE_PERMIT_TYPE),
            notEqual(HarvestPermit_.permitTypeCode, HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE));

    public static Specification<HarvestPermit> withPermitNumber(@Nonnull final String permitNumber) {
        return equal(HarvestPermit_.permitNumber, permitNumber);
    }

    public static Specification<HarvestPermit> withRhyId(@Nonnull final Long rhyId) {
        return hasRelationWithId(HarvestPermit_.rhy, Organisation_.id, rhyId);
    }

    public static Specification<HarvestPermit> withSpeciesCode(@Nonnull final Integer speciesCode) {
        return JpaSubQuery.inverseOf(HarvestPermitSpeciesAmount_.harvestPermit)
                .exists((root, cb) -> {
                    final Join<HarvestPermitSpeciesAmount, GameSpecies> subSpecies =
                            root.join(HarvestPermitSpeciesAmount_.gameSpecies);
                    final Path<Integer> speciesCodePath = subSpecies.get(GameSpecies_.officialCode);
                    return cb.equal(speciesCodePath, speciesCode);
                });
    }

    public static Specification<HarvestPermit> withYear(@Nonnull final String year) {
        return (root, query, cb) -> beginsWithIgnoreCase(cb, root.get(HarvestPermit_.permitNumber), year);
    }

    public static Specification<HarvestPermit> withAreaId(@Nonnull final Long areaId) {
        return joinPathToId(HarvestPermit_.rhy, Organisation_.parentOrganisation, Organisation_.id, areaId);
    }

    public static Specification<HarvestPermit> harvestReportNotDone() {
        return JpaSubQuery.inverseOf(HarvestReport_.harvestPermit)
                .notExists((root, cb) -> cb.notEqual(root.get(HarvestReport_.state), HarvestReport.State.DELETED));
    }

    public static Specification<HarvestPermit> isPermitContactPerson(final Person person) {
        //person is null when current user is moderator or admin
        if (person == null) {
            return disjunction();
        }

        final Specification<HarvestPermit> contactPerson = pathToValueExists(
                HarvestPermitContactPerson_.harvestPermit, HarvestPermitContactPerson_.contactPerson, person);

        return Specifications
                .where(equal(HarvestPermit_.originalContactPerson, person))
                .or(contactPerson);
    }

    public static Specification<HarvestPermit> withHarvestAuthor(final Person author) {
        return pathToValueExists(Harvest_.harvestPermit, GameDiaryEntry_.author, author);
    }

    public static Specification<HarvestPermit> withHarvestShooter(final Person shooter) {
        return pathToValueExists(Harvest_.harvestPermit, Harvest_.actualShooter, shooter);
    }

    public static Specification<HarvestPermit> validWithinHuntingYear(final int huntingYear) {
        final Interval huntingYearInterval = DateUtil.huntingYearInterval(huntingYear);

        return JpaSubQuery.inverseOf(HarvestPermitSpeciesAmount_.harvestPermit).exists((root, cb) -> cb.or(
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.beginDate), huntingYearInterval),
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.endDate), huntingYearInterval),
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.beginDate2), huntingYearInterval),
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.endDate2), huntingYearInterval)));
    }

    private HarvestPermitSpecs() {
        throw new AssertionError();
    }

}
