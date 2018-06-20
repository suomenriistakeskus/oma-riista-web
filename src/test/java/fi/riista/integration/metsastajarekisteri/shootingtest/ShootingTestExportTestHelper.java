package fi.riista.integration.metsastajarekisteri.shootingtest;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

@Component
public class ShootingTestExportTestHelper {

    @Resource(name = "shootingTestExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    public MR_ShootingTestRegistry unmarshal(final byte[] xmlBytes) throws IOException {
        try (final InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(xmlBytes))) {

            final Object unmarshalled = jaxbMarshaller.unmarshal(new StreamSource(reader));
            assertEquals(MR_ShootingTestRegistry.class, unmarshalled.getClass());

            return (MR_ShootingTestRegistry) unmarshalled;
        }
    }
}
