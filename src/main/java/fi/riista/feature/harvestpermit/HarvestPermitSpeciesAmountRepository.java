package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.hasRelationWithId;
import static java.util.stream.Collectors.joining;
import static org.springframework.data.jpa.domain.Specifications.where;

public interface HarvestPermitSpeciesAmountRepository extends BaseRepository<HarvestPermitSpeciesAmount, Long> {

    List<HarvestPermitSpeciesAmount> findByHarvestPermit(HarvestPermit harvestPermit);

    default List<HarvestPermitSpeciesAmount> findByHarvestPermitIdAndSpeciesCode(final long harvestPermitId,
                                                                                 final int speciesCode) {
        return findAll(
                where(hasRelationWithId(HarvestPermitSpeciesAmount_.harvestPermit, HarvestPermit_.id, harvestPermitId))
                        .and(equal(HarvestPermitSpeciesAmount_.gameSpecies, GameSpecies_.officialCode, speciesCode)));
    }

    default List<HarvestPermitSpeciesAmount> findByHarvestPermitAndSpeciesCode(@Nonnull final HarvestPermit harvestPermit,
                                                                               final int speciesCode) {

        Objects.requireNonNull(harvestPermit);
        return findByHarvestPermitIdAndSpeciesCode(harvestPermit.getId(), speciesCode);
    }

    default Optional<HarvestPermitSpeciesAmount> findOneByHarvestPermitIdAndSpeciesCode(final long harvestPermitId,
                                                                                        final int speciesCode) {
        final List<HarvestPermitSpeciesAmount> speciesAmounts =
                findByHarvestPermitIdAndSpeciesCode(harvestPermitId, speciesCode);

        if (speciesAmounts.size() > 1) {
            final String huntingYears = Has2BeginEndDates.streamUniqueHuntingYearsSorted(speciesAmounts.stream())
                    .mapToObj(String::valueOf)
                    .collect(joining(","));

            throw new IllegalStateException(String.format(
                    "Cannot resolve HarvestPermitSpeciesAmount unambiguously because multiple instances found for " +
                            "{ harvestPermitId: %d, speciesCode: %d } for following hunting years: %s",
                    harvestPermitId, speciesCode, huntingYears));
        }

        return speciesAmounts.isEmpty() ? Optional.empty() : Optional.of(speciesAmounts.get(0));
    }

    default HarvestPermitSpeciesAmount getOneByHarvestPermitIdAndSpeciesCode(final long harvestPermitId,
                                                                             final int speciesCode) {
        return findOneByHarvestPermitIdAndSpeciesCode(harvestPermitId, speciesCode)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Could not find HarvestPermitSpeciesAmount by { harvestPermitId: %d, speciesCode: %d }",
                        harvestPermitId, speciesCode)));
    }

    default HarvestPermitSpeciesAmount getOneByHarvestPermitAndSpeciesCode(@Nonnull final HarvestPermit harvestPermit,
                                                                           final int speciesCode) {
        Objects.requireNonNull(harvestPermit);
        return getOneByHarvestPermitIdAndSpeciesCode(harvestPermit.getId(), speciesCode);
    }

    default Optional<HarvestPermitSpeciesAmount> findByHuntingClubGroupPermit(final HuntingClubGroup huntingClubGroup) {
        if (huntingClubGroup.getHarvestPermit() == null) {
            return Optional.empty();
        }

        final Long harvestPermitId = huntingClubGroup.getHarvestPermit().getId();
        final int gameSpeciesOfficialCode = huntingClubGroup.getSpecies().getOfficialCode();

        return findOneByHarvestPermitIdAndSpeciesCode(harvestPermitId, gameSpeciesOfficialCode);
    }
}
