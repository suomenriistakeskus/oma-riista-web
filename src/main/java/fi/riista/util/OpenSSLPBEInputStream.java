package fi.riista.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.InputStream;

import static fi.riista.util.OpenSSLPBECommon.OPENSSL_HEADER_ENCODING;
import static fi.riista.util.OpenSSLPBECommon.OPENSSL_HEADER_STRING;
import static fi.riista.util.OpenSSLPBECommon.SALT_SIZE_BYTES;

/*
source: http://stackoverflow.com/questions/11783062/how-to-decrypt-an-encrypted-file-in-java-with-openssl-with-aes
*/
public class OpenSSLPBEInputStream extends InputStream {

    protected final static int READ_BLOCK_SIZE = 64 * 1024;

    private final Cipher cipher;
    private final InputStream inStream;

    private final byte[] bufferCipher = new byte[READ_BLOCK_SIZE];
    private byte[] bufferClear = null;

    private int index = Integer.MAX_VALUE;
    private int maxIndex = 0;

    public OpenSSLPBEInputStream(final InputStream streamIn,
                                 final String algIn,
                                 final int iterationCount,
                                 final char[] password) throws IOException {

        this.inStream = streamIn;

        readHeader();
        final byte[] salt = readSalt();

        try {
            this.cipher = OpenSSLPBECommon.initializeCipher(password, salt, Cipher.DECRYPT_MODE, algIn, iterationCount);
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    private void readHeader() throws IOException {
        final byte[] headerBytes = new byte[OPENSSL_HEADER_STRING.length()];
        inStream.read(headerBytes);
        final String headerString = new String(headerBytes, OPENSSL_HEADER_ENCODING);

        if (!OPENSSL_HEADER_STRING.equals(headerString)) {
            throw new IOException("unexpected file header " + headerString);
        }
    }

    private byte[] readSalt() throws IOException {
        final byte[] salt = new byte[SALT_SIZE_BYTES];
        inStream.read(salt);
        return salt;
    }

    @Override
    public int available() throws IOException {
        return inStream.available();
    }

    @Override
    public int read() throws IOException {
        if (maxIndex < 0) {
            return -1;
        }
        if (index > maxIndex) {
            index = 0;
            final int read = inStream.read(bufferCipher);

            if (read != -1) {
                bufferClear = cipher.update(bufferCipher, 0, read);
            }

            if (read == -1 || bufferClear == null || bufferClear.length == 0) {
                try {
                    bufferClear = cipher.doFinal();
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    bufferClear = null;
                }
            }

            maxIndex = bufferClear == null ? -1 : bufferClear.length - 1;

            if (maxIndex < 0) {
                return -1;
            }
        }

        return bufferClear[index++] & 0xff;
    }
}
