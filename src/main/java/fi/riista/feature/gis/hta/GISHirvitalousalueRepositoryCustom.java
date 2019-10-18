package fi.riista.feature.gis.hta;

import fi.riista.feature.common.entity.GeoLocation;

import java.util.List;

public interface GISHirvitalousalueRepositoryCustom {
    HirvitalousalueDTO getWithoutGeometry(int htaId);

    List<HirvitalousalueDTO> listWithoutGeometry();

    GISHirvitalousalue findByPoint(GeoLocation geoLocation);
}
