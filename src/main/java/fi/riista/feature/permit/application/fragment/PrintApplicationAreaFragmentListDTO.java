package fi.riista.feature.permit.application.fragment;

import fi.riista.integration.mapexport.MapPdfParameters;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class PrintApplicationAreaFragmentListDTO {

    // Geohashes for polygon centroids
    @NotNull
    private List<String> fragmentIds;

    @NotNull
    @Valid
    private MapPdfParameters pdfParameters;

    public List<String> getFragmentIds() {
        return fragmentIds;
    }

    public void setFragmentIds(final List<String> fragmentIds) {
        this.fragmentIds = fragmentIds;
    }

    public MapPdfParameters getPdfParameters() {
        return pdfParameters;
    }

    public void setPdfParameters(final MapPdfParameters pdfParameters) {
        this.pdfParameters = pdfParameters;
    }
}
