package fi.riista.integration.common.export;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.integration.common.export.huntingsummaries.CSUM_ClubHuntingSummary;
import fi.riista.integration.common.export.huntingsummaries.CSUM_HuntingSummaries;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE;
import static fi.riista.feature.permit.PermitTypeCode.WOLVERINE_DAMAGE_BASED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CommonHuntingSummaryExportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private CommonHuntingSummaryExportFeature feature;

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;
    private HuntingClub club;
    private HuntingClub partner;
    private GameSpecies mooseSpecies;
    private GameSpecies wolverineSpecies;
    private SystemUser apiUser;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        rhy.setGeoLocation(new GeoLocation(2, 2));
        final GISZone zone = model().newGISZone(1000);
        area = model().newHarvestPermitArea();
        area.setZone(zone);
        club = model().newHuntingClub(rhy);
        club.setGeoLocation(new GeoLocation(5, 5));
        partner = model().newHuntingClub(rhy);
        partner.setGeoLocation(new GeoLocation(10, 10));

        mooseSpecies = model().newGameSpeciesMoose();
        wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_RVR_COMMON);
    }

    @Test
    public void testMoosePermit() {
        final HarvestPermit permit = createMoosePermit(2019);
        persistInNewTransaction();

        final MooseHuntingSummary summary = createMooseHuntingSummaryWithPopulation(permit, true);
        final AreaSizeAndRemainingPopulation population = summary.getAreaSizeAndPopulation();
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CSUM_HuntingSummaries pojos = feature.exportHuntingSummaries(2019);
            assertThat(pojos.getHuntingSummary(), hasSize(1));
            final CSUM_ClubHuntingSummary pojo = pojos.getHuntingSummary().get(0);
            assertCommonData(permit, pojo);
            assertEquals(partner.getOfficialCode(), pojo.getClubOfficialCode());
            assertEquals(summary.getGameSpeciesCode(), pojo.getGameSpeciesCode());
            assertEquals(summary.getHuntingEndDate(), pojo.getHuntingEndDate());
            assertEquals(population.getTotalHuntingArea(), pojo.getTotalLandAreaSize());
            assertEquals(population.getEffectiveHuntingArea(), pojo.getAreaLandEffectiveSize());
            assertEquals(population.getRemainingPopulationInTotalArea(), pojo.getRemainingPopulationInTotalLandArea());
            assertEquals(population.getRemainingPopulationInEffectiveArea(),
                    pojo.getRemainingPopulationInEffectiveLandArea());
        });
    }

    @Test
    public void testWolverinePermit() {
        final HarvestPermit permit = createWolverinePermit(2019);

        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit,
                wolverineSpecies, 1.0f);
        persistInNewTransaction();

        final BasicClubHuntingSummary summary = createClubHuntingSummaryWithPopulation(permit, speciesAmount, true);
        final AreaSizeAndRemainingPopulation population = summary.getAreaSizeAndPopulation();
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CSUM_HuntingSummaries pojos = feature.exportHuntingSummaries(2019);
            assertThat(pojos.getHuntingSummary(), hasSize(1));
            final CSUM_ClubHuntingSummary pojo = pojos.getHuntingSummary().get(0);
            assertCommonData(permit, pojo);
            assertEquals(summary.getHuntingEndDate(), pojo.getHuntingEndDate());
            assertEquals(population.getTotalHuntingArea(), pojo.getTotalLandAreaSize());
            assertEquals(population.getEffectiveHuntingArea(), pojo.getAreaLandEffectiveSize());
            assertEquals(population.getRemainingPopulationInTotalArea(), pojo.getRemainingPopulationInTotalLandArea());
            assertEquals(population.getRemainingPopulationInEffectiveArea(),
                    pojo.getRemainingPopulationInEffectiveLandArea());
        });
    }

    @Test
    public void testAccess_rvr() {
        onSavedAndAuthenticated(createNewApiUser(SystemUserPrivilege.EXPORT_RVR_COMMON), () -> {
            feature.exportHuntingSummariesAsXml(2019);
            feature.exportHuntingSummaries(2019);
        });
    }

    @Test
    public void testAccess_luke() {
        onSavedAndAuthenticated(createNewApiUser(SystemUserPrivilege.EXPORT_LUKE_COMMON), () -> {
            feature.exportHuntingSummariesAsXml(2019);
            feature.exportHuntingSummaries(2019);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDenied() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportHuntingSummaries(2019));
    }

    @Test
    public void testMoosePermit_unfinishedNotIncluded() {
        final HarvestPermit permit = createMoosePermit(2019);
        persistInNewTransaction();

        createMooseHuntingSummaryWithPopulation(permit, false);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CSUM_HuntingSummaries pojos = feature.exportHuntingSummaries(2019);
            assertThat(pojos.getHuntingSummary(), hasSize(0));
        });
    }

    @Test
    public void testReturnsOnlyForCorrectYear() {
        final HarvestPermit permit18 = createMoosePermit(2018);
        final HarvestPermit permit19 = createMoosePermit(2019);
        final HarvestPermit permit20 = createMoosePermit(2020);
        persistInNewTransaction();

        createMooseHuntingSummaryWithPopulation(permit18, true);
        createMooseHuntingSummaryWithPopulation(permit19, true);
        createMooseHuntingSummaryWithPopulation(permit20, true);

        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CSUM_HuntingSummaries pojos = feature.exportHuntingSummaries(2019);
            assertThat(pojos.getHuntingSummary(), hasSize(1));
            final CSUM_ClubHuntingSummary pojo = pojos.getHuntingSummary().get(0);
            assertEquals(permit19.getPermitNumber(), pojo.getPermitNumber());
        });
    }

    @Test
    public void testNonMandatoryFieldsNotFilled_effectiveFields() {
        final HarvestPermit permit = createMoosePermit(2019);
        persistInNewTransaction();

        final MooseHuntingSummary summary = createMooseHuntingSummaryWithPopulation(permit, true);
        final AreaSizeAndRemainingPopulation population = summary.getAreaSizeAndPopulation();
        population
                .withRemainingPopulationInEffectiveArea(null)
                .withEffectiveHuntingArea(null);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CSUM_HuntingSummaries pojos = feature.exportHuntingSummaries(2019);
            assertThat(pojos.getHuntingSummary(), hasSize(1));
            final CSUM_ClubHuntingSummary pojo = pojos.getHuntingSummary().get(0);
            assertCommonData(permit, pojo);

            assertEquals(population.getTotalHuntingArea(), pojo.getTotalLandAreaSize());
            assertEquals(population.getRemainingPopulationInTotalArea(), pojo.getRemainingPopulationInTotalLandArea());
            assertNull(pojo.getAreaLandEffectiveSize());
            assertNull(pojo.getRemainingPopulationInEffectiveLandArea());
        });
    }

    @Test
    public void testNonMandatoryFieldsNotFilled_totalFields() {
        final HarvestPermit permit = createMoosePermit(2019);
        persistInNewTransaction();

        final MooseHuntingSummary summary = createMooseHuntingSummaryWithPopulation(permit, true);
        final AreaSizeAndRemainingPopulation population = summary.getAreaSizeAndPopulation();
        population
                .withRemainingPopulationInTotalArea(null)
                .withTotalHuntingArea(null);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CSUM_HuntingSummaries pojos = feature.exportHuntingSummaries(2019);
            assertThat(pojos.getHuntingSummary(), hasSize(1));
            final CSUM_ClubHuntingSummary pojo = pojos.getHuntingSummary().get(0);
            assertCommonData(permit, pojo);

            assertNull(pojo.getTotalLandAreaSize());
            assertNull(pojo.getRemainingPopulationInTotalLandArea());
            assertEquals(population.getEffectiveHuntingArea(), pojo.getAreaLandEffectiveSize());
            assertEquals(population.getRemainingPopulationInEffectiveArea(),
                    pojo.getRemainingPopulationInEffectiveLandArea());
        });
    }

    private void assertCommonData(final HarvestPermit permit, final CSUM_ClubHuntingSummary pojo) {
        assertEquals(permit.getPermitNumber(), pojo.getPermitNumber());
        assertEquals(permit.getPermitYear(), pojo.getPermitYear());
        assertEquals(rhy.getOfficialCode(), pojo.getRhyOfficialCode());
    }

    @Test
    public void testSummaryForPartnerWithNoLocationGetsLocationFromPermitsRhy() {
        final HarvestPermit permit = createMoosePermit(2019);
        partner.setGeoLocation(null);
        persistInNewTransaction();

        createMooseHuntingSummaryWithPopulation(permit, true);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CSUM_HuntingSummaries pojos = feature.exportHuntingSummaries(2019);
            assertThat(pojos.getHuntingSummary(), hasSize(1));
            final CSUM_ClubHuntingSummary pojo = pojos.getHuntingSummary().get(0);
            assertEquals(rhy.getGeoLocation().getLatitude(), pojo.getGeoLocation().getLatitude());
            assertEquals(rhy.getGeoLocation().getLongitude(), pojo.getGeoLocation().getLongitude());
        });
    }

    private MooseHuntingSummary createMooseHuntingSummaryWithPopulation(HarvestPermit permit, boolean finished) {
        final MooseHuntingSummary summary = model().newMooseHuntingSummary(permit, partner, finished);
        if (finished) {
            summary.setHuntingEndDate(new LocalDate(permit.getPermitYear(), 12, 1));
        }
        final AreaSizeAndRemainingPopulation population = createPopulation();
        summary.setAreaSizeAndPopulation(population);
        return summary;
    }

    private BasicClubHuntingSummary createClubHuntingSummaryWithPopulation(HarvestPermit permit,
                                                                           HarvestPermitSpeciesAmount speciesAmount,
                                                                           boolean finished) {
        final BasicClubHuntingSummary summary = model().newBasicHuntingSummary(speciesAmount, partner, finished);
        if (finished) {
            summary.setHuntingEndDate(new LocalDate(permit.getPermitYear(), 12, 1));
        }
        final AreaSizeAndRemainingPopulation population = createPopulation();
        summary.setAreaSizeAndPopulation(population);
        return summary;
    }

    private static AreaSizeAndRemainingPopulation createPopulation() {
        return new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(1000)
                .withEffectiveHuntingArea(800)
                .withRemainingPopulationInTotalArea(100)
                .withRemainingPopulationInEffectiveArea(80);
    }

    private HarvestPermit createMoosePermit(int year) {
        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 1));
        permit.setPermitTypeCode(MOOSELIKE);
        permit.setPermitAreaSize(1000);
        permit.setHuntingClub(club);
        model().newHarvestPermitSpeciesAmount(permit, mooseSpecies, 1.0f);
        permit.setPermitPartners(ImmutableSet.of(club, partner));
        return permit;
    }

    private HarvestPermit createWolverinePermit(final int year) {
        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 1));
        permit.setPermitTypeCode(WOLVERINE_DAMAGE_BASED);
        permit.setHuntingClub(club);
        permit.setPermitPartners(ImmutableSet.of(club, partner));
        return permit;
    }
}
