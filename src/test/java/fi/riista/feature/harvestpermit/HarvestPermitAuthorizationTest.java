package fi.riista.feature.harvestpermit;

import com.google.common.collect.Sets;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.CREATE_REMOVE_MOOSE_HARVEST_REPORT;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.LIST_LEADERS;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.UPDATE_ALLOCATIONS;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class HarvestPermitAuthorizationTest extends EmbeddedDatabaseTest {

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test
    @Transactional
    public void testPermitContactPerson() {
        testHasPermissions(createUserWithPerson());
    }

    @Test
    @Transactional
    public void testAdmin() {
        testHasPermissions(createNewAdmin());
    }

    @Test
    @Transactional
    public void testModerator() {
        testHasPermissions(createNewModerator());
    }

    private void testHasPermissions(SystemUser user) {
        HarvestPermit permit = model().newHarvestPermit(newRhy());
        if (user.getPerson() != null) {
            permit.setOriginalContactPerson(user.getPerson());
        }

        persistInCurrentlyOpenTransaction();

        authenticate(user);

        assertHasPermission(permit, READ);
        assertHasPermission(permit, UPDATE);
        assertHasPermission(permit, UPDATE_ALLOCATIONS);
        assertHasPermission(permit, CREATE_REMOVE_MOOSE_HARVEST_REPORT);
        assertHasPermission(permit, LIST_LEADERS);
    }

    @Test
    @Transactional
    public void testCoordinatorForPermitsRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = coordinatorRhy;
        testRead(true, coordinatorRhy, permitRhy, null, null);
    }

    @Test
    @Transactional
    public void testPastCoordinatorForPermitsRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = coordinatorRhy;
        testRead(false, coordinatorRhy, permitRhy, null, today().minusDays(1));
    }

    @Test
    @Transactional
    public void testFutureCoordinatorForPermitsRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = coordinatorRhy;
        testRead(false, coordinatorRhy, permitRhy, today().plusDays(1), null);
    }

    @Test
    @Transactional
    public void testCoordinatorForOtherRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = newRhy();
        testRead(false, coordinatorRhy, permitRhy, null, null);
    }

    @Test
    @Transactional
    public void testCoordinatorForRelatedRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = newRhy();
        testRead(true, coordinatorRhy, permitRhy, coordinatorRhy, null, null);
    }

    private void testRead(boolean canRead, Riistanhoitoyhdistys coordinatorRhy, Riistanhoitoyhdistys permitRhy, LocalDate begin, LocalDate end) {
        testRead(canRead, coordinatorRhy, permitRhy, null, begin, end);
    }

    private void testRead(boolean canRead, Riistanhoitoyhdistys coordinatorRhy, Riistanhoitoyhdistys permitRhy, Riistanhoitoyhdistys relatedRhy, LocalDate begin, LocalDate end) {
        SystemUser user = createUserWithPerson();
        Occupation occupation = model().newOccupation(coordinatorRhy, user.getPerson(), OccupationType.TOIMINNANOHJAAJA);
        occupation.setBeginDate(begin);
        occupation.setEndDate(end);

        HarvestPermit permit = model().newHarvestPermit(permitRhy);
        if (relatedRhy != null) {
            permit.setRelatedRhys(Sets.newHashSet(relatedRhy));
        }

        persistInCurrentlyOpenTransaction();
        authenticate(user);

        assertHasPermission(canRead, permit, READ);
    }

    @Test
    @Transactional
    public void testCoordinatorInOneHarvestRhy() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(permitRhy -> {

            final HarvestPermit permit = model().newHarvestPermit(permitRhy);

            model().newHarvest(permit);
            model().newHarvest(permit);
            final Harvest harvest = model().newHarvest(permit);
            harvest.setRhy(rhy);

            final SystemUser user = createUser(coordinator);

            persistInCurrentlyOpenTransaction();
            authenticate(user);

            assertHasPermission(permit, READ);
        }));
    }

    @Test
    @Transactional
    public void testMemberOfPermitHolder() {
        withPerson(member -> {
            final HuntingClub club = model().newHuntingClub(newRhy());
            model().newOccupation(club, member, OccupationType.SEURAN_JASEN);

            testPermitHolderAndPartner(member, club, null);
        });
    }

    @Test
    @Transactional
    public void testMemberOfPermitPartner() {
        withRhy(rhy -> withPerson(member -> {
            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            model().newOccupation(club1, member, OccupationType.SEURAN_JASEN);

            testPermitHolderAndPartner(member, null, Sets.newHashSet(club2, club1));
        }));
    }

    private void testPermitHolderAndPartner(Person clubMember, HuntingClub club, Set<HuntingClub> partners) {
        final HarvestPermit permit = model().newHarvestPermit(newRhy());
        permit.setPermitHolder(club);
        permit.setPermitPartners(partners);

        final SystemUser user = createUser(clubMember);

        persistInCurrentlyOpenTransaction();
        authenticate(user);

        assertHasPermission(permit, READ);
        assertNoPermission(permit, LIST_LEADERS);
    }

    @Test
    public void testCanUpdateAllocationsAndFinishPermit_clubRoles() {
        doTest(false, null, null, null, null);

        doTest(false, null, null, OccupationType.SEURAN_JASEN, null);
        doTest(false, null, null, OccupationType.SEURAN_JASEN, OccupationType.RYHMAN_JASEN);
        doTest(false, null, null, OccupationType.SEURAN_JASEN, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        doTest(false, null, null, OccupationType.SEURAN_YHDYSHENKILO, null);
        doTest(false, null, null, OccupationType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_JASEN);
        doTest(false, null, null, OccupationType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        doTest(false, OccupationType.SEURAN_JASEN, null, null, null);
        doTest(false, OccupationType.SEURAN_JASEN, OccupationType.RYHMAN_JASEN, null, null);
        doTest(true, OccupationType.SEURAN_JASEN, OccupationType.RYHMAN_METSASTYKSENJOHTAJA, null, null);
        doTest(false, OccupationType.SEURAN_JASEN, OccupationType.RYHMAN_METSASTYKSENJOHTAJA, null, null, false);

        doTest(true, OccupationType.SEURAN_YHDYSHENKILO, null, null, null);
        doTest(true, OccupationType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_JASEN, null, null);
        doTest(true, OccupationType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_METSASTYKSENJOHTAJA, null, null);
    }

    private void doTest(boolean expected,
                        OccupationType holderClubOccupation, OccupationType holderGroupOccupation,
                        OccupationType partnerClubOccupation, OccupationType partnerGroupOccupation) {

        doTest(expected, holderClubOccupation, holderGroupOccupation, partnerClubOccupation, partnerGroupOccupation, true);
    }

    private void doTest(boolean expected,
                        OccupationType holderClubOccupation, OccupationType holderGroupOccupation,
                        OccupationType partnerClubOccupation, OccupationType partnerGroupOccupation, boolean groupIsAttachedToPermit) {
        withPerson(person -> {
            doTest(expected, holderClubOccupation, holderGroupOccupation,
                    partnerClubOccupation, partnerGroupOccupation,
                    createUser(person), person, groupIsAttachedToPermit);
        });
    }

    private void doTest(boolean expected,
                        OccupationType holderClubOccupation, OccupationType holderGroupOccupation,
                        OccupationType partnerClubOccupation, OccupationType partnerGroupOccupation,
                        SystemUser user, Person person, boolean groupIsAttachedToPermit) {

        Riistanhoitoyhdistys rhy = newRhy();
        HuntingClub holderClub = model().newHuntingClub(rhy);
        HuntingClubGroup holderGroup = model().newHuntingClubGroup(holderClub);
        HuntingClub partnerClub = model().newHuntingClub(rhy);
        HuntingClubGroup partnerGroup = model().newHuntingClubGroup(partnerClub);

        if (holderClubOccupation != null) {
            model().newOccupation(holderClub, person, holderClubOccupation);
        }
        if (holderGroupOccupation != null) {
            model().newOccupation(holderGroup, person, holderGroupOccupation);
        }
        if (partnerClubOccupation != null) {
            model().newOccupation(partnerClub, person, partnerClubOccupation);
        }
        if (partnerGroupOccupation != null) {
            model().newOccupation(partnerGroup, person, partnerGroupOccupation);
        }

        HarvestPermit permit = model().newHarvestPermit(rhy);
        permit.setPermitHolder(holderClub);
        permit.setPermitPartners(Sets.newHashSet(partnerClub, holderClub));
        if (groupIsAttachedToPermit) {
            holderGroup.updateHarvestPermit(permit);
            partnerGroup.updateHarvestPermit(permit);
        }

        onSavedAndAuthenticated(user, tx(() -> {
            assertHasPermission(expected, permit, UPDATE_ALLOCATIONS);
            assertHasPermission(expected, permit, CREATE_REMOVE_MOOSE_HARVEST_REPORT);

            if (holderClubOccupation == OccupationType.SEURAN_YHDYSHENKILO ||
                    (groupIsAttachedToPermit && holderGroupOccupation == OccupationType.RYHMAN_METSASTYKSENJOHTAJA) ||
                    partnerClubOccupation == OccupationType.SEURAN_YHDYSHENKILO ||
                    (groupIsAttachedToPermit && partnerGroupOccupation == OccupationType.RYHMAN_METSASTYKSENJOHTAJA)) {
                assertHasPermission(permit, LIST_LEADERS);
            } else {
                assertNoPermission(permit, LIST_LEADERS);
            }
        }));
    }

    private Riistanhoitoyhdistys newRhy() {
        return model().newRiistanhoitoyhdistys(this.rka);
    }

}
