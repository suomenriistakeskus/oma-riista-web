package fi.riista.feature.permit.application.mooselike;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class MooselikePermitApplicationAreaDTO {

    private final HarvestPermitArea.StatusCode status;
    private final GISZoneSizeDTO size;
    private final boolean freeHunting;
    private final List<HarvestPermitAreaRhyDTO> rhy;
    private final List<HarvestPermitAreaHtaDTO> hta;

    public MooselikePermitApplicationAreaDTO(final @Nonnull HarvestPermitArea.StatusCode status,
                                             final @Nonnull GISZoneSizeDTO size,
                                             final boolean freeHunting,
                                             final @Nonnull List<HarvestPermitAreaRhyDTO> rhy,
                                             final @Nonnull List<HarvestPermitAreaHtaDTO> hta) {
        this.status = requireNonNull(status);
        this.size = requireNonNull(size);
        this.freeHunting = freeHunting;
        this.rhy = requireNonNull(rhy);
        this.hta = requireNonNull(hta);
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
