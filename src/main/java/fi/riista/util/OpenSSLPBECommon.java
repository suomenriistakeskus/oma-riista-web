package fi.riista.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/*
source: http://stackoverflow.com/questions/11783062/how-to-decrypt-an-encrypted-file-in-java-with-openssl-with-aes
*/
final class OpenSSLPBECommon {

    protected static final int SALT_SIZE_BYTES = 8;

    protected static final String OPENSSL_HEADER_STRING = "Salted__";
    protected static final String OPENSSL_HEADER_ENCODING = "ASCII";

    protected static Cipher initializeCipher(final char[] password,
                                             final byte[] salt,
                                             final int cipherMode,
                                             final String algorithm,
                                             final int iterationCount) throws Exception {

        final SecretKey key = SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(password));

        final Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(cipherMode, key, new PBEParameterSpec(salt, iterationCount));

        return cipher;
    }

    private OpenSSLPBECommon() {
        throw new AssertionError();
    }
}
