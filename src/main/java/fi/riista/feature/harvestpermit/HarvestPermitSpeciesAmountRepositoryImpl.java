package fi.riista.feature.harvestpermit;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
public class HarvestPermitSpeciesAmountRepositoryImpl implements HarvestPermitSpeciesAmountRepositoryCustom {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    @Override
    public Map<Integer, HarvestPermitSpeciesAmount> findSpeciesCodeToSpeciesAmountByHarvestPermit(final HarvestPermit harvestPermit) {
        final QHarvestPermit HARVEST_PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount HARVEST_PERMIT_SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies GAME_SPECIES = QGameSpecies.gameSpecies;

        return jpaQueryFactory.select(HARVEST_PERMIT_SPECIES_AMOUNT, GAME_SPECIES)
                .from(HARVEST_PERMIT_SPECIES_AMOUNT)
                .join(HARVEST_PERMIT_SPECIES_AMOUNT.gameSpecies, GAME_SPECIES)
                .join(HARVEST_PERMIT_SPECIES_AMOUNT.harvestPermit, HARVEST_PERMIT)
                .where(HARVEST_PERMIT.eq(harvestPermit))
                .transform(groupBy(GAME_SPECIES.officialCode).as(HARVEST_PERMIT_SPECIES_AMOUNT));
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, Set<HarvestPermitSpeciesAmount>> findAllByPermitId(Collection<HarvestPermit> harvestPermits) {
        final QHarvestPermit HARVEST_PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount HARVEST_PERMIT_SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return jpaQueryFactory
                .from(HARVEST_PERMIT_SPECIES_AMOUNT)
                .join(HARVEST_PERMIT_SPECIES_AMOUNT.harvestPermit, HARVEST_PERMIT)
                .where(HARVEST_PERMIT_SPECIES_AMOUNT.harvestPermit.in(harvestPermits))
                .transform(groupBy(HARVEST_PERMIT.id).as(GroupBy.set(HARVEST_PERMIT_SPECIES_AMOUNT)));
    }
}
