package fi.riista.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OpenSSLPBECodecTest {

    private static final Random RANDOM = new Random();

    @BeforeClass
    public static void setup() {
        JCEUtil.removeJavaCryptographyAPIRestrictions();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testRoundTripEncryption() throws IOException {
        final byte[] plaintext = createPlaintext();
        final char[] password = createPassword();

        final byte[] encrypted = OpenSSLPBECodec.encrypt(plaintext, password);
        assertFalse(Arrays.equals(plaintext, encrypted));

        final byte[] decrypted = OpenSSLPBECodec.decrypt(encrypted, password);
        assertTrue(Arrays.equals(plaintext, decrypted));
    }

    @Test
    public void testRoundTripCompressionAndEncryption() throws IOException {
        final byte[] plaintext = createPlaintext();
        final char[] password = createPassword();

        final byte[] compressedAndEncrypted = OpenSSLPBECodec.compressAndEncrypt(plaintext, password);
        assertFalse(Arrays.equals(plaintext, compressedAndEncrypted));

        final byte[] decryptedAndDecompressed = OpenSSLPBECodec.decryptAndDecompress(compressedAndEncrypted, password);
        assertTrue(Arrays.equals(plaintext, decryptedAndDecompressed));
    }

    private static byte[] createPlaintext() {
        final int maxStreamBufferSize =
                Math.max(OpenSSLPBEInputStream.READ_BLOCK_SIZE, OpenSSLPBEOutputStream.WRITE_BLOCK_SIZE);
        final int length = Double.valueOf(Math.PI * Integer.valueOf(maxStreamBufferSize).doubleValue()).intValue();
        final byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static char[] createPassword() {
        final byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return new String(Base64.encodeBase64(bytes)).toCharArray();
    }
}
