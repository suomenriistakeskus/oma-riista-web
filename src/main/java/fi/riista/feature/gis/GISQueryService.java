package fi.riista.feature.gis;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface GISQueryService {
    Riistanhoitoyhdistys findRhyByLocation(@Nonnull GeoLocation geoLocation);

    Riistanhoitoyhdistys findRhyByLocation(@Nonnull GISPoint gisPoint);

    Riistanhoitoyhdistys findRhyForEconomicZone(@Nonnull GeoLocation geoLocation);

    Integer findMetsahallitusHirviAlueId(@Nonnull GeoLocation geoLocation, int year);

    Integer findMetsahallitusPienriistaAlueId(@Nonnull GeoLocation geoLocation, int year);

    Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(GISPoint gisPoint);

    Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(GeoLocation geoLocation);

    Municipality findMunicipality(@Nonnull GeoLocation geoLocation);

    List<Long> findZonesWithChanges();

    GISHirvitalousalue findHirvitalousalue(@Nonnull GeoLocation geoLocation);

    OptionalInt findInhabitedBuildingDistance(GISPoint position, int maxDistanceToSeek);
}
