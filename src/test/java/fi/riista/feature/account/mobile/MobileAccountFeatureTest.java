package fi.riista.feature.account.mobile;

import com.google.common.collect.ImmutableSortedMap;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.SortedMap;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MobileAccountFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private PersonRepository personRepo;

    @Resource
    private MobileAccountFeature feature;

    @Test(expected = AccessDeniedException.class)
    public void testGetMobileAccount_failsWhenUserNotAuthenticated() {
        createUserWithPerson();
        persistInNewTransaction();

        feature.getMobileAccount();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMobileAccount_failsWhenUserAuthenticatedButNotAssociatedWithPerson() {
        persistAndAuthenticateWithNewUser(false);
        feature.getMobileAccount();
    }

    @Test
    public void testGetMobileAccount_succeedsWhenUserAuthenticated() {
        final SystemUser user = createEntityGraphForMobileAccountTest(
                ImmutableSortedMap.of(2014, 1, 2015, 2),
                ImmutableSortedMap.of(2013, 1, 2015, 1));

        onSavedAndAuthenticated(user, () -> {
            final DateTime now = DateUtil.now();
            final MobileAccountDTO dto = feature.getMobileAccount();

            runInTransaction(() -> {
                // Refresh Person object in order to have home municipality associated properly.
                final Long personId = requireNonNull(F.getId(user.getPerson()));
                final Person person = personRepo.findById(personId).orElseThrow(NotFoundException::new);

                assertEquals(user.getUsername(), dto.getUsername());
                assertEquals(person.getFirstName(), dto.getFirstName());
                assertEquals(person.getLastName(), dto.getLastName());
                assertEquals(person.parseDateOfBirth(), dto.getBirthDate());

                assertEquals(AddressDTO.from(person.getAddress()), dto.getAddress());

                if (person.getRhyMembership() != null) {
                    assertEquals(MobileOrganisationDTO.create(person.getRhyMembership()), dto.getRhy());
                }

                assertEquals(person.getHunterNumber(), dto.getHunterNumber());
                assertEquals(person.getHunterExamDate(), dto.getHunterExamDate());
                assertEquals(person.getHuntingCardStart(), dto.getHuntingCardStart());
                assertEquals(person.getHuntingCardEnd(), dto.getHuntingCardEnd());
                assertEquals(person.getHuntingBanStart(), dto.getHuntingBanStart());
                assertEquals(person.getHuntingBanEnd(), dto.getHuntingBanEnd());
                assertEquals(person.getHomeMunicipalityName().asMap(), dto.getHomeMunicipality());

                assertEquals(F.newSortedSet(2013, 2014, 2015), dto.getGameDiaryYears());
                assertEquals(F.newSortedSet(2014, 2015), dto.getHarvestYears());
                assertEquals(F.newSortedSet(2013, 2015), dto.getObservationYears());

                assertEquals(0, dto.getOccupations().size());

                assertNotNull(dto.getTimestamp());
                assertFalse(now.isAfter(dto.getTimestamp()));
            });
        });
    }

    @Test
    public void testGetMobileAccount_succeedsWithOccupations() {
        final LocalDate today = today();

        withPerson(person -> {
            final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

            model().newOccupation(model().newAlueellinenRiistaneuvosto(rka, "arn", "arn"), person, JASEN);

            final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

            // Valid occupation
            final Occupation shootingTestOfficialOccupation =
                    model().newOccupation(rhy, person, AMPUMAKOKEEN_VASTAANOTTAJA);
            shootingTestOfficialOccupation.setBeginDate(today.minusYears(1));
            shootingTestOfficialOccupation.setEndDate(today.plusYears(1));

            final Occupation notValid = model().newOccupation(rhy, person, PETOYHDYSHENKILO);
            notValid.setBeginDate(today.plusDays(1));

            final HuntingClub club = model().newHuntingClub(rhy);
            model().newOccupation(club, person, SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileAccountDTO dto = feature.getMobileAccount();
                assertEquals(1, dto.getOccupations().size());

                final MobileOccupationDTO occDTO = dto.getOccupations().get(0);

                assertEquals(shootingTestOfficialOccupation.getId(), Long.valueOf(occDTO.getId()));
                assertEquals(AMPUMAKOKEEN_VASTAANOTTAJA, occDTO.getOccupationType());
                assertEquals(shootingTestOfficialOccupation.getBeginDate(), occDTO.getBeginDate());
                assertEquals(shootingTestOfficialOccupation.getEndDate(), occDTO.getEndDate());

                assertEquals(
                        MobileOrganisationDTO.create(shootingTestOfficialOccupation.getOrganisation()),
                        occDTO.getOrganisation());
            });
        });
    }

    @Test
    public void testGetMobileAccount_deerPilotUserFlagNotSetForNonDeerPilotUser() {
        final Person person = model().newPerson();

        onSavedAndAuthenticated(createUser(person), () -> {
            final MobileAccountDTO dto = feature.getMobileAccount();
            assertFalse(dto.isDeerPilotUser());
        });
    }

    @Test
    public void testGetMobileAccount_deerPilotUserFlagSetForDeerPilotUser() {
        final HuntingGroupFixture fixture = new HuntingGroupFixture(model());
        model().newDeerPilot(fixture.permit);

        onSavedAndAuthenticated(createUser(fixture.groupMember), () -> {
            final MobileAccountDTO dto = feature.getMobileAccount();
            assertTrue(dto.isDeerPilotUser());
        });
    }

    private SystemUser createUserWithPersonForMobileAccountTest() {
        final LocalDate today = today();

        final Municipality municipality = model().newMunicipality();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final SystemUser user = createUserWithPerson("username");

        final Person person = user.getPerson();
        person.setHomeMunicipalityCode(municipality.getOfficialCode());
        person.setRhyMembership(rhy);

        // Two addresses are created in order to test that the one with higher
        // precedence will be preferred and picked into DTO.
        person.setMrAddress(model().newAddress());
        person.setOtherAddress(model().newAddress());

        person.setHuntingBanStart(today);
        person.setHuntingBanEnd(today.plusDays(1));

        return user;
    }

    private SystemUser createEntityGraphForMobileAccountTest(final SortedMap<Integer, Integer> harvestYearToAmountMapping,
                                                             final SortedMap<Integer, Integer> observationYearToAmountMapping) {

        final SystemUser user = createUserWithPersonForMobileAccountTest();

        // Sanity check for test input data
        assertFalse(F.isNullOrEmpty(harvestYearToAmountMapping));

        final Person person = user.getPerson();

        // Create a harvest that is shot by another person (but authored by the
        // requesting person) before the first given harvest year to test that
        // non-shot harvests are not included in array of harvest years.
        final int firstHarvestYear = harvestYearToAmountMapping.keySet().iterator().next();
        final Harvest harvestShotByOtherPerson = model().newHarvest(
                model().newGameSpecies(), model().newPerson(), new LocalDate(firstHarvestYear - 1, 8, 1));
        harvestShotByOtherPerson.setAuthor(person);

        harvestYearToAmountMapping.forEach((harvestYear, harvestAmount) -> {
            final LocalDate firstHarvestDateForYear = new LocalDate(harvestYear, 8, 1);
            final GameSpecies species = model().newGameSpecies();

            for (int i = 0; i < harvestAmount; i++) {
                model().newHarvest(species, person, firstHarvestDateForYear.plusDays(i));
            }
        });

        // Create an observation that is observed by another person (but
        // authored by the requesting person) before the first given
        // observation year to test that non-observed events are not included
        // in array of observation years.
        final int firstObservationYear = observationYearToAmountMapping.keySet().iterator().next();
        final Observation observationObservedByOtherPerson = model().newObservation(
                model().newGameSpecies(), model().newPerson(), new LocalDate(firstObservationYear - 1, 8, 1));
        observationObservedByOtherPerson.setAuthor(person);

        observationYearToAmountMapping.forEach((observationYear, observationAmount) -> {
            final LocalDate firstObservationDateForYear = new LocalDate(observationYear, 8, 1);
            final GameSpecies species = model().newGameSpecies();

            for (int i = 0; i < observationAmount; i++) {
                model().newObservation(species, person, firstObservationDateForYear.plusDays(i));
            }
        });

        return user;
    }
}
