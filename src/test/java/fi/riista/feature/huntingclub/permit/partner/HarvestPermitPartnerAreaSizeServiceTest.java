package fi.riista.feature.huntingclub.permit.partner;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class HarvestPermitPartnerAreaSizeServiceTest extends EmbeddedDatabaseTest {
    private static final int TOTAL_AREA_SIZE = 123;
    private static final int WATER_AREA_SIZE = 23;
    private static final int LAND_AREA_SIZE = TOTAL_AREA_SIZE - WATER_AREA_SIZE;

    @Resource
    private HarvestPermitPartnerAreaService harvestPermitPartnerAreaService;

    @Resource
    private JPQLQueryFactory queryFactory;

    private void createClubArea(final HarvestPermitArea permitArea, final HuntingClub club) {
        final GISZone zone = model().newGISZone();
        zone.setComputedAreaSize(10_000 * TOTAL_AREA_SIZE);
        zone.setWaterAreaSize(10_000 * WATER_AREA_SIZE);

        final HuntingClubArea clubArea = model().newHuntingClubArea(club, zone);

        model().newHarvestPermitAreaPartner(permitArea, clubArea);
    }

    private void createTestData(final int permitAreaSize, final int partnerCount) {
        final HarvestPermitArea permitArea = model().newHarvestPermitArea();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);

        for (int i = 0; i < partnerCount; i++) {
            createClubArea(permitArea, club);
        }

        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, permitArea, HarvestPermitCategory.MOOSELIKE);
        final PermitDecision decision = model().newPermitDecision(application);

        final HarvestPermit harvestPermit = model().newMooselikePermit(decision);
        harvestPermit.setPermitAreaSize(permitAreaSize);
    }

    @Test
    public void testWithoutPermitDecision() {
        final HarvestPermit harvestPermit = model().newHarvestPermit();
        harvestPermit.setPermitAreaSize(LAND_AREA_SIZE);
        final HuntingClub club = model().newHuntingClub();

        runInTransaction(() -> {
            final int permitAreaSizeLookupWithFallback = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(harvestPermit, club);

            assertEquals(LAND_AREA_SIZE, permitAreaSizeLookupWithFallback);
        });
    }

    @Test
    public void testSingleClubArea() {
        createTestData(999, 1);

        persistInNewTransaction();

        runInTransaction(() -> {
            final HarvestPermit harvestPermit = queryFactory.selectFrom(QHarvestPermit.harvestPermit).fetchOne();
            final HuntingClub club = queryFactory.selectFrom(QHuntingClub.huntingClub).fetchOne();
            final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(harvestPermit, club);

            assertEquals(LAND_AREA_SIZE, permitAreaSize);
        });
    }

    @Test
    public void testSingleClubArea_LargerThanPermitAreaSize() {
        createTestData(44, 1);

        persistInNewTransaction();

        runInTransaction(() -> {
            final HarvestPermit harvestPermit = queryFactory.selectFrom(QHarvestPermit.harvestPermit).fetchOne();
            final HuntingClub club = queryFactory.selectFrom(QHuntingClub.huntingClub).fetchOne();
            final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(harvestPermit, club);

            assertEquals(44, permitAreaSize);
        });
    }

    @Test
    public void testMultipleAreasForSameClub() {
        createTestData(999, 2);

        persistInNewTransaction();

        runInTransaction(() -> {
            final HarvestPermit harvestPermit = queryFactory.selectFrom(QHarvestPermit.harvestPermit).fetchOne();
            final HuntingClub club = queryFactory.selectFrom(QHuntingClub.huntingClub).fetchOne();
            final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(harvestPermit, club);

            assertEquals(2 * LAND_AREA_SIZE, permitAreaSize);
        });
    }
}
