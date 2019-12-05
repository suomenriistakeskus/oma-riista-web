package fi.riista.feature.huntingclub.hunting.overview;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;


@RunWith(MockitoJUnitRunner.class)
public class SharedPermitMapFeatureTest extends EmbeddedDatabaseTest {

    @Mock
    GISZoneRepository mockedZoneRepository;

    @Resource
    private SharedPermitMapFeature sharedPermitMapFeature;

    private final ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(5);
    private final AtomicInteger resultCounter = new AtomicInteger(0);
    private GameSpecies mooseSpecies;
    private SystemUser moderator;
    private HarvestPermit harvestPermit;

    @Before
    public void setup() {
        sharedPermitMapFeature.setZoneRepository(mockedZoneRepository);
        moderator = createNewModerator();
        mooseSpecies = model().newGameSpeciesMoose();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);
        final HarvestPermitArea harvestPermitArea = model().newHarvestPermitArea();
        final HuntingClubArea huntingClubArea = model().newHuntingClubArea(club);
        huntingClubArea.setZone(model().newGISZone());
        model().newHarvestPermitAreaPartner(harvestPermitArea, huntingClubArea);
        final HarvestPermitApplication application = model().newHarvestPermitApplication(rhy, harvestPermitArea,
                HarvestPermitCategory.MOOSELIKE);
        model().newHarvestPermitApplicationSpeciesAmount(application, mooseSpecies, 5.0f);
        final PermitDecision permitDecision = model().newPermitDecision(application);
        harvestPermit = model().newMooselikePermit(permitDecision);
    }

    @Test
    public void testMultipleSimultaneousRequests() throws Exception {
        final CompletableFuture<FeatureCollection> completableFuture = new CompletableFuture<>();

        // Await on the future to enable parallel requests
        Mockito.when(mockedZoneRepository.getCombinedFeatures(anySet(), any(GISUtils.SRID.class)))
                .thenAnswer(invocation -> completableFuture.get())
                .thenThrow(new IllegalStateException());

        // Persist before forking into separate threads
        persistInNewTransaction();

        for (int i = 0; i < 5; ++i) {
            executorService.execute(() -> {
                onAuthenticated(moderator, () -> {
                    final FeatureCollection permitArea =
                            sharedPermitMapFeature.findPermitArea(harvestPermit.getId(), 2019,
                                                                  mooseSpecies.getOfficialCode());

                    assertTrue(permitArea.getFeatures().isEmpty());
                    resultCounter.incrementAndGet();
                });

            });
        }

        // Complete the future to allow the requests to complete
        completableFuture.complete(new FeatureCollection());

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        Mockito.verify(mockedZoneRepository, times(1)).getCombinedFeatures(anySet(), any(GISUtils.SRID.class));
        assertEquals(5, resultCounter.get());
    }

}
