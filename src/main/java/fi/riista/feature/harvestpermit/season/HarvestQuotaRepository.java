package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.List;

public interface HarvestQuotaRepository extends BaseRepository<HarvestQuota, Long>{

    @Query("SELECT quota FROM #{#entityName} quota" +
            " JOIN FETCH quota.harvestSeason" +
            " JOIN FETCH quota.harvestArea" +
            " WHERE quota.id IN ?1")
    List<HarvestQuota> findAll(Iterable<Long> ids);

    default HarvestQuota findByHarvestSeasonAndArea(final HarvestSeason harvestSeason, final HarvestArea harvestArea) {
        return findOne((root, query, cb) -> {
            final Join<HarvestQuota, HarvestSeason> seasonToJoin = root.join(HarvestQuota_.harvestSeason, JoinType.LEFT);
            final Join<HarvestQuota, HarvestArea> areaToJoin = root.join(HarvestQuota_.harvestArea, JoinType.LEFT);

            return cb.and(cb.equal(seasonToJoin, harvestSeason), cb.equal(areaToJoin, harvestArea));
        }).orElse(null);
    }
}
