package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;

import static fi.riista.test.matchers.PatternMatcher.pattern;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class HarvestReportNotificationTest extends EmbeddedDatabaseTest {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Test
    public void testReportWithoutPermit() {
        Harvest harvest = createHarvestReport();

        final MailMessageDTO mailMessage = new HarvestReportNotification(handlebars, messageSource)
                .withHarvest(harvest)
                .withRiistakeskuksenAlue(createRka())
                .withRecipients(singleton("test@example.com"))
                .build("default@example.com");


        assertThat(mailMessage.getBody(), containsString("<p>\n" +
                "    Ville Saaristo <br/>\n" +
                "    Katuosoite 321 <br/>\n" +
                "    00101 Helsinki <br/>\n" +
                "    Puhelin:  <br/>\n" +
                "    Sähköposti: ville.saaristo@example.com\n" +
                "</p>"));
        assertThat(mailMessage.getBody(), containsString("<h2>Kaataja:</h2>\n" +
                "\n" +
                "<p>\n" +
                "    Etunimi Sukunimi <br/>\n" +
                "    Katuosoite 123 <br/>\n" +
                "    00100 Turku\n" +
                "</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Kiinteistötunnus: 002-002-0001-9999</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Riistakeskuksen alue: Etelä-Pohjanmaa</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Riistanhoitoyhdistys: Hämeenlinnan riistanhoitoyhdistys</p>"));
        assertThat(mailMessage.getBody(), containsString("Naaras"));
        assertThat(mailMessage.getBody(), containsString("Alle 1 v"));
        assertThat(mailMessage.getBody(), containsString("142,3"));
        assertThat(mailMessage.getBody(), containsString("<p>Pyyntiaika: 30.06.2012 14:39</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Pyyntipaikan koordinaatit: 502512 941235 ETRS-TM35FIN</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Pyyntialue: Metsästysseuran alue</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Metsästysseuran/-seurueen alue, jolta saalis saatiin: Kuusamon Eräjermut</p>"));
        assertThat(mailMessage.getBody(), containsString("<h2>ILVES</h2>"));
        assertThat(mailMessage.getBody(), containsString("<p>Onko ilmoitettu myös saalispuhelimeen: Kyllä</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Onko ammuttu ruokintapaikalla? Kyllä</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Onko taigametsähanhi? Kyllä</p>"));
        assertThat(mailMessage.getBody(), containsString("<p>Metsästysalueen pinta-ala: 62,51 ha</p>"));
        assertThat(mailMessage.getBody(), pattern(".*Viestin tunnistetiedot: \\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}, " +
                "id=5123, revision=2, state=SENT_FOR_APPROVAL.*"));
    }

    @Test
    public void testReportUsingPermit() {
        final Harvest harvest = createHarvestReport();

        final MailMessageDTO mailMessage = new HarvestReportNotification(handlebars, messageSource)
                .withHarvest(harvest)
                .withRiistakeskuksenAlue(createRka())
                .withQuota(createQuota(createHarvestArea()))
                .withPermit(createHarvestPermit())
                .withRecipients(singleton("test@example.com"))
                .build("default@example.com");

        assertThat(mailMessage.getBody(), containsString("Myönnetyllä luvalla on saatu saalista seuraavasti:"));
        assertThat(mailMessage.getBody(), containsString("Luvan numero: 2013-3-450-00260-2"));
        assertThat(mailMessage.getBody(), containsString("SUOMEN RIISTAKESKUS"));
    }

    private static Harvest createHarvestReport() {
        Person hunter = new Person();
        hunter.setFirstName("Etunimi");
        hunter.setLastName("Sukunimi");
        hunter.setEmail("esko.virtanen@example.com");
        hunter.setMrAddress(new Address("Katuosoite 123", "00100", "Turku", "FINLAND"));

        Person author = new Person();
        author.setFirstName("Ville");
        author.setLastName("Saaristo");
        author.setEmail("ville.saaristo@example.com");
        author.setOtherAddress(new Address("Katuosoite 321", "00101", "Helsinki", "FINLAND"));

        Harvest harvest = new Harvest();

        HarvestSpecimen specimen = new HarvestSpecimen(harvest);
        specimen.setGender(GameGender.FEMALE);
        specimen.setAge(GameAge.YOUNG);
        specimen.setWeight(142.3219323);

        harvest.setId(5123L);
        harvest.setConsistencyVersion(2);
        harvest.setAuthor(author);
        harvest.setActualShooter(hunter);
        harvest.setPropertyIdentifier("00200200019999");
        harvest.setPointOfTime(new DateTime(2012, 6, 30, 14, 39, Constants.DEFAULT_TIMEZONE).toDate());
        harvest.setGeoLocation(new GeoLocation(502512, 941235, GeoLocation.Source.GPS_DEVICE));
        harvest.setHuntingAreaType(HuntingAreaType.HUNTING_SOCIETY);
        harvest.setHuntingParty("Kuusamon Eräjermut");
        harvest.setHarvestPermit(createHarvestPermit());
        harvest.setHuntingAreaSize(Double.valueOf("62.51"));
        harvest.setReportedWithPhoneCall(Boolean.TRUE);
        harvest.setFeedingPlace(Boolean.TRUE);
        harvest.setSpecies(createSpecies());
        harvest.setHuntingMethod(HuntingMethod.CAPTURED_ALIVE);
        harvest.setRhy(createRhy());
        harvest.setSubSpeciesCode(GameSpecies.OFFICIAL_CODE_TAIGA_BEAN_GOOSE);
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        harvest.setHarvestReportAuthor(harvest.getAuthor());
        harvest.setHarvestReportDate(DateUtil.now());

        return harvest;
    }

    private static GameSpecies createSpecies() {
        GameSpecies gameSpecies = new GameSpecies();
        gameSpecies.setOfficialCode(GameSpecies.OFFICIAL_CODE_LYNX);
        gameSpecies.setNameFinnish("Ilves");
        gameSpecies.setNameSwedish("Lovdur");
        gameSpecies.setCategory(GameCategory.GAME_MAMMAL);
        return gameSpecies;
    }

    private static HarvestPermit createHarvestPermit() {
        HarvestPermit harvestPermit = HarvestPermit.create("2013-3-450-00260-2");
        return harvestPermit;
    }

    private static HarvestQuota createQuota(HarvestArea harvestArea) {
        HarvestQuota harvestQuota = new HarvestQuota();
        harvestQuota.setHarvestArea(harvestArea);

        return harvestQuota;
    }

    private static HarvestArea createHarvestArea() {
        HarvestArea harvestArea = new HarvestArea();
        harvestArea.setNameFinnish("Lapin poroalue");
        harvestArea.setNameSwedish("Lapin poroalue SV");
        return harvestArea;
    }

    private static Organisation createRka() {
        Organisation organisation = new RiistakeskuksenAlue(new Riistakeskus("rk", "rk"), "rka", "rka", "000");
        organisation.setNameFinnish("Etelä-Pohjanmaa");
        organisation.setNameSwedish("Etelä-Pohjanmaa SV");

        return organisation;
    }

    private static Riistanhoitoyhdistys createRhy() {
        Riistanhoitoyhdistys organisation = new Riistanhoitoyhdistys();
        organisation.setNameFinnish("Hämeenlinnan riistanhoitoyhdistys");
        organisation.setNameSwedish("Hämeenlinnan riistanhoitoyhdistys SV");

        return organisation;
    }
}
