package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.config.Constants;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSpeciesChangeForbiddenException;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHarvestDTO;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public class HarvestCommonMutation implements HarvestMutation {

    private final HarvestMutationRole mutationRole;
    private final LocalDateTime pointOfTime;
    private final LocalDate harvestDate;
    private final GameSpecies species;
    private final int amount;

    public HarvestCommonMutation(final HarvestDTO dto,
                                 final GameSpecies species,
                                 final HarvestMutationRole mutationRole) {

        this.pointOfTime = requireNonNull(dto.getPointOfTime());
        this.harvestDate = dto.getPointOfTime().toLocalDate();
        this.species = requireNonNull(species);
        this.amount = dto.getAmount();
        this.mutationRole = requireNonNull(mutationRole);
    }

    public HarvestCommonMutation(final MobileHarvestDTO dto,
                                 final GameSpecies species,
                                 final HarvestMutationRole mutationRole) {

        this.pointOfTime = requireNonNull(dto.getPointOfTime());
        this.harvestDate = dto.getPointOfTime().toLocalDate();
        this.species = requireNonNull(species);
        this.amount = dto.getAmount();
        this.mutationRole = requireNonNull(mutationRole);

        if (mutationRole != HarvestMutationRole.AUTHOR_OR_ACTOR) {
            throw new RuntimeException("Invalid role for mobile " + mutationRole);
        }
    }

    public HarvestCommonMutation(final MobileGroupHarvestDTO dto,
                                 final GameSpecies species,
                                 final HarvestMutationRole mutationRole) {

        this.pointOfTime = requireNonNull(dto.getPointOfTime());
        this.harvestDate = dto.getPointOfTime().toLocalDate();
        this.species = requireNonNull(species);
        this.amount = dto.getAmount();
        this.mutationRole = requireNonNull(mutationRole);
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

        harvest.setPointOfTime(pointOfTime.toDateTime(Constants.DEFAULT_TIMEZONE));
        harvest.setAmount(amount);
        harvest.setSpecies(species);

        if (mutationRole == HarvestMutationRole.MODERATOR) {
            harvest.setModeratorOverride(true);
        }
    }
}
