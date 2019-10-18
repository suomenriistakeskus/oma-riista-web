package fi.riista.integration.metsastajarekisteri.shootingtest;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Stopwatch;
import fi.riista.config.Constants;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.SortedMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Component
public class ShootingTestExportService {

    private static final Logger LOG = LoggerFactory.getLogger(ShootingTestExportService.class);

    private static final String NAMESPACE;
    private static final String ROOT_ELEMENT_NAME;
    private static final String REGISTER_DATE_ELEMENT_NAME = "RegisterDate";
    private static final String PERSON_LIST_ELEMENT_NAME = "Persons";

    static {
        NAMESPACE = MR_ShootingTestRegistry.class.getPackage().getAnnotation(XmlSchema.class).namespace();
        ROOT_ELEMENT_NAME = MR_ShootingTestRegistry.class.getAnnotation(XmlRootElement.class).name();
    }

    // Jackson mapper is used instead of Spring Jaxb2Marshaller in order to be able to use StAX API.
    @Resource
    private XmlMapper xmlMapper;

    @Resource
    private ShootingTestExportQueries queries;

    @Value("${shootingtest.export.batch.size}")
    private int batchSize;

    // Exposed for test code.
    void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    @Transactional(readOnly = true, rollbackFor = {IOException.class, XMLStreamException.class})
    public byte[] exportShootingTestData(final LocalDate registerDate) throws IOException, XMLStreamException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        // Using StAX API for lower memory footprint
        final XMLOutputFactory xof = XMLOutputFactory.newInstance();
        final XMLStreamWriter xsw = xof.createXMLStreamWriter(os, Constants.DEFAULT_ENCODING);

        writeDocumentStart(xsw, registerDate);
        addPersons(xsw, registerDate);
        writeDocumentEnd(xsw);

        xsw.close();

        return os.toByteArray();
    }

    private void addPersons(final XMLStreamWriter xsw, final LocalDate registerDate) throws IOException {
        String lastHunterNumberAlreadyProcessed = "10000000"; // lower than any valid hunter number
        SortedMap<String, MR_Person> personIndex = null;
        Stopwatch stopwatch = Stopwatch.createStarted();

        do {
            // Persons need to be fetched from database by ascending hunter number order.
            // Because of the XML structure shooting test attempts for one person need to be
            // serialized all at once.

            personIndex = queries.fetchPersonsWithHunterNumberGreaterThan(
                    lastHunterNumberAlreadyProcessed, batchSize, registerDate);

            if (!personIndex.isEmpty()) {
                for (final MR_Person person : personIndex.values()) {
                    if (!person.getValidTests().getShootingTest().isEmpty()) {
                        xmlMapper.writeValue(xsw, person);
                    }
                }

                // Reset offset for next round.
                lastHunterNumberAlreadyProcessed = personIndex.lastKey();
            }

            if (LOG.isDebugEnabled()) {
                final long elapsed = stopwatch.elapsed(MILLISECONDS);
                LOG.debug("Processed a batch of {} persons, took {} ms.", personIndex.size(), elapsed);

                // Reset Stopwatch object for next round.
                stopwatch = Stopwatch.createStarted();
            }

        } while (personIndex.size() == batchSize);
    }

    private static void writeDocumentStart(final XMLStreamWriter xsw, final LocalDate date) throws XMLStreamException {
        // Write the default XML declaration.
        xsw.writeStartDocument();
        xsw.writeCharacters("\n");
        xsw.writeCharacters("\n");

        // Open root element.
        xsw.writeStartElement(ROOT_ELEMENT_NAME);
        xsw.writeNamespace("", NAMESPACE);
        xsw.writeCharacters("\n");

        xsw.writeStartElement(REGISTER_DATE_ELEMENT_NAME);
        xsw.writeCharacters(date.toString());
        xsw.writeEndElement();
        xsw.writeCharacters("\n");

        // Open person list element.
        xsw.writeStartElement(PERSON_LIST_ELEMENT_NAME);
        xsw.writeCharacters("\n");
    }

    private static void writeDocumentEnd(final XMLStreamWriter xsw) throws XMLStreamException {
        // Close person list element.
        xsw.writeEndElement();
        xsw.writeCharacters("\n");

        // Close root element.
        xsw.writeEndElement();
        xsw.writeCharacters("\n");

        xsw.writeEndDocument();
    }
}
