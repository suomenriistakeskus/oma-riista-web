package fi.riista.integration.vtj;

import fi.riista.util.JCEUtil;
import fi.vrk.xml.schema.vtjkysely.VTJHenkiloVastaussanoma;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tempuri.SoSoSoap;

import javax.annotation.Resource;
import java.security.Security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore("If you want to run these, remove ignore and add correct username and password in your properties")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class VtjRemoteServiceTest {
    private static final String END_USER = "omariista_integration_test";

    static {
        JCEUtil.removeJavaCryptographyAPIRestrictions();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Resource
    private VtjRemoteService vtjApi;

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void test3Paluukoodi0000HakuOnnistui() throws Exception {
        final String ssn = "160162-9968";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        assertHETU(ssn, result);
    }

    @Test
    public void test4Paluukoodi0001TarkistusmerkkiVirheellinen() throws Exception {
        final String ssn = "160162-996P";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        assertNull(result);
    }

    @Test
    public void test5Paluukoodi0004() {
        final String ssn = "160162-9968";
        try {
            createVtjApiWithWrongConfig().search(END_USER, ssn);
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("0004"));
        }
    }

    private VtjRemoteService createVtjApiWithWrongConfig() {
        // create new instance of vtjApi with wrong config
        SoSoSoap soso = applicationContext.getBean(SoSoSoap.class);
        VtjConfig config = new VtjConfig() {
            @Override
            public String getUsername() {
                return "thisUsernameIsWrong";
            }

            @Override
            public String getPassword() {
                return "thisPasswordIsWrong";
            }
        };
        return new VtjRemoteService(soso, config);
    }

    @Test
    public void test6AktiivinenHETU() throws Exception {
        final String ssn = "160162-9968";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        assertHETU(ssn, result);
    }

    private static void assertHETU(String ssn, VTJHenkiloVastaussanoma.Henkilo result) {
        assertNotNull(result);
        assertNotNull(result.getHenkilotunnus());
        assertEquals(ssn, result.getHenkilotunnus().getValue());
        assertEquals("1", result.getHenkilotunnus().getVoimassaolokoodi());
    }

    @Test
    public void test7PassiivinenHETU() throws Exception {
        final String ssn = "221182-998D";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        assertNull(ssn, result);
    }

    @Test
    public void test8NykyinenSukunimiJaEtunimet() throws Exception {
        final String ssn = "160162-9968";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        VTJHenkiloVastaussanoma.Henkilo.NykyinenSukunimi nykyinenSukunimi = result.getNykyinenSukunimi();
        String sukunimi = nykyinenSukunimi.getSukunimi();
        assertEquals("Tuulispää", sukunimi);

        VTJHenkiloVastaussanoma.Henkilo.NykyisetEtunimet nykyisetEtunimet = result.getNykyisetEtunimet();
        String etunimet = nykyisetEtunimet.getEtunimet();
        assertEquals("Kanerva", etunimet);
    }

    @Test
    public void test9VakinainenKotimainenLahiosoite() throws Exception {
        final String ssn = "251159-999E";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        VTJHenkiloVastaussanoma.Henkilo.VakinainenKotimainenLahiosoite osoite = result.getVakinainenKotimainenLahiosoite();

        assertEquals("Sepänkatu 11 A 12", osoite.getLahiosoiteS());
        assertEquals("", osoite.getLahiosoiteR());

        assertEquals("70100", osoite.getPostinumero());

        assertEquals("KUOPIO", osoite.getPostitoimipaikkaS());
        assertEquals("KUOPIO", osoite.getPostitoimipaikkaR());

        assertEquals("20060726", osoite.getAsuminenAlkupvm());
    }

    @Test
    public void test10VakinainenKotimainenLahiosoiteLaitos() throws Exception {
        final String ssn = "090369-9998";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        VTJHenkiloVastaussanoma.Henkilo.VakinainenKotimainenLahiosoite osoite = result.getVakinainenKotimainenLahiosoite();

        assertNotNull(osoite.getLahiosoiteS());
        assertNotNull(osoite.getLahiosoiteR());

        assertNotNull(osoite.getPostinumero());

        assertNotNull(osoite.getPostitoimipaikkaS());
        assertNotNull(osoite.getPostitoimipaikkaR());
    }

    @Test
    public void test11VakinainenUlkomainenLahiosoite() throws Exception {
        final String ssn = "020143-998C";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        VTJHenkiloVastaussanoma.Henkilo.VakinainenUlkomainenLahiosoite osoite = result.getVakinainenUlkomainenLahiosoite();

        assertEquals("Ruutli 33", osoite.getUlkomainenLahiosoite());
        assertEquals("TARTU 01360, Viro", osoite.getUlkomainenPaikkakuntaJaValtioS());
        assertEquals("TARTU 01360, Estland", osoite.getUlkomainenPaikkakuntaJaValtioR());
        assertEquals("", osoite.getUlkomainenPaikkakuntaJaValtioSelvakielinen());
    }

    @Test
    public void test15Kuolinpvm() throws Exception {
        final String ssn = "251115-9982";

        VTJHenkiloVastaussanoma.Henkilo result = vtjApi.search(END_USER, ssn);

        assertNotNull(result);
    }
}
