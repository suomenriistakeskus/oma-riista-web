package fi.riista.feature.dashboard;

import java.util.List;

public class DashboardMooseHuntingDTO {

    private List<DashboardMooselikeHuntingDTO> mooselikeHuntingMetrics;

    public List<DashboardMooselikeHuntingDTO> getMooselikeHuntingMetrics() {
        return mooselikeHuntingMetrics;
    }

    public void setMooselikeHuntingMetrics(List<DashboardMooselikeHuntingDTO> mooselikeHuntingMetrics) {
        this.mooselikeHuntingMetrics = mooselikeHuntingMetrics;
    }
}
