package fi.riista.feature.harvestpermit;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.feature.harvestpermit.search.HarvestPermitDecisionOrigin;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecision_;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaSpecs;
import fi.riista.util.jpa.JpaSubQuery;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

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
            equal(HarvestPermit_.permitTypeCode, PermitTypeCode.MOOSELIKE);

    public static final Specification<HarvestPermit> IS_NOT_ANY_MOOSELIKE_PERMIT = and(
            notEqual(HarvestPermit_.permitTypeCode, PermitTypeCode.MOOSELIKE),
            notEqual(HarvestPermit_.permitTypeCode, PermitTypeCode.MOOSELIKE_AMENDMENT));

    public static final Specification<HarvestPermit> IS_NOT_MOOSELIKE_AMENDMENT_PERMIT =
            notEqual(HarvestPermit_.permitTypeCode, PermitTypeCode.MOOSELIKE_AMENDMENT);

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

    public static Specification<HarvestPermit> withDecisionGrantStatus(@Nonnull final List<GrantStatus> grantStatuses) {
        return (root, query, cb) -> {
            final Join<HarvestPermit, PermitDecision> decision = root.join(HarvestPermit_.permitDecision, JoinType.LEFT);
            final Path<GrantStatus> grantStatusPath = decision.get(PermitDecision_.grantStatus);
            final List<Predicate> grantStatusPredicate = new ArrayList<>();
            grantStatuses.forEach(grantStatus -> grantStatusPredicate.add(cb.equal(grantStatusPath, grantStatus)));
            return cb.or(grantStatusPredicate.toArray(new Predicate[]{}));
        };
    }

    public static Specification<HarvestPermit> withYear(@Nonnull final String year) {
        return (root, query, cb) -> beginsWithIgnoreCase(cb, root.get(HarvestPermit_.permitNumber), year);
    }

    public static Specification<HarvestPermit> withAreaId(@Nonnull final Long areaId) {
        return joinPathToId(HarvestPermit_.rhy, Organisation_.parentOrganisation, Organisation_.id, areaId);
    }

    public static Specification<HarvestPermit> isPermitContactPerson(final Person person) {
        //person is null when current user is moderator or admin
        if (person == null) {
            return disjunction();
        }

        final Specification<HarvestPermit> contactPerson = pathToValueExists(
                HarvestPermitContactPerson_.harvestPermit, HarvestPermitContactPerson_.contactPerson, person);

        return Specification
                .where(equal(HarvestPermit_.originalContactPerson, person))
                .or(contactPerson);
    }

    public static Specification<HarvestPermit> validWithinHuntingYear(final int huntingYear) {
        final Interval huntingYearInterval = DateUtil.huntingYearInterval(huntingYear);

        return JpaSubQuery.inverseOf(HarvestPermitSpeciesAmount_.harvestPermit).exists((root, cb) -> cb.or(
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.beginDate), huntingYearInterval),
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.endDate), huntingYearInterval),
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.beginDate2), huntingYearInterval),
                overlapsInterval(cb, root.get(HarvestPermitSpeciesAmount_.endDate2), huntingYearInterval)));
    }

    public static Specification<HarvestPermit> passed(final LocalDate date) {
        return JpaSubQuery.inverseOf(HarvestPermitSpeciesAmount_.harvestPermit).exists((root, cb) -> {
            final Expression<LocalDate> endDate = cb.coalesce(root.get(HarvestPermitSpeciesAmount_.endDate2),
                    root.get(HarvestPermitSpeciesAmount_.endDate));
            return cb.lessThan(endDate, date);
        });
    }

    public static Specification<HarvestPermit> active(final LocalDate date) {
        return JpaSubQuery.inverseOf(HarvestPermitSpeciesAmount_.harvestPermit).exists((root, cb) -> {
            final Predicate beginDate = cb.lessThanOrEqualTo(root.get(HarvestPermitSpeciesAmount_.beginDate), date);
            final Predicate endDate = cb.greaterThanOrEqualTo(cb.coalesce(root.get(HarvestPermitSpeciesAmount_.endDate2),
                    root.get(HarvestPermitSpeciesAmount_.endDate)), date);
            return cb.and(beginDate, endDate);
        });
    }

    public static Specification<HarvestPermit> future(final LocalDate date) {
        return JpaSubQuery.inverseOf(HarvestPermitSpeciesAmount_.harvestPermit).exists((root, cb) ->
                cb.greaterThan(root.get(HarvestPermitSpeciesAmount_.beginDate), date));
    }


    private HarvestPermitSpecs() {
        throw new AssertionError();
    }

    public static Specification<HarvestPermit> withPermitTypeCode(final String permitTypeCode) {
        return JpaSpecs.equal(HarvestPermit_.permitTypeCode, permitTypeCode);
    }

    public static Specification<HarvestPermit> withDecisionOrigin(final HarvestPermitDecisionOrigin origin) {
        switch (origin){
            case LUPAHALLINTA:
                return JpaSpecs.isNull(HarvestPermit_.permitDecision);
            case OMA_RIISTA:
                return JpaSpecs.isNotNull(HarvestPermit_.permitDecision);
            default:
                throw new IllegalArgumentException("Unexpected value: " + origin);
        }
    }
}
