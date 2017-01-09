package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.mobile.MobileAccountDTO;
import fi.riista.feature.gamediary.mobile.MobileGameDiaryFeature;
import fi.riista.feature.gamediary.mobile.MobileOrganisationDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import javaslang.Tuple2;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class MobileAccountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    protected PersonRepository personRepo;

    protected abstract MobileGameDiaryFeature feature();

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
        final SystemUser user = createUserWithPerson("username");

        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        model().newOccupation(model().newAlueellinenRiistaneuvosto(rka, "arn", "arn"),
                user.getPerson(), OccupationType.JASEN);

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        // Valid occupation
        model().newOccupation(rhy, user.getPerson(), OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);

        final Occupation notValid = model().newOccupation(rhy, user.getPerson(), OccupationType.PETOYHDYSHENKILO);
        notValid.setBeginDate(DateUtil.today().plusDays(1));

        final HuntingClub club = model().newHuntingClub(rhy);
        model().newOccupation(club, user.getPerson(), OccupationType.SEURAN_YHDYSHENKILO);

        onSavedAndAuthenticated(user, () -> {
            final MobileAccountDTO dto = feature().getMobileAccount();
    
            assertEquals(1, dto.getOccupations().size());
            assertEquals(OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA, dto.getOccupations().get(0).getOccupationType());
        });
    }

    protected static void doMobileAccountAssertions(
            final MobileAccountDTO dto,
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

    protected SystemUser createEntityGraphForMobileAccountTest(
            final Iterable<Tuple2<Integer, Integer>> pairsOfHarvestYearAndAmount) {

        // Sanity check for test input data
        assertFalse(F.isNullOrEmpty(pairsOfHarvestYearAndAmount));

        final LocalDate today = DateUtil.today();

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
        final int firstHarvestYear = pairsOfHarvestYearAndAmount.iterator().next()._1();
        final Harvest harvestShotByOtherPerson = model().newHarvest(
                model().newGameSpecies(), model().newPerson(), new LocalDate(firstHarvestYear - 1, 8, 1));
        harvestShotByOtherPerson.setAuthor(person);

        for (final Tuple2<Integer, Integer> pair : pairsOfHarvestYearAndAmount) {
            final int harvestYear = pair._1();
            final LocalDate firstHarvestDateForYear = new LocalDate(harvestYear, 8, 1);
            final GameSpecies species = model().newGameSpecies();

            final int numHarvestsForYear = pair._2();
            for (int i = 0; i < numHarvestsForYear; i++) {
                model().newHarvest(species, person, firstHarvestDateForYear.plusDays(i));
            }
        }

        return user;
    }

}
