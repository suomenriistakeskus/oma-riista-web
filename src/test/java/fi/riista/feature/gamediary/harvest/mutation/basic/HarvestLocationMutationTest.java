package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.InvalidGeoLocationException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Theories.class)
public class HarvestLocationMutationTest {

    @Nonnull
    private static Harvest createExistingHarvest(final GeoLocation location) {
        final Harvest harvest = new Harvest();
        harvest.setId(1L);
        harvest.setGeoLocation(Objects.requireNonNull(location));
        return harvest;
    }

    @Nonnull
    private static HarvestDTO forWeb(final GeoLocation geoLocation) {
        final HarvestDTO dto = new HarvestDTO();
        dto.setGeoLocation(geoLocation);
        return dto;
    }

    @Nonnull
    private static MobileHarvestDTO forMobile(final HarvestSpecVersion specVersion, final GeoLocation geoLocation) {
        final MobileHarvestDTO dto = new MobileHarvestDTO();
        dto.setHarvestSpecVersion(specVersion);
        dto.setGeoLocation(geoLocation);
        return dto;
    }

    // WEB

    @Test
    public void testWeb_create() {
        final Harvest harvest = new Harvest();
        final HarvestDTO dto = forWeb(new GeoLocation(1, 2, GeoLocation.Source.MANUAL));

        HarvestLocationMutation.createForWeb(dto, null).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
    }

    @Test
    public void testWeb_create_withoutSource() {
        final Harvest harvest = new Harvest();
        final HarvestDTO dto = forWeb(new GeoLocation(1, 2, null));

        HarvestLocationMutation.createForWeb(dto, null).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
    }

    @Test(expected = InvalidGeoLocationException.class)
    public void testWeb_create_invalidLatitude() {
        final Harvest harvest = new Harvest();
        final GeoLocation originalLocation = new GeoLocation(0, 2, GeoLocation.Source.MANUAL);
        final HarvestDTO dto = forWeb(originalLocation);

        HarvestLocationMutation.createForWeb(dto, originalLocation).accept(harvest);
    }

    @Test(expected = InvalidGeoLocationException.class)
    public void testWeb_create_invalidLongitude() {
        final Harvest harvest = new Harvest();
        final HarvestDTO dto = forWeb(new GeoLocation(1, 0, GeoLocation.Source.MANUAL));

        HarvestLocationMutation.createForWeb(dto, null).accept(harvest);
    }

    @Test
    public void testWeb_update() {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(newGpsLocation(1, 2));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    @Test
    public void testWeb_update_updateSource() {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(new GeoLocation(1, 2, GeoLocation.Source.MANUAL));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
    }

    @Test(expected = InvalidGeoLocationException.class)
    public void testWeb_update_invalidLatitude() {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(newGpsLocation(0, 2));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    @Test(expected = InvalidGeoLocationException.class)
    public void testWeb_update_invalidLongitude() {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(newGpsLocation(1, 0));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    // MOBILE

    @Theory
    public void testMobile_create(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        final MobileHarvestDTO dto = forMobile(specVersion, newGpsLocation(1, 2));

        HarvestLocationMutation.createForMobile(dto, null).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    @Theory
    public void testMobile_create_invalidLatitude(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        final MobileHarvestDTO dto = forMobile(specVersion, newGpsLocation(0, 2));

        try {
            HarvestLocationMutation.createForMobile(dto, null).accept(harvest);
            fail("Should throw GeoLocationInvalidException");

        } catch (InvalidGeoLocationException ignore) {
        }
    }

    @Theory
    public void testMobile_create_invalidLongitude(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        final MobileHarvestDTO dto = forMobile(specVersion, newGpsLocation(1, 0));

        try {
            HarvestLocationMutation.createForMobile(dto, null).accept(harvest);
            fail("Should throw GeoLocationInvalidException");

        } catch (InvalidGeoLocationException ignore) {
        }
    }

    @Theory
    public void testMobile_update(final HarvestSpecVersion specVersion) {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final MobileHarvestDTO dto = forMobile(specVersion, newGpsLocation(1, 2));

        HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    @Theory
    public void testMobile_update_manualLocation(final HarvestSpecVersion specVersion) {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 2, GeoLocation.Source.MANUAL));

        HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
    }

    @Theory
    public void testMobile_update_invalidLatitude(final HarvestSpecVersion specVersion) {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final MobileHarvestDTO dto = forMobile(specVersion, newGpsLocation(0, 2));

        try {
            HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);
            fail("Should throw GeoLocationInvalidException");

        } catch (InvalidGeoLocationException ignore) {
        }
    }

    @Theory
    public void testMobile_update_invalidLongitude(final HarvestSpecVersion specVersion) {
        final GeoLocation existingLocation = newGpsLocation(10, 20);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final MobileHarvestDTO dto = forMobile(specVersion, newGpsLocation(1, 0));

        try {
            HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);
            fail("Should throw GeoLocationInvalidException");

        } catch (InvalidGeoLocationException ignore) {
        }
    }

    private static GeoLocation newGpsLocation(final int lat, final int lng) {
        return new GeoLocation(lat, lng, GeoLocation.Source.GPS_DEVICE);
    }
}
