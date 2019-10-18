package fi.riista.feature.harvestpermit;

import com.google.common.collect.Maps;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.RiistakeskuksenAlueRepository;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

@Component
public class HarvestPermitIdFetchService {

    @Resource
    private RiistakeskuksenAlueRepository rkaRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public Map<Long, Set<Long>> findMooselikePermitIdsGroupedByRkaId(final int huntingYear, final int gameSpeciesCode) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;

        return jpqlQueryFactory
                .from(SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .join(SPECIES_AMOUNT.harvestPermit, PERMIT)
                .join(PERMIT.rhy, RHY)
                .join(RHY.parentOrganisation, RKA._super)
                .where(PERMIT.isMooselikePermit(),
                        PERMIT.permitYear.eq(huntingYear),
                        SPECIES.officialCode.eq(gameSpeciesCode))
                .transform(groupBy(RKA.id).as(set(PERMIT.id)));
    }

    @Transactional(readOnly = true)
    public Map<RiistakeskuksenAlue, Set<Long>> getMooselikePermitIdsGroupedByRka(final int huntingYear,
                                                                                 final int speciesCode) {

        final Map<Long, Set<Long>> permitIdsByRkaId = findMooselikePermitIdsGroupedByRkaId(huntingYear, speciesCode);

        return Maps.toMap(rkaRepository.findAll(), rka -> {

            final Set<Long> permitIds = permitIdsByRkaId.get(rka.getId());

            return Optional.ofNullable(permitIds).orElseGet(Collections::emptySet);
        });
    }
}
