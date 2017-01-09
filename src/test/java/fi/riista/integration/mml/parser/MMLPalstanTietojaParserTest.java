package fi.riista.integration.mml.parser;

import fi.riista.integration.mml.dto.MMLPalstanTietoja;
import fi.riista.integration.mml.support.WFSUtil;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MMLPalstanTietojaParserTest {

    @Test
    public void testSimple() throws Exception {
        try (InputStream testXmlResource = getTestXmlResource()) {
            List<MMLPalstanTietoja> result = MMLPalstanTietojaParser.parse(WFSUtil.parse(testXmlResource));

            assertNotNull(result);
            assertEquals(2, result.size());

            assertProperty(result.get(0), "83706571150005", new LocalDate(2012, 2, 7));
            assertProperty(result.get(1), "88702678115003", new LocalDate(2014, 3, 30));
        }
    }

    private static void assertProperty(MMLPalstanTietoja mmlProperty, final String propertyIdentifier, LocalDate updatedAt) {
        assertEquals(propertyIdentifier, mmlProperty.getPropertyIdentifier());
        assertEquals(updatedAt, mmlProperty.getLastUpdateAt());
        assertNotNull(mmlProperty.getGmlGeometry());

        final String gml = mmlProperty.getGmlGeometry();

        assertThat(gml, is(notNullValue()));
        assertThat(gml, containsString("gml:Surface"));
        assertThat(gml, containsString("EPSG:3067"));
        assertThat(gml, containsString("gml:PolygonPatch"));
        assertThat(gml, containsString("gml:patches"));
        assertThat(gml, containsString("gml:exterior"));
        assertThat(gml, containsString("gml:LinearRing"));
        assertThat(gml, containsString("gml:posList"));
    }

    private InputStream getTestXmlResource() throws IOException {
        ClassPathResource xmlResource = new ClassPathResource("palstanTietoja.xml", getClass());
        assertTrue(xmlResource.exists());

        return xmlResource.getInputStream();
    }
}
