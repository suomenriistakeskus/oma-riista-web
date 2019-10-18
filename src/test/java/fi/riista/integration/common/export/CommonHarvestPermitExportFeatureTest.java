package fi.riista.integration.common.export;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.integration.common.export.permits.CPER_Permit;
import fi.riista.integration.common.export.permits.CPER_PermitPartner;
import fi.riista.integration.common.export.permits.CPER_PermitSpeciesAmount;
import fi.riista.integration.common.export.permits.CPER_Permits;
import fi.riista.integration.common.export.permits.CPER_ValidityTimeInterval;
import fi.riista.test.EmbeddedDatabaseTest;
import org.hamcrest.CustomTypeSafeMatcher;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE;
import static fi.riista.feature.permit.PermitTypeCode.WOLVERINE_DAMAGE_BASED;
import static fi.riista.feature.permit.decision.derogation.HabitatsConstants.HABITATS_REASON_TYPE_POPULATION_PRESERVATION;
import static fi.riista.util.DateUtil.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CommonHarvestPermitExportFeatureTest extends EmbeddedDatabaseTest {


    private static CustomTypeSafeMatcher<CPER_PermitPartner> equalTo(HuntingClub partner) {
        return new CustomTypeSafeMatcher<CPER_PermitPartner>("") {
            @Override
            protected boolean matchesSafely(final CPER_PermitPartner item) {
                return item.getNameFinnish().equals(partner.getNameFinnish()) &&
                        item.getClubOfficialCode().equals(partner.getOfficialCode()) &&
                        geoLocationMatches(item);
            }

            private boolean geoLocationMatches(final CPER_PermitPartner item) {
                return item.getGeoLocation() == null && partner.getGeoLocation() == null ||
                        item.getGeoLocation().getLatitude() == partner.getGeoLocation().getLatitude() &&
                                item.getGeoLocation().getLongitude() == partner.getGeoLocation().getLongitude();
            }
        };
    }

    @Resource
    private CommonHarvestPermitExportFeature feature;

    private Riistanhoitoyhdistys rhy;
    private Riistanhoitoyhdistys rhy2;
    private HarvestPermitArea area;
    private HuntingClub club;
    private HuntingClub club2;
    private GameSpecies mooseSpecies;
    private GameSpecies bearSpecies;
    private SystemUser apiUser;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        rhy.setGeoLocation(new GeoLocation(2, 2));
        rhy2 = model().newRiistanhoitoyhdistys();
        final GISZone zone = model().newGISZone(1000);
        area = model().newHarvestPermitArea();
        area.setZone(zone);
        club = model().newHuntingClub(rhy);
        club.setGeoLocation(new GeoLocation(5, 5));
        club2 = model().newHuntingClub(rhy2);
        club2.setGeoLocation(new GeoLocation(10, 10));

        mooseSpecies = model().newGameSpeciesMoose();
        bearSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_RVR_COMMON);
    }

    @Test
    public void testMoosePermit() {
        final HarvestPermit permit = createMoosePermit(2019);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);
        });
    }

    @Test
    public void testBearPermit_populationPreservation() {
        final HarvestPermit permit = createBearPermit(2019);

        model().newPermitDecisionDerogationReason(permit.getPermitDecision(),
                PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);

            assertEquals(permit.getPermitNumber(), pojo.getPermitNumber());
            assertEquals(permit.getPermitYear(), pojo.getPermitYear());
            assertEquals(rhy.getOfficialCode(), pojo.getRhyOfficialCode());
            assertEquals(rhy.getGeoLocation().getLatitude(), pojo.getGeoLocation().getLatitude());
            assertEquals(rhy.getGeoLocation().getLongitude(), pojo.getGeoLocation().getLongitude());
            assertEquals(permit.getPermitType(), pojo.getPermitDisplayName());
            assertEquals(ImmutableList.of(HABITATS_REASON_TYPE_POPULATION_PRESERVATION), pojo.getDerogationReasons());
        });
    }

    @Test
    public void testBearPermit_damageBased() {
        final HarvestPermit permit = createBearPermit(2019);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);

            assertEquals(permit.getPermitNumber(), pojo.getPermitNumber());
            assertEquals(permit.getPermitYear(), pojo.getPermitYear());
            assertEquals(rhy.getOfficialCode(), pojo.getRhyOfficialCode());
            assertEquals(rhy.getGeoLocation().getLatitude(), pojo.getGeoLocation().getLatitude());
            assertEquals(rhy.getGeoLocation().getLongitude(), pojo.getGeoLocation().getLongitude());
            assertEquals(permit.getPermitType(), pojo.getPermitDisplayName());
            assertThat(pojo.getDerogationReasons(), hasSize(0));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDenied() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportPermits(2018));
    }

    @Test
    public void testFindOnlyForCorrectYear() {
        createMoosePermit(2018);
        final HarvestPermit permit2019 = createMoosePermit(2019);
        createMoosePermit(2020);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit2019, pojo);
        });
    }

    @Test
    public void testSpeciesAmountFields_singlePeriod() {
        final HarvestPermit permit = createMoosePermit(2019);
        final HarvestPermitSpeciesAmount spa = createSpeciesAmounts(permit);
        spa.setBeginDate(new LocalDate(2019, 9, 1));
        spa.setEndDate(new LocalDate(2020, 1, 15));

        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);
            assertThat(pojos.getPermitSpeciesAmount(), hasSize(1));
            final CPER_PermitSpeciesAmount pojoAmount = pojos.getPermitSpeciesAmount().get(0);
            assertAmountInfo(spa, pojoAmount);
        });
    }

    @Test
    public void testSpeciesAmountFields_twoPeriods() {

        final HarvestPermit permit = createMoosePermit(2019);
        final HarvestPermitSpeciesAmount spa = createSpeciesAmounts(permit);
        spa.setBeginDate(new LocalDate(2019, 9, 1));
        spa.setEndDate(new LocalDate(2019, 10, 15));
        spa.setBeginDate2(new LocalDate(2019, 11, 1));
        spa.setEndDate2(new LocalDate(2020, 1, 1));
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);
            assertThat(pojos.getPermitSpeciesAmount(), hasSize(1));
            final CPER_PermitSpeciesAmount pojoAmount = pojos.getPermitSpeciesAmount().get(0);
            assertAmountInfo(spa, pojoAmount);
        });
    }


    @Test
    public void testPartners() {
        final HarvestPermit permit = createMoosePermit(2019);
        permit.setPermitPartners(Sets.newHashSet(club, club2));
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertThat(pojos.getPermitPartner(), hasSize(2));
            final List<CPER_PermitPartner> permitPartner = pojos.getPermitPartner();
            assertThat(permitPartner, contains(Arrays.asList(equalTo(club), equalTo(club2))));
        });
    }

    @Test
    public void testPartners_withNoLocation() {
        final HarvestPermit permit = createMoosePermit(2019);
        club2.setGeoLocation(null);
        permit.setPermitPartners(Sets.newHashSet(club, club2));
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertThat(pojos.getPermitPartner(), hasSize(2));
            final List<CPER_PermitPartner> permitPartner = pojos.getPermitPartner();
            assertThat(permitPartner, contains(Arrays.asList(equalTo(club), equalTo(club2))));
        });
    }

    @Test
    public void testHuntingFinished_harvestReportState() {
        final Person person = model().newPerson();
        final HarvestPermit permit = createMoosePermit(2019);
        permit.setHarvestReportState(HarvestReportState.APPROVED);
        permit.setHarvestReportAuthor(person);
        permit.setHarvestReportDate(now());
        permit.setHarvestReportModeratorOverride(false);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertTrue(pojo.isHuntingFinished());
        });
    }

    @Test
    public void testHuntingFinished_speciesAmount() {
        final HarvestPermit permit = createMoosePermit(2019);
        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, mooseSpecies, 2.0f);
        speciesAmount.setMooselikeHuntingFinished(true);

        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertTrue(pojo.isHuntingFinished());
        });
    }

    @Test
    public void testHuntingFinished_notFinishedIfOnlySomeSpeciesFinished() {
        final HarvestPermit permit = createMoosePermit(2019);
        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, mooseSpecies, 2.0f);
        speciesAmount.setMooselikeHuntingFinished(true);
        final HarvestPermitSpeciesAmount speciesAmount2 = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies(), 2.0f);
        speciesAmount2.setMooselikeHuntingFinished(false);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertFalse(pojo.isHuntingFinished());
        });
    }

    @Test
    public void testPersonPermitGetsLocationFromRhy() {
        final HarvestPermit permit = model().newHarvestPermit(
                rhy, permitNumber(2019, 1));
        permit.setPermitTypeCode(WOLVERINE_DAMAGE_BASED);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertEquals(permit.getPermitNumber(), pojo.getPermitNumber());
            assertEquals(rhy.getOfficialCode(), pojo.getRhyOfficialCode());
            assertEquals(rhy.getGeoLocation().getLatitude(), pojo.getGeoLocation().getLatitude());
            assertEquals(rhy.getGeoLocation().getLongitude(), pojo.getGeoLocation().getLongitude());
            assertEquals(permit.getPermitType(), pojo.getPermitDisplayName());
        });
    }

    private void assertAmountInfo(HarvestPermitSpeciesAmount spa, CPER_PermitSpeciesAmount pojoAmount) {
        assertEquals(spa.getGameSpecies().getOfficialCode(), pojoAmount.getGameSpeciesCode());
        assertEquals(spa.getAmount(), pojoAmount.getAmount(), 0.01);

        assertTrue(pojoAmount.getValidityPeriod().size() > 0);
        assertTrue(pojoAmount.getValidityPeriod().size() <= 2);

        final CPER_ValidityTimeInterval firstPeriod = pojoAmount.getValidityPeriod().get(0);
        assertEquals(spa.getBeginDate(), firstPeriod.getBeginDate());
        assertEquals(spa.getEndDate(), firstPeriod.getEndDate());

        if (pojoAmount.getValidityPeriod().size() == 2) {
            final CPER_ValidityTimeInterval secondPeriod = pojoAmount.getValidityPeriod().get(1);
            assertEquals(spa.getBeginDate2(), secondPeriod.getBeginDate());
            assertEquals(spa.getEndDate2(), secondPeriod.getEndDate());
        } else {
            assertNull(spa.getBeginDate2());
            assertNull(spa.getEndDate2());
        }
    }

    private void assertPermitInfoForMoosePermit(HarvestPermit permit, CPER_Permit pojo) {
        assertEquals(permit.getPermitNumber(), pojo.getPermitNumber());
        assertEquals(permit.getPermitYear(), pojo.getPermitYear());
        assertEquals(rhy.getOfficialCode(), pojo.getRhyOfficialCode());
        assertEquals(club.getGeoLocation().getLatitude(), pojo.getGeoLocation().getLatitude());
        assertEquals(club.getGeoLocation().getLongitude(), pojo.getGeoLocation().getLongitude());
        assertEquals(permit.getPermitType(), pojo.getPermitDisplayName());

    }

    private HarvestPermit createMoosePermit(int year) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.MOOSELIKE);
        application.setApplicationYear(year);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, mooseSpecies, 5);
        application.setSpeciesAmounts(Collections.singletonList(speciesAmount));
        application.setApplicationYear(year);

        final PermitDecision decision = model().newPermitDecision(application);
        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 1));
        permit.setPermitTypeCode(MOOSELIKE);
        permit.setPermitAreaSize(1000);
        permit.setPermitDecision(decision);
        permit.setHuntingClub(club);

        return permit;
    }

    private HarvestPermit createBearPermit(int year) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.LARGE_CARNIVORE_BEAR);
        application.setApplicationYear(year);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, bearSpecies, 5);
        application.setSpeciesAmounts(Collections.singletonList(speciesAmount));
        application.setApplicationYear(year);

        final PermitDecision decision = model().newPermitDecision(application);
        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 1));
        permit.setPermitTypeCode(PermitTypeCode.BEAR_KANNAHOIDOLLINEN);
        permit.setPermitDecision(decision);

        return permit;
    }

    private HarvestPermitSpeciesAmount createSpeciesAmounts(HarvestPermit permit) {
        return model().newHarvestPermitSpeciesAmount(permit, mooseSpecies);
    }

}
