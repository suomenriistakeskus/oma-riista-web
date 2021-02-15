package fi.riista.feature.gis;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.metsahallitus.MetsahallitusAreaLookupResult;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

public interface GISQueryService {
    Riistanhoitoyhdistys findRhyByLocation(@Nonnull GeoLocation geoLocation);

    Riistanhoitoyhdistys findRhyByLocation(@Nonnull GISPoint gisPoint);

    Optional<HarvestArea> findHarvestAreaByLocation(@Nonnull HarvestArea.HarvestAreaType areaType, @Nonnull GeoLocation geoLocation);

    Riistanhoitoyhdistys findRhyForEconomicZone(@Nonnull GeoLocation geoLocation);

    MetsahallitusAreaLookupResult findMetsahallitusAreas(@Nonnull GeoLocation geoLocation);

    Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(GISPoint gisPoint);

    Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(GeoLocation geoLocation);

    Municipality findMunicipality(@Nonnull GeoLocation geoLocation);

    Map<Long, Boolean> findZonesWithChanges(@Nonnull Set<Long> zoneIds);

    GISHirvitalousalue findHirvitalousalue(@Nonnull GeoLocation geoLocation);

    OptionalInt findInhabitedBuildingDistance(GISPoint position, int maxDistanceToSeek);
}
