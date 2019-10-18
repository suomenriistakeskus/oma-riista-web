package fi.riista.feature.permit.application.email;

import fi.riista.config.HandlebarsConfig;
import fi.riista.config.LocalizationConfig;
import fi.riista.feature.permit.application.HarvestPermitApplicationName;
import fi.riista.test.rules.SpringRuleConfigurer;
import fi.riista.util.Locales;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.net.URI;
import java.util.Locale;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

@ContextConfiguration
public class HarvestPermitApplicationNotificationEmailFactoryTest extends SpringRuleConfigurer {

    @Configuration
    @Import({HandlebarsConfig.class, LocalizationConfig.class})
    static class Context {
        @Bean
        public HarvestPermitApplicationNotificationEmailFactory harvestPermitApplicationNotificationEmailFactory() {
            return new HarvestPermitApplicationNotificationEmailFactory();
        }
    }

    @Resource
    private HarvestPermitApplicationNotificationEmailFactory harvestPermitApplicationNotificationEmailFactory;

    @Test
    public void testFinnish() {
        final HarvestPermitApplicationNotificationDTO dto = createDTO(Locales.FI);
        final HarvestPermitApplicationNotificationEmail email = harvestPermitApplicationNotificationEmailFactory.create(dto);

        assertEquals(singleton("contact@invalid"), email.getRecipients());
        assertEquals("Oma.riista.fi - Lupahakemus jätetty", email.getSubject());
        assertEquals("<h1>Lupahakemus jätetty</h1>\n" +
                "<h2>Haettava lupa</h2>\n" +
                "<p>\n" +
                "    Haettava lupa: Poikkeuslupa riistalinnuille ja rauhoittamattomille linnuille<br/>\n" +
                "    Hakemusnumero: 10001\n" +
                "</p>\n" +
                "\n" +
                "<h3>Luvan yhteyshenkilö</h3>\n" +
                "<p>\n" +
                "    First Last<br/>\n" +
                "    Street address 123<br/>\n" +
                "    12345 City, Suomi<br/>\n" +
                "    Puhelinnumero: +358501234567<br/>\n" +
                "    Sähköpostiosoite: contact@invalid\n" +
                "</p>\n" +
                "\n" +
                "<h3>Lupaa hakee</h3>\n" +
                "<p>\n" +
                "        52534153 - Luvansaaja\n" +
                "    \n" +
                "</p>\n" +
                "\n" +
                "<p>\n" +
                "    Hakemus on noudettavissa oheisesta linkistä<br>\n" +
                "    <a href=\"http://invalid\">http://invalid</a>\n" +
                "</p>\n" +
                "<p>Terveisin,</p>\n" +
                "<p>Suomen riistakeskus</p>\n" +
                "<p>Julkiset hallintotehtävät</p>\n" +
                "<hr/>\n" +
                "<p>Tämä on automaattinen järjestelmän muodostama viesti, älä vastaa tähän viestiin.</p>\n", email.getBody());
    }

    @Test
    public void testSwedish() {
        final HarvestPermitApplicationNotificationDTO dto = createDTO(Locales.SV);
        final HarvestPermitApplicationNotificationEmail email = harvestPermitApplicationNotificationEmailFactory.create(dto);

        assertEquals(singleton("contact@invalid"), email.getRecipients());
        assertEquals("Oma.riista.fi - Licensansökan inlämnad", email.getSubject());
        assertEquals("<h1>Ansökning inlämnad</h1>\n" +
                "<h2>Tillstånd som ansöks</h2>\n" +
                "<p>\n" +
                "    Tillstånd som ansöks: Dispens för viltfåglar och icke fredade fåglar<br/>\n" +
                "    Ansökningsnummer: 10001\n" +
                "</p>\n" +
                "\n" +
                "<h3>Tillståndets kontaktperson</h3>\n" +
                "<p>\n" +
                "    First Last<br/>\n" +
                "    Street address 123<br/>\n" +
                "    12345 City, Suomi<br/>\n" +
                "    Telefonnummer: +358501234567<br/>\n" +
                "    E-postadress: contact@invalid\n" +
                "</p>\n" +
                "\n" +
                "<h3>Tillståndet söks av</h3>\n" +
                "<p>\n" +
                "        52534153 - Luvansaaja\n" +
                "    </p>\n" +
                "\n" +
                "<p>\n" +
                "    Ansökning kan hämtas via Webb-länken nedan<br>\n" +
                "    <a href=\"http://invalid\">http://invalid</a>\n" +
                "</p>\n" +
                "<p>Hälsningar,</p>\n" +
                "<p>Finlands viltcentral</p>\n" +
                "<p>Offentliga förvaltningsuppgifter</p>\n" +
                "<hr/>\n" +
                "<p>Detta är ett automatiskt meddelande, svara inte på detta meddelande.</p>\n", email.getBody());
    }

    @Nonnull
    private HarvestPermitApplicationNotificationDTO createDTO(final Locale locale) {
        return HarvestPermitApplicationNotificationDTO.builder()
                .withLocale(locale)
                .withApplicationNumber(10001)
                .withApplicationType(HarvestPermitApplicationName.BIRD.getTranslation(locale))
                .withEmailLink(URI.create("http://invalid"))
                .withPermitHolderName("Luvansaaja")
                .withPermitHolderCode("52534153")
                .withContactPersonFirstName("First")
                .withContactPersonLastName("Last")
                .withContactPersonEmail("contact@invalid")
                .withContactPersonPhoneNumber("+358501234567")
                .withContactPersonStreetAddress("Street address 123")
                .withContactPersonPostalCode("12345")
                .withContactPersonCity("City")
                .withContactPersonCountry("Suomi")
                .build();
    }

}
