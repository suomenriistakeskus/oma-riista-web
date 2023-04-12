package fi.riista.feature.training.mobile;

import fi.riista.feature.organization.jht.mobile.MobileJHTTrainingDTO;
import fi.riista.feature.organization.rhy.mobile.MobileOccupationTrainingDTO;
import java.util.List;
import javax.validation.constraints.NotNull;

public class MobileTrainingsDTO {

    public static MobileTrainingsDTO create(
        final List<MobileJHTTrainingDTO> jhtTrainings,
        final List<MobileOccupationTrainingDTO> occupationTrainings
    ) {
        final MobileTrainingsDTO dto = new MobileTrainingsDTO();

        dto.jhtTrainings = jhtTrainings;
        dto.occupationTrainings = occupationTrainings;

        return dto;
    }

    @NotNull
    private List<MobileJHTTrainingDTO> jhtTrainings;

    @NotNull
    private List<MobileOccupationTrainingDTO> occupationTrainings;

    public List<MobileJHTTrainingDTO> getJhtTrainings() {
        return jhtTrainings;
    }

    public void setJhtTrainings(final List<MobileJHTTrainingDTO> jhtTrainings) {
        this.jhtTrainings = jhtTrainings;
    }

    public List<MobileOccupationTrainingDTO> getOccupationTrainings() {
        return occupationTrainings;
    }

    public void setOccupationTrainings(final List<MobileOccupationTrainingDTO> occupationTrainings) {
        this.occupationTrainings = occupationTrainings;
    }
}
