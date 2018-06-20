package fi.riista.integration.mapexport;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MapPdfModel {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy HH:mm");
    private static final DateTimeFormatter DTF_FILENAME = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final String exportFileName;
    private final GeoJsonObject geometry;
    private final double[] bbox;

    private final String clubName;
    private final String areaName;
    private final String saveDate;
    private final String areaSize;

    private MapPdfModel(final GeoJsonObject geometry, final double[] bbox,
                        final String exportFileName,
                        final String clubName, final String areaName,
                        final String saveDate, final String areaSize) {
        this.exportFileName = Objects.requireNonNull(exportFileName);
        this.geometry = Objects.requireNonNull(geometry);
        this.bbox = Objects.requireNonNull(bbox);
        this.clubName = Objects.requireNonNull(clubName);
        this.areaName = Objects.requireNonNull(areaName);
        this.saveDate = Objects.requireNonNull(saveDate);
        this.areaSize = Objects.requireNonNull(areaSize);
    }

    public boolean isPreferLandscape() {
        // bbox = [minX, minY, maxX, maxY]
        final double width = Math.abs(bbox[2] - bbox[0]);
        final double height = Math.abs(bbox[3] - bbox[1]);

        return width > height;
    }

    public String getExportFileName() {
        return exportFileName;
    }

    public FeatureCollection toFeatureCollection() {
        final Feature feature = new Feature();
        feature.setGeometry(geometry);
        feature.setBbox(bbox);

        feature.setProperty("clubName", clubName);
        feature.setProperty("saveDate", saveDate);
        feature.setProperty("areaName", areaName);
        feature.setProperty("areaSize", areaSize);
        feature.setProperty("fill", "rgb(0, 192, 60)");
        feature.setProperty("fill-opacity", 0.3);
        feature.setProperty("stroke-width", 3.0);
        feature.setProperty("stroke", "rgb(0,0,0)");

        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(GISUtils.SRID.ETRS_TM35FIN.getGeoJsonCrs());
        featureCollection.setBbox(bbox);
        featureCollection.setFeatures(ImmutableList.of(feature));

        return featureCollection;
    }

    public static class Builder {
        private MessageSource messageSource;
        private Locale locale;
        private LocalisedString clubName;
        private LocalisedString areaName;
        private GeoJsonObject geometry;
        private double[] bbox;
        private Date modificationTime;
        private TotalLandWaterSizeDTO size;

        public Builder(final MessageSource messageSource,
                       final Locale locale) {
            this.messageSource = Objects.requireNonNull(messageSource);
            this.locale = Objects.requireNonNull(locale);
        }

        public Builder withClubName(final LocalisedString clubName) {
            this.clubName = clubName;
            return this;
        }

        public Builder withAreaName(final LocalisedString areaName) {
            this.areaName = areaName;
            return this;
        }

        public Builder withModificationTime(final Date modificationTime) {
            this.modificationTime = modificationTime;
            return this;
        }

        public Builder withGeometry(final Geometry geometry) {
            this.geometry = PolygonConversionUtil.javaToGeoJSON(geometry);
            return this;
        }

        public Builder withBbox(final double[] bbox) {
            this.bbox = bbox;
            return this;
        }

        public Builder withSize(final TotalLandWaterSizeDTO size) {
            this.size = size;
            return this;
        }

        private String i18n(final String key, final Locale locale) {
            return messageSource.getMessage("HuntingClubArea.pdf." + key, null, locale);
        }

        private String formatClubName() {
            return clubName.getAnyTranslation(this.locale);
        }

        @Nonnull
        private String formatAreaSize() {
            return formatAreaSize("totalAreaSize", size.getTotal()) + " " +
                    formatAreaSize("landAreaSize", size.getLand()) + " " +
                    formatAreaSize("waterAreaSize", size.getWater());
        }

        private String formatAreaSize(final String key, final double areaSize) {
            return i18n(key, locale) + " " + String.format("%.2f", areaSize / 10_000) + " " + i18n("ha", locale);
        }

        private String formatSaveDate() {
            return i18n("saveDate", locale) + " " + DTF.print(DateUtil.toLocalDateTimeNullSafe(modificationTime));
        }

        private String formatAreaName() {
            return i18n("areaName", locale) + " " + this.areaName.getAnyTranslation(locale);
        }

        private String formatExportFileName() {
            final LocalDateTime saveDateTime = DateUtil.toLocalDateTimeNullSafe(modificationTime);
            return DTF_FILENAME.print(saveDateTime) + " " + areaName.getAnyTranslation(locale) + ".pdf";
        }

        public MapPdfModel build() {
            Objects.requireNonNull(clubName, "clubName is null");
            Objects.requireNonNull(areaName, "areaName is null");
            Objects.requireNonNull(geometry, "geometry is null");
            Objects.requireNonNull(bbox, "bbox is null");
            Objects.requireNonNull(modificationTime, "modificationTime is null");
            Objects.requireNonNull(size, "size is null");

            return new MapPdfModel(this.geometry, this.bbox, formatExportFileName(),
                    formatClubName(), formatAreaName(),
                    formatSaveDate(), formatAreaSize());
        }
    }
}
