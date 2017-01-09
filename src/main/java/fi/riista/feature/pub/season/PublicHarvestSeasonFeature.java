package fi.riista.feature.pub.season;

import com.google.common.collect.Maps;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestQuota_;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.HarvestReport_;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.QHarvestSeason;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PublicHarvestSeasonFeature {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @Cacheable(value = "publicSeasonsWithQuotas")
    public List<PublicHarvestSeasonDTO> listSeasonsWithQuotas(Boolean onlyActive) {
        List<HarvestSeason> activeSeasons = getHarvestSeasons(onlyActive);
        final Map<Long, Integer> quotaIdToUsedQuota = fetchUsedQuotas();

        return F.mapNonNullsToList(activeSeasons, input -> {
            return PublicHarvestSeasonDTO.create(Objects.requireNonNull(input), quotaIdToUsedQuota);
        });
    }

    private List<HarvestSeason> getHarvestSeasons(Boolean onlyActive) {
        QHarvestSeason season = QHarvestSeason.harvestSeason;
        BooleanExpression q = season.quotas.isNotEmpty();
        if (Objects.equals(true, onlyActive)) {
            LocalDate date = DateUtil.today();
            BooleanExpression range1 = season.beginDate.loe(date).and(season.endDate.goe(date));
            BooleanExpression range2 = season.beginDate2.loe(date).and(season.endDate2.goe(date));
            q = q.and(range1.or(range2));
        }
        return new JPAQuery<HarvestSeason>(entityManager).from(season).where(q).fetch();
    }

    private Map<Long, Integer> fetchUsedQuotas() {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> query = builder.createTupleQuery();
        final Root<HarvestReport> root = query.from(HarvestReport.class);

        Join<Harvest, HarvestQuota> joinedQuotas = root.join(HarvestReport_.harvests).join(Harvest_.harvestQuota, JoinType.LEFT);
        Path<Long> quotaId = joinedQuotas.get(HarvestQuota_.id);

        Expression<Long> count = builder.count(root.get(HarvestReport_.id));

        Predicate onlyApproved = builder.equal(root.get(HarvestReport_.state), HarvestReport.State.APPROVED);
        Predicate quotaNotNull = builder.isNotNull(quotaId);

        CriteriaQuery<Tuple> q = query
                .multiselect(quotaId, count)
                .where(onlyApproved, quotaNotNull)
                .groupBy(quotaId);
        return map(entityManager.createQuery(q).getResultList());
    }

    private static Map<Long, Integer> map(List<Tuple> resultList) {
        Map<Long, Integer> map = Maps.newHashMap();
        for (Tuple t : resultList) {
            Long quotaId = t.get(0, Long.class);
            Integer usedQuota = t.get(1, Long.class).intValue();
            map.put(quotaId, usedQuota);
        }
        return map;
    }
}
