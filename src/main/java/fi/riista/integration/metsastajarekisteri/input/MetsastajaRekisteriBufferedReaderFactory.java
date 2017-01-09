package fi.riista.integration.metsastajarekisteri.input;

import fi.riista.util.OpenSSLPBEInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

@Component
public class MetsastajaRekisteriBufferedReaderFactory implements BufferedReaderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriBufferedReaderFactory.class);

    private static final String CIPHER = "PBEWITHMD5AND256BITAES-CBC-OPENSSL";

    private final String encryptionPassword;

    public MetsastajaRekisteriBufferedReaderFactory(
            @Value("${batch.metsastajarekisteri.encryptionPassword}") String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    @Override
    public BufferedReader create(Resource resource, String encoding) throws IOException {
        LOG.info("Processing inputFileName {}", resource.getFilename());

        String filename = resource.getFilename();
        InputStream inputStream = resource.getInputStream();

        if (filename.endsWith(".enc") || filename.endsWith(".enc.gz")) {
            if (encryptionPassword == null) {
                throw new IllegalStateException("Password is not defined");
            }

            inputStream = new OpenSSLPBEInputStream(inputStream, CIPHER, 1,
                    encryptionPassword.toCharArray());
        }

        if (filename.endsWith(".gz") || filename.endsWith("gz.enc")) {
            inputStream = new GZIPInputStream(inputStream);
        }

        return new BufferedReader(new InputStreamReader(inputStream, encoding));
    }
}
