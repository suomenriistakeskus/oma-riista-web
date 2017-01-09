package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.io.CharStreams;
import fi.riista.config.Constants;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportRepository;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EndOfHuntingReportNotificationTest extends EmbeddedDatabaseTest {

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestReportRepository harvestReportRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

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

        createHarvestReport(species, user.getPerson(), permit, GameAge.ADULT, GameGender.MALE);
        createHarvestReport(species, user.getPerson(), permit, GameAge.YOUNG, GameGender.MALE);

        final HarvestReport endOfHuntingReport = model().newHarvestReport_endOfHunting(
                permit, HarvestReport.State.SENT_FOR_APPROVAL, user.getPerson());

        permit.setEndOfHuntingReport(endOfHuntingReport);

        onSavedAndAuthenticated(user, tx(() -> {
            checkEmail(permit, endOfHuntingReport, "EndOfHuntingReportNotificationTest_testReportUsingPermit.html");
        }));
    }

    private void createHarvestReport(GameSpecies species,
                                     Person hunter,
                                     HarvestPermit permit,
                                     GameAge age,
                                     GameGender gender) {
        final Harvest harvest = model().newHarvest(species, hunter);
        model().newHarvestSpecimen(harvest, age, gender);
        final HarvestReport approvedHarvestReport = model().newHarvestReport(harvest, HarvestReport.State.APPROVED);
        approvedHarvestReport.setHarvestPermit(permit);
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

        final HarvestReport endOfHuntingReport = model().newHarvestReport_endOfHunting(
                permit, HarvestReport.State.SENT_FOR_APPROVAL, user.getPerson());

        permit.setEndOfHuntingReport(endOfHuntingReport);

        onSavedAndAuthenticated(user, tx(() -> {
            checkEmail(permit, endOfHuntingReport, "EndOfHuntingReportNotificationTest_testReportUsingPermit_noHarvests.html");
        }));
    }

    private void checkEmail(final HarvestPermit permit,
                            final HarvestReport endOfHuntingReport,
                            final String expectedMessagePath) {
        final HarvestReport reloadedReport = harvestReportRepository.getOne(endOfHuntingReport.getId());
        final HarvestPermit reloadedPermit = harvestPermitRepository.getOne(permit.getId());

        final MailMessageDTO.Builder notification = new EndOfHuntingReportNotification(handlebars, messageSource)
                .withReport(reloadedReport)
                .withPermit(reloadedPermit)
                .withEmail("test@example.com")
                .withSummaries(EndOfHuntingReportNotification.SpecimenSummary.create(reloadedPermit.getUndeletedHarvestReports()))
                .build();

        final MailMessageDTO mailMessage = notification.withFrom("default@example.com").build();

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
        // Trim leading/trainling whitespace and changing timestamp with format 06.07.2016 14:35:49
        return Pattern.compile("\n.*\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2},.*\n").matcher(message).replaceAll("").trim();
    }
}
