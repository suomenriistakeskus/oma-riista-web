package fi.riista.integration.koiratutka.export;

import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.util.LocalisedString;
import org.geojson.Feature;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Objects;

class GeoJsonMetadata {
    private final GISZoneSizeDTO areaSize;
    private final LocalisedString clubName;
    private final LocalisedString areaName;
    private final int huntingYear;
    private final DateTime saveDate;

    GeoJsonMetadata(final GISZoneSizeDTO areaSize,
                    final AreaExportDTO dto,
                    final DateTime latestModificationTime) {
        Objects.requireNonNull(latestModificationTime);

        this.saveDate = latestModificationTime;
        this.areaSize = areaSize;
        this.clubName = Objects.requireNonNull(dto.getClubName());
        this.areaName = Objects.requireNonNull(dto.getAreaName());
        this.huntingYear = dto.getHuntingYear();
    }

    public void updateFeature(final Feature feature) {
        feature.setId(null);
        feature.setProperty(GeoJSONConstants.PROPERTY_CLUB_NAME, this.clubName);
        feature.setProperty(GeoJSONConstants.PROPERTY_AREA_NAME, this.areaName);
        feature.setProperty(GeoJSONConstants.PROPERTY_HUNTING_YEAR, huntingYear);
        feature.setProperty(GeoJSONConstants.PROPERTY_SAVE_DATE, ISODateTimeFormat.basicDateTimeNoMillis().print(saveDate));

        if (areaSize != null) {
            feature.setProperty(GeoJSONConstants.PROPERTY_AREA_SIZE, Math.round(areaSize.getAll().getTotal()));
            feature.setProperty(GeoJSONConstants.PROPERTY_WATER_AREA_SIZE, Math.round(areaSize.getAll().getWater()));
        } else {
            // Do not break API compatibility  when area size is not yet calculated.
            feature.setProperty(GeoJSONConstants.PROPERTY_AREA_SIZE, 0);
            feature.setProperty(GeoJSONConstants.PROPERTY_WATER_AREA_SIZE, 0);
        }
    }
}
