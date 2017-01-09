package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HarvestReportCrudFeatureListMineTest extends EmbeddedDatabaseTest {

    @Resource
    HarvestReportCrudFeature crudFeature;

    @Test
    public void testListMine_singleHarvest() {
        doTestListSingleHarvest(createUserWithPerson(), null);
    }

    @Test
    public void testListMine_singleHarvest_asModerator() {
        doTestListSingleHarvest(createNewModerator(), model().newPerson());
    }

    private void doTestListSingleHarvest(SystemUser callingUser, Person personViewed) {
        withRhy(rhy -> {
            final Person me = personViewed != null ? personViewed : callingUser.getPerson();
            final Person other = model().newPerson();

            final Person foo = model().newPerson();
            createHarvestReport(foo, foo, HarvestReport.State.SENT_FOR_APPROVAL);

            createHarvestReportWithPermit(foo, foo, HarvestReport.State.SENT_FOR_APPROVAL, rhy);

            final HarvestReport meAsAuthor = createHarvestReport(me, other, HarvestReport.State.SENT_FOR_APPROVAL);
            final HarvestReport meAsHunter = createHarvestReport(other, me, HarvestReport.State.APPROVED);
            createHarvestReport(me, other, HarvestReport.State.DELETED);

            onSavedAndAuthenticated(callingUser, () -> {
                final List<HarvestReportDTOBase> mine = crudFeature.listMine(F.getId(personViewed));
                assertEquals(2, mine.size());
                assertHarvestReports(mine, meAsAuthor, meAsHunter);
            });
        });
    }

    @Test
    public void testListMine_listHarvests() {
        withPerson(me -> withRhy(rhy -> {
            final Person other = model().newPerson();

            final Person foo = model().newPerson();
            createHarvestReport(foo, foo, HarvestReport.State.SENT_FOR_APPROVAL);

            createHarvestReportWithPermit(foo, foo, HarvestReport.State.SENT_FOR_APPROVAL, rhy);

            final HarvestReport meAsAuthor =
                    createHarvestReportWithPermit(me, other, HarvestReport.State.SENT_FOR_APPROVAL, rhy);
            createHarvestReportWithPermit(other, me, HarvestReport.State.APPROVED, rhy);
            createHarvestReportWithPermit(me, other, HarvestReport.State.DELETED, rhy);

            onSavedAndAuthenticated(createUser(me), () -> {
                final List<HarvestReportDTOBase> mine = crudFeature.listMine(null);
                assertEquals(1, mine.size());
                assertHarvestReports(mine, meAsAuthor);
            });
        }));
    }

    private static void assertHarvestReports(
            final List<HarvestReportDTOBase> reports, final HarvestReport... expected) {

        assertTrue(F.containsAll(F.getUniqueIds(reports), F.getUniqueIds(expected)));
    }

    private HarvestReport createHarvestReport(
            final Person author, final Person hunter, final HarvestReport.State state) {

        final Harvest harvest = model().newHarvest(author, hunter);
        model().newHarvestSpecimen(harvest);
        return model().newHarvestReport(harvest, state);
    }

    private HarvestReport createHarvestReportWithPermit(
            final Person author, final Person hunter, final HarvestReport.State state, final Riistanhoitoyhdistys rhy) {

        final HarvestPermit permit = model().newHarvestPermit(rhy, true);

        final Harvest harvest = model().newHarvest(author, hunter);
        harvest.setHarvestPermit(permit);
        harvest.setRhy(permit.getRhy());
        model().newHarvestSpecimen(harvest);

        return model().newHarvestReport(harvest, state);
    }

}
