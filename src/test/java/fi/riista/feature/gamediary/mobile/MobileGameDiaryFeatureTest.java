package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MobileGameDiaryFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileGameDiaryFeature mobileGameDiaryFeature;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPreloadPermits() {
        doTestPreloadPermits(null);
    }

    @Test
    public void testPreloadPermitsWhenHarvestReportsDone() {
        doTestPreloadPermits(HarvestReportState.APPROVED);
    }

    private void doTestPreloadPermits(final HarvestReportState state) {
        withPerson(person -> withRhy(rhy -> {

            // these 3 permits should not be preloaded
            model().newHarvestPermit(rhy, true);

            final HarvestPermit originalPermit = model().newMooselikePermit(rhy);

            final HarvestPermit amendmentPermit = model().newHarvestPermit(originalPermit);
            amendmentPermit.setPermitTypeCode(HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);

            // these permits should be preloaded, because we create harvests to those
            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermit listPermit = model().newHarvestPermit(rhy, true);
            final HarvestPermit permitWhereContactPerson = model().newHarvestPermit(rhy, person);
            final HarvestPermit listPermitWhereContactPerson = model().newHarvestPermit(rhy, person, true);

            final Person otherPerson = model().newPerson();

            createHarvestAndReport(person, person, permit, state);
            createHarvestAndReport(otherPerson, person, listPermit, state);
            createHarvestAndReport(person, person, permitWhereContactPerson, state);
            createHarvestAndReport(otherPerson, person, listPermitWhereContactPerson, state);
            createHarvestAndReport(person, person, amendmentPermit, state);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileHarvestPermitExistsDTO> permitDtos = mobileGameDiaryFeature.preloadPermits();

                final Set<Long> permitIds =
                        F.getUniqueIds(permit, listPermit, permitWhereContactPerson, listPermitWhereContactPerson);
                assertEquals(permitIds, F.getUniqueIds(permitDtos));
            });
        }));
    }

    private void createHarvestAndReport(
            final Person author, final Person shooter, final HarvestPermit permit, final HarvestReportState state) {

        final Harvest harvest = model().newHarvest(author, shooter);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(permit != null ? Harvest.StateAcceptedToHarvestPermit.ACCEPTED : null);
        harvest.setRhy(permit.getRhy());

        if (state != null) {
            harvest.setHarvestReportState(state);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());
        }
    }

    @Test
    public void testDeleteImage_notFound() {
        persistAndAuthenticateWithNewUser(true);
        mobileGameDiaryFeature.deleteGameDiaryImage(UUID.randomUUID());
    }

}
