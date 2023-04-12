package fi.riista.feature.gis.hta;

import com.querydsl.jpa.JPQLQueryFactory;
import io.vavr.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

import static fi.riista.util.Collect.tuplesToMap;

@Repository
public class RHYHirvitalousalueRepository {
    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public Map<RHYHirvitalousalueId, Double> getLandAreaSizes() {
        final QRHYHirvitalousalue RHY_HTA = QRHYHirvitalousalue.rHYHirvitalousalue;

        return jpqlQueryFactory.selectFrom(RHY_HTA)
                .where(RHY_HTA.landAreaSize.isNotNull())
                .fetch().stream()
                .map(rh -> Tuple.of(rh.getId(), rh.getLandAreaSize()))
                .collect(tuplesToMap());
    }

}
