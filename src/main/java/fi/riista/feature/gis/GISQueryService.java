package fi.riista.feature.gis;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.annotation.Nonnull;
import java.util.List;

public interface GISQueryService {
    Riistanhoitoyhdistys findRhyByLocation(@Nonnull GeoLocation geoLocation);

    Riistanhoitoyhdistys findRhyByLocation(@Nonnull GISPoint gisPoint);

    String getRhyGeoJSON(@Nonnull String officialCode);

    WGS84Bounds getRhyBounds(@Nonnull String officialCode);

    Integer findMetsahallitusHirviAlueId(@Nonnull GeoLocation geoLocation, int year);

    Integer findMetsahallitusPienriistaAlueId(@Nonnull GeoLocation geoLocation, int year);

    Municipality findMunicipality(@Nonnull GeoLocation geoLocation);

    List<Long> findZonesWithChanges();

    GISHirvitalousalue findHirvitalousalue(@Nonnull GeoLocation geoLocation);
}
