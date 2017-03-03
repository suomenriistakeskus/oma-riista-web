package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public abstract class MobileGameDiaryFeatureTest extends EmbeddedDatabaseTest {

    protected abstract MobileGameDiaryFeature feature();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPreloadPermits() {
        doTestPreloadPermits(null);
    }

    @Test
    public void testPreloadPermitsWhenHarvestReportsDone() {
        doTestPreloadPermits(HarvestReport.State.APPROVED);
    }

    private void doTestPreloadPermits(final HarvestReport.State state) {
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

                final List<MobileHarvestPermitExistsDTO> permitDtos = feature().preloadPermits();

                final Set<Long> permitIds =
                        F.getUniqueIds(permit, listPermit, permitWhereContactPerson, listPermitWhereContactPerson);
                assertEquals(permitIds, F.getUniqueIds(permitDtos));
            });
        }));
    }

    private void createHarvestAndReport(
            final Person author, final Person shooter, final HarvestPermit permit, final HarvestReport.State state) {

        final Harvest harvest = model().newHarvest(author, shooter);
        harvest.setHarvestPermit(permit);
        harvest.setRhy(permit.getRhy());

        if (state != null) {
            model().newHarvestReport(harvest, state);
        }
    }

    @Test
    public void testDeleteImage_notFound() {
        persistAndAuthenticateWithNewUser(true);
        feature().deleteGameDiaryImage(UUID.randomUUID());
    }

}
