package fi.riista.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.InputStream;

/*
source: http://stackoverflow.com/questions/11783062/how-to-decrypt-an-encrypted-file-in-java-with-openssl-with-aes
*/
public class OpenSSLPBEInputStream extends InputStream {
    private static final String OPENSSL_HEADER_STRING = "Salted__";
    private static final String OPENSSL_HEADER_ENCODE = "ASCII";
    private final static int READ_BLOCK_SIZE = 64 * 1024;
    private static final int SALT_SIZE_BYTES = 8;

    private static Cipher initializeCipher(final char[] password, final byte[] salt, final int cipherMode,
                                           final String algorithm, final int iterationCount) throws Exception {

        final PBEKeySpec keySpec = new PBEKeySpec(password);
        final SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        final SecretKey key = factory.generateSecret(keySpec);

        final Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(cipherMode, key, new PBEParameterSpec(salt, iterationCount));

        return cipher;
    }

    private final Cipher cipher;
    private final InputStream inStream;
    private final byte[] bufferCipher = new byte[READ_BLOCK_SIZE];

    private byte[] bufferClear = null;

    private int index = Integer.MAX_VALUE;
    private int maxIndex = 0;

    public OpenSSLPBEInputStream(final InputStream streamIn, final String algIn,
                                 final int iterationCount, final char[] password) throws IOException {
        this.inStream = streamIn;
        readHeader();
        final byte[] salt = readSalt();

        try {
            cipher = initializeCipher(password, salt, Cipher.DECRYPT_MODE, algIn, iterationCount);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void readHeader() throws IOException {
        final byte[] headerBytes = new byte[OPENSSL_HEADER_STRING.length()];
        inStream.read(headerBytes);
        final String headerString = new String(headerBytes, OPENSSL_HEADER_ENCODE);

        if (!OPENSSL_HEADER_STRING.equals(headerString)) {
            throw new IOException("unexpected file header " + headerString);
        }
    }

    private byte[] readSalt() throws IOException {
        byte[] salt = new byte[SALT_SIZE_BYTES];
        inStream.read(salt);
        return salt;
    }

    @Override
    public int available() throws IOException {
        return inStream.available();
    }

    @Override
    public int read() throws IOException {
        if (index > maxIndex) {
            index = 0;
            int read = inStream.read(bufferCipher);
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
            if (bufferClear == null || bufferClear.length == 0) {
                return -1;
            }
            maxIndex = bufferClear.length - 1;
        }
        return bufferClear[index++] & 0xff;
    }
}
