package fi.riista.feature.harvestpermit.mobile;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class MobileHarvestPermitFeatureTest extends EmbeddedDatabaseTest {

    @DataPoints("specVersions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS =
            EnumSet.allOf(HarvestSpecVersion.class).stream().toArray(HarvestSpecVersion[]::new);

    @Resource
    private MobileHarvestPermitFeature mobileHarvestPermitFeature;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Theory
    public void testPreloadPermits(final HarvestSpecVersion specVersion) {
        doTestPreloadPermits(null, specVersion);
    }

    @Theory
    public void testPreloadPermitsWhenHarvestReportsDone(final HarvestSpecVersion specVersion) {
        doTestPreloadPermits(HarvestReportState.APPROVED, specVersion);
    }

    private void doTestPreloadPermits(final HarvestReportState state, final HarvestSpecVersion specVersion) {
        withPerson(person -> withRhy(rhy -> {

            // these 3 permits should not be preloaded
            model().newHarvestPermit(rhy, true);

            final HarvestPermit originalPermit = model().newMooselikePermit(rhy);

            final HarvestPermit amendmentPermit = model().newHarvestPermit(originalPermit);
            amendmentPermit.setPermitTypeCode(PermitTypeCode.MOOSELIKE_AMENDMENT);

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
                final List<MobileHarvestPermitExistsDTO> permitDtos =
                        mobileHarvestPermitFeature.preloadPermits(specVersion);

                final Set<Long> permitIds =
                        F.getUniqueIds(permit, listPermit, permitWhereContactPerson, listPermitWhereContactPerson);
                assertEquals(permitIds, F.getUniqueIds(permitDtos));
            });
        }));
    }

    private void createHarvestAndReport(final Person author,
                                        final Person shooter,
                                        final HarvestPermit permit,
                                        final HarvestReportState state) {

        final Harvest harvest = model().newHarvest(author, shooter);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setRhy(permit.getRhy());

        if (state != null) {
            harvest.setHarvestReportState(state);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());
        }
    }
}
