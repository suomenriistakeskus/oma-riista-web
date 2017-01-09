package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.gamediary.GameDiaryEntry_;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.season.HarvestArea_;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount_;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.harvestpermit.season.HarvestQuota_;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields_;
import fi.riista.feature.harvestpermit.season.HarvestSeason_;
import fi.riista.feature.organization.Organisation_;
import fi.riista.util.jpa.JpaPreds;
import fi.riista.util.jpa.JpaSubQuery;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Collection;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static fi.riista.util.jpa.JpaSpecs.likeIgnoreCase;
import static fi.riista.util.jpa.JpaSpecs.pathToIdExists;

public class HarvestReportSpecs {

    private HarvestReportSpecs() {
    }

    public static Specification<HarvestReport> withPermitNumber(final String permitNumber) {
        return equal(HarvestReport_.harvestPermit, HarvestPermit_.permitNumber, permitNumber);
    }

    public static Specification<HarvestReport> withFieldsId(final Long fieldsId) {
        return pathToIdExists(Harvest_.harvestReport, Harvest_.harvestReportFields, HarvestReportFields_.id, fieldsId);
    }

    public static Specification<HarvestReport> withHarvestSeasonId(final Long seasonId) {
        return pathToIdExists(Harvest_.harvestReport, Harvest_.harvestSeason, HarvestSeason_.id, seasonId);
    }

    public static Specification<HarvestReport> withRhyId(final Long rhyId) {
        return pathToIdExists(Harvest_.harvestReport, GameDiaryEntry_.rhy, Organisation_.id, rhyId);
    }

    public static Specification<HarvestReport> withStates(final Collection<HarvestReport.State> states) {
        return inCollection(HarvestReport_.state, states);
    }

    public static Specification<HarvestReport> withDescriptionLike(final String s) {
        return likeIgnoreCase(HarvestReport_.description, s);
    }

    public static Specification<HarvestReport> withPermitRhyId(final Long rhyId) {
        return (root, query, cb) -> {
            final Join<HarvestReport, HarvestPermit> permitJoined =
                    root.join(HarvestReport_.harvestPermit, JoinType.LEFT);

            return cb.equal(permitJoined.get(HarvestPermit_.rhy).get(Organisation_.id), rhyId);
        };
    }

    public static Specification<HarvestReport> withQuotaAreaId(final Long harvestAreaId) {
        return pathToIdExists(
                Harvest_.harvestReport, Harvest_.harvestQuota, HarvestQuota_.harvestArea, HarvestArea_.id, harvestAreaId);
    }

    public static Specification<HarvestReport> withAreaId(final Long areaId) {
        return pathToIdExists(
                Harvest_.harvestReport, GameDiaryEntry_.rhy, Organisation_.parentOrganisation, Organisation_.id, areaId);
    }

    public static Specification<HarvestReport> withHarvestBetween(final LocalDate begin, final LocalDate end) {
        return JpaSubQuery.inverseOf(Harvest_.harvestReport)
                .exists((root, cb) -> JpaPreds.withinInterval(cb, root.get(GameDiaryEntry_.pointOfTime), begin, end));
    }

    public static Specification<HarvestReport> hasEndOfHuntingReportAndFieldsSpeciesIsPermitSpecies(final Long fieldsId) {
        return (root, query, cb) -> {
            final Subquery<Integer> permitQuery = query.subquery(Integer.class);
            final Root<HarvestPermit> permitRoot = permitQuery.from(HarvestPermit.class);

            final ListJoin<HarvestPermit, HarvestPermitSpeciesAmount> speciesAmounts = permitRoot.join(HarvestPermit_.speciesAmounts);
            final Path<GameSpecies> speciesAmountsSpecies = speciesAmounts.get(HarvestPermitSpeciesAmount_.gameSpecies);

            final Subquery<Integer> fieldsQuery = query.subquery(Integer.class);
            final Root<HarvestReportFields> fieldsRoot = fieldsQuery.from(HarvestReportFields.class);

            final Predicate permitSpeciesEqualToFieldsSpecies = cb.and(
                    cb.equal(fieldsRoot.get(HarvestReportFields_.id), fieldsId),
                    cb.equal(fieldsRoot.get(HarvestReportFields_.species), speciesAmountsSpecies));
            final Predicate fieldsExists = cb.exists(fieldsQuery.select(cb.literal(1)).where(permitSpeciesEqualToFieldsSpecies));

            return cb.exists(permitQuery
                    .select(cb.literal(1))
                    .where(cb.and(
                            cb.equal(permitRoot.join(HarvestPermit_.endOfHuntingReport), permitQuery.correlate(root)),
                            fieldsExists))
            );
        };
    }
}
