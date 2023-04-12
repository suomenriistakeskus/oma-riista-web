package fi.riista.feature.huntingclub.poi;

import javax.validation.constraints.NotNull;
import java.util.List;

public class PoiCollectionDTO {

    @NotNull
    private List<Long> poiIds;

    public PoiCollectionDTO() {
    }

    public PoiCollectionDTO(final List<Long> poiIds) {
        this.poiIds = poiIds;
    }

    public List<Long> getPoiIds() {
        return poiIds;
    }

    public void setPoiIds(final List<Long> poiIds) {
        this.poiIds = poiIds;
    }
}
