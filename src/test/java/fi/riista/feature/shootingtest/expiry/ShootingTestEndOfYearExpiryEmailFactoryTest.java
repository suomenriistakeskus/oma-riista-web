package fi.riista.feature.shootingtest.expiry;

import fi.riista.config.HandlebarsConfig;
import fi.riista.config.LocalizationConfig;
import fi.riista.test.rules.SpringRuleConfigurer;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
public class ShootingTestEndOfYearExpiryEmailFactoryTest extends SpringRuleConfigurer {

    @Configuration
    @Import({HandlebarsConfig.class, LocalizationConfig.class})
    static class Context {
        @Bean
        public ShootingTestEndOfYearExpiryEmailFactory shootingTestExpiryEmailFactory() {
            return new ShootingTestEndOfYearExpiryEmailFactory();
        }
    }

    @Resource
    private ShootingTestEndOfYearExpiryEmailFactory shootingTestEndOfYearExpiryEmailFactory;

    @Test
    public void testEmail() {
        final String date1 = "29.6.2020";
        final String time1 = "12:00 - 13:30";
        final ShootingTestEndOfYearExpiryDTO dto1 = new ShootingTestEndOfYearExpiryDTO(date1, time1);

        final String date2 = "30.6.2020";
        final String time2 = "11:00";
        final ShootingTestEndOfYearExpiryDTO dto2 = new ShootingTestEndOfYearExpiryDTO(date2, time2);

        final Set<String> recipients = new HashSet<>(Arrays.asList("invalid@invalid.com"));

        final ShootingTestEndOfYearExpiryEmail email =
                shootingTestEndOfYearExpiryEmailFactory.buildEmail(Arrays.asList(dto1, dto2), recipients);

        assertEquals(recipients, email.getRecipients());
        assertEquals("Avoimet ampumakokeet / Öppna skjutprovstillfället", email.getSubject());
        assertEquals("<p>Riistanhoitoyhdistyksen ampumakoetilaisuus  Oma riistassa on  edelleen avoinna,  Odottaa- tilassa.</p>\n" +
                "<p>Tarkistathan kokeen tiedot mahdollisimman pian ja suljet ampumakoetilaisuuden.</p>\n" +
                "\n" +
                "<ul>\n" +
                "  <li>29.6.2020 12:00 - 13:30</li>\n" +
                "  <li>30.6.2020 11:00</li>\n" +
                "</ul>\n" +
                "\n" +
                "<p>Ystävällisin terveisin,</p>\n" +
                "<p>Suomen riistakeskus</p>\n" +
                "<hr/>\n" +
                "<p>Tämä on automaattinen järjestelmän muodostama viesti, älä vastaa tähän viestiin.</p>\n" +
                "\n" +
                "<hr/>\n" +
                "<p>Jaktvårdsföreningens skjutprovstillfälle i Oma riista är fortfarande öppet, i läget Väntar.</p>\n" +
                "<p>Du kontrollerar väl provets uppgifter så fort som möjligt och stänger skjutprovstillfället.</p>\n" +
                "\n" +
                "<ul>\n" +
                "  <li>29.6.2020 12:00 - 13:30</li>\n" +
                "  <li>30.6.2020 11:00</li>\n" +
                "</ul>\n" +
                "\n" +
                "<p>Med vänlig hälsning,</p>\n" +
                "<p>Finlands viltcentral</p>\n" +
                "<hr/>\n" +
                "<p>Detta är ett automatiskt meddelande, svara inte på detta meddelande.</p>\n",
                email.getBody());
    }
}
