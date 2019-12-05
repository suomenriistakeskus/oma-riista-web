package fi.riista.feature.organization.jht.expiry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.riista.config.HandlebarsConfig;
import fi.riista.config.LocalizationConfig;
import fi.riista.test.rules.SpringRuleConfigurer;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
public class JHTOccupationExpiryEmailFactoryTest extends SpringRuleConfigurer {

    @Configuration
    @Import({HandlebarsConfig.class, LocalizationConfig.class})
    static class Context {
        @Bean
        public JHTOccupationExpiryEmailFactory jhtOccupationExpiryEmailFactory() {
            return new JHTOccupationExpiryEmailFactory();
        }
    }

    @Resource
    private JHTOccupationExpiryEmailFactory jhtOccupationExpiryEmailFactory;

    @Test
    public void testReceivers() {
        final Map<Long, Set<String>> rhyEmailMapping = ImmutableMap.of(
                1L, ImmutableSet.of("rhy1-1@invalid", "rhy1-2@invalid"),
                2L, ImmutableSet.of("rhy2-1@invalid", "rhy2-2@invalid"));

        final JHTOccupationExpiryDTO dto = createDTO(Locales.FI);
        final JHTOccupationExpiryEmail email = jhtOccupationExpiryEmailFactory.buildEmail(dto, rhyEmailMapping);

        assertEquals(ImmutableSet.of("rhy1-1@invalid", "rhy1-2@invalid", dto.getOccupationEmail()), email.getRecipients());
    }

    @Test
    public void testFinnish() {
        final JHTOccupationExpiryEmail email = jhtOccupationExpiryEmailFactory.buildEmail(createDTO(Locales.FI),
                Collections.emptyMap());

        assertEquals("Nimitys vanhenee", email.getSubject());
        assertEquals("<p>Hyvä vastaanottaja,</p>\n" +
                        "\n" +
                        "<p>\n" +
                        "    Nimitys tehtävään vanhenee 30.11.2018:\n" +
                        "</p>\n" +
                        "\n" +
                        "<p>\n" +
                        "    <strong>Etunimi Sukunimi</strong>\n" +
                        "    <br>\n" +
                        "    <strong>Ampumakokeen vastaanottaja</strong>\n" +
                        "    <br>\n" +
                        "    <strong>Mäntyharjun-Hirvensalmen riistanhoitoyhdistys</strong>\n" +
                        "</p>\n" +
                        "\n" +
                        "<p>\n" +
                        "    Uutta nimitystä varten teidän on suoritettava kyseisen tehtävän vaatima kertauskoulutus yllä olevaan päivämäärään\n" +
                        "    mennessä osoitteessa <a href=\"https://www.riistainfo.fi/\">www.riistainfo.fi</a>. Kertauskoulutuksen hyväksytyn\n" +
                        "    suorittamisen jälkeen teidät voidaan nimittää uudelleen kyseiseen tehtävään. Vaihtoehtoisesti voitte osallistua\n" +
                        "    Suomen riistakeskuksen järjestämään koulutustilaisuuteen.\n" +
                        "    Kertauskoulutusvaatimus ei koske rhy:n edustajia riistavahinkojen maastokatselmuksessa.\n" +
                        "</p>\n" +
                        "\n" +
                        "<p>Tämä viesti on lähetetty myös Mäntyharjun-Hirvensalmen riistanhoitoyhdistykselle.</p>\n" +
                        "\n" +
                        "<p>Ystävällisin terveisin,</p>\n" +
                        "<p>Suomen riistakeskus</p>\n" +
                        "<hr/>\n" +
                        "<p>Tämä on automaattinen järjestelmän muodostama viesti, älä vastaa tähän viestiin.</p>\n",
                email.getBody());
    }

    @Test
    public void testSwedish() {
        final JHTOccupationExpiryEmail email = jhtOccupationExpiryEmailFactory.buildEmail(createDTO(Locales.SV),
                Collections.emptyMap());

        assertEquals("Utnämning upphör", email.getSubject());
        assertEquals("<p>Ärade mottagare,</p>\n" +
                        "\n" +
                        "<p>\n" +
                        "    Utnämning upphör 30.11.2018:\n" +
                        "</p>\n" +
                        "\n" +
                        "<p>\n" +
                        "    <strong>Etunimi Sukunimi</strong>\n" +
                        "    <br>\n" +
                        "    <strong>Examinator för skjutprov</strong>\n" +
                        "    <br>\n" +
                        "    <strong>Mäntyharju-Hirvensalmi jaktvårdsförening</strong>\n" +
                        "</p>\n" +
                        "\n" +
                        "<p>\n" +
                        "    För att få en ny utnämning måste ni avlägga en repetitionsutbildning som krävs för ifrågavarande uppdrag före ovan\n" +
                        "    nämnda datum på adressen <a href=\"https://www.riistainfo.fi/\">www.riistainfo.fi</a>. Efter att\n" +
                        "    repetitionsutbildningen är godkänd kan ni utnämnas på nytt för ifrågavarande uppdrag. Alternativt kan ni delta i\n" +
                        "    utbildningstillfälle som ordnas av Finlands viltcentral.\n" +
                        "    Kravet på repetitionsutbildning gäller inte för jvf:s representanter vid terrängundersökningar av viltskador.\n" +
                        "</p>\n" +
                        "\n" +
                        "<p>Det här meddelandet har också sänts till Mäntyharju-Hirvensalmi jaktvårdsförening.</p>\n" +
                        "\n" +
                        "<p>Med vänlig hälsning,</p>\n" +
                        "<p>Finlands viltcentral</p>\n" +
                        "<hr/>\n" +
                        "<p>Detta är ett automatiskt meddelande, svara inte på detta meddelande.</p>\n",
                email.getBody());
    }

    @Nonnull
    private JHTOccupationExpiryDTO createDTO(final Locale locale) {
        return new JHTOccupationExpiryDTO(locale,
                new LocalDate(2018, 11, 30), "Etunimi Sukunimi",
                LocalisedString.of(
                        "Ampumakokeen vastaanottaja",
                        "Examinator för skjutprov"),
                "", 1L, LocalisedString.of(
                "Mäntyharjun-Hirvensalmen riistanhoitoyhdistys",
                "Mäntyharju-Hirvensalmi jaktvårdsförening"));
    }
}
