package fi.riista.feature.huntingclub.permit.endofhunting.basicsummary;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static org.springframework.data.jpa.domain.Specifications.where;

public interface BasicClubHuntingSummaryRepository extends BaseRepository<BasicClubHuntingSummary, Long> {

    List<BasicClubHuntingSummary> findBySpeciesAmount(HarvestPermitSpeciesAmount speciesAmount);

    default Optional<BasicClubHuntingSummary> findByClubAndSpeciesAmount(
            @Nonnull final HuntingClub club,
            @Nonnull final HarvestPermitSpeciesAmount speciesAmount) {

        Objects.requireNonNull(club, "club is null");
        Objects.requireNonNull(speciesAmount, "speciesAmount is null");

        return Optional.ofNullable(findOne(where(
                equal(BasicClubHuntingSummary_.speciesAmount, speciesAmount))
                        .and(equal(BasicClubHuntingSummary_.club, club))));
    }

    default Optional<BasicClubHuntingSummary> findByClubAndSpeciesAmount(
            @Nonnull final HuntingClub club,
            @Nonnull final HarvestPermitSpeciesAmount speciesAmount,
            final boolean moderatorOverridden) {

        return findByClubAndSpeciesAmount(club, speciesAmount)
                .filter(summary -> summary.isModeratorOverride() == moderatorOverridden);
    }

    default List<BasicClubHuntingSummary> findModeratorOverriddenHuntingSummaries(
            @Nonnull final HarvestPermitSpeciesAmount speciesAmount) {

        Objects.requireNonNull(speciesAmount);

        return findAll(where(equal(BasicClubHuntingSummary_.speciesAmount, speciesAmount))
                .and(equal(BasicClubHuntingSummary_.moderatorOverride, true)));
    }

}
