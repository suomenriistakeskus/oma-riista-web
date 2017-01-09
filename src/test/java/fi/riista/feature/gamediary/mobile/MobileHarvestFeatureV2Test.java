package fi.riista.feature.gamediary.mobile;

import static fi.riista.util.TestUtils.expectValidationException;

import org.junit.Test;

import javax.annotation.Resource;

public class MobileHarvestFeatureV2Test extends MobileHarvestFeatureTest {

    @Resource
    private MobileGameDiaryV2Feature feature;

    @Override
    protected MobileGameDiaryFeature feature() {
        return feature;
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
