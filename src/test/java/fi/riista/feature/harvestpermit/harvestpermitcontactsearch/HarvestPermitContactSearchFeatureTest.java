package fi.riista.feature.harvestpermit.harvestpermitcontactsearch;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.contactsearch.HarvestPermitContactSearchFeature;
import fi.riista.feature.harvestpermit.contactsearch.PermitContactSearchConditionDTO;
import fi.riista.feature.harvestpermit.contactsearch.PermitContactSearchResultDTO;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAN_GOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.BIRD;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MAMMAL;
import static fi.riista.feature.permit.PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;

public class HarvestPermitContactSearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitContactSearchFeature feature;

    private RiistakeskuksenAlue rka1;
    private RiistakeskuksenAlue rka2;
    private Riistanhoitoyhdistys rhy1;
    private Riistanhoitoyhdistys rhy2;
    private Riistanhoitoyhdistys rhy3;
    private Person person1;
    private Person person2;
    private Person person3;
    private Person person4;

    @Before
    public void setup() {
        rka1 = model().newRiistakeskuksenAlue();
        rka1.setNameFinnish("RKA 1");
        // Swedish names mixed for testing sorting
        rka1.setNameSwedish("VCO 2");

        rhy1 = model().newRiistanhoitoyhdistys(rka1);
        rhy1.setNameFinnish("RHY 1");
        rhy1.setNameSwedish("JVF 2");

        rhy2 = model().newRiistanhoitoyhdistys(rka1);
        rhy2.setNameFinnish("RHY 2");
        rhy2.setNameSwedish("JVF 1");

        rka2 = model().newRiistakeskuksenAlue();
        rka2.setNameFinnish("RKA 2");
        rka2.setNameSwedish("VCO 1");

        rhy3 = model().newRiistanhoitoyhdistys(rka2);
        rhy3.setNameFinnish("RHY 3");
        rhy3.setNameSwedish("JVF 3");

        person1 = model().newPerson("A", "A", "290990-421S", null);
        person1.setRhyMembership(rhy1);

        person2 = model().newPerson("B", "B", "280390-8803", null);
        person2.setRhyMembership(rhy2);

        person3 = model().newPerson("C", "C", "250280-433B", null);
        person3.setRhyMembership(rhy2);

        person4 = model().newPerson("D", "D", "080580-2574", null);
        person4.setRhyMembership(rhy3);

        final HarvestPermitApplication application1 = model().newHarvestPermitApplication(rhy1, null, MAMMAL);
        final PermitDecision decision1 = model().newPermitDecision(application1);
        final HarvestPermit permit1 = model().newHarvestPermit(rhy1, "2020-1-000-10001-9", MAMMAL_DAMAGE_BASED);
        permit1.setPermitDecision(decision1);
        permit1.setOriginalContactPerson(person1);

        final HarvestPermitApplication application2 = model().newHarvestPermitApplication(rhy1, null, MAMMAL);
        final PermitDecision decision2 = model().newPermitDecision(application2);
        final HarvestPermit permit2 = model().newHarvestPermit(rhy1, "2020-1-000-10003-5", MAMMAL_DAMAGE_BASED);
        permit2.setPermitDecision(decision2);
        permit2.setOriginalContactPerson(person2);
        model().newHarvestPermitContactPerson(permit2, person1);

        final HarvestPermitApplication application3 = model().newHarvestPermitApplication(rhy2, null, MAMMAL);
        final PermitDecision decision3 = model().newPermitDecision(application3);
        final HarvestPermit permit3 = model().newHarvestPermit(rhy2, "2020-1-000-10004-8", MAMMAL_DAMAGE_BASED);
        permit3.setPermitDecision(decision3);
        permit3.setOriginalContactPerson(person3);

        final HarvestPermitApplication application4 = model().newHarvestPermitApplication(rhy3, null, MAMMAL);
        final PermitDecision decision4 = model().newPermitDecision(application4);
        final HarvestPermit permit4 = model().newHarvestPermit(rhy3, "2020-1-000-10005-1", MAMMAL_DAMAGE_BASED);
        permit4.setPermitDecision(decision4);
        permit4.setOriginalContactPerson(person4);

        final HarvestPermitApplication application5 = model().newHarvestPermitApplication(rhy1, null, BIRD);
        final GameSpecies species1 = model().newGameSpecies(OFFICIAL_CODE_BEAN_GOOSE);
        final HarvestPermitApplicationSpeciesAmount amount1 = model().newHarvestPermitApplicationSpeciesAmount(application5, species1, 1, 1);
        application5.setSpeciesAmounts(singletonList(amount1));
        final PermitDecision decision5 = model().newPermitDecision(application5);
        final HarvestPermit permit5 = model().newHarvestPermit(rhy1, "2019-1-000-10027-1", FOWL_AND_UNPROTECTED_BIRD);
        permit5.setPermitDecision(decision5);
        permit5.setOriginalContactPerson(person2);

        final HarvestPermitApplication application6 = model().newHarvestPermitApplication(rhy2, null, BIRD);
        final GameSpecies species2 = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE);
        final HarvestPermitApplicationSpeciesAmount amount2 = model().newHarvestPermitApplicationSpeciesAmount(application6, species2, 1, 1);
        application6.setSpeciesAmounts(singletonList(amount2));
        final PermitDecision decision6 = model().newPermitDecision(application6);
        final HarvestPermit permit6 = model().newHarvestPermit(rhy2, "2020-1-000-10069-5", FOWL_AND_UNPROTECTED_BIRD);
        permit6.setPermitDecision(decision6);
        permit6.setOriginalContactPerson(person1);
    }

    @Test
    public void testSearchPermitContacts() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitContactSearchConditionDTO mammalSearchParams = new PermitContactSearchConditionDTO(MAMMAL, 2020);
            final PermitContactSearchConditionDTO birdSearchParams = new PermitContactSearchConditionDTO(BIRD, 2019);
            final List<PermitContactSearchResultDTO> results = feature.searchPermitContacts(Arrays.asList(mammalSearchParams, birdSearchParams), Locales.FI);
            assertThat(results, is(notNullValue()));

            final PermitContactSearchResultDTO birdContact =
                    new PermitContactSearchResultDTO(BIRD, 2019,
                            rka1.getNameLocalisation(), rhy1.getNameLocalisation(),
                            person2.getFirstName(), person2.getLastName(), person2.getEmail());
            final PermitContactSearchResultDTO mammalContact1 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka1.getNameLocalisation(), rhy1.getNameLocalisation(),
                        person1.getFirstName(), person1.getLastName(), person1.getEmail());
            final PermitContactSearchResultDTO mammalContact2 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka1.getNameLocalisation(), rhy1.getNameLocalisation(),
                            person2.getFirstName(),  person2.getLastName(), person2.getEmail());
            final PermitContactSearchResultDTO mammalContact3 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka1.getNameLocalisation(), rhy2.getNameLocalisation(),
                            person3.getFirstName(), person3.getLastName(), person3.getEmail());
            final PermitContactSearchResultDTO mammalContact4 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka2.getNameLocalisation(), rhy3.getNameLocalisation(),
                            person4.getFirstName(), person4.getLastName(), person4.getEmail());

            assertThat(results, contains(birdContact, mammalContact1, mammalContact2, mammalContact3, mammalContact4));
        });
    }

    @Test
    public void testSearchPermitContacts_swedishSorting() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitContactSearchConditionDTO mammalSearchParams = new PermitContactSearchConditionDTO(MAMMAL, 2020);
            final PermitContactSearchConditionDTO birdSearchParams = new PermitContactSearchConditionDTO(BIRD, 2019);
            final List<PermitContactSearchResultDTO> results = feature.searchPermitContacts(Arrays.asList(mammalSearchParams, birdSearchParams), Locales.SV);
            assertThat(results, is(notNullValue()));

            final PermitContactSearchResultDTO birdContact =
                    new PermitContactSearchResultDTO(BIRD, 2019,
                            rka1.getNameLocalisation(), rhy1.getNameLocalisation(),
                            person2.getFirstName(), person2.getLastName(), person2.getEmail());
            final PermitContactSearchResultDTO mammalContact1 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka1.getNameLocalisation(), rhy1.getNameLocalisation(),
                            person1.getFirstName(), person1.getLastName(), person1.getEmail());
            final PermitContactSearchResultDTO mammalContact2 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka1.getNameLocalisation(), rhy1.getNameLocalisation(),
                            person2.getFirstName(),  person2.getLastName(), person2.getEmail());
            final PermitContactSearchResultDTO mammalContact3 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka1.getNameLocalisation(), rhy2.getNameLocalisation(),
                            person3.getFirstName(), person3.getLastName(), person3.getEmail());
            final PermitContactSearchResultDTO mammalContact4 =
                    new PermitContactSearchResultDTO(MAMMAL, 2020,
                            rka2.getNameLocalisation(), rhy3.getNameLocalisation(),
                            person4.getFirstName(), person4.getLastName(), person4.getEmail());

            assertThat(results, contains(birdContact, mammalContact4, mammalContact3, mammalContact1, mammalContact2));
        });
    }

    @Test
    public void testSearchPermitContacts_notFound() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitContactSearchConditionDTO searchParams = new PermitContactSearchConditionDTO(MAMMAL, 2019);
            final List<PermitContactSearchResultDTO> results = feature.searchPermitContacts(singletonList(searchParams), Locales.FI);
            assertThat(results, is(notNullValue()));
            assertThat(results.isEmpty(), is(true));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testSearchPermitContacts_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            final PermitContactSearchConditionDTO searchParams = new PermitContactSearchConditionDTO(MAMMAL, 2020);
            feature.searchPermitContacts(singletonList(searchParams), Locales.FI);
        });
    }
}
