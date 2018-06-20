package fi.riista.integration.fivaldi;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FivaldiRecordServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(FivaldiRecordServiceTest.class);

    // Sample provided by Riistakeskus, altered by zero-padding creditor reference number.
    private static final String TEST_FILE =
            "src/test/resources/fi/riista/integration/fivaldi/Fivaldi-sample-20171108.txt";

    @Test
    public void testParseFromFile() {
        assertEquals(1, parseTestFile().size());
    }

    @Test
    public void testDeserializeAndSerialize() throws IOException {
        final byte[] originalBytes = Files.readAllBytes(Paths.get(TEST_FILE));
        final byte[] reSerializedBytes = serializeToBytes(parseTestFile());

        assertTrue(Arrays.equals(originalBytes, reSerializedBytes));
    }

    @Test
    public void testCreateRecord() throws IOException {
        final LocalDate date = new LocalDate(2017, 11, 7);
        final BigDecimal amount = new BigDecimal(125);
        final FivaldiPaymentMethod paymentMethod = FivaldiPaymentMethod.PAYTRAIL;
        final FivaldiRecordParams params =
                new FivaldiRecordParams(71504687, amount, date, date, 17168046876L, paymentMethod, true);

        final FivaldiRecord record = FivaldiRecordService.createRecord(params);

        final byte[] serializedBytes = serializeToBytes(singletonList(record));
        final byte[] bytesToCompare = Files.readAllBytes(Paths.get(TEST_FILE));

        assertTrue(Arrays.equals(bytesToCompare, serializedBytes));
    }

    private static List<FivaldiRecord> parseTestFile() {
        return FivaldiRecordService.parseFromFile(TEST_FILE);
    }

    private static byte[] serializeToBytes(final List<FivaldiRecord> records) {
        return FivaldiRecordService.serialize(records).getBytes(ISO_8859_1);
    }

    public static void main(final String[] args) {
        try {
            final AtomicInteger recordNumberHolder = new AtomicInteger(1);

            parseTestFile().forEach(record -> {

                final int recordNumber = recordNumberHolder.getAndIncrement();

                LOG.info("#{}: {}", recordNumber, record.getReslas());

                record.getRestap().forEach(item -> LOG.info("#{}: {}", recordNumber, item));

            });
        } catch (final FivaldiRecordParseException e) {
            e.printStackTrace();
        }
    }
}
