package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSpeciesChangeForbiddenException;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Objects;
import java.util.Optional;

public class HarvestCommonMutation implements HarvestMutation {
    private final HarvestMutationRole mutationRole;
    private final LocalDateTime pointOfTime;
    private final LocalDate harvestDate;
    private final GameSpecies species;
    private final int amount;

    public HarvestCommonMutation(final HarvestDTO dto,
                                 final GameSpecies species,
                                 final HarvestMutationRole mutationRole) {
        this.pointOfTime = Objects.requireNonNull(dto.getPointOfTime());
        this.harvestDate = dto.getPointOfTime().toLocalDate();
        this.species = Objects.requireNonNull(species);
        this.amount = dto.getAmount();
        this.mutationRole = Objects.requireNonNull(mutationRole);
    }

    public HarvestCommonMutation(final MobileHarvestDTO dto,
                                 final GameSpecies species,
                                 final HarvestMutationRole mutationRole) {
        this.pointOfTime = Objects.requireNonNull(dto.getPointOfTime());
        this.harvestDate = dto.getPointOfTime().toLocalDate();
        this.species = Objects.requireNonNull(species);
        // Use 1 as default specimen amount
        this.amount = Optional.ofNullable(dto.getAmount()).orElse(1);
        this.mutationRole = Objects.requireNonNull(mutationRole);

        if (mutationRole != HarvestMutationRole.AUTHOR_OR_ACTOR) {
            throw new RuntimeException("Invalid role for mobile " + mutationRole);
        }
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public int getSpeciesCode() {
        return species.getOfficialCode();
    }

    @Override
    public void accept(final Harvest harvest) {
        final boolean roleCanChangeSpecies = mutationRole == HarvestMutationRole.AUTHOR_OR_ACTOR;
        final boolean speciesChanged = harvest.getSpecies() != null
                && !harvest.getSpecies().equals(species);

        if (speciesChanged && !roleCanChangeSpecies) {
            throw new HarvestSpeciesChangeForbiddenException(mutationRole);
        }

        harvest.setPointOfTime(DateUtil.toDateNullSafe(this.pointOfTime));
        harvest.setAmount(amount);
        harvest.setSpecies(species);

        if (mutationRole == HarvestMutationRole.MODERATOR) {
            harvest.setModeratorOverride(true);
        }
    }
}
