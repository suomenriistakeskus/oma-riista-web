package fi.riista.integration.mml.service;

import fi.riista.feature.gis.GISPoint;
import fi.riista.integration.mml.dto.MMLPalstanTietoja;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MMLPalstanTietojaServiceTest extends AbstractMMLServiceTest {

    private MMLPalstanTietojaService palstanTietojaService;

    @Before
    public void init() throws IOException {
        this.palstanTietojaService = new MMLPalstanTietojaService(createRequestTemplate());
    }

    @Ignore
    @Test
    public void testSimple() {
        List<MMLPalstanTietoja> result = palstanTietojaService.findByPosition(GISPoint.create(6962313.418, 670872.931));
        assertThat(result, hasSize(1));

        MMLPalstanTietoja palstanTietoja = result.get(0);
        assertThat(palstanTietoja.getPropertyIdentifier(), equalTo("16743700010590"));

        String gml = palstanTietoja.getGmlGeometry();

        assertThat(gml, is(notNullValue()));
        assertThat(gml, containsString("gml:Surface"));
        assertThat(gml, containsString("EPSG:3067"));
        assertThat(gml, containsString("gml:PolygonPatch"));
        assertThat(gml, containsString("gml:patches"));
        assertThat(gml, containsString("gml:exterior"));
        assertThat(gml, containsString("gml:LinearRing"));
        assertThat(gml, containsString("gml:posList"));
    }
}
