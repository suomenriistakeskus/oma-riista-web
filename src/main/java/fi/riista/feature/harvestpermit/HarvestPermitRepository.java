package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.list.MooselikeHuntingYearDTO;
import fi.riista.feature.permit.decision.PermitDecision;

import java.util.List;

public interface HarvestPermitRepository extends BaseRepository<HarvestPermit, Long>, HarvestPermitRepositoryCustom{

    HarvestPermit findByPermitNumber(String permitNumber);

    List<HarvestPermit> findByPermitDecision(PermitDecision permitDecision);

    default List<HarvestPermit> findMooselikePermits(final int huntingYear, final int gameSpeciesCode) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return findAllAsList(PERMIT.isMooselikePermit()
                .and(PERMIT.matchesPermitYear(huntingYear))
                .and(SPECIES_AMOUNT.matchesSpecies(PERMIT, gameSpeciesCode)));
    }

    default List<HarvestPermit> findMooselikePermits(final long rhyId,
                                                     final int huntingYear,
                                                     final int gameSpeciesCode) {

        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return findAllAsList(PERMIT.isMooselikePermit()
                .and(PERMIT.hasRhyOrRelatedRhy(rhyId))
                .and(PERMIT.matchesPermitYear(huntingYear))
                .and(SPECIES_AMOUNT.matchesSpecies(PERMIT, gameSpeciesCode)));
    }

    default List<MooselikeHuntingYearDTO> listRhyMooselikeHuntingYears(final long rhyId) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        return MooselikeHuntingYearDTO.create(findAllAsList(
                PERMIT.isMooselikePermit().and(PERMIT.hasRhyOrRelatedRhy(rhyId))));
    }

    default List<HarvestPermit> findAmendmentPermits(final HarvestPermit originalPermit) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        return findAllAsList(PERMIT.originalPermit.eq(originalPermit));
    }
}
