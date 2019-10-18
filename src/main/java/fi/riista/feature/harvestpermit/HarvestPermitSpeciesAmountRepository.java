package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.springframework.data.jpa.repository.Modifying;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.jpa.domain.Specifications.where;

public interface HarvestPermitSpeciesAmountRepository extends BaseRepository<HarvestPermitSpeciesAmount, Long> {

    List<HarvestPermitSpeciesAmount> findByHarvestPermit(HarvestPermit harvestPermit);

    @Modifying
    void deleteByHarvestPermit(HarvestPermit harvestPermit);

    default List<HarvestPermitSpeciesAmount> findByHarvestPermitAndSpeciesCode(final @Nonnull HarvestPermit harvestPermit,
                                                                               final int speciesCode) {
        requireNonNull(harvestPermit);

        return findAll(where(equal(HarvestPermitSpeciesAmount_.harvestPermit, harvestPermit))
                .and(equal(HarvestPermitSpeciesAmount_.gameSpecies, GameSpecies_.officialCode, speciesCode)));
    }

    default Optional<HarvestPermitSpeciesAmount> findOneByHarvestPermitIdAndSpeciesCode(final @Nonnull HarvestPermit harvestPermit,
                                                                                        final int speciesCode) {

        final List<HarvestPermitSpeciesAmount> speciesAmounts = findByHarvestPermitAndSpeciesCode(harvestPermit, speciesCode);

        if (speciesAmounts.size() > 1) {
            throw HarvestPermitSpeciesAmountNotFound.uniqueHuntingYearNotFound(harvestPermit.getPermitNumber(), speciesCode, speciesAmounts);
        }

        return speciesAmounts.stream().findFirst();
    }

    default HarvestPermitSpeciesAmount getOneByHarvestPermitAndSpeciesCode(final @Nonnull HarvestPermit harvestPermit,
                                                                           final int speciesCode) {
        return findOneByHarvestPermitIdAndSpeciesCode(harvestPermit, speciesCode)
                .orElseThrow(() -> HarvestPermitSpeciesAmountNotFound.notFound(harvestPermit.getPermitNumber(), speciesCode));
    }

    default List<HarvestPermitSpeciesAmount> findMooseAmounts(final @Nonnull HarvestPermit harvestPermit) {
        return findByHarvestPermitAndSpeciesCode(harvestPermit, GameSpecies.OFFICIAL_CODE_MOOSE);
    }

    default HarvestPermitSpeciesAmount getMooseAmount(final @Nonnull HarvestPermit harvestPermit) {
        return getOneByHarvestPermitAndSpeciesCode(harvestPermit, GameSpecies.OFFICIAL_CODE_MOOSE);
    }

    default Optional<HarvestPermitSpeciesAmount> findByHuntingClubGroupPermit(final @Nonnull HuntingClubGroup group) {
        return Optional
                .of(group) // throws NPE if null
                .map(HuntingClubGroup::getHarvestPermit)
                .flatMap(permit -> findOneByHarvestPermitIdAndSpeciesCode(permit, group.getSpecies().getOfficialCode()));
    }

    default List<HarvestPermitSpeciesAmount> getAmendmentPermitSpeciesAmounts(final HarvestPermit originalPermit) {
        return findAll(where(
                equal(HarvestPermitSpeciesAmount_.harvestPermit, HarvestPermit_.originalPermit, originalPermit))
                .and(fetch(HarvestPermitSpeciesAmount_.harvestPermit)));
    }

    default List<HarvestPermitSpeciesAmount> getAmendmentPermitSpeciesAmounts(final HarvestPermit originalPermit,
                                                                              final GameSpecies species) {
        return findAll(where(
                equal(HarvestPermitSpeciesAmount_.harvestPermit, HarvestPermit_.originalPermit, originalPermit))
                .and(equal(HarvestPermitSpeciesAmount_.gameSpecies, species))
                .and(fetch(HarvestPermitSpeciesAmount_.harvestPermit)));
    }

    default Map<String, Float> countAmendmentPermitNumbersAndAmounts(final HarvestPermit permit,
                                                                     final GameSpecies species) {

        return getAmendmentPermitSpeciesAmounts(permit, species).stream().collect(Collectors.toMap(
                speciesAmount -> speciesAmount.getHarvestPermit().getPermitNumber(),
                HarvestPermitSpeciesAmount::getAmount,
                (u, v) -> u + v,
                TreeMap::new));
    }
}
