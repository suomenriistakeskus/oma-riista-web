package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.io.CharStreams;
import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitSpecimenSummary;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EndOfHuntingReportNotificationTest extends EmbeddedDatabaseTest {

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Before
    public void setUp() {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2017, 6, 8, 10, 12).getMillis());
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testReportUsingPermit() {
        final SystemUser user = createUserWithPerson();
        user.getPerson().setFirstName("Matti");
        user.getPerson().setLastName("Meikäläinen");
        user.getPerson().setOtherAddress(model().newAddress("katuosoite 123", "00036", "kotikaupunki", "suomi"));
        user.getPerson().setEmail("user@example.invalid");
        user.getPerson().setPhoneNumber("+358 400 11 22 33");

        final GameSpecies species = model().newGameSpecies();
        species.setNameFinnish("Karhu");
        species.setNameSwedish("Björn");

        final HarvestPermit permit = model().newHarvestPermit("2016-1-043-00046-5");
        model().newHarvestPermitSpeciesAmount(permit, species, 2.0f);

        createHarvestReport(species, user.getPerson(), permit, ADULT_MALE,
                new DateTime(2017, 1, 2, 3, 4));
        createHarvestReport(species, user.getPerson(), permit, YOUNG_MALE,
                new DateTime(2017, 5, 6, 7, 8));

        permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        permit.setHarvestReportAuthor(user.getPerson());
        permit.setHarvestReportDate(DateUtil.now());
        permit.setHarvestReportModeratorOverride(false);

        onSavedAndAuthenticated(user, tx(() -> {
            checkEmail(permit, "EndOfHuntingReportNotificationTest_testReportUsingPermit.html");
        }));
    }

    private void createHarvestReport(GameSpecies species,
                                     Person hunter,
                                     HarvestPermit permit,
                                     HarvestSpecimenType specimenType,
                                     DateTime pointOfTime) {

        final Harvest harvest = model().newHarvest(species, hunter);
        model().newHarvestSpecimen(harvest, specimenType);
        harvest.setPointOfTime(pointOfTime);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportAuthor(hunter);
        harvest.setHarvestReportDate(DateUtil.now());
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setRhy(permit.getRhy());
    }

    @Test
    public void testReportUsingPermit_noHarvests() {
        final SystemUser user = createUserWithPerson();
        user.getPerson().setFirstName("Matti");
        user.getPerson().setLastName("Meikäläinen");
        user.getPerson().setOtherAddress(model().newAddress("katuosoite 123", "00036", "kotikaupunki", "suomi"));
        user.getPerson().setEmail("user@example.invalid");
        user.getPerson().setPhoneNumber("+358 400 11 22 33");

        final GameSpecies species = model().newGameSpecies();

        final HarvestPermit permit = model().newHarvestPermit("2016-1-043-00046-5");
        model().newHarvestPermitSpeciesAmount(permit, species, 2.0f);

        permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        permit.setHarvestReportAuthor(user.getPerson());
        permit.setHarvestReportDate(DateUtil.now());
        permit.setHarvestReportModeratorOverride(false);

        onSavedAndAuthenticated(user, tx(() -> {
            checkEmail(permit, "EndOfHuntingReportNotificationTest_testReportUsingPermit_noHarvests.html");
        }));
    }

    private void checkEmail(final HarvestPermit permit,
                            final String expectedMessagePath) {
        final HarvestPermit reloadedPermit = harvestPermitRepository.getOne(permit.getId());

        final MailMessageDTO mailMessage = new EndOfHuntingReportNotification(handlebars, messageSource)
                .withPermit(reloadedPermit)
                .withRecipients(singleton("test@example.com"))
                .withSummaries(HarvestPermitSpecimenSummary.create(reloadedPermit.getAcceptedHarvestForEndOfHuntingReport()))
                .build("default@example.com");

        final ClassPathResource resource = new ClassPathResource(expectedMessagePath, EndOfHuntingReportNotificationTest.class);

        try (final InputStream is = resource.getInputStream()) {
            final String expectedMessage = trimMessage(CharStreams.toString(new InputStreamReader(is, Constants.DEFAULT_CHARSET)));
            final String actualMessage = trimMessage(mailMessage.getBody());

            assertEquals("Message body does not match", expectedMessage, actualMessage);

        } catch (IOException e) {
            fail("Caught exception: " + e.getMessage());
        }
    }

    private static String trimMessage(final String message) {
        // remove whitespace formatting
        final String whitespaceRemoved = Pattern.compile(" {2,}").matcher(message).replaceAll("");
        // remove author id number in format author=3
        final String authorIdRemoved = Pattern.compile("(?!author=)(\\d+)").matcher(whitespaceRemoved).replaceAll("");
        return authorIdRemoved.trim();
    }
}
