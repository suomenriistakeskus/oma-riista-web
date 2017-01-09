package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.SetJoin;
import java.util.List;

public interface HarvestQuotaRepository extends BaseRepository<HarvestQuota, Long> {

    @Override
    @Query("SELECT quota FROM #{#entityName} quota" +
            " JOIN FETCH quota.harvestSeason" +
            " JOIN FETCH quota.harvestArea" +
            " WHERE quota.id IN ?1")
    List<HarvestQuota> findAll(Iterable<Long> ids);


    default HarvestQuota findByHarvestSeasonAndRhy(final long harvestSeasonId, final long rhyId) {
        return findOne((root, query, cb) -> {
            final Join<HarvestQuota, HarvestSeason> seasonToJoin = root.join(HarvestQuota_.harvestSeason, JoinType.LEFT);
            final Predicate harvestSeasonPredicate = cb.equal(seasonToJoin.get(HarvestSeason_.id), harvestSeasonId);

            final Join<HarvestQuota, HarvestArea> areaToJoin = root.join(HarvestQuota_.harvestArea, JoinType.LEFT);
            final SetJoin<HarvestArea, Riistanhoitoyhdistys> rhysToJoin = areaToJoin.join(HarvestArea_.rhys, JoinType.LEFT);
            final Predicate rhyPredicate = cb.equal(rhysToJoin.get(Organisation_.id), rhyId);

            return cb.and(harvestSeasonPredicate, rhyPredicate);
        });
    }

    default HarvestQuota findByHarvestSeasonAndRhy(final HarvestSeason harvestSeason, final Riistanhoitoyhdistys rhy) {
        return findByHarvestSeasonAndRhy(harvestSeason.getId(), rhy.getId());
    }
}
