package fi.riista.feature.gis.hta;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class GISHirvitalousalueRepositoryImpl implements GISHirvitalousalueRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates sqlTemplates;

    @Override
    @Transactional(readOnly = true)
    public HirvitalousalueDTO getWithoutGeometry(final int htaId) {
        final QGISHirvitalousalue HTA = QGISHirvitalousalue.gISHirvitalousalue;
        return jpqlQueryFactory.select(createDTOProjection(HTA))
                .from(HTA)
                .where(HTA.id.eq(htaId))
                .fetchOne();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HirvitalousalueDTO> listWithoutGeometry() {
        final QGISHirvitalousalue HTA = QGISHirvitalousalue.gISHirvitalousalue;
        return jpqlQueryFactory.select(createDTOProjection(HTA)).from(HTA).fetch();
    }

    @Nonnull
    private static ConstructorExpression<HirvitalousalueDTO> createDTOProjection(final QGISHirvitalousalue HTA) {
        return Projections.constructor(HirvitalousalueDTO.class, HTA.id, HTA.nameFinnish, HTA.nameSwedish, HTA.number);
    }

    @Override
    @Transactional(readOnly = true)
    public GISHirvitalousalue findByPoint(final GeoLocation geoLocation) {
        final QGISHirvitalousalue hta = QGISHirvitalousalue.gISHirvitalousalue;
        return new JPASQLQuery<>(entityManager, sqlTemplates).select(hta)
                .from(hta)
                .where(hta.geom.intersects(geoLocation.toPointGeometry()))
                .fetchOne();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HirvitalousalueDTO> findByRHY(final Riistanhoitoyhdistys rhy) {
        final QGISHirvitalousalue hta = QGISHirvitalousalue.gISHirvitalousalue;
        final QRHYHirvitalousalue rhyHTA = QRHYHirvitalousalue.rHYHirvitalousalue;

        return jpqlQueryFactory.select(createDTOProjection(hta))
                .from(hta)
                .innerJoin(hta.rhyHTAs, rhyHTA)
                .where(rhyHTA.rhy.eq(rhy))
                .fetch();
    }

}
