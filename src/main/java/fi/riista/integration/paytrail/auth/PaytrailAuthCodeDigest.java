package fi.riista.integration.paytrail.auth;

import fi.riista.config.Constants;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum PaytrailAuthCodeDigest {
    SHA256,
    MD5;

    public byte[] getMessageDigest(final String text) {
        try {
            MessageDigest md = MessageDigest.getInstance(this == MD5 ? "MD5" : "SHA-256");
            return md.digest(text.getBytes(Constants.DEFAULT_ENCODING));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
