package fi.riista.feature.harvestpermit.season;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
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

    private static BooleanExpression validOnDate(DatePath<LocalDate> begin, DatePath<LocalDate> end, LocalDate day) {
        return begin.loe(day).and(end.goe(day));
    }
}
