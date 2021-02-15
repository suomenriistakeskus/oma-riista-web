package fi.riista.integration.luke;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Address;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Amount;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Club;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Group;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_HuntingDay;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Permit;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Person;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LukeExportFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    private static final int YEAR = 2015;

    @Resource
    private LukeExportFeature feature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    private SystemUser apiUser;
    private GameSpecies mooseSpecies;
    private GameSpecies otherSpecies;
    private RiistakeskuksenAlue rka;

    @Before
    public void setUp() {
        apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_LUKE_MOOSE);
        mooseSpecies = model().newGameSpeciesMoose();
        otherSpecies = model().newDeerSubjectToClubHunting();
        rka = model().newRiistakeskuksenAlue();

        createPermitsNotExported();
    }

    private void createPermitsNotExported() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(this.rka);
        model().newHarvestPermit(rhy, permitNumber(YEAR));

        final HarvestPermit otherPermit = model().newHarvestPermit(rhy, permitNumber(YEAR));
        createSpeciesAmount(otherPermit, otherSpecies);
        final HuntingClub club = model().newHuntingClub(rhy);
        otherPermit.setHuntingClub(club);
        otherPermit.setPermitHolder(PermitHolder.createHolderForClub(club));
        otherPermit.getPermitPartners().add(club);

        final HuntingClubGroup group = model().newHuntingClubGroup(club, otherSpecies);
        group.updateHarvestPermit(otherPermit);
    }

    private void createSpeciesAmount(final HarvestPermit permit, final GameSpecies species) {
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, species);
        spa.setBeginDate(new LocalDate(YEAR, 9, 10));
        spa.setBeginDate(new LocalDate(YEAR, 12, 31));
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDenied() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportMoose(YEAR));
    }

    @Test
    public void testAccessGranted() {
        onSavedAndAuthenticated(apiUser, () -> Asserts.assertEmpty(feature.exportMoose(YEAR).getPermits()));
    }

    @Test
    public void testOnlyPermitExists() {
        // Only permit exists, no partners etc. This should not happen in real life
        HarvestPermit permit = createMoosePermit();

        onSavedAndAuthenticated(apiUser, () -> {
            List<LEM_Permit> exportDtos = feature.exportMoose(YEAR).getPermits();
            assertEquals(1, exportDtos.size());
            runInTransaction(() -> assertPermit(reload(permit), exportDtos));
        });
    }

    @Test
    public void test_huntingDaysCreatedBySystemAreNotExported() {
        withHuntingGroupFixture(mooseSpecies, fixture -> {
            final GroupHuntingDay automaticallyCreatedDay =
                    model().newGroupHuntingDay(fixture.group, fixture.speciesAmount.getBeginDate());
            automaticallyCreatedDay.setCreatedBySystem(true);

            final GroupHuntingDay groupHuntingDay = model().newGroupHuntingDay(fixture.group,
                    fixture.speciesAmount.getBeginDate().plusDays(1));


            onSavedAndAuthenticated(apiUser, () -> {
                List<LEM_Permit> exportDtos = feature.exportMoose(fixture.permit.getPermitYear()).getPermits();
                assertThat(exportDtos, hasSize(1));

                final LEM_Permit permit = exportDtos.get(0);
                assertThat(permit.getHuntingClubs(), hasSize(1));

                final LEM_Club club = permit.getHuntingClubs().get(0);
                assertEquals(fixture.club.getOfficialCode(), club.getClubOfficialCode());
                assertThat(club.getGroups(), hasSize(1));

                final LEM_Group group = club.getGroups().get(0);
                assertThat(group.getHuntingDays(), hasSize(1));

                final LEM_HuntingDay huntingDay = group.getHuntingDays().get(0);
                assertEquals(groupHuntingDay.getStartDate(), huntingDay.getStartDate());
            });
        });


    }

    private HarvestPermit reload(HarvestPermit permit) {
        return harvestPermitRepository.getOne(permit.getId());
    }

    private HarvestPermit createMoosePermit() {
        final HarvestPermit permit = model().newMooselikePermit(model().newRiistanhoitoyhdistys(this.rka), YEAR);
        createSpeciesAmount(permit, mooseSpecies);
        return permit;
    }

    private static void assertPermit(HarvestPermit permit, List<LEM_Permit> exportDtos) {
        List<LEM_Permit> matchingDtos = exportDtos.stream()
                .filter(d -> Objects.equals(permit.getPermitNumber(), d.getPermitNumber()))
                .collect(toList());
        assertEquals(1, matchingDtos.size());
        assertPermit(permit, matchingDtos.get(0));
    }

    private static void assertPermit(HarvestPermit entity, LEM_Permit dto) {
        assertEquals(entity.getPermitNumber(), dto.getPermitNumber());
        assertEquals(entity.getRhy().getOfficialCode(), dto.getRhyOfficialCode());
        assertPerson(entity.getOriginalContactPerson(), dto.getContactPerson());
        assertMooseAmount(entity.getSpeciesAmounts(), dto.getMooseAmount());
        // amendmentPermits?
        assertClubs(entity.getPermitPartners(), dto.getHuntingClubs());
    }

    private static void assertPerson(final Person entity, final LEM_Person dto) {
        assertEquals(entity.getFirstName(), dto.getFirstName());
        assertEquals(entity.getLastName(), dto.getLastName());
        assertEquals(entity.getEmail(), dto.getEmail());
        assertEquals(entity.getPhoneNumber(), dto.getPhoneNumber());

        assertAddress(entity.getAddress(), dto.getAddress());
    }

    private static void assertAddress(final Address entity, final LEM_Address dto) {
        if (validateNulls(entity, dto)) {
            assertEquals(entity.getStreetAddress(), dto.getStreetAddress());
            assertEquals(entity.getCity(), dto.getCity());
            assertEquals(entity.getPostalCode(), dto.getPostalCode());
            assertEquals(entity.getCountry(), dto.getCountry());
        }
    }

    private static void assertMooseAmount(final List<HarvestPermitSpeciesAmount> speciesAmounts, final LEM_Amount dto) {
        final HarvestPermitSpeciesAmount entity = speciesAmounts.stream()
                .filter(s -> s.getGameSpecies().isMoose())
                .findAny()
                .get();

        assertEquals(entity.getSpecimenAmount(), dto.getAmount(), 0.01f);
        assertEquals(entity.getRestrictionAmount(), dto.getRestrictedAmount());
        assertEqualNames(entity.getRestrictionType(), dto.getRestriction());
    }

    private static void assertClubs(final Set<HuntingClub> entityClubs, final List<LEM_Club> dtoClubs) {
        assertEquals(entityClubs.size(), dtoClubs.size());

        final Map<String, LEM_Club> dtoClubsMap = F.index(dtoClubs, LEM_Club::getClubOfficialCode);
        entityClubs.forEach(entity -> assertClub(entity, dtoClubsMap.get(entity.getOfficialCode())));
    }

    private static void assertClub(final HuntingClub entity, final LEM_Club dto) {
        if (validateNulls(entity, dto)) {
            assertEquals(entity.getOfficialCode(), dto.getRhyOfficialCode());
            assertEquals(entity.getNameFinnish(), dto.getNameFinnish());
            // contactPerson?
            assertEquals(entity.getGeoLocation().getLatitude(), dto.getGeoLocation().getLatitude());
            assertEquals(entity.getGeoLocation().getLongitude(), dto.getGeoLocation().getLongitude());
            assertEqualNames(entity.getGeoLocation().getSource(), dto.getGeoLocation().getSource());
            assertEquals(entity.getParentOrganisation().getOfficialCode(), dto.getRhyOfficialCode());
            // huntingDays?
            // huntingSummary?
        }
    }

    private static boolean validateNulls(final Object a, final Object b) {
        if (a != null && b == null || a == null && b != null) {
            fail("both must be null or not null");
        }
        return a != null;
    }

    private static void assertEqualNames(final Enum<?> first, final Enum<?> second) {
        final Function<Enum<?>, String> nameFn = enumValue -> enumValue == null ? null : enumValue.name();
        assertEquals(nameFn.apply(first), nameFn.apply(second));
    }
}
