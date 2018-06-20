package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static fi.riista.test.TestUtils.expectValidationException;

public class MobileHarvestFeatureV2Test extends MobileHarvestFeatureTest {

    @Override
    protected int getApiVersion() {
        return 2;
    }

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return Arrays.asList(HarvestSpecVersion._3, HarvestSpecVersion._4, HarvestSpecVersion._5);
    }

    @Test
    public void testCreateHarvest_whenMobileClientRefIdIsNull() {
        forEachVersion(expectValidationException(specVersion -> {

            invokeCreateHarvest(newDTOBuilderAndFixtureForCreate(specVersion).withMobileClientRefId(null).build());
        }));
    }

    @Test
    public void testCreateHarvest_whenGeoLocationSourceIsNull() {
        forEachVersion(expectValidationException(specVersion -> {

            final MobileHarvestDTO dto = newDTOBuilderAndFixtureForCreate(specVersion).build();
            dto.getGeoLocation().setSource(null);

            invokeCreateHarvest(dto);
        }));
    }

    @Test
    public void testUpdateHarvest_whenSpecimensIsNull() {
        forEachVersion(expectValidationException(specVersion -> {

            invokeCreateHarvest(newDTOBuilderAndFixtureForUpdate(specVersion).withSpecimens(null).build());
        }));
    }
}
