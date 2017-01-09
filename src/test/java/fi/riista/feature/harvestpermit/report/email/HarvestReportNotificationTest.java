package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.config.Constants;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;

import static fi.riista.test.PatternMatcher.pattern;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class HarvestReportNotificationTest extends EmbeddedDatabaseTest {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Test
    public void testReportWithoutPermit() {
        HarvestReport harvestReport = createHarvestReport();
        Harvest harvest = harvestReport.getHarvests().iterator().next();

        final MailMessageDTO.Builder notification = new HarvestReportNotification(handlebars, messageSource)
                .withReport(harvestReport, harvest)
                .withRiistakeskuksenAlue(createRka())
                .withEmail("test@example.com")
                .build();

        MailMessageDTO mailMessage = notification
                .withFrom("default@example.com").build();

        assertThat(mailMessage.getBody(), startsWith("<pre>"));
        assertThat(mailMessage.getBody(), endsWith("</pre>"));
        assertThat(mailMessage.getBody(), containsString("Ville Saaristo\nKatuosoite 321\n00101 Helsinki"));
        assertThat(mailMessage.getBody(), containsString("Kaataja:\nEtunimi Sukunimi\nKatuosoite 123\n00100 Turku\n"));
        assertThat(mailMessage.getBody(), containsString("002-002-0001-9999"));
        assertThat(mailMessage.getBody(), containsString("Riistakeskuksen alue: Etelä-Pohjanmaa\n"));
        assertThat(mailMessage.getBody(), containsString("Riistanhoitoyhdistys: Hämeenlinnan riistanhoitoyhdistys\n"));
        assertThat(mailMessage.getBody(), containsString("Sukupuoli: Naaras"));
        assertThat(mailMessage.getBody(), containsString("Ikä: Alle 1 v"));
        assertThat(mailMessage.getBody(), containsString("Paino: 142,322 kg\n"));
        assertThat(mailMessage.getBody(), containsString("Pyyntiaika: 30.06.2012 14:39\n"));
        assertThat(mailMessage.getBody(), containsString("Pyyntipaikan koordinaatit: 502512 941235 ETRS-TM35FIN\n"));
        assertThat(mailMessage.getBody(), containsString("Pyyntialue: Metsästysseuran alue\n"));
        assertThat(mailMessage.getBody(), containsString("Metsästysseuran/-seurueen alue, jolta saalis saatiin: Kuusamon Eräjermut\n"));
        assertThat(mailMessage.getBody(), containsString("ILVES"));
        assertThat(mailMessage.getBody(), containsString("Onko ilmoitettu myös saalispuhelimeen: Kyllä"));
        assertThat(mailMessage.getBody(), containsString("Metsästysalueen pinta-ala: 62,51 ha"));
        assertThat(mailMessage.getBody(), pattern(".*Viestin tunnistetiedot: \\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}, " +
                "id=5123, revision=2, state=SENT_FOR_APPROVAL.*"));
    }

    @Test
    public void testReportUsingPermit() {
        HarvestReport harvestReport = createHarvestReport();
        Harvest harvest = harvestReport.getHarvests().iterator().next();
        final MailMessageDTO.Builder notification = new HarvestReportNotification(handlebars, messageSource)
                .withReport(harvestReport, harvest)
                .withRiistakeskuksenAlue(createRka())
                .withQuota(createQuota(createHarvestArea()))
                .withPermit(createHarvestPermit())
                .withEmail("test@example.com")
                .build();

        MailMessageDTO mailMessage = notification.withFrom("default@example.com").build();

        assertThat(mailMessage.getBody(), containsString("Myönnetyllä luvalla on saatu saalista seuraavasti:"));
        assertThat(mailMessage.getBody(), containsString("Luvan numero: 2013-3-450-00260-2\n"));
        assertThat(mailMessage.getBody(), containsString("SUOMEN RIISTAKESKUS\n"));
    }

    private static HarvestReport createHarvestReport() {
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
        harvest.setSpecies(createSpecies());
        harvest.setHuntingMethod(HuntingMethod.CAPTURED_ALIVE);
        harvest.setRhy(createRhy());

        HarvestReport report = new HarvestReport();
        report.addHarvest(harvest);
        harvest.setHarvestReport(report);
        report.setId(5123L);
        report.setConsistencyVersion(2);
        report.setState(HarvestReport.State.SENT_FOR_APPROVAL);

        return report;
    }

    private static GameSpecies createSpecies() {
        GameSpecies gameSpecies = new GameSpecies();
        gameSpecies.setNameFinnish("Ilves");
        gameSpecies.setNameSwedish("Lovdur");
        gameSpecies.setCategory(GameCategory.GAME_MAMMAL);
        return gameSpecies;
    }

    private static HarvestPermit createHarvestPermit() {
        HarvestPermit harvestPermit = new HarvestPermit();
        harvestPermit.setPermitNumber("2013-3-450-00260-2");

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
