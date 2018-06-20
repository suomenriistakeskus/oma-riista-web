package fi.riista.integration.mml.service;

import fi.riista.feature.gis.GISPoint;
import fi.riista.integration.mml.support.MMLWebFeatureServiceRequestTemplate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.util.OptionalDouble;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MMLBuildingUnitServiceTest {
    protected static MMLWebFeatureServiceRequestTemplate createRequestTemplate() {
        try {
            final ClassPathResource classPathResource = new ClassPathResource("configuration/mml.properties");
            final ResourcePropertySource propertySource = new ResourcePropertySource(classPathResource);
            final String uri = propertySource.getProperty("wfs.building.uri").toString();
            final ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            return MMLWebFeatureServiceRequestTemplate.create(uri, requestFactory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MMLBuildingUnitService mmlBuildingUnitService;

    @Before
    public void before() {
        this.mmlBuildingUnitService = new MMLBuildingUnitService(createRequestTemplate());
    }

    @Test
    @Ignore
    public void testSmoke() {
        final GISPoint point = GISPoint.create(6830839, 298353);
        final int countDWithin = mmlBuildingUnitService.findCountDWithin(point, 500);
        assertEquals(4, countDWithin);
    }

    @Test
    @Ignore
    public void testMinimumDistance() {
        final GISPoint point = GISPoint.create(6830839, 298353);
        final OptionalDouble minDistance = mmlBuildingUnitService.findMinimumDistanceToGeometryDWithin(point, 300);

        assertTrue(minDistance.isPresent());
        assertEquals(115.94, minDistance.getAsDouble(), 0.5);
    }
}
