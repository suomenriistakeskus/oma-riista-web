package fi.riista.feature.harvestpermit.report;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.fields.QHarvestReportFields;
import fi.riista.feature.harvestpermit.season.QHarvestSeason;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Objects;

@Component
public class HarvestReportRequirementsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private GISQueryService gisQueryService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isHarvestReportRequired(final GameSpecies species,
                                           final LocalDate pointOfTime,
                                           final GeoLocation location,
                                           @Nullable final HarvestPermit permit) {

        Objects.requireNonNull(species, "species must not be null");
        Objects.requireNonNull(pointOfTime, "pointOfTime must not be null");
        Objects.requireNonNull(location, "location must not be null");

        return !pointOfTime.isBefore(HarvestReport.REQUIRED_SINCE)
                && !(permit != null && permit.isHarvestsAsList())
                && findRhy(location) != null
                && query(species, pointOfTime);
    }

    private Riistanhoitoyhdistys findRhy(final GeoLocation location) {
        return gisQueryService.findRhyByLocation(location);
    }

    private boolean query(final GameSpecies species, final LocalDate date) {
        final QGameSpecies gameSpecies = QGameSpecies.gameSpecies;
        final QHarvestReportFields hf = QHarvestReportFields.harvestReportFields;
        final QHarvestSeason hs = QHarvestSeason.harvestSeason;

        final BooleanExpression permitExists = JPAExpressions.selectFrom(hf)
                .where(hf.usedWithPermit.eq(true),
                        hf.freeHuntingAlso.eq(false),
                        hf.species.eq(species))
                .exists();

        final BooleanExpression dates1 = hs.beginDate.loe(date).and(hs.endDate.goe(date));
        final BooleanExpression dates2 = hs.beginDate2.loe(date).and(hs.endDate2.goe(date));
        final BooleanExpression seasonExists = JPAExpressions.selectFrom(hs)
                .join(hs.fields, hf)
                .where(hf.species.eq(species), dates1.or(dates2))
                .exists();

        final JPAQuery<?> query = new JPAQuery<>(entityManager).from(gameSpecies).where(permitExists.or(seasonExists));
        return query.fetchCount() > 0;
    }

}
