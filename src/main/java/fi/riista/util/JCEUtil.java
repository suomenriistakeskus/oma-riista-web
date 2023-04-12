package fi.riista.util;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Utf8;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class JCEUtil {
    private static final String JCE_PROVIDER = "BC";
    private static final String SHA1WITH_ECDSA = "SHA256withECDSA";

    private static final Logger LOG = LoggerFactory.getLogger(JCEUtil.class);

    public static void removeJavaCryptographyAPIRestrictions() {
        // FIXME This method can be removed? https://bugs.openjdk.org/browse/JDK-8170157
    }

    public static PrivateKey loadEllipticCurvePkcs8PrivateKey(String p8key) throws Exception {
        byte[] key = BaseEncoding.base64().decode(p8key.replaceAll("-{5}[^-]+-{5}", "").replaceAll("\\s", ""));
        return KeyFactory.getInstance("EC", JCE_PROVIDER).generatePrivate(new PKCS8EncodedKeySpec(key));
    }

    public static PublicKey loadEllipticCurvePublicKey(String b64) throws Exception {
        byte[] key = BaseEncoding.base64().decode(b64.replaceAll("-{5}[^-]+-{5}", "").replaceAll("\\s", ""));
        return KeyFactory.getInstance("EC", JCE_PROVIDER).generatePublic(new X509EncodedKeySpec(key));
    }

    public static String createECDSASignature(final PrivateKey privateKey, final String payload) throws Exception {
        Signature ecdsaSign = Signature.getInstance(SHA1WITH_ECDSA, JCE_PROVIDER);
        ecdsaSign.initSign(privateKey);
        ecdsaSign.update(Utf8.encode(payload));

        return BaseEncoding.base64().encode(ecdsaSign.sign());
    }

    public static boolean verifyECDSASignature(String payload, String signature, PublicKey publicKey) throws Exception {
        Signature ecdsaSign = Signature.getInstance(SHA1WITH_ECDSA, JCE_PROVIDER);

        ecdsaSign.initVerify(publicKey);
        ecdsaSign.update(Utf8.encode(payload));

        return ecdsaSign.verify(BaseEncoding.base64().decode(signature));
    }

    private JCEUtil() {
        throw new AssertionError();
    }
}
