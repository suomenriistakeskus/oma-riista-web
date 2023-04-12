package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.huntingclub.poi.gpx.GpxPoiLocationDTO;

import java.util.Collection;
import java.util.List;


public interface PoiLocationRepositoryCustom {

    List<GpxPoiLocationDTO> getGpxPointsByPoi(PoiLocationGroup poi);

    List<GpxPoiLocationDTO> getGpxPointsByPoiIn(Collection<PoiLocationGroup> pois);

}
