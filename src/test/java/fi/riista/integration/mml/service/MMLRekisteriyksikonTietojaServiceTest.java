package fi.riista.integration.mml.service;

import fi.riista.feature.gis.GISPoint;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

public class MMLRekisteriyksikonTietojaServiceTest extends AbstractMMLServiceTest {

    private MMLRekisteriyksikonTietojaService rekisteriyksikonTietojaService;

    @Before
    public void init() throws IOException {
        this.rekisteriyksikonTietojaService = new MMLRekisteriyksikonTietojaService(createRequestTemplate());
    }

    @Ignore
    @Test
    public void testSimple() {
        List<MMLRekisteriyksikonTietoja> result = rekisteriyksikonTietojaService.findByPosition(GISPoint.create(6962313.418, 670872.931));
        assertThat(result, hasSize(1));

        MMLRekisteriyksikonTietoja rekisteriyksikonTietoja = result.get(0);

        assertThat(rekisteriyksikonTietoja.getPropertyIdentifier(), equalTo("16743700010590"));
        assertThat(rekisteriyksikonTietoja.getMunicipalityCode(), equalTo("167"));
    }
}
