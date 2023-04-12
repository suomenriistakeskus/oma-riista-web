package fi.riista.integration.mapexport;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import fi.riista.util.NumberUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;

public class MapPdfModel {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy HH:mm");
    private static final DateTimeFormatter DTF_FILENAME = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final String exportFileName;
    private final FeatureCollection featureCollection;
    private final double[] bbox;

    private MapPdfModel(final FeatureCollection featureCollection, final double[] bbox, final String exportFileName) {
        this.exportFileName = Objects.requireNonNull(exportFileName);
        this.featureCollection = Objects.requireNonNull(featureCollection);
        this.bbox = Objects.requireNonNull(bbox);
    }

    public boolean isPreferLandscape() {
        // bbox = [minX, minY, maxX, maxY]
        final double width = Math.abs(bbox[2] - bbox[0]);
        final double height = Math.abs(bbox[3] - bbox[1]);

        return width > height;
    }

    public FeatureCollection getFeatures() {
        return featureCollection;
    }

    public String getExportFileName() {
        return exportFileName;
    }

    public static class Builder {
        private Locale locale;
        private String externalId;
        private LocalisedString clubName;
        private LocalisedString areaName;
        private GeoJsonObject geometry;
        private GeoJsonObject overlayGeometry;
        private double[] bbox;
        private DateTime modificationTime;
        private GISZoneSizeDTO areaSize;
        private FeatureCollection featureCollection;

        public Builder(final Locale locale) {
            this.locale = Objects.requireNonNull(locale);
        }

        public Builder withExternalId(final String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder withClubName(final LocalisedString clubName) {
            this.clubName = clubName;
            return this;
        }

        public Builder withAreaName(final LocalisedString areaName) {
            this.areaName = areaName;
            return this;
        }

        public Builder withModificationTime(final DateTime modificationTime) {
            this.modificationTime = modificationTime;
            return this;
        }

        public Builder withGeometry(final GeoJsonObject geometry) {
            this.geometry = geometry;
            return this;
        }

        public Builder withGeometry(final Geometry geometry) {
            this.geometry = PolygonConversionUtil.javaToGeoJSON(geometry);
            return this;
        }

        public Builder withOverlayGeometry(final Geometry overlayGeometry) {
            if (overlayGeometry != null) {
                this.overlayGeometry = PolygonConversionUtil.javaToGeoJSON(overlayGeometry);
            }
            return this;
        }

        public Builder withBbox(final double[] bbox) {
            this.bbox = bbox;
            return this;
        }

        public Builder withAreaSize(final GISZoneSizeDTO size) {
            this.areaSize = size;
            return this;
        }

        public Builder withFeatureCollection(final FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
            return this;
        }

        private String formatAreaNameAndSaveDate() {
            final String areaNameI18n = this.areaName.getAnyTranslation(locale);
            return modificationTime != null ? (areaNameI18n + " - " + formatSaveDate()) : areaNameI18n;
        }

        private String formatClubName() {
            return clubName.getAnyTranslation(this.locale) + (externalId != null ? " - " + externalId : "");
        }

        private String formatWaterLandTotalAreaSize() {
            if (areaSize == null) {
                return "";
            }

            final LocalisedString areaStringFormat = new LocalisedString(
                    "Maa %s, vesi %s, yhteensä %s",
                    "Markyta %s, vattenarealen %s, total %s");

            return String.format(areaStringFormat.getAnyTranslation(locale),
                    formatAreaSize(areaSize.getAll().getLand()),
                    formatAreaSize(areaSize.getAll().getWater()),
                    formatAreaSize(areaSize.getAll().getTotal()));
        }

        private String formatStatePrivateAreaSize() {
            if (areaSize == null) {
                return "";
            }

            if (areaSize.getPrivateLandAreaSize() < 1 && areaSize.getStateLandAreaSize() < 1) {
                return "";
            }

            final LocalisedString statePrivateStringFormat = new LocalisedString(
                    "Valtionmaa-alue %s, yksityismaa-alue %s",
                    "Statsägda markyta %s, privatägda markyta %s");

            return String.format(statePrivateStringFormat.getAnyTranslation(locale),
                    formatAreaSize(areaSize.getStateLandAreaSize()),
                    formatAreaSize(areaSize.getPrivateLandAreaSize()));
        }

        private String formatAreaSize(final double areaSize) {
            final LocalisedString unitName = new LocalisedString("ha", "hektar");
            return NumberUtils.squareMetersToHectares(areaSize) + " " + unitName.getTranslation(locale);
        }

        private String formatSaveDate() {
            if (modificationTime == null) {
                return "";
            }

            final LocalisedString title = new LocalisedString("tallennettu", "sparade");
            return title.getTranslation(locale) + " " + DTF.print(DateUtil.toLocalDateTimeNullSafe(modificationTime));
        }

        private String formatExportFileName() {
            final String ts = DTF_FILENAME.print(modificationTime != null
                    ? DateUtil.toLocalDateTimeNullSafe(modificationTime)
                    : DateUtil.today());

            final String areaNameI18n = F.mapNullable(areaName, name -> name.getAnyTranslation(locale));

            if (StringUtils.hasText(areaNameI18n)) {
                return ts + "-" + areaNameI18n + ".pdf";
            }

            return ts + ".pdf";
        }

        private Feature createBaseFeature() {
            final Feature feature = new Feature();
            feature.setGeometry(geometry);
            feature.setBbox(bbox);

            feature.setProperty("fill", "rgb(255, 0, 0)");
            feature.setProperty("fill-opacity", 0.4);
            feature.setProperty("stroke-width", 2.0);
            feature.setProperty("stroke", "rgb(0,0,0)");

            final String mainTitle = formatClubName();
            final String subTitle1 = formatAreaNameAndSaveDate();
            final String subTitle2 = formatWaterLandTotalAreaSize();
            final String subTitle3 = formatStatePrivateAreaSize();

            feature.setProperty("mainTitle", mainTitle);
            feature.setProperty("subTitle1", subTitle1);
            feature.setProperty("subTitle2", subTitle2);
            feature.setProperty("subTitle3", subTitle3);

            // TODO: Remove after migration
            feature.setProperty("clubName", mainTitle);
            feature.setProperty("areaName", subTitle1);
            feature.setProperty("areaSize", subTitle2);
            feature.setProperty("saveDate", subTitle3);

            return feature;
        }

        private Feature createOverlayFeature() {
            final Feature feature = new Feature();
            feature.setGeometry(overlayGeometry);
            feature.setBbox(bbox);

            feature.setProperty("fill", "rgb(154, 101, 0)");
            feature.setProperty("fill-opacity", 0.3);
            feature.setProperty("stroke-width", 1.0);
            feature.setProperty("stroke", "rgb(0,0,0)");

            return feature;
        }

        public MapPdfModel build() {
            Objects.requireNonNull(bbox, "bbox is null");

            if (featureCollection == null) {
                Objects.requireNonNull(geometry, "geometry is null");
                Objects.requireNonNull(areaName, "areaName is null");
                Objects.requireNonNull(clubName, "clubName is null");

                featureCollection = new FeatureCollection();
                featureCollection.setCrs(GISUtils.SRID.ETRS_TM35FIN.getGeoJsonCrs());
                featureCollection.setBbox(bbox);
                featureCollection.add(createBaseFeature());
            }

            if (overlayGeometry != null) {
                featureCollection.add(createOverlayFeature());
            }

            return new MapPdfModel(featureCollection, this.bbox, formatExportFileName());
        }
    }
}
