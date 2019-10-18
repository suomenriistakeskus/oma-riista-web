package fi.riista.feature.gis.metsahallitus;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.sql.SQMhPienriista;
import fi.riista.util.GISUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class MetsahallitusPienriistaRepository {

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<MetsahallitusPienriistaDTO> findAll(final int year) {
        final SQMhPienriista MH_PIENRIISTA = SQMhPienriista.mhPienriista;

        return sqlQueryFactory
                .select(Projections.constructor(MetsahallitusPienriistaDTO.class,
                        MH_PIENRIISTA.gid,
                        Expressions.constant(year),
                        MH_PIENRIISTA.koodi,
                        MH_PIENRIISTA.nimi,
                        MH_PIENRIISTA.pintaAla.multiply(10_000)))
                .from(MH_PIENRIISTA)
                .where(MH_PIENRIISTA.vuosi.eq(year))
                .fetch();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Integer findPienriistaAlueId(final GeoLocation geoLocation, final int year) {
        final SQMhPienriista MH_PIENRIISTA = SQMhPienriista.mhPienriista;

        return sqlQueryFactory
                .select(MH_PIENRIISTA.gid)
                .from(MH_PIENRIISTA)
                .where(MH_PIENRIISTA.vuosi.eq(year), MH_PIENRIISTA.geom.intersects(GISUtils.createPointWithDefaultSRID(geoLocation)))
                .fetchFirst();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Integer findLatestYear() {
        final SQMhPienriista MH_PIENRIISTA = SQMhPienriista.mhPienriista;

        return sqlQueryFactory
                .select(MH_PIENRIISTA.vuosi.max())
                .from(MH_PIENRIISTA)
                .fetchOne();
    }
}
