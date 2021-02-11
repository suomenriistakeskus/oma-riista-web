package fi.riista.feature.permit.application.dogevent.fixture;

import fi.riista.feature.gamediary.GameSpecies;

public interface SpeciesMap {
    GameSpecies byOfficialCode(final int officialCode);
}
