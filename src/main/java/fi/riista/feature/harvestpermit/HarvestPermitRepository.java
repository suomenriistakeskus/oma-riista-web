package fi.riista.feature.harvestpermit;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.common.repository.NativeQueries;
import fi.riista.feature.huntingclub.permit.MooselikeHuntingYearDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface HarvestPermitRepository extends BaseRepository<HarvestPermit, Long> {

    HarvestPermit findByPermitNumber(String permitNumber);

    Page<HarvestPermit> findByRhy(Riistanhoitoyhdistys rhy, Pageable page);

    @Query(value = NativeQueries.COUNT_PERMITS_REQUIRING_ACTION, nativeQuery = true)
    long countPermitsRequiringAction(@Param("personId") long personId);

    default List<HarvestPermit> listRhyPermitsByHuntingYearAndSpecies(long rhyId, int huntingYear, int gameSpeciesCode) {
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount spa = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return findAllAsList(permit.isMooselikePermit()
                .and(permit.hasRhyOrRelatedRhy(rhyId))
                .and(spa.matchesSpeciesAndHuntingYear(permit, gameSpeciesCode, huntingYear)));
    }

    default List<MooselikeHuntingYearDTO> listRhyMooselikeHuntingYears(long rhyId) {
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final BooleanExpression predicate = permit.isMooselikePermit().and(permit.hasRhyOrRelatedRhy(rhyId));

        return MooselikeHuntingYearDTO.create(
                findAllAsStream(predicate).map(HarvestPermit::getSpeciesAmounts).flatMap(Collection::stream));
    }
}
