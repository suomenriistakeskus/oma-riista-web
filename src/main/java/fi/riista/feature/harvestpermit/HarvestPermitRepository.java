package fi.riista.feature.harvestpermit;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.list.MooselikeHuntingYearDTO;
import fi.riista.feature.permit.decision.PermitDecision;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface HarvestPermitRepository extends BaseRepository<HarvestPermit, Long> {

    HarvestPermit findByPermitNumber(String permitNumber);

    List<HarvestPermit> findByPermitDecision(PermitDecision permitDecision);

    default List<HarvestPermit> listRhyPermitsByHuntingYearAndSpecies(long rhyId, int huntingYear, int gameSpeciesCode) {
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount spa = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return findAllAsList(permit.isMooselikePermit()
                .and(permit.hasRhyOrRelatedRhy(rhyId))
                .and(spa.matchesSpeciesAndHuntingYear(permit, gameSpeciesCode, huntingYear)));
    }

    default List<MooselikeHuntingYearDTO> listRhyMooselikeHuntingYears(final long rhyId) {
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final BooleanExpression predicate = permit.isMooselikePermit().and(permit.hasRhyOrRelatedRhy(rhyId));

        try (final Stream<HarvestPermit> stream = findAllAsStream(predicate)) {
            return MooselikeHuntingYearDTO.create(
                    stream.map(HarvestPermit::getSpeciesAmounts).flatMap(Collection::stream));
        }
    }
}
