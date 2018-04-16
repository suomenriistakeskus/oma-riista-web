package fi.riista.feature.huntingclub.hunting.overview;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SharedPermitMapHarvestTest extends EmbeddedDatabaseTest {

    @Resource
    private SharedPermitMapFeature sharedPermitMapFeature;

    private class SimpleFixture {
        final int huntingYear;
        final GameSpecies gameSpecies;
        final HuntingClub huntingClub;
        final HuntingClubGroup huntingClubGroup;
        final GroupHuntingDay groupHuntingDay;
        final HarvestPermit harvestPermit;
        final HarvestPermitSpeciesAmount speciesAmount;
        final Person person;
        final Harvest harvest;

        public SimpleFixture(final EntitySupplier model) {
            this(model, true);
        }

        public SimpleFixture(final EntitySupplier model, final boolean createOccupation) {
            final Riistanhoitoyhdistys rhy = model.newRiistanhoitoyhdistys();

            this.huntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
            this.gameSpecies = model.newGameSpecies();
            this.huntingClub = model.newHuntingClub(rhy);

            // Person with club membership
            this.person = model.newPerson();

            if (createOccupation) {
                model.newOccupation(huntingClub, person, OccupationType.SEURAN_JASEN);
            }

            // Permit with club as partner
            this.harvestPermit = model.newMooselikePermit(rhy);
            this.harvestPermit.getPermitPartners().add(huntingClub);
            this.speciesAmount = model.newHarvestPermitSpeciesAmount(harvestPermit, gameSpecies, huntingYear);

            this.huntingClubGroup = model.newHuntingClubGroup(huntingClub, speciesAmount);
            this.groupHuntingDay = model.newGroupHuntingDay(huntingClubGroup, DateUtil.today());

            // Harvest linked to hunting club day
            this.harvest = model.newHarvest(person, person);
            this.harvest.updateHuntingDayOfGroup(groupHuntingDay, null);
        }

        public List<HarvestDTO> listHarvest() {
            return sharedPermitMapFeature.listHarvest(this.harvestPermit.getId(),
                    this.huntingClubGroup.getHuntingYear(),
                    this.huntingClubGroup.getSpecies().getOfficialCode());
        }


        public void assertNoHarvest() {
            final List<HarvestDTO> harvests = listHarvest();
            assertNotNull(harvests);
            assertThat(harvests, hasSize(0));
        }

        public void assertExpectedHarvests(final Harvest... expectedHarvests) {
            final List<HarvestDTO> harvests = listHarvest();
            assertNotNull(harvests);
            assertThat(harvests, hasSize(expectedHarvests.length));
            assertEquals(F.getUniqueIds(expectedHarvests), F.getUniqueIds(harvests));
        }
    }

    @Test
    public void testSmoke() {
        final SimpleFixture f = new SimpleFixture(model());

        onSavedAndAuthenticated(createUser(f.person), () -> f.assertExpectedHarvests(f.harvest));
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDeniedWithoutClubMembership() {
        final SimpleFixture f = new SimpleFixture(model(), false);

        onSavedAndAuthenticated(createUser(f.person), f::listHarvest);
    }

    @Test
    public void testMultiplePermitPartnerClubs() {
        final SimpleFixture f1 = new SimpleFixture(model());
        final SimpleFixture f2 = new SimpleFixture(model());

        f1.harvestPermit.getPermitPartners().add(f2.huntingClub);
        f2.harvestPermit.getPermitPartners().add(f1.huntingClub);

        onSavedAndAuthenticated(createUser(f1.person), () -> {
            // Harvest should be found only in one group
            f1.assertExpectedHarvests(f1.harvest);
            f2.assertExpectedHarvests(f2.harvest);
        });
    }

    @Test
    public void testExcludeNonMoosePermitTypeCode() {
        final SimpleFixture f = new SimpleFixture(model());

        // Normally typeCode should be 100
        f.harvestPermit.setPermitTypeCode("200");

        onSavedAndAuthenticated(createUser(f.person), f::assertNoHarvest);
    }

    @Test
    public void testIncludeOnlyWhenGroupIsLinkedToPermit() {
        final SimpleFixture f = new SimpleFixture(model());

        // Remove group link to permit
        f.huntingClubGroup.updateHarvestPermit(null);

        // Use admin-role to skip authorization
        onSavedAndAuthenticated(createNewAdmin(), f::assertNoHarvest);
    }

    @Test
    public void testIncludeOnlyWhenHarvestLinkedToGroupHuntingDay() {
        final SimpleFixture f = new SimpleFixture(model());
        model().newHarvest(f.person, f.person);

        onSavedAndAuthenticated(createUser(f.person), () -> f.assertExpectedHarvests(f.harvest));
    }

    @Test
    public void testIncludeOnlyWhenGroupHuntingYearIsCorrect() {
        final SimpleFixture f = new SimpleFixture(model());

        onSavedAndAuthenticated(createUser(f.person), () -> {
            final List<HarvestDTO> result = sharedPermitMapFeature.listHarvest(
                    f.harvestPermit.getId(), f.huntingYear + 1, f.gameSpecies.getOfficialCode());

            assertThat(result, hasSize(0));
        });
    }

    @Test
    public void testIncludeOnlyWhenGroupSpeciesIsCorrect() {
        final SimpleFixture f = new SimpleFixture(model());
        final GameSpecies otherGameSpecies = model().newGameSpecies();

        onSavedAndAuthenticated(createUser(f.person), () -> {
            final List<HarvestDTO> result = sharedPermitMapFeature.listHarvest(
                    f.harvestPermit.getId(), f.huntingYear, otherGameSpecies.getOfficialCode());

            assertThat(result, hasSize(0));
        });
    }
}
