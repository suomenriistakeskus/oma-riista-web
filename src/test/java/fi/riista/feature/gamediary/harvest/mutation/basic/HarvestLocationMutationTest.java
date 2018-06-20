package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.GeoLocationInvalidException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestLocationSourceRequiredException;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.util.VersionedTestExecutionSupport;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HarvestLocationMutationTest implements VersionedTestExecutionSupport<HarvestSpecVersion> {

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(HarvestSpecVersion.class));
    }

    @Nonnull
    private Harvest createExistingHarvest(final GeoLocation location) {
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
    private static MobileHarvestDTO forMobile(final HarvestSpecVersion specVersion,
                                              final GeoLocation geoLocation) {
        final MobileHarvestDTO dto = new MobileHarvestDTO();
        dto.setHarvestSpecVersion(specVersion);
        dto.setGeoLocation(geoLocation);
        return dto;
    }

    // WEB

    @Test
    public void testWeb_Create() {
        final Harvest harvest = new Harvest();
        final HarvestDTO dto = forWeb(new GeoLocation(1, 2, GeoLocation.Source.MANUAL));

        HarvestLocationMutation.createForWeb(dto, null).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
    }

    @Test
    public void testWeb_Create_WithoutSource() {
        final Harvest harvest = new Harvest();
        final HarvestDTO dto = forWeb(new GeoLocation(1, 2, null));

        HarvestLocationMutation.createForWeb(dto, null).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
    }

    @Test(expected = GeoLocationInvalidException.class)
    public void testWeb_Create_InvalidLatitude() {
        final Harvest harvest = new Harvest();
        final GeoLocation originalLocation = new GeoLocation(0, 2, GeoLocation.Source.MANUAL);
        final HarvestDTO dto = forWeb(originalLocation);

        HarvestLocationMutation.createForWeb(dto, originalLocation).accept(harvest);
    }

    @Test(expected = GeoLocationInvalidException.class)
    public void testWeb_Create_InvalidLongitude() {
        final Harvest harvest = new Harvest();
        final HarvestDTO dto = forWeb(new GeoLocation(1, 0, GeoLocation.Source.MANUAL));

        HarvestLocationMutation.createForWeb(dto, null).accept(harvest);
    }

    @Test
    public void testWeb_Update() {
        final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(new GeoLocation(1, 2, GeoLocation.Source.GPS_DEVICE));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    @Test
    public void testWeb_Update_UpdateSource() {
        final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(new GeoLocation(1, 2, GeoLocation.Source.MANUAL));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
    }

    @Test(expected = GeoLocationInvalidException.class)
    public void testWeb_Update_InvalidLatitude() {
        final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(new GeoLocation(0, 2, GeoLocation.Source.GPS_DEVICE));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    @Test(expected = GeoLocationInvalidException.class)
    public void testWeb_Update_InvalidLongitude() {
        final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
        final Harvest harvest = createExistingHarvest(existingLocation);
        final HarvestDTO dto = forWeb(new GeoLocation(1, 0, GeoLocation.Source.GPS_DEVICE));

        HarvestLocationMutation.createForWeb(dto, existingLocation).accept(harvest);

        assertEquals(1, harvest.getGeoLocation().getLatitude());
        assertEquals(2, harvest.getGeoLocation().getLongitude());
        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
    }

    // MOBILE

    @Test
    public void testMobile_Create() {
        forEachVersion(specVersion -> {
            final Harvest harvest = new Harvest();
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 2, GeoLocation.Source.GPS_DEVICE));

            HarvestLocationMutation.createForMobile(dto, null).accept(harvest);

            assertEquals(1, harvest.getGeoLocation().getLatitude());
            assertEquals(2, harvest.getGeoLocation().getLongitude());
            assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
        });
    }

    @Test
    public void testMobile_Create_UseDefaultSource() {
        forEachVersion(specVersion -> {
            final Harvest harvest = new Harvest();
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 2, null));

            HarvestLocationMutation.createForMobile(dto, null).accept(harvest);

            assertEquals(1, harvest.getGeoLocation().getLatitude());
            assertEquals(2, harvest.getGeoLocation().getLongitude());
            assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
        });
    }

    @Test
    public void testMobile_Create_InvalidLatitude() {
        forEachVersion(specVersion -> {
            final Harvest harvest = new Harvest();
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(0, 2, GeoLocation.Source.GPS_DEVICE));

            try {
                HarvestLocationMutation.createForMobile(dto, null).accept(harvest);
                fail("Should throw GeoLocationInvalidException");

            } catch (GeoLocationInvalidException ignore) {
            }
        });
    }

    @Test
    public void testMobile_Create_InvalidLongitude() {
        forEachVersion(specVersion -> {
            final Harvest harvest = new Harvest();
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 0, GeoLocation.Source.GPS_DEVICE));

            try {
                HarvestLocationMutation.createForMobile(dto, null).accept(harvest);
                fail("Should throw GeoLocationInvalidException");

            } catch (GeoLocationInvalidException ignore) {
            }
        });
    }

    @Test
    public void testMobile_Update() {
        forEachVersion(specVersion -> {
            final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
            final Harvest harvest = createExistingHarvest(existingLocation);
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 2, GeoLocation.Source.GPS_DEVICE));

            HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);

            assertEquals(1, harvest.getGeoLocation().getLatitude());
            assertEquals(2, harvest.getGeoLocation().getLongitude());
            assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
        });
    }

    @Test
    public void testMobile_Update_ManualLocation() {
        forEachVersion(specVersion -> {
            final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
            final Harvest harvest = createExistingHarvest(existingLocation);
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 2, GeoLocation.Source.MANUAL));

            HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);

            assertEquals(1, harvest.getGeoLocation().getLatitude());
            assertEquals(2, harvest.getGeoLocation().getLongitude());
            assertEquals(GeoLocation.Source.MANUAL, harvest.getGeoLocation().getSource());
        });
    }

    @Test
    public void testMobile_Update_KeepOriginalIfSourceMissing() {
        forEachVersionBefore(HarvestSpecVersion.LOWEST_VERSION_REQUIRING_LOCATION_SOURCE, specVersion -> {
            final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
            final Harvest harvest = createExistingHarvest(existingLocation);
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 2, null));

            HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);

            assertEquals(10, harvest.getGeoLocation().getLatitude());
            assertEquals(20, harvest.getGeoLocation().getLongitude());
            assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
        });
    }

    @Test
    public void testMobile_Update_SourceRequiredBySpecVersion() {
        forEachVersionStartingFrom(HarvestSpecVersion.LOWEST_VERSION_REQUIRING_LOCATION_SOURCE, specVersion -> {
            final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
            final Harvest harvest = createExistingHarvest(existingLocation);
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 2, null));

            try {
                HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);
                fail("Should throw HarvestLocationSourceRequiredException");

            } catch (HarvestLocationSourceRequiredException ignore) {
            }
        });
    }

    @Test
    public void testMobile_Update_InvalidLatitude() {
        forEachVersion(specVersion -> {
            final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
            final Harvest harvest = createExistingHarvest(existingLocation);
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(0, 2, GeoLocation.Source.GPS_DEVICE));

            try {
                HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);
                fail("Should throw GeoLocationInvalidException");

            } catch (GeoLocationInvalidException ignore) {
            }
        });
    }

    @Test
    public void testMobile_Update_InvalidLongitude() {
        forEachVersion(specVersion -> {
            final GeoLocation existingLocation = new GeoLocation(10, 20, GeoLocation.Source.GPS_DEVICE);
            final Harvest harvest = createExistingHarvest(existingLocation);
            final MobileHarvestDTO dto = forMobile(specVersion, new GeoLocation(1, 0, GeoLocation.Source.GPS_DEVICE));

            try {
                HarvestLocationMutation.createForMobile(dto, existingLocation).accept(harvest);
                fail("Should throw GeoLocationInvalidException");

            } catch (GeoLocationInvalidException ignore) {
            }
        });
    }
}
