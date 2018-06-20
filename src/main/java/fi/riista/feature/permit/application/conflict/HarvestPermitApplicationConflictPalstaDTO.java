package fi.riista.feature.permit.application.conflict;

public class HarvestPermitApplicationConflictPalstaDTO {
    private final Integer palstaId;
    private final String palstaTunnus;
    private final String palstaNimi;
    private final Double areaSize;
    private final boolean metsahallitus;

    public HarvestPermitApplicationConflictPalstaDTO(final Integer palstaId, final String palstaTunnus,
                                                     final String palstaNimi, final Double areaSize,
                                                     final boolean metsahallitus) {
        this.palstaId = palstaId;
        this.palstaTunnus = palstaTunnus;
        this.palstaNimi = palstaNimi;
        this.areaSize = areaSize;
        this.metsahallitus = metsahallitus;
    }

    public Integer getPalstaId() {
        return palstaId;
    }

    public String getPalstaTunnus() {
        return palstaTunnus;
    }

    public String getPalstaNimi() {
        return palstaNimi;
    }

    public Double getAreaSize() {
        return areaSize;
    }

    public boolean isMetsahallitus() {
        return metsahallitus;
    }
}
