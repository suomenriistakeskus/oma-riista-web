package fi.riista.feature.harvestpermit.season;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
public class HarvestSeasonRepositoryImpl implements HarvestSeasonRepositoryCustom {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<HarvestSeason> getAllSeasonsForHarvest(final @Nonnull GameSpecies species,
                                                       final @Nonnull LocalDate harvestDate) {
        Objects.requireNonNull(species, "species is null");
        Objects.requireNonNull(harvestDate, "harvestDate is null");

        final QHarvestSeason SEASON = QHarvestSeason.harvestSeason;

        final BooleanExpression dates1 = validOnDate(SEASON.beginDate, SEASON.endDate, harvestDate);
        final BooleanExpression dates2 = validOnDate(SEASON.beginDate2, SEASON.endDate2, harvestDate);

        return queryFactory.selectFrom(SEASON).where(SEASON.species.eq(species), dates1.or(dates2)).fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestSeason> listAllForReportingFetchSpecies(final LocalDate activeOnDate) {
        final QHarvestSeason SEASON = QHarvestSeason.harvestSeason;
        final JPQLQuery<HarvestSeason> rootQuery = queryFactory.selectFrom(SEASON)
                .join(SEASON.species, QGameSpecies.gameSpecies).fetchJoin();

        if (activeOnDate == null) {
            return rootQuery.fetch();
        }

        final BooleanExpression dates1 = validOnDate(SEASON.beginDate, SEASON.endOfReportingDate, activeOnDate);
        final BooleanExpression dates2 = validOnDate(SEASON.beginDate2, SEASON.endOfReportingDate2, activeOnDate);

        return rootQuery.where(dates1.or(dates2)).fetch();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, noRollbackFor = RuntimeException.class)
    public List<HarvestSeason> findBySeasonInHuntingYear(final int huntingYear) {
        final LocalDate huntingYearBeginDate = DateUtil.huntingYearBeginDate(huntingYear);
        final LocalDate huntingYearEndDate = DateUtil.huntingYearEndDate(huntingYear);

        final QHarvestSeason SEASON = QHarvestSeason.harvestSeason;
        return queryFactory.selectFrom(SEASON)
                .where(SEASON.beginDate.between(huntingYearBeginDate, huntingYearEndDate))
                .fetch();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, noRollbackFor = RuntimeException.class)
    public List<HarvestSeason> findOverlappingSeasons(final Long id, final GameSpecies species,
                                                      final LocalDate beginDate, final LocalDate endDate,
                                                      final LocalDate beginDate2, final LocalDate endDate2) {
        final QHarvestSeason SEASON = QHarvestSeason.harvestSeason;

        final BooleanExpression ignoreSameSeason = F.mapNullable(id, SEASON.id::ne);
        final BooleanExpression beginDateInFirstPeriod = validOnDate(SEASON.beginDate, SEASON.endDate, beginDate);
        final BooleanExpression beginDateInSecondPeriod = validOnDate(SEASON.beginDate2, SEASON.endDate2, beginDate);

        final BooleanExpression endDateInFirstPeriod = validOnDate(SEASON.beginDate, SEASON.endDate, endDate);
        final BooleanExpression endDateInSecondPeriod = validOnDate(SEASON.beginDate2, SEASON.endDate2, endDate);

        final BooleanExpression beginDate2InFirstPeriod = validOnDate(SEASON.beginDate, SEASON.endDate, beginDate2);
        final BooleanExpression beginDate2InSecondPeriod = validOnDate(SEASON.beginDate2, SEASON.endDate2, beginDate2);

        final BooleanExpression endDate2InFirstPeriod = validOnDate(SEASON.beginDate, SEASON.endDate, endDate2);
        final BooleanExpression endDate2InSecondPeriod = validOnDate(SEASON.beginDate2, SEASON.endDate2, endDate2);

        return queryFactory.selectFrom(SEASON).where(SEASON.species.eq(species),
                ignoreSameSeason,
                beginDateInFirstPeriod.or(beginDateInSecondPeriod)
                        .or(endDateInFirstPeriod).or(endDateInSecondPeriod)
                        .or(beginDate2InFirstPeriod).or(beginDate2InSecondPeriod)
                        .or(endDate2InFirstPeriod).or(endDate2InSecondPeriod))
                .fetch();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, noRollbackFor = RuntimeException.class)
    public List<GameSpecies> findGameSpeciesBySeasons(final Collection<HarvestSeason> seasons) {
        if (seasons.isEmpty()) {
            return Collections.emptyList();
        }

        final QHarvestSeason SEASON = QHarvestSeason.harvestSeason;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        return queryFactory.select(SPECIES)
                .from(SEASON)
                .join(SEASON.species, SPECIES)
                .where(SEASON.in(seasons))
                .fetch();
    }
    private static BooleanExpression validOnDate(DatePath<LocalDate> begin, DatePath<LocalDate> end, LocalDate day) {
        return F.mapNullable(day, (d) -> begin.loe(d).and(end.goe(d)));
    }
}
