package fi.riista.feature.gamediary.srva;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.season.QHarvestArea;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.huntingYearBeginDate;
import static fi.riista.util.DateUtil.huntingYearEndDate;
import static fi.riista.util.DateUtil.toDateTimeNullSafe;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

@Repository
public class SrvaEventRepositoryImpl implements SrvaEventRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Integer, List<SrvaEvent>> findBySpeciesCodeAndPointOfTime(Collection<Integer> speciesCodes, int huntingYear) {
        if (requireNonNull(speciesCodes).isEmpty()) {
            return emptyMap();
        }

        final QSrvaEvent EVENT = QSrvaEvent.srvaEvent;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final DateTime beginTime = toDateTimeNullSafe(huntingYearBeginDate(huntingYear));
        final DateTime endTime = toDateTimeNullSafe(huntingYearEndDate(huntingYear)).plusDays(1).minusMillis(1);

        return jpqlQueryFactory.from(EVENT)
                .innerJoin(EVENT.species, SPECIES)
                .where(SPECIES.officialCode.in(speciesCodes)
                        .and(EVENT.pointOfTime.between(beginTime, endTime))
                        .and(EVENT.eventResult.ne(SrvaResultEnum.UNDUE_ALARM).or(EVENT.eventResult.isNull())))
                .transform(GroupBy.groupBy(SPECIES.officialCode).as(GroupBy.list(EVENT)));
    }
}
