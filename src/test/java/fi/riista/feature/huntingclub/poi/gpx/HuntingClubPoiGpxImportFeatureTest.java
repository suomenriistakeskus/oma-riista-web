package fi.riista.feature.huntingclub.poi.gpx;

import fi.riista.feature.huntingclub.poi.PoiLocationDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class HuntingClubPoiGpxImportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubPoiGpxImportFeature feature;

    @Test
    public void testConvertGpxPoints_garminWaypoints() throws IOException {
        final byte[] gpxFile = Files.readAllBytes(new File("src/test/java/fi/riista/feature/huntingclub/poi/gpx/garmin_waypoints.gpx").toPath());
        final MultipartFile file = new MockMultipartFile("test.gpx", "/test.gpx", "application/octet-stream", gpxFile);

        final List<PoiLocationDTO> actual = feature.convertGpxPoints(file);

        assertThat(actual, hasSize(2));
        final PoiLocationDTO poiOne = actual.get(0);
        assertThat(poiOne.getDescription(), equalTo(  "362"));
        assertThat(poiOne.getGeoLocation().getLatitude(), equalTo(  7203701));
        assertThat(poiOne.getGeoLocation().getLongitude(), equalTo(  586407));
        final PoiLocationDTO poiTwo = actual.get(1);
        assertThat(poiTwo.getDescription(), equalTo(  "363"));
        assertThat(poiTwo.getGeoLocation().getLatitude(), equalTo(  7203745));
        assertThat(poiTwo.getGeoLocation().getLongitude(), equalTo(  586388));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertGpxPoints_garminProprietary_notSupported() throws IOException {
        final byte[] gpxFile = Files.readAllBytes(new File("src/test/java/fi/riista/feature/huntingclub/poi/gpx/garmin_proprietary.gpx").toPath());
        final MultipartFile file = new MockMultipartFile("test.gpx", "/test.gpx", "application/octet-stream", gpxFile);
        feature.convertGpxPoints(file);
    }

    @Test
    public void testConvertGpxPoints_tracker() throws IOException {
        final byte[] gpxFile = Files.readAllBytes(new File("src/test/java/fi/riista/feature/huntingclub/poi/gpx/tracker.gpx").toPath());
        final MultipartFile file = new MockMultipartFile("test.gpx", "/test.gpx", "application/octet-stream", gpxFile);

        final List<PoiLocationDTO> actual = feature.convertGpxPoints(file);

        assertThat(actual, hasSize(1));
        final PoiLocationDTO poi = actual.get(0);
        assertThat(poi.getDescription(), equalTo(  "Paratiisi"));
        assertThat(poi.getGeoLocation().getLatitude(), equalTo(  7080046));
        assertThat(poi.getGeoLocation().getLongitude(), equalTo(  631767));
    }

}