package fi.riista.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;

import static fi.riista.util.OpenSSLPBECommon.OPENSSL_HEADER_ENCODING;
import static fi.riista.util.OpenSSLPBECommon.OPENSSL_HEADER_STRING;
import static fi.riista.util.OpenSSLPBECommon.SALT_SIZE_BYTES;

/*
source: http://stackoverflow.com/questions/11783062/how-to-decrypt-an-encrypted-file-in-java-with-openssl-with-aes
*/
public class OpenSSLPBEOutputStream extends OutputStream {

    protected static final int WRITE_BLOCK_SIZE = 64 * 1024;

    private final Cipher cipher;
    private final OutputStream outStream;
    private final byte[] buffer = new byte[WRITE_BLOCK_SIZE];
    private int bufferIndex = 0;

    public OpenSSLPBEOutputStream(final OutputStream outputStream,
                                  final String algIn,
                                  final int iterationCount,
                                  final char[] password)
            throws IOException {

        this.outStream = outputStream;

        try {
            // Create and use a random SALT for each instance of this output stream.
            final byte[] salt = new byte[SALT_SIZE_BYTES];
            new SecureRandom().nextBytes(salt);
            this.cipher = OpenSSLPBECommon.initializeCipher(password, salt, Cipher.ENCRYPT_MODE, algIn, iterationCount);

            // Write header.
            writeHeader(salt);

        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(final int b) throws IOException {
        buffer[bufferIndex] = (byte) b;
        bufferIndex++;

        if (bufferIndex == WRITE_BLOCK_SIZE) {
            final byte[] result = cipher.update(buffer, 0, bufferIndex);
            outStream.write(result);
            bufferIndex = 0;
        }
    }

    @Override
    public void flush() throws IOException {
        if (bufferIndex > 0) {
            try {
                final byte[] result = cipher.doFinal(buffer, 0, bufferIndex);
                outStream.write(result);
            } catch (final IllegalBlockSizeException | BadPaddingException e) {
                throw new IOException(e);
            }
            bufferIndex = 0;
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        outStream.close();
    }

    private void writeHeader(final byte[] salt) throws IOException {
        outStream.write(OPENSSL_HEADER_STRING.getBytes(OPENSSL_HEADER_ENCODING));
        outStream.write(salt);
    }
}
