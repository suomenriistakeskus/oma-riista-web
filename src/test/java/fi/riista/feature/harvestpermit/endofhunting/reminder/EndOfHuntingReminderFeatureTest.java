package fi.riista.feature.harvestpermit.endofhunting.reminder;

import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BROWN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GARGANEY;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOUNTAIN_HARE;
import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType.ALL;
import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType.MULTI_YEAR;
import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType.ONE_YEAR;
import static fi.riista.feature.permit.PermitTypeCode.ANNUAL_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.NEST_REMOVAL_BASED;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class EndOfHuntingReminderFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private EndOfHuntingReminderFeature feature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    private Person contactPerson;
    private HarvestPermit permit;
    private GameSpecies species;
    private HarvestPermitSpeciesAmount speciesAmount;
    private LocalDate beginDate = new LocalDate(2020, 12, 1);
    private LocalDate endDate = new LocalDate(2020, 12, 31);
    private LocalDate endDate2 = new LocalDate(2020, 2, 15);

    private EndOfHuntingReminderDTO expectedOneYearReminderDTO;
    private EndOfHuntingReminderDTO expectedMultiYearReminderDTO;
    private EndOfHuntingReminderDTO expectedAnnualRenewalReminderDTO;
    private EndOfHuntingReminderDTO expectedAnnualRenewalReminderDTO2;

    @Before
    public void setup() {
        // One year permit
        contactPerson = model().newPerson();
        contactPerson.setEmail("email@invalid");

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final PermitDecision decision = model().newPermitDecision(rhy);
        decision.setValidityYears(1);

        permit = model().newHarvestPermit(rhy);
        permit.setOriginalContactPerson(contactPerson);
        permit.setPermitDecision(decision);
        permit.setPermitTypeCode(MAMMAL_DAMAGE_BASED);

        species = model().newGameSpecies(OFFICIAL_CODE_MOUNTAIN_HARE, GameCategory.GAME_MAMMAL, "jänis-fi", "jänis-sv", "jänis-en");

        speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);
        speciesAmount.setBeginDate(beginDate);
        speciesAmount.setEndDate(endDate);

        // Multi year permit
        final Person multiYearContact = model().newPerson();
        multiYearContact.setEmail("email2@invalid");

        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final PermitDecision multiYearDecision = model().newPermitDecision(rhy2);
        multiYearDecision.setValidityYears(3);

        final HarvestPermit multiYearPermit = model().newHarvestPermit(rhy2);
        multiYearPermit.setOriginalContactPerson(multiYearContact);
        multiYearPermit.setPermitDecision(multiYearDecision);
        multiYearPermit.setPermitTypeCode(MAMMAL_DAMAGE_BASED);

        final HarvestPermitSpeciesAmount multiYearSpeciesAmount = model().newHarvestPermitSpeciesAmount(multiYearPermit, species);
        multiYearSpeciesAmount.setBeginDate(beginDate);
        multiYearSpeciesAmount.setEndDate(endDate);

        final GameSpecies multiYearSpecies = model().newGameSpecies(OFFICIAL_CODE_BROWN_HARE, GameCategory.GAME_MAMMAL, "rusakko-fi", "rusakko-sv", "rusakko-en");
        final HarvestPermitSpeciesAmount multiYearSpeciesAmount2 = model().newHarvestPermitSpeciesAmount(multiYearPermit, multiYearSpecies);
        multiYearSpeciesAmount2.setBeginDate(beginDate);
        multiYearSpeciesAmount2.setEndDate(endDate);

        // Annual renewal (multi year) permit
        final Person annualRenewalContact = model().newPerson();
        annualRenewalContact.setEmail("email3@invalid");

        final Riistanhoitoyhdistys rhy3 = model().newRiistanhoitoyhdistys();
        final PermitDecision annualRenewalDecision = model().newPermitDecision(rhy3);
        annualRenewalDecision.setValidityYears(1);

        final HarvestPermit annualRenewalPermit = model().newHarvestPermit(rhy3);
        annualRenewalPermit.setOriginalContactPerson(annualRenewalContact);
        annualRenewalPermit.setPermitDecision(multiYearDecision);
        annualRenewalPermit.setPermitTypeCode(ANNUAL_UNPROTECTED_BIRD);

        final GameSpecies birdSpecies = model().newGameSpecies(OFFICIAL_CODE_GARGANEY, GameCategory.UNPROTECTED, "heinätavi-fi", "heinätavi-sv", "heinätavi-en");

        final HarvestPermitSpeciesAmount annualRenewalSpeciesAmount = model().newHarvestPermitSpeciesAmount(annualRenewalPermit, birdSpecies);
        annualRenewalSpeciesAmount.setBeginDate(beginDate);
        annualRenewalSpeciesAmount.setEndDate(endDate);

        // Annual renewal permit with different end date
        final Person annualRenewalContact2 = model().newPerson();
        annualRenewalContact2.setEmail("email4@invalid");

        final Riistanhoitoyhdistys rhy4 = model().newRiistanhoitoyhdistys();
        final PermitDecision annualRenewalDecision2 = model().newPermitDecision(rhy4);
        annualRenewalDecision2.setValidityYears(1);

        final HarvestPermit annualRenewalPermit2 = model().newHarvestPermit(rhy4);
        annualRenewalPermit2.setOriginalContactPerson(annualRenewalContact2);
        annualRenewalPermit2.setPermitDecision(annualRenewalDecision2);
        annualRenewalPermit2.setPermitTypeCode(ANNUAL_UNPROTECTED_BIRD);

        final HarvestPermitSpeciesAmount annualRenewalSpeciesAmount2 = model().newHarvestPermitSpeciesAmount(annualRenewalPermit2, birdSpecies);
        annualRenewalSpeciesAmount2.setBeginDate(new LocalDate(2020, 1, 1));
        annualRenewalSpeciesAmount2.setEndDate(new LocalDate(2020, 1, 15));
        annualRenewalSpeciesAmount2.setBeginDate(new LocalDate(2020, 2, 1));
        annualRenewalSpeciesAmount2.setEndDate(endDate2);

        // Another permit not to be found in searches
        final Person contact2 = model().newPerson();
        contact2.setEmail("contact2-email@invalid");

        final PermitDecision decision2 = model().newPermitDecision(rhy);
        decision2.setValidityYears(1);

        final HarvestPermit permit2 = model().newHarvestPermit(rhy);
        permit2.setOriginalContactPerson(contact2);
        permit2.setPermitDecision(decision2);
        permit2.setPermitTypeCode(NEST_REMOVAL_BASED);

        final HarvestPermitSpeciesAmount speciesAmount2 = model().newHarvestPermitSpeciesAmount(permit2, species);
        speciesAmount2.setBeginDate(beginDate);
        speciesAmount2.setEndDate(endDate);

        // Permit with harvest report not to be found
        final PermitDecision decisionForReportedPermit = model().newPermitDecision(rhy);
        decisionForReportedPermit.setValidityYears(1);

        final HarvestPermit reportedPermit = model().newHarvestPermit(rhy);
        reportedPermit.setOriginalContactPerson(contactPerson);
        reportedPermit.setPermitDecision(decision);
        reportedPermit.setPermitTypeCode(MAMMAL_DAMAGE_BASED);
        reportedPermit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        reportedPermit.setHarvestReportDate(DateUtil.now());
        reportedPermit.setHarvestReportAuthor(contactPerson);
        reportedPermit.setHarvestReportModeratorOverride(false);

        final HarvestPermitSpeciesAmount speciesAmountForReportedPermit = model().newHarvestPermitSpeciesAmount(reportedPermit, species);
        speciesAmountForReportedPermit.setBeginDate(beginDate);
        speciesAmountForReportedPermit.setEndDate(endDate);

        persistInNewTransaction();

        expectedOneYearReminderDTO =
                new EndOfHuntingReminderDTO(
                        contactPerson.getEmail(),
                        null,
                        permit.getId(),
                        permit.getPermitNumber(),
                        asList(species.getNameLocalisation()));

        expectedMultiYearReminderDTO =
                new EndOfHuntingReminderDTO(
                        multiYearContact.getEmail(),
                        null,
                        multiYearPermit.getId(),
                        multiYearPermit.getPermitNumber(),
                        asList(species.getNameLocalisation(), multiYearSpecies.getNameLocalisation()));

        expectedAnnualRenewalReminderDTO =
                new EndOfHuntingReminderDTO(
                        annualRenewalContact.getEmail(),
                        null,
                        annualRenewalPermit.getId(),
                        annualRenewalPermit.getPermitNumber(),
                        asList(birdSpecies.getNameLocalisation()));

        expectedAnnualRenewalReminderDTO2 =
                new EndOfHuntingReminderDTO(
                        annualRenewalContact2.getEmail(),
                        null,
                        annualRenewalPermit2.getId(),
                        annualRenewalPermit2.getPermitNumber(),
                        asList(birdSpecies.getNameLocalisation()));
    }

    @Test
    public void testGetMissingEndOfHuntingReports() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<EndOfHuntingReminderDTO> missingReports = feature.getMissingEndOfHuntingReports(endDate, ALL);
            assertThat(missingReports, containsInAnyOrder(expectedOneYearReminderDTO, expectedMultiYearReminderDTO, expectedAnnualRenewalReminderDTO));
        });
    }

    @Test
    public void testGetMissingEndOfHuntingReports_endDate2() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<EndOfHuntingReminderDTO> missingReports = feature.getMissingEndOfHuntingReports(endDate2, ALL);
            assertThat(missingReports, hasSize(1));

            final EndOfHuntingReminderDTO missing = missingReports.get(0);
            assertThat(missing, is(equalTo(expectedAnnualRenewalReminderDTO2)));
        });
    }

    @Test
    public void testGetMissingEndOfHuntingReports_additionalContact() {
        final Person additionalContact = model().newPerson();
        additionalContact.setEmail("email2@invalid");
        model().newHarvestPermitContactPerson(permit, additionalContact);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<EndOfHuntingReminderDTO> missingReports = feature.getMissingEndOfHuntingReports(endDate, ALL);

            final EndOfHuntingReminderDTO expectedAdditionalContactDTO =
                    new EndOfHuntingReminderDTO(
                            contactPerson.getEmail(),
                            asList(additionalContact.getEmail()),
                            permit.getId(),
                            permit.getPermitNumber(),
                            asList(species.getNameLocalisation()));

            assertThat(missingReports, containsInAnyOrder(expectedAdditionalContactDTO, expectedMultiYearReminderDTO, expectedAnnualRenewalReminderDTO));
        });
    }

    @Test
    public void testGetMissingEndOfHuntingReports_originalContactEmailMissing() {
        runInTransaction(() -> {
            final HarvestPermit updatePermit = harvestPermitRepository.getOne(permit.getId());
            updatePermit.getOriginalContactPerson().setEmail(null);
        });

        final Person additionalContact = model().newPerson();
        additionalContact.setEmail("email2@invalid");
        model().newHarvestPermitContactPerson(permit, additionalContact);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<EndOfHuntingReminderDTO> missingReports = feature.getMissingEndOfHuntingReports(endDate, ALL);

            final EndOfHuntingReminderDTO expectedAdditionalContactDTO =
                    new EndOfHuntingReminderDTO(
                            null,
                            asList(additionalContact.getEmail()),
                            permit.getId(),
                            permit.getPermitNumber(),
                            asList(species.getNameLocalisation()));

            assertThat(missingReports, containsInAnyOrder(expectedAdditionalContactDTO, expectedMultiYearReminderDTO, expectedAnnualRenewalReminderDTO));
        });
    }

    @Test
    public void testGetMissingEndOfHuntingReports_noEmail() {
        runInTransaction(() -> {
            final HarvestPermit updatePermit = harvestPermitRepository.getOne(permit.getId());
            updatePermit.getOriginalContactPerson().setEmail(null);
        });

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<EndOfHuntingReminderDTO> missingReports = feature.getMissingEndOfHuntingReports(endDate, ALL);
            assertThat(missingReports, containsInAnyOrder(expectedMultiYearReminderDTO, expectedAnnualRenewalReminderDTO));
        });
    }

    @Test
    public void testGetMissingEndOfHuntingReports_oneYearPermit() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<EndOfHuntingReminderDTO> missingReports = feature.getMissingEndOfHuntingReports(endDate, ONE_YEAR);
            assertThat(missingReports, hasSize(1));

            final EndOfHuntingReminderDTO missing = missingReports.get(0);
            assertThat(missing, is(equalTo(expectedOneYearReminderDTO)));
        });
    }

    @Test
    public void testGetMissingEndOfHuntingReports_multiYearPermit() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<EndOfHuntingReminderDTO> missingReports = feature.getMissingEndOfHuntingReports(endDate.getYear(), MULTI_YEAR);
            assertThat(missingReports, containsInAnyOrder(expectedMultiYearReminderDTO, expectedAnnualRenewalReminderDTO, expectedAnnualRenewalReminderDTO2));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetMissingEndOfHuntingReports_unauthorizedUser() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getMissingEndOfHuntingReports(endDate, ALL);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetMissingEndOfHuntingReports_unauthorizedModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.getMissingEndOfHuntingReports(endDate, ALL);
        });
    }
}
