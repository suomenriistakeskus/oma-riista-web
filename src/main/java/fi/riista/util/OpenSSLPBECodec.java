package fi.riista.util;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class OpenSSLPBECodec {

    public static final String DEFAULT_ALGORITHM = "PBEWITHMD5AND256BITAES-CBC-OPENSSL";

    public static byte[] encrypt(final byte[] plaintext, final char[] password) throws IOException {
        final ByteArrayOutputStream byteOS = new ByteArrayOutputStream();

        try (final OpenSSLPBEOutputStream encOS = new OpenSSLPBEOutputStream(byteOS, DEFAULT_ALGORITHM, 1, password)) {
            IOUtils.write(plaintext, encOS);
        }

        return byteOS.toByteArray();
    }

    public static byte[] decrypt(final byte[] encrypted, final char[] password) throws IOException {
        final ByteArrayInputStream byteIS = new ByteArrayInputStream(encrypted);

        try (final OpenSSLPBEInputStream encIS = new OpenSSLPBEInputStream(byteIS, DEFAULT_ALGORITHM, 1, password)) {
            return IOUtils.toByteArray(encIS);
        }
    }

    public static byte[] compressAndEncrypt(final byte[] plaintext, final char[] password) throws IOException {
        final ByteArrayOutputStream byteOS = new ByteArrayOutputStream();

        try (final OpenSSLPBEOutputStream encOS = new OpenSSLPBEOutputStream(byteOS, DEFAULT_ALGORITHM, 1, password);
             final GZIPOutputStream gzOS = new GZIPOutputStream(encOS)) {

            IOUtils.write(plaintext, gzOS);
        }

        return byteOS.toByteArray();
    }

    public static byte[] decryptAndDecompress(final byte[] encrypted, final char[] password) throws IOException {
        final ByteArrayInputStream byteIS = new ByteArrayInputStream(encrypted);

        try (final OpenSSLPBEInputStream encIS = new OpenSSLPBEInputStream(byteIS, DEFAULT_ALGORITHM, 1, password);
             final GZIPInputStream gzIS = new GZIPInputStream(encIS)) {

            return IOUtils.toByteArray(gzIS);
        }
    }

    private OpenSSLPBECodec() {
        throw new AssertionError();
    }
}
