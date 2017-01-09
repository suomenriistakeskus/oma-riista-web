package fi.riista.feature.organization.person;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ProcessDeceasedPersonFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ProcessDeceasedPersonFeature processDeceasedPersonFeature;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private UserRepository userRepository;

    // Occupations

    @Test
    public void testEndOccupations() {
        withRhy(rhy -> {
            final Person person = model().newPerson();
            person.setDeletionCode(Person.DeletionCode.D);

            final HuntingClub huntingClub = model().newHuntingClub(rhy);
            final HuntingClubGroup huntingClubGroup = model().newHuntingClubGroup(huntingClub);

            final Occupation o1 = model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
            final Occupation o2 = model().newOccupation(huntingClub, person, OccupationType.SEURAN_YHDYSHENKILO);
            final Occupation o3 = model().newOccupation(huntingClubGroup, person, OccupationType.RYHMAN_JASEN);
            o1.setBeginDate(new LocalDate(2000, 1, 1));
            o2.setBeginDate(new LocalDate(2000, 1, 1));
            o3.setBeginDate(new LocalDate(2000, 1, 1));
            o2.setEndDate(new LocalDate(2100, 1, 1));

            persistInNewTransaction();

            processDeceasedPersonFeature.execute();

            final List<Occupation> all = occupationRepository.findAll();
            final LocalDate yesterday = DateUtil.today().minusDays(1);

            assertThat(all, hasSize(3));
            all.forEach(occupation -> {
                assertEquals(new LocalDate(2000, 1, 1), occupation.getBeginDate());
                assertEquals(yesterday, occupation.getEndDate());
            });
        });
    }

    @Test
    public void testEndOccupations_OnlyIfDeceased() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            persistInNewTransaction();

            processDeceasedPersonFeature.execute();

            final List<Occupation> all = occupationRepository.findAll();
            assertThat(all, hasSize(1));

            all.forEach(occupation -> {
                assertNull(occupation.getBeginDate());
                assertNull(occupation.getEndDate());
            });
        });
    }

    @Test
    public void testEndOccupations_OnlyIfValidNow() {
        withRhyAndCoordinatorOccupation((rhy, o1) -> {
            o1.getPerson().setDeletionCode(Person.DeletionCode.D);

            o1.setBeginDate(new LocalDate(2000, 1, 1));
            o1.setEndDate(new LocalDate(2001, 1, 1));

            persistInNewTransaction();

            processDeceasedPersonFeature.execute();

            final List<Occupation> all = occupationRepository.findAll();
            assertThat(all, hasSize(1));

            all.forEach(occupation -> {
                assertEquals(new LocalDate(2000, 1, 1), occupation.getBeginDate());
                assertEquals(new LocalDate(2001, 1, 1), occupation.getEndDate());
            });
        });
    }

    // Users

    @Test
    public void testDeactivateAccounts() {
        final Person person = model().newPerson();
        person.setDeletionCode(Person.DeletionCode.D);

        model().newUser(person);
        model().newUser(person);
        model().newUser(person);

        persistInNewTransaction();

        processDeceasedPersonFeature.execute();

        final List<SystemUser> all = userRepository.findAll();

        assertThat(all, hasSize(3));

        for (SystemUser systemUser : all) {
            assertFalse(systemUser.isActive());
        }
    }

    @Test
    public void testDeactivateAccounts_OnlyIfDeceased() {
        model().newUser(model().newPerson());

        persistInNewTransaction();

        processDeceasedPersonFeature.execute();

        final List<SystemUser> all = userRepository.findAll();

        assertThat(all, hasSize(1));

        for (SystemUser systemUser : all) {
            assertTrue(systemUser.isActive());
        }
    }
}
