package fi.riista.feature.permit.application.geometry;

import fi.riista.util.LocalisedString;
import org.geojson.FeatureCollection;

import java.util.Map;

public class HarvestPermitApplicationAreaPartnerExportDTO {
    private final FeatureCollection featureCollection;
    private final Map<Long, LocalisedString> areaNames;
    private final Map<Long, LocalisedString> clubNames;

    public HarvestPermitApplicationAreaPartnerExportDTO(final FeatureCollection featureCollection,
                                                        final Map<Long, LocalisedString> areaNames,
                                                        final Map<Long, LocalisedString> clubNames) {
        this.featureCollection = featureCollection;
        this.areaNames = areaNames;
        this.clubNames = clubNames;
    }

    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    public Map<Long, LocalisedString> getAreaNames() {
        return areaNames;
    }

    public Map<Long, LocalisedString> getClubNames() {
        return clubNames;
    }
}
