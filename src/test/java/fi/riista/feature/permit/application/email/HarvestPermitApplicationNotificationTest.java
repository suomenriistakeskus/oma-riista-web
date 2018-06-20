package fi.riista.feature.permit.application.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import fi.riista.config.Constants;
import fi.riista.config.HandlebarsConfig;
import fi.riista.config.LocalizationConfig;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHta;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartner;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhy;
import fi.riista.test.rules.SpringRuleConfigurer;
import fi.riista.util.Locales;
import fi.riista.util.NumberSequence;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static java.util.Collections.emptySet;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = HarvestPermitApplicationNotificationTest.Context.class)
public class HarvestPermitApplicationNotificationTest extends SpringRuleConfigurer {

    @Configuration
    @PropertySource("classpath:git.properties")
    @PropertySource("classpath:configuration/application.properties")
    @Import({HandlebarsConfig.class,
            LocalizationConfig.class,
            RuntimeEnvironmentUtil.class})
    static class Context {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    private final EntitySupplier model = new EntitySupplier(NumberSequence.INSTANCE,
            new ArrayList<>(), () -> new Riistakeskus("", ""));

    private static double haToSqm2(final int m2) {
        return m2 * 10000;
    }

    private static TotalLandWaterSizeDTO createSizeDto(final int total, final int land, final int water) {
        return new TotalLandWaterSizeDTO(haToSqm2(total), haToSqm2(land), haToSqm2(water));
    }

    @Test
    public void testSmoke() {
        final MailMessageDTO mailMessage = createNotification()
                .withLocale(Locales.FI)
                .withRecipients(Collections.singleton("contact@email.fi"))
                .createMailMessage("to@email.fi");

        assertTrue(mailMessage.getBody().length() > 1000);
        assertEquals("Oma.riista.fi - Lupahakemus jätetty", mailMessage.getSubject());
    }

    @Test
    public void testSmokeFinnish() {
        final HarvestPermitApplicationNotification notification = createNotification();
        final String messageBodyFinnish = notification.createMessageBodyFinnish();

        assertEquals(loadExpectedMessageBody("testSmokeFinnish"), trimMessage(messageBodyFinnish));
    }

    @Test
    public void testSmokeSwedish() {
        final HarvestPermitApplicationNotification notification = createNotification();
        final String messageBodySwedish = notification.createMessageBodySwedish();

        assertEquals(loadExpectedMessageBody("testSmokeSwedish"), trimMessage(messageBodySwedish));
    }

    @Test
    public void testNotSpeciesDescriptions() {
        final HarvestPermitApplicationNotification notification = createNotification();

        final String expectedHeaderText = "Hakemuksen perustelut";

        assertThat(notification.createMessageBodyFinnish(), containsString(expectedHeaderText));

        for (HarvestPermitApplicationSpeciesAmount speciesAmount : notification.getApplication().getSpeciesAmounts()) {
            speciesAmount.setDescription(null);
        }

        // Description element is skipped
        assertThat(notification.createMessageBodyFinnish(), not(containsString(expectedHeaderText)));
    }

    // Exceptional cases, should not happen...

    @Test
    public void testMissingSpeciesAmountsOk() {
        final HarvestPermitApplicationNotification notification = createNotification();
        notification.getApplication().setSpeciesAmounts(null);

        assertThat(notification.createMessageBodyFinnish(), not(isEmptyOrNullString()));
    }

    @Test
    public void testMissingPartnersOk() {
        final HarvestPermitApplicationNotification notification = createNotification();
        notification.getApplication().setPermitPartners(null);

        assertThat(notification.createMessageBodyFinnish(), not(isEmptyOrNullString()));
    }

    @Test
    public void testEmptyPartnersOk() {
        final HarvestPermitApplicationNotification notification = createNotification();
        notification.getApplication().setPermitPartners(emptySet());

        assertThat(notification.createMessageBodyFinnish(), not(isEmptyOrNullString()));
    }

    @Test
    public void testMissingAreaOk() {
        final HarvestPermitApplicationNotification notification = createNotification();
        notification.getApplication().setArea(null);

        assertThat(notification.createMessageBodyFinnish(), not(isEmptyOrNullString()));
    }


    @Test
    public void testEmptyAreaPartnersOk() {
        final HarvestPermitApplicationNotification notification = createNotification();
        notification.getApplication().getArea().getPartners().clear();

        assertThat(notification.createMessageBodyFinnish(), not(isEmptyOrNullString()));
    }

    @Test
    public void testMissingAllPartnersOk() {
        final HarvestPermitApplicationNotification notification = createNotification();
        notification.getApplication().setPermitPartners(null);
        notification.getApplication().getArea().getPartners().clear();

        assertThat(notification.createMessageBodyFinnish(), not(isEmptyOrNullString()));
    }

    @Test
    public void testFooterFinnish() {
        assertEquals("<p>(Viestin tunnistetiedot: 30.06.2018 01:20:15, id=123, revision=5, state=ACTIVE, c=1, m=2)</p>", createNotification().getFooterTextFinnish());
    }

    @Test
    public void testFooterSwedish() {
        assertEquals("<p>(Meddelandets identifikationsuppgifter: 30.06.2018 01:20:15, id=123, revision=5, state=ACTIVE, c=1, m=2)</p>", createNotification().getFooterTextSwedish());
    }

    private HarvestPermitApplicationNotification createNotification() {
        // CONTACT PERSON

        final Person contactPerson = model.newPerson();
        contactPerson.setFirstName("Etunimi");
        contactPerson.setLastName("Sukunimi");
        contactPerson.setEmail("etunimi.sukunimi@mail.fi");
        contactPerson.setPhoneNumber("+358500000018");
        contactPerson.setMrAddress(new Address("Katuosoite", "00001", "Kaupunki", "Suomi"));

        // HOLDER

        final HuntingClub permitHolder = model.newHuntingClub();
        permitHolder.setOfficialCode("1000003");
        permitHolder.setNameFinnish("HOLDER fi");
        permitHolder.setNameSwedish("HOLDER sv");

        // AREA

        final GISZoneSizeDTO areaSize = new GISZoneSizeDTO(
                createSizeDto(123, 234, 345), haToSqm2(114), haToSqm2(3050));

        final GISZone zone = model.newGISZone();
        zone.setComputedAreaSize(areaSize.getAll().getTotal());
        zone.setWaterAreaSize(areaSize.getAll().getWater());
        zone.setStateLandAreaSize(areaSize.getStateLandAreaSize());
        zone.setPrivateLandAreaSize(areaSize.getPrivateLandAreaSize());

        final HarvestPermitArea permitArea = new HarvestPermitArea();
        permitArea.setZone(zone);
        permitArea.setExternalId("2RYF88RTHWMO");
        permitArea.setFreeHunting(true);

        // RHY

        final Riistanhoitoyhdistys rhy1 = model.newRiistanhoitoyhdistys();
        rhy1.setNameFinnish("RHY #1 fi");
        rhy1.setNameSwedish("RHY #1 sv");

        final Riistanhoitoyhdistys rhy2 = model.newRiistanhoitoyhdistys();
        rhy2.setNameFinnish("RHY #2 fi");
        rhy2.setNameSwedish("RHY #2 sv");

        permitArea.getRhy().add(new HarvestPermitAreaRhy(permitArea, rhy1,
                createSizeDto(9, 6, 3),
                createSizeDto(8, 5, 2),
                createSizeDto(7, 4, 1)));

        permitArea.getRhy().add(new HarvestPermitAreaRhy(permitArea, rhy2,
                createSizeDto(1, 4, 7),
                createSizeDto(2, 5, 8),
                createSizeDto(3, 6, 9)));

        // HTA

        final GISHirvitalousalue hta1 = model.newGISHirvitalousalue();
        hta1.setNameFinnish("HTA #1 fi");
        hta1.setNameSwedish("HTA #1 sv");

        final GISHirvitalousalue hta2 = model.newGISHirvitalousalue();
        hta2.setNameFinnish("HTA #2 fi");
        hta2.setNameSwedish("HTA #2 sv");

        permitArea.getHta().add(new HarvestPermitAreaHta(permitArea, hta1, haToSqm2(234)));
        permitArea.getHta().add(new HarvestPermitAreaHta(permitArea, hta2, haToSqm2(123)));

        // PARTNERS

        final HuntingClub club1 = model.newHuntingClub(rhy1);
        club1.setOfficialCode("1000001");
        club1.setNameFinnish("CLUB #1 fi");
        club1.setNameSwedish("CLUB #1 sv");

        final HuntingClub club2 = model.newHuntingClub(rhy2);
        club2.setOfficialCode("1000002");
        club2.setNameFinnish("CLUB #2 fi");
        club2.setNameSwedish("CLUB #2 sv");

        // AREA PARTNERS

        permitArea.getPartners().add(new HarvestPermitAreaPartner(permitArea,
                new HuntingClubArea(club1, "", "",
                        2018, 2018, "AAAAAAAAAA"), new GISZone()));

        permitArea.getPartners().add(new HarvestPermitAreaPartner(permitArea,
                new HuntingClubArea(club1, "", "",
                        2018, 2018, "BBBBBBBBBB"), new GISZone()));

        permitArea.getPartners().add(new HarvestPermitAreaPartner(permitArea,
                new HuntingClubArea(club2, "", "",
                        2018, 2018, "CCCCCCCCCC"), new GISZone()));

        // SPECIES

        final GameSpecies moose = model.newGameSpecies();
        moose.setNameFinnish("Hirvi");
        moose.setNameSwedish("Älg");

        final GameSpecies deer = model.newGameSpecies();
        deer.setNameFinnish("Valkohäntäpeura");
        deer.setNameSwedish("Vitsvanshjort");

        final HarvestPermitApplicationSpeciesAmount spaMoose = new HarvestPermitApplicationSpeciesAmount();
        spaMoose.setGameSpecies(moose);
        spaMoose.setAmount(20);
        spaMoose.setDescription("Dolor sit amet.");

        final HarvestPermitApplicationSpeciesAmount spaDeer = new HarvestPermitApplicationSpeciesAmount();
        spaDeer.setGameSpecies(deer);
        spaDeer.setAmount(700);
        spaDeer.setDescription("Lorem ipsum.");

        // APPLICATION

        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setId(123L);
        application.setConsistencyVersion(5);
        application.getAuditFields().setCreatedByUserId(1L);
        application.getAuditFields().setModifiedByUserId(2L);
        application.getLifecycleFields().setCreationTime(DateTime.parse("2018-06-30T01:20:15").toDate());
        application.setStatus(HarvestPermitApplication.Status.ACTIVE);
        application.setApplicationNumber(10006);
        application.setPermitTypeCode("100");
        application.setContactPerson(contactPerson);
        application.setPermitHolder(permitHolder);
        application.setPermitPartners(ImmutableSet.of(club1, club2));
        application.setArea(permitArea);
        application.setRhy(rhy1);
        application.setRelatedRhys(new HashSet<>());

        application.setShooterOnlyClub(111);
        application.setShooterOtherClubPassive(222);
        application.setShooterOtherClubActive(333);

        application.getSpeciesAmounts().add(spaMoose);
        application.getSpeciesAmounts().add(spaDeer);

        // ATTACHMENTS

        application.getAttachments().add(new HarvestPermitApplicationAttachment(application, "shooter1.pdf", null, HarvestPermitApplicationAttachment.Type.SHOOTER_LIST));
        application.getAttachments().add(new HarvestPermitApplicationAttachment(application, "shooter2.pdf", null, HarvestPermitApplicationAttachment.Type.SHOOTER_LIST));

        application.getAttachments().add(new HarvestPermitApplicationAttachment(application, "area1.pdf", null, HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT));
        application.getAttachments().add(new HarvestPermitApplicationAttachment(application, "area2.pdf", null, HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT));

        application.getAttachments().add(new HarvestPermitApplicationAttachment(application, "other1.pdf", null, HarvestPermitApplicationAttachment.Type.OTHER));
        application.getAttachments().add(new HarvestPermitApplicationAttachment(application, "other2.pdf", null, HarvestPermitApplicationAttachment.Type.OTHER));

        return new HarvestPermitApplicationNotification(handlebars, messageSource)
                .withApplication(application)
                .withAreaSize(areaSize);
    }

    private static String loadExpectedMessageBody(final String testName) {
        final String fileName = String.format("HarvestPermitApplicationNotificationTest_%s.html", testName);
        final ClassPathResource classPathResource = new ClassPathResource(fileName, HarvestPermitApplicationNotificationTest.class);

        try (final InputStream is = classPathResource.getInputStream()) {
            return trimMessage(CharStreams.toString(new InputStreamReader(is, Constants.DEFAULT_CHARSET)));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String trimMessage(final String message) {
        return message.replaceAll(" {2,}", "").replaceAll("(.*)[ ]*$", "$1");
    }
}
