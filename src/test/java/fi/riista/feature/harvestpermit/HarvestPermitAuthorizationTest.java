package fi.riista.feature.harvestpermit;

import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.CREATE_REMOVE_MOOSE_HARVEST_REPORT;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.LIST_LEADERS;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.UPDATE_ALLOCATIONS;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.VIEW_OBSERVATION_SUMMARY;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;
import static java.util.Arrays.asList;

public class HarvestPermitAuthorizationTest extends EmbeddedDatabaseTest {

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test
    public void testPermitContactPerson() {
        testHasPermissions(createUserWithPerson(), PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    @Test
    public void testAdmin() {
        testHasPermissions(createNewAdmin(), PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    @Test
    public void testModerator() {
        testHasPermissions(createNewModerator(), PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    @Test
    public void testModerator_disabilityPermitWithPrivileges() {
        testHasPermissions(createNewModerator(SystemUserPrivilege.MODERATE_DISABILITY_PERMIT_APPLICATION), PermitTypeCode.DISABILITY_BASED);
    }

    @Test
    public void testModerator_disabilityPermitWithoutPrivileges() {
        HarvestPermit permit = model().newHarvestPermit(newRhy(), permitNumber(), PermitTypeCode.DISABILITY_BASED);

        onSavedAndAuthenticated(createNewModerator(),
                () -> assertNoPermissions(permit, asList(READ, UPDATE, UPDATE_ALLOCATIONS, CREATE_REMOVE_MOOSE_HARVEST_REPORT, LIST_LEADERS)));
    }

    private void testHasPermissions(final SystemUser user, final String permitTypeCode) {
        HarvestPermit permit = model().newHarvestPermit(newRhy(), permitNumber(), permitTypeCode);
        if (user.getPerson() != null) {
            permit.setOriginalContactPerson(user.getPerson());
        }

        onSavedAndAuthenticated(user, () -> assertHasPermissions(permit, asList(
                READ, UPDATE, UPDATE_ALLOCATIONS, CREATE_REMOVE_MOOSE_HARVEST_REPORT, LIST_LEADERS, VIEW_OBSERVATION_SUMMARY)));
    }

    @Test
    public void testCoordinatorForPermitsRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = coordinatorRhy;
        testCoordinator(true, coordinatorRhy, permitRhy, null, null, PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    @Test
    public void testCoordinatorForPermitsRhy_disabilityPermit() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = coordinatorRhy;
        testCoordinator(false, coordinatorRhy, permitRhy, null, null, PermitTypeCode.DISABILITY_BASED);
    }

    @Test
    public void testPastCoordinatorForPermitsRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = coordinatorRhy;
        testCoordinator(false, coordinatorRhy, permitRhy, null, today().minusDays(1), PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    @Test
    public void testFutureCoordinatorForPermitsRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = coordinatorRhy;
        testCoordinator(false, coordinatorRhy, permitRhy, today().plusDays(1), null, PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    @Test
    public void testCoordinatorForOtherRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = newRhy();
        testCoordinator(false, coordinatorRhy, permitRhy, null, null, PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    @Test
    public void testCoordinatorForRelatedRhy() {
        Riistanhoitoyhdistys coordinatorRhy = newRhy();
        Riistanhoitoyhdistys permitRhy = newRhy();
        testCoordinator(true, coordinatorRhy, permitRhy, coordinatorRhy, null, null, PermitTypeCode.MAMMAL_DAMAGE_BASED);
    }

    private void testCoordinator(final boolean hasPermission,
                                 final Riistanhoitoyhdistys coordinatorRhy,
                                 final Riistanhoitoyhdistys permitRhy,
                                 final LocalDate begin,
                                 final LocalDate end,
                                 final String permitTypeCode) {
        testCoordinator(hasPermission, coordinatorRhy, permitRhy, null, begin, end, permitTypeCode);
    }

    private void testCoordinator(final boolean hasPermission,
                                 final Riistanhoitoyhdistys coordinatorRhy,
                                 final Riistanhoitoyhdistys permitRhy,
                                 final Riistanhoitoyhdistys relatedRhy,
                                 final LocalDate begin,
                                 final LocalDate end,
                                 final String permitTypeCode) {
        withPerson(person -> {
            Occupation occupation = model().newOccupation(coordinatorRhy, person, TOIMINNANOHJAAJA);
            occupation.setBeginDate(begin);
            occupation.setEndDate(end);

            HarvestPermit permit = model().newHarvestPermit(permitRhy, permitNumber(), permitTypeCode);
            if (relatedRhy != null) {
                permit.setRelatedRhys(Sets.newHashSet(relatedRhy));
            }

            onSavedAndAuthenticated(createUser(person), () -> {
                assertPermission(hasPermission, permit, READ);
                assertPermission(hasPermission, permit, LIST_LEADERS);
            });
        });
    }

    @Test
    public void testCoordinatorInOneHarvestRhy() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(permitRhy -> {

            final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
            final HarvestPermit permit = model().newHarvestPermit(permitRhy);

            model().newHarvest(permit, species);
            model().newHarvest(permit, species);
            model().newHarvest(permit, species).setRhy(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> assertHasPermission(permit, READ));
        }));
    }

    @Test
    public void testMemberOfPermitHolder() {
        withPerson(member -> {
            final HuntingClub club = model().newHuntingClub(newRhy());
            model().newOccupation(club, member, SEURAN_JASEN);

            testPermitHolderAndPartner(member, club, null);
        });
    }

    @Test
    public void testMemberOfPermitPartner() {
        withRhy(rhy -> withPerson(member -> {
            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            model().newOccupation(club1, member, SEURAN_JASEN);

            testPermitHolderAndPartner(member, null, Sets.newHashSet(club2, club1));
        }));
    }

    private void testPermitHolderAndPartner(final Person clubMember, final HuntingClub club, final Set<HuntingClub> partners) {
        final HarvestPermit permit = model().newHarvestPermit(newRhy());
        if (club != null) {
            permit.setHuntingClub(club);
            permit.setPermitHolder(PermitHolder.createHolderForClub(club));
        }
        permit.setPermitPartners(partners);

        onSavedAndAuthenticated(createUser(clubMember), () -> {
            assertHasPermission(permit, READ);
            assertNoPermission(permit, LIST_LEADERS);
        });
    }

    @Test
    public void testCanUpdateAllocationsAndFinishPermit_clubRoles() {
        doTest(false, null, null, null, null);

        doTest(false, null, null, SEURAN_JASEN, null);
        doTest(false, null, null, SEURAN_JASEN, RYHMAN_JASEN);
        doTest(false, null, null, SEURAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA);

        doTest(false, null, null, SEURAN_YHDYSHENKILO, null);
        doTest(false, null, null, SEURAN_YHDYSHENKILO, RYHMAN_JASEN);
        doTest(false, null, null, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);

        doTest(false, SEURAN_JASEN, null, null, null);
        doTest(false, SEURAN_JASEN, RYHMAN_JASEN, null, null);
        doTest(true, SEURAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA, null, null);
        doTest(false, SEURAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA, null, null, false);

        doTest(true, SEURAN_YHDYSHENKILO, null, null, null);
        doTest(true, SEURAN_YHDYSHENKILO, RYHMAN_JASEN, null, null);
        doTest(true, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, null, null);
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
        permit.setHuntingClub(holderClub);
        permit.setPermitHolder(PermitHolder.createHolderForClub(holderClub));
        permit.setPermitPartners(Sets.newHashSet(partnerClub, holderClub));

        if (groupIsAttachedToPermit) {
            holderGroup.updateHarvestPermit(permit);
            partnerGroup.updateHarvestPermit(permit);
        }

        onSavedAndAuthenticated(user, () -> {
            assertPermission(expected, permit, UPDATE_ALLOCATIONS);
            assertPermission(expected, permit, CREATE_REMOVE_MOOSE_HARVEST_REPORT);

            final boolean shouldBeAbleToListReaders =
                    holderClubOccupation == SEURAN_YHDYSHENKILO ||
                            groupIsAttachedToPermit && holderGroupOccupation == RYHMAN_METSASTYKSENJOHTAJA ||
                            partnerClubOccupation == SEURAN_YHDYSHENKILO ||
                            groupIsAttachedToPermit && partnerGroupOccupation == RYHMAN_METSASTYKSENJOHTAJA;

            assertPermission(shouldBeAbleToListReaders, permit, LIST_LEADERS);
        });
    }

    private Riistanhoitoyhdistys newRhy() {
        return model().newRiistanhoitoyhdistys(this.rka);
    }
}
