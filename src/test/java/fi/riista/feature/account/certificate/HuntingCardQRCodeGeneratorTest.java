package fi.riista.feature.account.certificate;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.JCEUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class HuntingCardQRCodeGeneratorTest {
    /**
     * Testing keys generated using OpenSSL:
     *
     * openssl ecparam -name secp112r1 -genkey -out private-ec.pem
     * openssl pkcs8 -topk8 -nocrypt -in private-ec.pem -out private.pem
     * openssl ec -in private.pem -pubout -out public.pem
     */
    private static final String PEM_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "ME4CAQAwEAYHKoZIzj0CAQYFK4EEAAYENzA1AgEBBA5haR2N/hi/L4eqa5cIFKEg\n" +
            "Ax4ABGp6Zhh2IisynHIR4drfkSQ1imQgiuUzkVrK1Ck=\n" +
            "-----END PRIVATE KEY-----";

    private static final String PEM_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MDIwEAYHKoZIzj0CAQYFK4EEAAYDHgAEanpmGHYiKzKcchHh2t+RJDWKZCCK5TOR\n" +
            "WsrUKQ==\n" +
            "-----END PUBLIC KEY-----";

    private static final String FAKE_SSN = "111111-1034";
    private static final String FAKE_HUNTER_NUMBER = "12345678";

    @BeforeClass
    public static void initCipher() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testGenerate() throws Exception {
        Person person = new Person();
        person.setFirstName("Etunimi");
        person.setLastName("Sukunimi");
        person.setHomeMunicipality(new Municipality("101", "Kunta suomeksi", "Kunta ruotsiksi"));
        person.setHunterNumber("12345678");
        person.setSsn(FAKE_SSN);
        person.setRhyMembership(new Riistanhoitoyhdistys(null, "Tampereen RHY", "På svenska", "701"));
        person.setHuntingCardEnd(new LocalDate(2016, 7, 31));

        checkResult(person, "Sukunimi;Etunimi;Kunta suomeksi;11111911;12345678;31072016;701");
    }

    @Test
    public void testGenerate_MunicipalityIsOptional() throws Exception {
        Person person = new Person();
        person.setFirstName("Etunimi");
        person.setLastName("Sukunimi");
        person.setHomeMunicipality(null);
        person.setHomeMunicipalityCode(null);
        person.setHunterNumber("12345678");
        person.setSsn(FAKE_SSN);
        person.setRhyMembership(new Riistanhoitoyhdistys(null, "Tampereen RHY", "På svenska", "701"));
        person.setHuntingCardEnd(new LocalDate(2016, 7, 31));

        checkResult(person, "Sukunimi;Etunimi;;11111911;12345678;31072016;701");
    }

    @Test
    public void testMaximumLength() throws Exception {
        Person person = new Person();
        person.setFirstName("Ensimmäinen Toinen Kolmas");
        person.setLastName("Pitkä-Ääkkösellinen");
        person.setHomeMunicipality(new Municipality("101", "Kristiinankaupunki", "Kristiinankaupunki"));
        person.setHunterNumber("12345678");
        person.setSsn(FAKE_SSN);
        person.setRhyMembership(new Riistanhoitoyhdistys(null, "Tampereen RHY", "På svenska", "701"));
        person.setHuntingCardEnd(new LocalDate(2016, 7, 31));

        checkResult(person, "Pitkä-Ääkkösellinen;Ensimmäinen Toinen Kolmas;Kristiinankaupunki;11111911;12345678;31072016;701");
    }

    @Test
    public void testTooLong() throws Exception {
        Person person = new Person();
        person.setFirstName("Ensimmäinen Toinen Kolmas Neljäs");
        person.setLastName("Aivan-Liian-Pitkä-Ääkkösellinen");
        person.setHomeMunicipality(new Municipality("101", "Kristiinankaupunki-Uppsala", "Kristiinankaupunki-Uppsala"));
        person.setHunterNumber(FAKE_HUNTER_NUMBER);
        person.setSsn(FAKE_SSN);
        person.setRhyMembership(new Riistanhoitoyhdistys(null, "Tampereen RHY", "På svenska", "701"));
        person.setHuntingCardEnd(new LocalDate(2016, 7, 31));

        checkResult(person, "Aivan-Liian-Pitkä-Ääkköse;Ensimmäinen Toinen Kolmas N;Kristiinankaupunki;11111911;12345678;31072016;701");
    }

    private static void checkResult(final Person person, final String expectedPayload) throws Exception {
        PrivateKey privateKey = JCEUtil.loadEllipticCurvePkcs8PrivateKey(PEM_PRIVATE_KEY);
        PublicKey publicKey = JCEUtil.loadEllipticCurvePublicKey(PEM_PUBLIC_KEY);

        HuntingCardQRCodeGenerator generator = HuntingCardQRCodeGenerator.forPerson(person);

        String result = generator.build(privateKey, "fi");

        int split = result.lastIndexOf(';');
        String payload = result.substring(0, split);
        String signature = result.substring(split + 1);

        assertThat(payload, Matchers.equalTo(expectedPayload));
        assertTrue(JCEUtil.verifyECDSASignature(payload, signature, publicKey));
    }
}
