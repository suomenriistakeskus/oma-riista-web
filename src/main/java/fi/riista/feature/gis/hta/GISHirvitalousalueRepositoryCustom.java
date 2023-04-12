package fi.riista.feature.gis.hta;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import java.util.List;

public interface GISHirvitalousalueRepositoryCustom {
    HirvitalousalueDTO getWithoutGeometry(int htaId);

    List<HirvitalousalueDTO> listWithoutGeometry();

    GISHirvitalousalue findByPoint(GeoLocation geoLocation);

    List<HirvitalousalueDTO> findByRHY(Riistanhoitoyhdistys rhy_id);
}
