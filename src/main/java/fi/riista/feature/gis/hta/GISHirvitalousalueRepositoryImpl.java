package fi.riista.feature.gis.hta;

import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.common.entity.GeoLocation;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class GISHirvitalousalueRepositoryImpl implements GISHirvitalousalueRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates sqlTemplates;

    @Transactional(readOnly = true)
    @Override
    public GISHirvitalousalue findByPoint(final GeoLocation geoLocation) {
        final QGISHirvitalousalue hta = QGISHirvitalousalue.gISHirvitalousalue;
        return new JPASQLQuery<>(entityManager, sqlTemplates).select(hta)
                .from(hta)
                .where(hta.geom.intersects(geoLocation.toPointGeometry()))
                .fetchOne();
    }

}
