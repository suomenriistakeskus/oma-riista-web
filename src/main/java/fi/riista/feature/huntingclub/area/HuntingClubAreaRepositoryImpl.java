package fi.riista.feature.huntingclub.area;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gis.zone.QGISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.query.HuntingClubAreaPoiQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.LinkedList;
import java.util.List;

@Repository
public class HuntingClubAreaRepositoryImpl implements HuntingClubAreaRepositoryCustom {

    private static final int ONE_HA_SQUARE_METRES = 10_000;

    @Resource
    private JPQLQueryFactory queryFactory;

    private JdbcTemplate jdbcTemplate;
    private HuntingClubAreaPoiQuery huntingClubAreaPoiQuery;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.huntingClubAreaPoiQuery = new HuntingClubAreaPoiQuery(jdbcTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HuntingClubArea> findByClubAndYear(final HuntingClub club,
                                                   final Integer year,
                                                   final boolean activeOnly,
                                                   final boolean includeEmpty) {
        final QHuntingClubArea AREA = QHuntingClubArea.huntingClubArea;
        final QGISZone ZONE = QGISZone.gISZone;

        final List<BooleanExpression> predicates = new LinkedList<>();
        predicates.add(AREA.club.eq(club));

        if (year != null) {
            predicates.add(AREA.huntingYear.eq(year));
        }

        if (activeOnly) {
            predicates.add(AREA.active.isTrue());
        }

        if (!includeEmpty) {
            // Minimum geometry size 1 ha
            predicates.add(ZONE.isNotNull().and(ZONE.computedAreaSize.goe(ONE_HA_SQUARE_METRES)));
        }

        return queryFactory.selectFrom(AREA)
                .leftJoin(AREA.zone, ZONE)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listPois(final long areaId) {
        return huntingClubAreaPoiQuery.listPois(areaId);
    }

    @Override
    @Transactional
    public void addPois(final long areaId, final List<Long> pois) {
        huntingClubAreaPoiQuery.insertPois(areaId, pois);
    }

    @Override
    @Transactional
    public void updatePois(final long areaId, final List<Long> pois) {
        huntingClubAreaPoiQuery.removeZoneFeatures(areaId);
        huntingClubAreaPoiQuery.insertPois(areaId, pois);
    }

    @Override
    @Transactional
    public void removeConnectionsToPoi(final long poiGroupId) {
        huntingClubAreaPoiQuery.removeConnectionsToPoi(poiGroupId);
    }
}
