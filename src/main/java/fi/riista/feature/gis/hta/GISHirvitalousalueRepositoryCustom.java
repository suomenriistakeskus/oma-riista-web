package fi.riista.feature.gis.hta;

import fi.riista.feature.common.entity.GeoLocation;

public interface GISHirvitalousalueRepositoryCustom {
    GISHirvitalousalue findByPoint(GeoLocation geoLocation);
}
