package fi.riista.feature.account.certificate;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Simple ECDSA signature utility
 *
 * Generate private and public key using OpenSSL:
 *
 * openssl ecparam -name secp112r1 -genkey -out private-ec.pem
 * openssl pkcs8 -topk8 -nocrypt -in private-ec.pem -out private.pem
 * openssl ec -in private.pem -pubout -out public.pem
 *
 * To create signature:
 *
 * mvn exec:java -Dexec.mainClass="fi.riista.feature.organization.certificate.HuntingCardQRCodeUtil" -Dexec.args="-s input.txt private.pem"
 *
 * To verify signature:
 *
 * mvn exec:java -Dexec.mainClass="fi.riista.feature.organization.certificate.HuntingCardQRCodeUtil" -Dexec.args="input.txt signature.txt public.pem"
 */
public class HuntingCardQRCodeUtil {
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    public static void main(String[] args) {
        try {
            if (args.length == 3) {
                handleArguments(args);

            } else if (args.length == 0) {
                final boolean verifyResult = verifySignature(
                        "Sukunimi;Etunimi;Kunta suomeksi;20031851;12345678;31072016;701",
                        "MCECDlREO4MLxJUQcfrlUszhAg8Ay75Xm9fEtE4ElWNbFrw=",
                        loadPublicKey("-----BEGIN PUBLIC KEY-----\n" +
                                "MDIwEAYHKoZIzj0CAQYFK4EEAAYDHgAEJ7aWH9Uvx7Hg7PalXSO2GRUrfvd48Nhc\n" +
                                "+wYYkQ==\n" +
                                "-----END PUBLIC KEY-----"));

                System.out.println(verifyResult ? "SUCCESS" : "FAILURE");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleArguments(final String[] args) throws Exception {
        if (args[0].startsWith("-s")) {
            PrivateKey privateKey = loadPrivateKey(loadFile(args[2]));
            String payload = loadFile(args[1]);
            String signature = createSignature(payload, privateKey);

            System.out.println(signature);
            System.out.flush();

        } else {
            PublicKey publicKey = loadPublicKey(loadFile(args[2]));
            String payload = loadFile(args[0]);
            String signature = loadFile(args[1]);

            System.out.println(verifySignature(payload, signature, publicKey) ? "SUCCESS" : "FAILURE");
            System.out.flush();
        }
    }

    private static String loadFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.US_ASCII);
    }

    private static String createSignature(String payload, PrivateKey privateKey) throws Exception {
        final Signature ecdsaSign = Signature.getInstance(SIGNATURE_ALGORITHM, "SunEC");
        ecdsaSign.initSign(privateKey);
        ecdsaSign.update(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(ecdsaSign.sign());
    }

    private static boolean verifySignature(String payload, String signature, PublicKey publicKey) throws Exception {
        final Signature ecdsaSign = Signature.getInstance(SIGNATURE_ALGORITHM, "SunEC");
        ecdsaSign.initVerify(publicKey);
        ecdsaSign.update(payload.getBytes(StandardCharsets.UTF_8));
        return ecdsaSign.verify(Base64.decodeBase64(signature));
    }

    private static PublicKey loadPublicKey(String b64) throws Exception {
        byte[] key = Base64.decodeBase64(b64.replaceAll("-{5}[^-]+-{5}", "").replaceAll("\\s", ""));
        return KeyFactory.getInstance("EC", "SunEC").generatePublic(new X509EncodedKeySpec(key));
    }

    private static PrivateKey loadPrivateKey(String p8key) throws Exception {
        byte[] key = Base64.decodeBase64(p8key.replaceAll("-{5}[^-]+-{5}", "").replaceAll("\\s", ""));
        return KeyFactory.getInstance("EC", "SunEC").generatePrivate(new PKCS8EncodedKeySpec(key));
    }
}
