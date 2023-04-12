package fi.riista.feature.gamediary.summary;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.gamediary.srva.QSrvaEvent;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.Interval;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

public class AdminGameSummaryPredicates {

    @Nonnull
    public static BooleanBuilder createHarvestPredicate(final Interval interval,
                                                        final GameSpecies gameSpecies,
                                                        final OrganisationType organisationType,
                                                        final String organisationOfficialCode,
                                                        final boolean harvestReportOnly,
                                                        final boolean officialHarvestOnly) {
        final QHarvest HARVEST = QHarvest.harvest;
        return new BooleanBuilder()
                .and(harvestReportOnly
                        ? HARVEST.harvestReportState.isNotNull()
                        .and(HARVEST.harvestReportState.ne(HarvestReportState.REJECTED))
                        : HARVEST.harvestReportState.isNull()
                        .or(HARVEST.harvestReportState.ne(HarvestReportState.REJECTED)))
                .and(officialHarvestOnly
                        ? HARVEST.harvestReportState.eq(HarvestReportState.APPROVED)
                        .or(HARVEST.huntingDayOfGroup.isNotNull())
                        : null)
                .and(organisationPredicate(HARVEST.rhy, organisationType, organisationOfficialCode))
                .and(interval != null ? HARVEST.pointOfTime.between(
                        interval.getStart(),
                        interval.getEnd()) : null)
                .and(gameSpecies != null ? HARVEST.species.eq(gameSpecies) : null);
    }

    @Nonnull
    public static BooleanBuilder createObservationPredicate(final Interval interval,
                                                      final GameSpecies gameSpecies,
                                                      final OrganisationType organisationType,
                                                      final String organisationOfficialCode) {
        final QObservation OBSERVATION = QObservation.observation;
        return new BooleanBuilder()
                .and(organisationPredicate(OBSERVATION.rhy, organisationType, organisationOfficialCode))
                .and(interval != null ? OBSERVATION.pointOfTime.between(
                        interval.getStart(),
                        interval.getEnd()) : null)
                .and(gameSpecies != null ? OBSERVATION.species.eq(gameSpecies) : null);
    }

    public static BooleanBuilder createSrvaPredicate(final Interval interval,
                                               final GameSpecies gameSpecies,
                                               final OrganisationType organisationType,
                                               final String organisationOfficialCode) {
        final QSrvaEvent SRVA = QSrvaEvent.srvaEvent;

        return new BooleanBuilder()
                .and(organisationPredicate(SRVA.rhy, organisationType, organisationOfficialCode))
                .and(interval != null ? SRVA.pointOfTime.between(
                        interval.getStart(),
                        interval.getEnd()) : null)
                .and(gameSpecies != null ? SRVA.species.eq(gameSpecies) : null);
    }

    public static Predicate organisationPredicate(final QRiistanhoitoyhdistys RHY,
                                            final OrganisationType organisationType,
                                            final String organisationOfficialCode) {
        if (StringUtils.hasText(organisationOfficialCode)) {
            if (organisationType == OrganisationType.RHY) {
                return RHY.officialCode.eq(organisationOfficialCode);
            } else if (organisationType == OrganisationType.RKA) {
                return RHY.in(rkaSubQuery(organisationOfficialCode));
            }
        }

        // Inside Finland
        return RHY.isNotNull();
    }


    private static JPQLQuery<Riistanhoitoyhdistys> rkaSubQuery(final String officialCode) {
        final QOrganisation RKA = new QOrganisation("rka");
        final QRiistanhoitoyhdistys RHY = new QRiistanhoitoyhdistys("rka_rhy");

        return JPAExpressions.selectFrom(RHY)
                .where(RHY.organisationType.eq(OrganisationType.RHY),
                        RHY.parentOrganisation.eq(
                                JPAExpressions.selectFrom(RKA).where(
                                        RKA.organisationType.eq(OrganisationType.RKA),
                                        RKA.officialCode.eq(officialCode))));
    }

    private AdminGameSummaryPredicates() {
        throw new AssertionError();
    }
}
