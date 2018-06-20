package fi.riista.feature.gamediary.harvest;

import fi.riista.util.LocalisedEnum;

public enum HuntingMethod implements LocalisedEnum {
    // Ammuttu
    SHOT,
    // Elävänä pyytävällä loukulla pyydetty
    CAPTURED_ALIVE,
    // Ammuttu, mutta menetetty
    SHOT_BUT_LOST
}
