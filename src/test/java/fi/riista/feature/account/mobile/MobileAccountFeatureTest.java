package fi.riista.feature.account.mobile;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.SortedMap;
import java.util.SortedSet;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class MobileAccountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    protected PersonRepository personRepo;

    protected abstract MobileAccountFeature feature();

    @Test(expected = RuntimeException.class)
    public void testGetMobileAccount_failsWhenUserNotAuthenticated() {
        createUserWithPerson();
        persistInNewTransaction();

        feature().getMobileAccount();
    }

    @Test(expected = RuntimeException.class)
    public void testGetMobileAccount_failsWhenUserAuthenticatedButNotAssociatedWithPerson() {
        persistAndAuthenticateWithNewUser(false);
        feature().getMobileAccount();
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
                final MobileAccountDTO dto = feature().getMobileAccount();
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

    protected static void doMobileAccountAssertions(final MobileAccountDTO dto,
                                                    final Person person,
                                                    final String expectedUsername,
                                                    final SortedSet<Integer> expectedGameDiaryYears,
                                                    final DateTime minTimestamp) {

        assertEquals(expectedUsername, dto.getUsername());
        assertEquals(person.getFirstName(), dto.getFirstName());
        assertEquals(person.getLastName(), dto.getLastName());
        assertEquals(person.parseDateOfBirth(), dto.getBirthDate());

        assertEquals(AddressDTO.from(person.getAddress()), dto.getAddress());

        assertEquals(person.getHunterNumber(), dto.getHunterNumber());
        assertEquals(person.getHunterExamDate(), dto.getHunterExamDate());
        assertEquals(person.getHuntingCardStart(), dto.getHuntingCardStart());
        assertEquals(person.getHuntingCardEnd(), dto.getHuntingCardEnd());
        assertEquals(person.getHuntingBanStart(), dto.getHuntingBanStart());
        assertEquals(person.getHuntingBanEnd(), dto.getHuntingBanEnd());
        assertEquals(person.getHomeMunicipalityName().asMap(), dto.getHomeMunicipality());

        assertEquals(expectedGameDiaryYears, dto.getGameDiaryYears());

        if (person.getRhyMembership() != null) {
            assertEquals(MobileOrganisationDTO.create(person.getRhyMembership()), dto.getRhy());
        }

        assertNotNull(dto.getTimestamp());
        assertFalse(minTimestamp.isAfter(dto.getTimestamp()));
    }

    protected SystemUser createEntityGraphForMobileAccountTest(final SortedMap<Integer, Integer> harvestYearToAmountMapping) {
        // Sanity check for test input data
        assertFalse(F.isNullOrEmpty(harvestYearToAmountMapping));

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

        return user;
    }

}
