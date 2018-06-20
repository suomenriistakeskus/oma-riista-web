package fi.riista.feature.permit.application;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;

import java.util.List;
import java.util.Objects;

public class HarvestPermitApplicationAreaDTO {
    private final HarvestPermitArea.StatusCode status;
    private final GISZoneSizeDTO size;
    private final boolean freeHunting;
    private final List<HarvestPermitAreaRhyDTO> rhy;
    private final List<HarvestPermitAreaHtaDTO> hta;

    public HarvestPermitApplicationAreaDTO(final HarvestPermitArea.StatusCode status,
                                           final GISZoneSizeDTO size,
                                           final boolean freeHunting,
                                           final List<HarvestPermitAreaRhyDTO> rhy,
                                           final List<HarvestPermitAreaHtaDTO> hta) {
        this.status = Objects.requireNonNull(status);
        this.size = Objects.requireNonNull(size);
        this.freeHunting = freeHunting;
        this.rhy = Objects.requireNonNull(rhy);
        this.hta = Objects.requireNonNull(hta);
    }

    public HarvestPermitArea.StatusCode getStatus() {
        return status;
    }

    public GISZoneSizeDTO getSize() {
        return size;
    }

    public boolean isFreeHunting() {
        return freeHunting;
    }

    public List<HarvestPermitAreaRhyDTO> getRhy() {
        return rhy;
    }

    public List<HarvestPermitAreaHtaDTO> getHta() {
        return hta;
    }
}
