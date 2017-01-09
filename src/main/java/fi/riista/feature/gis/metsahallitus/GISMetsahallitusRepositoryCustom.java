package fi.riista.feature.gis.metsahallitus;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.metsahallitus.GISMetsahallitusHirviDTO;
import fi.riista.util.GISUtils;
import org.geojson.Feature;

import java.util.List;

public interface GISMetsahallitusRepositoryCustom {
    Integer findHirviAlueId(GeoLocation geoLocation, int year);

    Integer findPienriistaAlueId(GeoLocation geoLocation, int year);

    List<GISMetsahallitusHirviDTO> listHirvi(final int year);

    Feature getHirviFeature(int id, final GISUtils.SRID srid);

    List<Feature> listZoneHirviFeatures(long zoneId, final GISUtils.SRID srid);
}
