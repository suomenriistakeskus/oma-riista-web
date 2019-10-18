package fi.riista.feature.permit.application.conflict;

import fi.riista.integration.mapexport.MapPdfParameters;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class PrintApplicationConflictRequestDTO {

    @NotNull
    private List<Long> palstaIds;

    @NotNull
    @Valid
    private MapPdfParameters mapParameters;

    public List<Long> getPalstaIds() {
        return palstaIds;
    }

    public void setPalstaIds(final List<Long> palstaIds) {
        this.palstaIds = palstaIds;
    }

    public MapPdfParameters getMapParameters() {
        return mapParameters;
    }

    public void setMapParameters(final MapPdfParameters mapParameters) {
        this.mapParameters = mapParameters;
    }
}
