package fi.riista.integration.mml.parser;

import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MMLRekisteriyksikonTietojaParserTest {

    @Test
    public void testParser() throws Exception {
        List<MMLRekisteriyksikonTietoja> result = MMLRekisteriyksikonTietojaParser.parse(getTestXmlResource());

        assertThat(result, not(empty()));
        assertThat(result, hasSize(3));
        assertThat(result.get(0).getPropertyIdentifier(), equalTo("09141000010004"));
        assertThat(result.get(1).getPropertyIdentifier(), equalTo("09101799010000"));
        assertThat(result.get(2).getPropertyIdentifier(), equalTo("09101700130001"));

        assertThat(result.get(0).getMunicipalityCode(), equalTo("167"));
        assertThat(result.get(1).getMunicipalityCode(), nullValue());
        assertThat(result.get(2).getMunicipalityCode(), equalTo("380"));
    }

    private Document getTestXmlResource() throws Exception {
        ClassPathResource xmlResource = new ClassPathResource("rekisteriyksikonTietoja.xml", getClass());
        assertTrue(xmlResource.exists());

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final DocumentBuilder builder = dbf.newDocumentBuilder();

        try (final InputStream is = xmlResource.getInputStream()) {
            return builder.parse(is);
        }
    }
}
