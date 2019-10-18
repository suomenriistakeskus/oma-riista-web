package fi.riista.feature.harvestregistry.quartz;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.HarvestRegistryItemRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.common.repository.IntegrationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.harvestregistry.quartz.HarvestRegistrySynchronizerService.REGISTRY_START_TIME_STAMP;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.toDateNullSafe;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HarvestRegistrySynchronizerServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestRegistrySynchronizerService service;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestRegistryItemRepository itemRepository;

    @Resource
    private IntegrationRepository integrationRepository;

    private Riistanhoitoyhdistys rhy;
    private Person shooter;
    private Person approver;
    private HarvestPermit derogationPermit;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        derogationPermit = model().newHarvestPermit(rhy);
        derogationPermit.setPermitTypeCode(PermitTypeCode.DEROGATION_PERMIT_CODES.stream().findFirst().get());
        approver = model().newPerson();
        shooter = model().newPerson();

        // Use mocked time to have sync occurring up to current time during sync by default
        final LocalDate previousRun = new LocalDate(2019, 6, 15);
        MockTimeProvider.mockTime(toDateNullSafe(previousRun).getTime());
        final Integration integration = model().newIntegration(Integration.HARVEST_REGISTRY_SYNC_ID);
        integration.setLastRun(previousRun.minusDays(1).toDateTimeAtStartOfDay());

        persistInNewTransaction();
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testHarvestIsSynchronized() {
        createHarvest();
        persistInNewTransaction();

        runInTransaction(service::synchronize);

        runInTransaction(() -> {
            final List<HarvestRegistryItem> items = itemRepository.findAll();
            assertThat(items, hasSize(1));
        });

    }

    @Test
    public void testHarvestDeleteCascadesToRegistryItem() {
        createHarvest();
        persistInNewTransaction();

        runInTransaction(service::synchronize);

        runInTransaction(() -> {
            final List<HarvestRegistryItem> items = itemRepository.findAll();
            assertThat(items, hasSize(1));
            harvestRepository.deleteAll();
        });

        runInTransaction(() -> {
            final List<HarvestRegistryItem> items = itemRepository.findAll();
            assertThat(items, hasSize(0));
        });

    }

    @Test
    public void testInitialSyncPeriod() {
        // remove object created in setup
        runInTransaction(integrationRepository::deleteAll);

        runInTransaction(service::synchronize);

        runInTransaction(() -> {
            final Integration integration = integrationRepository.findOne(Integration.HARVEST_REGISTRY_SYNC_ID);
            assertEquals(REGISTRY_START_TIME_STAMP.plusWeeks(1), integration.getLastRun().toLocalDate());
        });
    }

    @Test
    public void testNumberOfDatabaseQueriesStaysFlat() {
        for (int i = 0; i < 500; ++i) {
            createHarvest();
        }
        persistInNewTransaction();

        // Arbitrary count of 510 should still reveal N+1 issues with 500 harvests, (500 inserts plus 10)
        assertMaxQueryCount(510, service::synchronize);

        runInTransaction(() -> {
            final List<HarvestRegistryItem> items = itemRepository.findAll();
            assertThat(items, hasSize(500));
        });
    }

    private Harvest createHarvest() {
        final Harvest harvest = model().newHarvest(shooter, shooter);
        harvest.setRhy(rhy);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportDate(now());
        harvest.setHarvestReportAuthor(approver);
        harvest.setHarvestPermit(derogationPermit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        return harvest;
    }
}
