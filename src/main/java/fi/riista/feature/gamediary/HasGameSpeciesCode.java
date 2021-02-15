package fi.riista.feature.gamediary;

@FunctionalInterface
public interface HasGameSpeciesCode {

    int getGameSpeciesCode();

    // Helpers for checking one specific species.

    default boolean isBeanGoose() {
        return GameSpecies.isBeanGoose(getGameSpeciesCode());
    }

    default boolean isBear() {
        return GameSpecies.isBear(getGameSpeciesCode());
    }

    default boolean isGreySeal() {
        return GameSpecies.isGreySeal(getGameSpeciesCode());
    }

    default boolean isFallowDeer() {
        return GameSpecies.isFallowDeer(getGameSpeciesCode());
    }

    default boolean isMoose() {
        return GameSpecies.isMoose(getGameSpeciesCode());
    }

    default boolean isRoeDeer() {
        return GameSpecies.isRoeDeer(getGameSpeciesCode());
    }

    default boolean isWhiteTailedDeer() {
        return GameSpecies.isWhiteTailedDeer(getGameSpeciesCode());
    }

    default boolean isWildBoar() {
        return GameSpecies.isWildBoar(getGameSpeciesCode());
    }

    default boolean isWildForestReindeer() {
        return GameSpecies.isWildForestReindeer(getGameSpeciesCode());
    }

    default boolean isWolf() {
        return GameSpecies.isWolf(getGameSpeciesCode());
    }

    // Helpers for checking species group.

    default boolean isDeerRequiringPermitForHunting() {
        return GameSpecies.isDeerRequiringPermitForHunting(getGameSpeciesCode());
    }

    default boolean isMooselike() {
        return GameSpecies.isMooselike(getGameSpeciesCode());
    }

    default boolean isMooseOrDeerRequiringPermitForHunting() {
        return GameSpecies.isMooseOrDeerRequiringPermitForHunting(getGameSpeciesCode());
    }
}
