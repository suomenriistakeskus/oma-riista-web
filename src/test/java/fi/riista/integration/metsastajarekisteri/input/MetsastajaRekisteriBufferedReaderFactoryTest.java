package fi.riista.integration.metsastajarekisteri.input;

import com.google.common.io.CharStreams;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.h2.engine.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Security;

public class MetsastajaRekisteriBufferedReaderFactoryTest {
    private static final String ENCRYPTION_PASSWORD = "HardPassword";

    private MetsastajaRekisteriBufferedReaderFactory readerFactory;

    // Test data was prepared as follows:
    // echo "Hello World!" > sample.csv
    // gzip -k sample.csv
    // openssl enc -aes-256-cbc -pass pass:HardPassword -salt -in sample.csv.gz -out sample.csv.gz.enc

    @BeforeClass
    public static void initCipher() {
        //JCEUtil.removeJavaCryptographyAPIRestrictions();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void initTest() {
        this.readerFactory = new MetsastajaRekisteriBufferedReaderFactory(ENCRYPTION_PASSWORD);
    }

    @Test
    public void testWithPlain() throws IOException {
        final String content = readContent("sample.csv");
        Assert.assertEquals("Hello World!", content);
    }

    @Test
    public void testWithGzipAndEncryption() throws IOException {
        final String content = readContent("sample.csv.gz");
        Assert.assertEquals("Hello World!", content);
    }

    @Test
    public void testWithGzip() throws IOException {
        final String content = readContent("sample.csv.gz.enc");
        Assert.assertEquals("Hello World!", content);
    }

    private String readContent(final String fileName) throws IOException {
        final ClassPathResource resource = new ClassPathResource(fileName, this.getClass());

        try (BufferedReader bufferedReader = readerFactory.create(resource, Constants.UTF8.name())) {
            final StringBuilder to = new StringBuilder();
            CharStreams.copy(bufferedReader, to);
            return to.toString();
        }
    }
}
