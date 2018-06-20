package fi.riista.integration.lupahallinta;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.account.user.SystemUserPrivilege.EXPORT_LUPAHALLINTA_HARVESTREPORTS;
import static fi.riista.feature.harvestpermit.report.HarvestReportState.APPROVED;
import static org.junit.Assert.assertEquals;

public class HarvestReportExportToLupahallintaFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportExportToLupahallintaFeature exportFeature;

    @Test
    public void testPermitBearApproved_permitNotCompleted() {
        final DateTime since = DateUtil.now();
        final HarvestPermit permit = model().newHarvestPermit();
        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        createApprovedHarvestForPermit(permit, species);

        persistInNewTransaction();

        doAssert(since);
        doAssert(DateUtil.now());
    }

    @Test
    public void testPermitBearApproved_permitCompleted() {
        final DateTime since = DateUtil.now();

        final Person harvestReportAuthor = model().newPerson();
        final HarvestPermit permit = model().newHarvestPermit();
        permit.setHarvestReportDate(DateUtil.now());
        permit.setHarvestReportAuthor(harvestReportAuthor);
        permit.setHarvestReportState(APPROVED);
        permit.setHarvestReportModeratorOverride(false);

        // bump up harvest ids
        for (int i = 0; i < 10; i++) {
            model().newHarvest();
        }

        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);

        createApprovedHarvestForPermit(permit, species);
        createRejectedHarvestForPermit(permit, species);

        persistInNewTransaction();

        doAssert(since, permit.getPermitNumber());
        doAssert(DateUtil.now());
    }

    private void doAssert(final DateTime dateTime) {
        doAssert(dateTime, null);
    }

    private void doAssert(final DateTime dateTime, final String expectedPermitNumber) {
        final SystemUser user = createNewUser();
        user.addPrivilege(EXPORT_LUPAHALLINTA_HARVESTREPORTS);
        onSavedAndAuthenticated(user, () -> {
            final List<HarvestReportExportCSVDTO> dtos = exportFeature.exportToCSCV(dateTime.getMillis());
            if (expectedPermitNumber != null) {
                assertEquals(1, dtos.size());
                final HarvestReportExportCSVDTO dto = dtos.get(0);
                assertEquals(expectedPermitNumber, dto.getHuntingLicenseNumber());
            } else {
                assertEquals(0, dtos.size());
            }
        });
    }

    private Harvest createApprovedHarvestForPermit(final HarvestPermit permit, final GameSpecies species) {
        model().newHarvestPermitSpeciesAmount(permit, species);

        final Person harvestReportAuthor = model().newPerson();
        final Harvest harvest = model().newHarvest(species, permit.getRhy());
        harvest.setHarvestReportAuthor(harvestReportAuthor);
        harvest.setHarvestReportDate(DateTime.now());
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(APPROVED);
        return harvest;
    }

    private Harvest createRejectedHarvestForPermit(final HarvestPermit permit, final GameSpecies species) {
        model().newHarvestPermitSpeciesAmount(permit, species);

        final Harvest harvest = model().newHarvest(species, permit.getRhy());
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);
        return harvest;
    }
}