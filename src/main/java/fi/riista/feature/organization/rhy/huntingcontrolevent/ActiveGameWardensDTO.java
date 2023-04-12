package fi.riista.feature.organization.rhy.huntingcontrolevent;

import java.util.List;

public class ActiveGameWardensDTO {

    public static ActiveGameWardensDTO createWithNoActiveNomination() {
        final ActiveGameWardensDTO dto = new ActiveGameWardensDTO();
        dto.setActiveNomination(false);
        return dto;
    }

    public static ActiveGameWardensDTO create(final List<HuntingControlInspectorDTO> gameWardens) {
        final ActiveGameWardensDTO dto = new ActiveGameWardensDTO();
        dto.setActiveNomination(true);
        dto.setGameWardens(gameWardens);
        return dto;
    }

    private List<HuntingControlInspectorDTO> gameWardens;

    private boolean activeNomination;

    public List<HuntingControlInspectorDTO> getGameWardens() {
        return gameWardens;
    }

    public void setGameWardens(final List<HuntingControlInspectorDTO> gameWardens) {
        this.gameWardens = gameWardens;
    }

    public boolean isActiveNomination() {
        return activeNomination;
    }

    public void setActiveNomination(final boolean activeNomination) {
        this.activeNomination = activeNomination;
    }
}
