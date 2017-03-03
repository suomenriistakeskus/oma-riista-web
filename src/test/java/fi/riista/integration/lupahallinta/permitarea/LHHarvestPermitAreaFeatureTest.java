package fi.riista.integration.lupahallinta.permitarea;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.area.HarvestPermitArea;
import fi.riista.feature.harvestpermit.area.HarvestPermitArea.StatusCode;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaEvent;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaEventRepository;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaHta;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaRepository;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaRhy;
import fi.riista.feature.harvestpermit.area.IllegalHarvestPermitAreaStateTransitionException;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import java.util.List;

import static fi.riista.util.NumberUtils.squareMetersToHectares;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LHHarvestPermitAreaFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private LHHarvestPermitAreaFeature feature;

    @Resource
    private HarvestPermitAreaRepository areaRepo;

    @Resource
    private HarvestPermitAreaEventRepository eventRepo;

    @Test(expected = AccessDeniedException.class)
    public void testUpdateLockedStatus_withoutRequiredPrivilege() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        area.setStatusPending();
        area.setStatusProcessing();
        area.setStatusReady();
        onSavedAndAuthenticated(createNewUser(), () -> feature.updateLockedStatus(area.getExternalId(), true));
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateLockedStatus_setLocked_withNonExistentExternalAreaId() {
        updateLockedStatusWithPrivilegedUser("NON_EXISTENT", true);
    }

    @Test(expected = IllegalHarvestPermitAreaStateTransitionException.class)
    public void testUpdateLockedStatus_setLocked_whenAreaIncomplete() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        updateLockedStatusWithPrivilegedUser(area.getExternalId(), true);
    }

    @Test(expected = IllegalHarvestPermitAreaStateTransitionException.class)
    public void testUpdateLockedStatus_setLocked_whenAreaAlreadyLocked() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        area.setStatusLocked();
        updateLockedStatusWithPrivilegedUser(area.getExternalId(), true);
    }

    @Test
    public void testUpdateLockedStatus_setLocked_whenAreaReady() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        area.setStatusPending();
        area.setStatusProcessing();
        area.setStatusReady();

        updateLockedStatusWithPrivilegedUser(area.getExternalId(), true);

        final HarvestPermitArea reloadedArea = areaRepo.findOne(area.getId());
        assertNotNull(reloadedArea);
        assertEquals(StatusCode.LOCKED, reloadedArea.getStatus());

        final List<HarvestPermitAreaEvent> events = eventRepo.findByHarvestPermitArea(area);
        assertEquals(1, events.size());
        assertEquals(StatusCode.LOCKED, events.get(0).getStatus());
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateLockedStatus_setNotLocked_withNonExistentExternalAreaId() {
        updateLockedStatusWithPrivilegedUser("NON_EXISTENT", false);
    }

    @Test(expected = IllegalHarvestPermitAreaStateTransitionException.class)
    public void testUpdateLockedStatus_setNotLocked_whenAreaIncomplete() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        updateLockedStatusWithPrivilegedUser(area.getExternalId(), false);
    }

    @Test(expected = IllegalHarvestPermitAreaStateTransitionException.class)
    public void testUpdateLockedStatus_setNotLocked_whenAreaReady() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        area.setStatusReady();
        updateLockedStatusWithPrivilegedUser(area.getExternalId(), false);
    }

    @Test
    public void testUpdateLockedStatus_setNotLocked_whenAreaLocked() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        area.setStatusPending();
        area.setStatusProcessing();
        area.setStatusReady();
        area.setStatusLocked();

        updateLockedStatusWithPrivilegedUser(area.getExternalId(), false);

        final HarvestPermitArea reloadedArea = areaRepo.findOne(area.getId());
        assertNotNull(reloadedArea);
        assertEquals(StatusCode.READY, reloadedArea.getStatus());

        final List<HarvestPermitAreaEvent> events = eventRepo.findByHarvestPermitArea(area);
        assertEquals(1, events.size());
        assertEquals(StatusCode.READY, events.get(0).getStatus());
    }

    private void updateLockedStatusWithPrivilegedUser(final String externalId, final boolean locked) {
        onSavedAndAuthenticated(
                createNewApiUser(SystemUserPrivilege.EXPORT_LUPAHALLINTA_PERMIT_AREA),
                () -> feature.updateLockedStatus(externalId, locked));
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetByExternalId_withoutRequiredPrivilege() {
        final HarvestPermitArea area = model().newHarvestPermitArea();
        area.setStatusPending();
        area.setStatusProcessing();
        area.setStatusReady();
        onSavedAndAuthenticated(createNewUser(), () -> feature.getByExternalId(area.getExternalId()));
    }

    @Test
    public void testGetByExternalId() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();

        final GISHirvitalousalue hta1 = model().newGISHirvitalousalue();
        final GISHirvitalousalue hta2 = model().newGISHirvitalousalue();

        final HuntingClub club1 = model().newHuntingClub(rhy1);
        club1.setGeoLocation(geoLocation());
        club1.setMooseArea(hta1);

        final HuntingClub club2 = model().newHuntingClub(rhy2);
        club2.setGeoLocation(geoLocation());
        club2.setMooseArea(hta2);

        final GISZone zone1 = model().newGISZone(123000.0);
        zone1.setWaterAreaSize(12300.0);

        final GISZone zone2 = model().newGISZone(456000.0);
        zone2.setWaterAreaSize(45600.0);

        final GISZone zone3 = model().newGISZone(789000.0);
        zone3.setWaterAreaSize(78900.0);

        final HuntingClubArea club1Area1 = model().newHuntingClubArea(club1, zone1);
        final HuntingClubArea club1Area2 = model().newHuntingClubArea(club1, zone2);
        final HuntingClubArea club2Area = model().newHuntingClubArea(club2, zone3);

        final double club1CombinedTotalAreaSize = zone1.getComputedAreaSize() + zone2.getComputedAreaSize();
        final double club1CombinedWaterAreaSize = zone1.getWaterAreaSize() + zone2.getWaterAreaSize();

        final GISZone zone = model().newGISZone(club1CombinedTotalAreaSize + zone3.getComputedAreaSize());
        zone.setWaterAreaSize(club1CombinedWaterAreaSize + zone3.getWaterAreaSize());

        final HarvestPermitArea permitArea =
                model().newHarvestPermitArea(club1, DateUtil.getFirstCalendarYearOfCurrentHuntingYear(), zone);
        model().newHarvestPermitAreaPartner(permitArea, club1Area1);
        model().newHarvestPermitAreaPartner(permitArea, club1Area2);
        model().newHarvestPermitAreaPartner(permitArea, club2Area);

        permitArea.setStatusPending();
        permitArea.setStatusProcessing();
        permitArea.setStatusReady();

        final HarvestPermitAreaRhy permitAreaRhy1 =
                model().newHarvestPermitAreaRhy(permitArea, rhy1, club1CombinedTotalAreaSize * 0.5);
        final HarvestPermitAreaRhy permitAreaRhy2 =
                model().newHarvestPermitAreaRhy(permitArea, rhy2, zone3.getComputedAreaSize() * 0.5);

        final HarvestPermitAreaHta permitAreaHta1 =
                model().newHarvestPermitAreaHta(permitArea, hta1, club1CombinedTotalAreaSize);
        final HarvestPermitAreaHta permitAreaHta2 =
                model().newHarvestPermitAreaHta(permitArea, hta2, zone3.getComputedAreaSize() * 0.5);

        onSavedAndAuthenticated(createNewApiUser(SystemUserPrivilege.EXPORT_LUPAHALLINTA_PERMIT_AREA), () -> {

            final LHPA_PermitArea result = feature.getByExternalId(permitArea.getExternalId());
            assertNotNull(result);

            assertEquals(permitArea.getNameFinnish(), result.getNameFinnish());
            assertEquals(permitArea.getNameSwedish(), result.getNameSwedish());
            assertEquals(permitArea.getExternalId(), result.getOfficialCode());
            assertEquals(
                    DateUtil.toLocalDateTimeNullSafe(permitArea.getLifecycleFields().getModificationTime()),
                    result.getLastModified());
            assertEquals(squareMetersToHectares(zone.getComputedAreaSize()), result.getTotalAreaSize());
            assertEquals(squareMetersToHectares(zone.getWaterAreaSize()), result.getWaterAreaSize());

            assertNotNull(result.getPartners());
            assertEquals(2, result.getPartners().size());

            assertPartner(result.getPartners().get(0), club1, club1CombinedTotalAreaSize, club1CombinedWaterAreaSize);
            assertPartner(result.getPartners().get(1), club2, zone3.getComputedAreaSize(), zone3.getWaterAreaSize());

            assertNotNull(result.getRhy());
            assertEquals(2, result.getRhy().size());
            assertRhy(result.getRhy().get(0), permitAreaRhy2);
            assertRhy(result.getRhy().get(1), permitAreaRhy1);

            assertNotNull(result.getHta());
            assertEquals(2, result.getHta().size());
            assertHta(result.getHta().get(0), permitAreaHta1);
            assertHta(result.getHta().get(1), permitAreaHta2);
        });
    }

    private static void assertPartner(final LHPA_Partner result,
                                      final HuntingClub club,
                                      final double totalAreaSize,
                                      final double waterAreaSize) {

        assertEquals(club.getOfficialCode(), result.getOfficialCode());
        assertEquals(club.getNameFinnish(), result.getNameFinnish());
        assertEquals(club.getNameSwedish(), result.getNameSwedish());
        assertEquals(club.getGeoLocation().getLatitude(), result.getLocation().getLatitude());
        assertEquals(club.getGeoLocation().getLongitude(), result.getLocation().getLongitude());

        assertEquals(squareMetersToHectares(totalAreaSize), result.getTotalAreaSize());
        assertEquals(squareMetersToHectares(waterAreaSize), result.getWaterAreaSize());
    }

    private static void assertRhy(final LHPA_NameWithOfficialCode result, final HarvestPermitAreaRhy permitAreaRhy) {
        final Riistanhoitoyhdistys rhy = permitAreaRhy.getRhy();
        assertEquals(rhy.getOfficialCode(), result.getOfficialCode());
        assertEquals(rhy.getNameFinnish(), result.getNameFinnish());
        assertEquals(rhy.getNameSwedish(), result.getNameSwedish());

        assertEquals(squareMetersToHectares(permitAreaRhy.getAreaSize()), result.getAreaSize());
    }

    private static void assertHta(final LHPA_NameWithOfficialCode result, final HarvestPermitAreaHta permitAreaHta) {
        final GISHirvitalousalue hta = permitAreaHta.getHta();
        assertEquals(hta.getNumber(), result.getOfficialCode());
        assertEquals(hta.getNameFinnish(), result.getNameFinnish());
        assertEquals(hta.getNameSwedish(), result.getNameSwedish());

        assertEquals(squareMetersToHectares(permitAreaHta.getAreaSize()), result.getAreaSize());
    }

}
