package fi.riista.feature.gis.garmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.feature.huntingclub.area.HuntingClubAreaDTO;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GarminPolishFormatConverter {
    private static final String POLYGON_TYPE_CODE = "0x4C";
    private static final String LINE_TYPE_CODE = "0x1C";

    private static final int MAX_GARMIN_ID = 1 << 16;
    private static final int MIN_GARMIN_ID = 1 << 15;

    // For development and testing
    public static void main(String[] args) {
        final ObjectMapper objectMapper = new ObjectMapper();

        final Path source = Paths.get(System.getenv("HOME") + "/area.json");
        final Path target = Paths.get(System.getenv("HOME") + "/gmapsupp.mp");

        try (final InputStream is = new FileInputStream(source.toFile());
             final BufferedInputStream bis = new BufferedInputStream(is)) {
            final HuntingClubAreaDTO area = new HuntingClubAreaDTO();
            area.setId(1L);
            area.setNameFI("omariista");
            final FeatureCollection featureCollection = objectMapper.readValue(bis, FeatureCollection.class);
            final String content = new GarminPolishFormatConverter(featureCollection, area).export();

            System.out.println(content);

            Files.write(target, content.getBytes(StandardCharsets.ISO_8859_1));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final FeatureCollection featureCollection;
    private final HuntingClubAreaDTO area;
    private StringBuilder stringBuilder;

    public GarminPolishFormatConverter(final FeatureCollection featureCollection,
                                       final HuntingClubAreaDTO area) {
        this.featureCollection = featureCollection;
        this.area = area;
    }

    public String export() {
        this.stringBuilder = new StringBuilder();
        printHeader();

        featureCollection.forEach(feature -> {
            if (feature.getGeometry() instanceof Polygon) {
                final Polygon polygon = (Polygon) feature.getGeometry();
                exportPolygon(polygon.getCoordinates());
                exportPolygonBorders(polygon.getExteriorRing());

            } else if (feature.getGeometry() instanceof MultiPolygon) {
                final MultiPolygon multiPolygon = (MultiPolygon) feature.getGeometry();
                multiPolygon.getCoordinates().forEach(this::exportPolygon);
                multiPolygon.getCoordinates().forEach(polygon -> {
                    exportPolygonBorders(polygon.get(0));
                });
            }
        });

        exportPolygon(Collections.singletonList(Arrays.asList(
                new LngLatAlt(29.02086, 60.40264),
                new LngLatAlt(29.03108, 60.40256),
                new LngLatAlt(29.02585, 60.3984))));

        stringBuilder.append("[END]\n\n");

        return stringBuilder.toString();
    }

    private void printHeader() {
        final String garminMapId = String.format("%08d", MAX_GARMIN_ID - area.getId() % MIN_GARMIN_ID);

        stringBuilder
                .append(";#################\n")
                .append(";Created by omariista\n")
                .append(";#################\n")
                .append(";\n")
                .append("[IMG ID]\n")
                .append("ID=").append(garminMapId).append("\n")
                .append("Name=").append(area.getNameFI()).append("\n")
                .append("Elevation=M\n")
                .append("LBLcoding=9\n")
                .append("Codepage=1250\n")
                .append("Marine=N\n")
                .append("Preprocess=F\n")
                .append("TreSize=1000\n")
                .append("POIIndex=N\n")
                .append("Transparent=Y\n")
                .append("Levels=2\n")
                .append("Level0=22\n")
                .append("Level1=18\n")
                .append("Zoom0=0\n")
                .append("Zoom1=1\n")
                .append("[END-IMG ID]\n\n");
    }

    private void exportPolygon(final List<List<LngLatAlt>> polygon) {
        stringBuilder
                .append("[POLYGON]\n")
                .append("Type=").append(POLYGON_TYPE_CODE).append("\n")
                .append("Label=").append(area.getNameFI()).append("\n");
        polygon.forEach(this::exportRing);
        stringBuilder.append("[END-POLYGON]\n\n");
    }

    private void exportPolygonBorders(final List<LngLatAlt> polygonExterior) {
        stringBuilder
                .append("[POLYLINE]\n")
                .append("Type=").append(LINE_TYPE_CODE).append("\n");
        exportRing(polygonExterior);
        stringBuilder.append("[END-POLYLINE]\n\n");
    }

    private void exportRing(final List<LngLatAlt> ring) {
        stringBuilder.append("Data0=");

        for (final LngLatAlt lngLatAlt : ring) {
            stringBuilder.append(String.format("(%.6f,%.6f)", lngLatAlt.getLatitude(), lngLatAlt.getLongitude()));
            stringBuilder.append(',');
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append('\n');
    }
}
