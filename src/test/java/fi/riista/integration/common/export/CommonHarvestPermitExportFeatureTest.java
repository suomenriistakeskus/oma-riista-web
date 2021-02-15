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
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.integration.common.export.permits.CPER_Permit;
import fi.riista.integration.common.export.permits.CPER_PermitPartner;
import fi.riista.integration.common.export.permits.CPER_PermitSpeciesAmount;
import fi.riista.integration.common.export.permits.CPER_Permits;
import fi.riista.integration.common.export.permits.CPER_ValidityTimeInterval;
import fi.riista.test.EmbeddedDatabaseTest;
import org.hamcrest.CustomTypeSafeMatcher;
import org.joda.time.LocalDate;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BROWN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE;
import static fi.riista.feature.permit.PermitTypeCode.WOLVERINE_DAMAGE_BASED;
import static fi.riista.feature.permit.decision.derogation.HabitatsConstants.HABITATS_REASON_TYPE_CROPS_OR_PROPERTY_DAMAGES;
import static fi.riista.feature.permit.decision.derogation.HabitatsConstants.HABITATS_REASON_TYPE_FLORA_AND_FAUNA;
import static fi.riista.feature.permit.decision.derogation.HabitatsConstants.HABITATS_REASON_TYPE_POPULATION_PRESERVATION;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_CATTLE_DAMAGE_41A;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FAUNA_41A;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FLORA_41A;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION;
import static fi.riista.util.DateUtil.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@RunWith(Theories.class)
public class CommonHarvestPermitExportFeatureTest extends EmbeddedDatabaseTest {


    private static CustomTypeSafeMatcher<CPER_PermitPartner> partnerEqualTo(HuntingClub partner) {
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
        final HarvestPermit permit = createMoosePermitWithSpeciesAmount(2019);

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

        model().newPermitDecisionDerogationReason(permit.getPermitDecision(), REASON_POPULATION_PRESERVATION);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);

            assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
            assertThat(pojo.getDerogationReasons(), contains(HABITATS_REASON_TYPE_POPULATION_PRESERVATION));
            assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
            assertThat(pojo.getPermitYear(), equalTo(permit.getPermitYear()));
            assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
            assertThat(pojo.getGeoLocation().getLatitude(), equalTo(rhy.getGeoLocation().getLatitude()));
            assertThat(pojo.getGeoLocation().getLongitude(), equalTo(rhy.getGeoLocation().getLongitude()));
        });
    }

    @Test
    public void testDamageBasedMammal_nonRvrSpecies() {
        final HarvestPermit permit = createDamageBasedMammalPermit(2019);
        createSpeciesAmount(permit, model().newGameSpecies(OFFICIAL_CODE_BROWN_HARE), 5);
        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(0));
        });
    }

    @Test
    public void testDamageBasedMammal_nonRvrSpeciesWithRvrSpecies() {
        final HarvestPermit permit = createDamageBasedMammalPermit(2019);
        createSpeciesAmount(permit, model().newGameSpecies(OFFICIAL_CODE_BROWN_HARE), 5);
        createSpeciesAmount(permit, model().newGameSpecies(OFFICIAL_CODE_WOLVERINE), 5);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));

            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
            assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
            assertThat(pojo.getPermitYear(), equalTo(permit.getPermitYear()));
            assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
            assertThat(pojo.getGeoLocation().getLatitude(), equalTo(rhy.getGeoLocation().getLatitude()));
            assertThat(pojo.getGeoLocation().getLongitude(), equalTo(rhy.getGeoLocation().getLongitude()));

            assertThat(pojos.getPermitSpeciesAmount(), hasSize(1));
            final CPER_PermitSpeciesAmount speciesAmount = pojos.getPermitSpeciesAmount().get(0);
            assertThat(speciesAmount.getGameSpeciesCode(), equalTo(OFFICIAL_CODE_WOLVERINE));
            assertThat(Double.valueOf(speciesAmount.getAmount()), closeTo(5.0, 0.01));
        });
    }

    @Test
    public void testBearPermit_damageBased_multipleReasons() {
        final HarvestPermit permit = createBearPermit(2019);
        model().newPermitDecisionDerogationReason(permit.getPermitDecision(), REASON_FAUNA_41A);
        model().newPermitDecisionDerogationReason(permit.getPermitDecision(), REASON_CATTLE_DAMAGE_41A);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);

            assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
            assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
            assertThat(pojo.getPermitYear(), equalTo(permit.getPermitYear()));
            assertThat(pojo.getDerogationReasons(), hasSize(2));
            assertThat(pojo.getDerogationReasons(),
                    containsInAnyOrder(HABITATS_REASON_TYPE_FLORA_AND_FAUNA,
                            HABITATS_REASON_TYPE_CROPS_OR_PROPERTY_DAMAGES));
            assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
            assertThat(pojo.getGeoLocation().getLatitude(), equalTo(rhy.getGeoLocation().getLatitude()));
            assertThat(pojo.getGeoLocation().getLongitude(), equalTo(rhy.getGeoLocation().getLongitude()));
        });
    }

    @Test
    public void testDamageBasedMammal_multiYearPermit() {

        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.MAMMAL);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, bearSpecies, 5, 5);
        application.setSpeciesAmounts(Collections.singletonList(speciesAmount));
        application.setApplicationYear(2020);
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_WILD_BOAR);

        final PermitDecision decision = model().newPermitDecision(application);
        model().newPermitDecisionDerogationReason(decision, REASON_FAUNA_41A);

        final List<Integer> years = ImmutableList.of(2020, 2021, 2022, 2023, 2024);
        final Map<Integer, HarvestPermit> permits = new HashMap<>();

        years.forEach(year -> {
            final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 5));
            permit.setPermitTypeCode(PermitTypeCode.MAMMAL_DAMAGE_BASED);
            permit.setPermitDecision(decision);
            permits.put(year, permit);

            createSpeciesAmount(permit, species, 5);
        });


        onSavedAndAuthenticated(apiUser, () -> {
            years.forEach(year->{
                final CPER_Permits pojos = feature.exportPermits(year);
                assertThat(pojos.getPermit(), hasSize(1));
                final CPER_Permit pojo = pojos.getPermit().get(0);
                final HarvestPermit permit = permits.get(year);

                assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
                assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
                assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
                assertThat(pojo.getGeoLocation().getLatitude(), equalTo(rhy.getGeoLocation().getLatitude()));
                assertThat(pojo.getGeoLocation().getLongitude(), equalTo(rhy.getGeoLocation().getLongitude()));

                assertThat(pojo.getDerogationReasons(), contains(HABITATS_REASON_TYPE_FLORA_AND_FAUNA));
            });
        });
    }

    @Test
    public void testBearPermit_damageBased_sameHabidesConstant() {
        final HarvestPermit permit = createBearPermit(2019);
        model().newPermitDecisionDerogationReason(permit.getPermitDecision(), REASON_FAUNA_41A);
        model().newPermitDecisionDerogationReason(permit.getPermitDecision(), REASON_FLORA_41A);
        persistInNewTransaction();

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);

            assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
            assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
            assertThat(pojo.getPermitYear(), equalTo(permit.getPermitYear()));
            assertThat(pojo.getDerogationReasons(), hasSize(1));
            assertThat(pojo.getDerogationReasons().get(0), equalTo(HABITATS_REASON_TYPE_FLORA_AND_FAUNA));
            assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
            assertThat(pojo.getGeoLocation().getLatitude(), equalTo(rhy.getGeoLocation().getLatitude()));
            assertThat(pojo.getGeoLocation().getLongitude(), equalTo(rhy.getGeoLocation().getLongitude()));
        });
    }

    @Theory
    public void testDamageBasedMammal(final PermitDecisionDerogationReasonType reasonType) {
        // Skip bird reasons
        Assume.assumeFalse(reasonType.getLawSection() == DerogationLawSection.SECTION_41B);

        final HarvestPermit permit = createDamageBasedMammalPermitWithSpeciesAmount(2019);
        model().newPermitDecisionDerogationReason(permit.getPermitDecision(), reasonType);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);

            assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
            assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
            assertThat(pojo.getPermitYear(), equalTo(permit.getPermitYear()));
            assertThat(pojo.getDerogationReasons(), hasSize(1));
            assertThat(pojo.getDerogationReasons().get(0), equalTo(reasonType.getHabidesCodeForMammals()));
            assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
            assertThat(pojo.getGeoLocation().getLatitude(), equalTo(rhy.getGeoLocation().getLatitude()));
            assertThat(pojo.getGeoLocation().getLongitude(), equalTo(rhy.getGeoLocation().getLongitude()));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDenied() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportPermits(2018));
    }

    @Test
    public void testFindOnlyForCorrectYear() {
        createMoosePermitWithSpeciesAmount(2018);
        final HarvestPermit permit2019 = createMoosePermitWithSpeciesAmount(2019);
        createMoosePermitWithSpeciesAmount(2020);

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
        final HarvestPermitSpeciesAmount spa = createSpeciesAmount(permit, mooseSpecies, 5);
        spa.setBeginDate(new LocalDate(2019, 9, 1));
        spa.setEndDate(new LocalDate(2020, 1, 15));

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
        final HarvestPermitSpeciesAmount spa = createSpeciesAmount(permit, mooseSpecies, 5);
        spa.setBeginDate(new LocalDate(2019, 9, 1));
        spa.setEndDate(new LocalDate(2019, 10, 15));
        spa.setBeginDate2(new LocalDate(2019, 11, 1));
        spa.setEndDate2(new LocalDate(2020, 1, 1));

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
        final HarvestPermit permit = createMoosePermitWithSpeciesAmount(2019);
        permit.setPermitPartners(Sets.newHashSet(club, club2));

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertThat(pojos.getPermitPartner(), hasSize(2));
            final List<CPER_PermitPartner> permitPartner = pojos.getPermitPartner();
            assertThat(permitPartner, contains(Arrays.asList(partnerEqualTo(club), partnerEqualTo(club2))));
        });
    }

    @Test
    public void testPartners_withNoLocation() {
        final HarvestPermit permit = createMoosePermitWithSpeciesAmount(2019);
        club2.setGeoLocation(null);
        permit.setPermitPartners(Sets.newHashSet(club, club2));

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertThat(pojos.getPermitPartner(), hasSize(2));
            final List<CPER_PermitPartner> permitPartner = pojos.getPermitPartner();
            assertThat(permitPartner, contains(Arrays.asList(partnerEqualTo(club), partnerEqualTo(club2))));
        });
    }

    @Test
    public void testHuntingFinished_harvestReportState() {
        final Person person = model().newPerson();
        final HarvestPermit permit = createMoosePermitWithSpeciesAmount(2019);
        permit.setHarvestReportState(HarvestReportState.APPROVED);
        permit.setHarvestReportAuthor(person);
        permit.setHarvestReportDate(now());
        permit.setHarvestReportModeratorOverride(false);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertThat(pojo.isHuntingFinished(), is(true));
        });
    }

    @Test
    public void testHuntingFinished_speciesAmount() {
        final HarvestPermit permit = createMoosePermit(2019);
        final HarvestPermitSpeciesAmount speciesAmount =
                model().newHarvestPermitSpeciesAmount(permit, mooseSpecies, 2.0f);
        speciesAmount.setMooselikeHuntingFinished(true);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertThat(pojo.isHuntingFinished(), is(true));
        });
    }

    @Test
    public void testHuntingFinished_notFinishedIfOnlySomeSpeciesFinished() {
        final HarvestPermit permit = createMoosePermit(2019);
        final HarvestPermitSpeciesAmount speciesAmount =
                model().newHarvestPermitSpeciesAmount(permit, mooseSpecies, 2.0f);
        speciesAmount.setMooselikeHuntingFinished(true);
        final HarvestPermitSpeciesAmount speciesAmount2 =
                model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies(), 2.0f);
        speciesAmount2.setMooselikeHuntingFinished(false);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertPermitInfoForMoosePermit(permit, pojo);

            assertThat(pojo.isHuntingFinished(), is(false));
        });
    }

    @Test
    public void testPersonPermitGetsLocationFromRhy() {
        final HarvestPermit permit = model().newHarvestPermit(
                rhy, permitNumber(2019, 1));
        permit.setPermitTypeCode(WOLVERINE_DAMAGE_BASED);
        createSpeciesAmount(permit, model().newGameSpecies(OFFICIAL_CODE_WOLVERINE), 5);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(1));
            final CPER_Permit pojo = pojos.getPermit().get(0);
            assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
            assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
            assertThat(pojo.getPermitYear(), equalTo(permit.getPermitYear()));
            assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
            assertThat(pojo.getGeoLocation().getLatitude(), equalTo(rhy.getGeoLocation().getLatitude()));
            assertThat(pojo.getGeoLocation().getLongitude(), equalTo(rhy.getGeoLocation().getLongitude()));
        });
    }

    @Test
    public void testDoesNotSendRejectedApplicationPermits() {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.LARGE_CARNIVORE_BEAR);
        application.setApplicationYear(2019);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, bearSpecies, 5);
        application.setSpeciesAmounts(Collections.singletonList(speciesAmount));

        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(2019, 1));
        permit.setPermitTypeCode(PermitTypeCode.BEAR_KANNAHOIDOLLINEN);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits pojos = feature.exportPermits(2019);
            assertThat(pojos.getPermit(), hasSize(0));
        });
    }

    @Test
    public void testExportPermits_otter() {
        final int year = 2021;

        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 1));
        permit.setPermitTypeCode(MAMMAL_DAMAGE_BASED);

        final GameSpecies otter = model().newGameSpecies(OFFICIAL_CODE_OTTER);

        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, otter, year);

        onSavedAndAuthenticated(apiUser, () -> {
            final CPER_Permits exportedPermits = feature.exportPermits(2021);
            assertThat(exportedPermits.getPermit(), hasSize(1));

            final CPER_Permit exportedPermit = exportedPermits.getPermit().get(0);
            assertThat(exportedPermit.getPermitNumber(), equalTo(permit.getPermitNumber()));
            assertThat(exportedPermit.getPermitDisplayName(), equalTo(permit.getPermitType()));
            assertThat(exportedPermit.getPermitYear(), equalTo(permit.getPermitYear()));
            assertThat(exportedPermit.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));

            assertThat(exportedPermits.getPermitSpeciesAmount(), hasSize(1));

            final CPER_PermitSpeciesAmount exportedSpa = exportedPermits.getPermitSpeciesAmount().get(0);
            assertThat(exportedSpa.getGameSpeciesCode(), equalTo(OFFICIAL_CODE_OTTER));
            assertThat((double)exportedSpa.getAmount(), is(closeTo(speciesAmount.getSpecimenAmount(), 0.01)));
        });
    }

    private static void assertAmountInfo(final HarvestPermitSpeciesAmount spa,
                                         final CPER_PermitSpeciesAmount pojoAmount) {

        assertThat(pojoAmount.getGameSpeciesCode(), equalTo(spa.getGameSpecies().getOfficialCode()));
        assertThat(Double.valueOf(pojoAmount.getAmount()), closeTo(spa.getSpecimenAmount(), 0.01));

        assertThat(pojoAmount.getValidityPeriod(), is(not(empty())));
        assertThat(pojoAmount.getValidityPeriod().size(), lessThanOrEqualTo(2));

        final CPER_ValidityTimeInterval firstPeriod = pojoAmount.getValidityPeriod().get(0);
        assertThat(firstPeriod.getBeginDate(), equalTo(spa.getBeginDate()));
        assertThat(firstPeriod.getEndDate(), equalTo(spa.getEndDate()));

        if (pojoAmount.getValidityPeriod().size() == 2) {
            final CPER_ValidityTimeInterval secondPeriod = pojoAmount.getValidityPeriod().get(1);
            assertThat(secondPeriod.getBeginDate(), equalTo(spa.getBeginDate2()));
            assertThat(secondPeriod.getEndDate(), equalTo(spa.getEndDate2()));
        } else {
            assertThat(spa.getBeginDate2(), is(nullValue()));
            assertThat(spa.getEndDate2(), is(nullValue()));
        }
    }

    private void assertPermitInfoForMoosePermit(final HarvestPermit permit, final CPER_Permit pojo) {
        assertThat(pojo.getPermitNumber(), equalTo(permit.getPermitNumber()));
        assertThat(pojo.getPermitDisplayName(), equalTo(permit.getPermitType()));
        assertThat(pojo.getPermitYear(), equalTo(permit.getPermitYear()));
        assertThat(pojo.getRhyOfficialCode(), equalTo(rhy.getOfficialCode()));
        assertThat(pojo.getGeoLocation().getLatitude(), equalTo(club.getGeoLocation().getLatitude()));
        assertThat(pojo.getGeoLocation().getLongitude(), equalTo(club.getGeoLocation().getLongitude()));

    }

    private HarvestPermit createMoosePermit(final int year) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.MOOSELIKE);
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

    private HarvestPermit createMoosePermitWithSpeciesAmount(final int year) {
        final HarvestPermit permit = createMoosePermit(year);
        createSpeciesAmount(permit, mooseSpecies, 5);
        return permit;
    }

    private HarvestPermit createBearPermit(final int year) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.LARGE_CARNIVORE_BEAR);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, bearSpecies, 5);
        application.setSpeciesAmounts(Collections.singletonList(speciesAmount));
        application.setApplicationYear(year);

        final PermitDecision decision = model().newPermitDecision(application);
        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 1));
        permit.setPermitTypeCode(PermitTypeCode.BEAR_KANNAHOIDOLLINEN);
        permit.setPermitDecision(decision);
        createSpeciesAmount(permit, bearSpecies, 5);

        return permit;
    }

    private HarvestPermit createDamageBasedMammalPermit(final int year) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.MAMMAL);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, bearSpecies, 5);
        application.setSpeciesAmounts(Collections.singletonList(speciesAmount));
        application.setApplicationYear(year);

        final PermitDecision decision = model().newPermitDecision(application);
        final HarvestPermit permit = model().newHarvestPermit(rhy, permitNumber(year, 1));
        permit.setPermitTypeCode(PermitTypeCode.MAMMAL_DAMAGE_BASED);
        permit.setPermitDecision(decision);

        return permit;
    }

    private HarvestPermit createDamageBasedMammalPermitWithSpeciesAmount(final int year) {
        final HarvestPermit permit = createDamageBasedMammalPermit(year);
        createSpeciesAmount(permit, bearSpecies, 5);

        return permit;
    }

    private HarvestPermitSpeciesAmount createSpeciesAmount(final HarvestPermit permit,
                                                           final GameSpecies species,
                                                           final int amount) {
        return model().newHarvestPermitSpeciesAmount(permit, species, permit.getPermitYear(), amount);
    }

}
