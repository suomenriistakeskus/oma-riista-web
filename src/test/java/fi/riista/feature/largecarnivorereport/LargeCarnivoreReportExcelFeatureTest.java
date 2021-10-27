package fi.riista.feature.largecarnivorereport;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceased;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedCause;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedDTO;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedSource;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DEPORTATION;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_BEAR;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF_PORONHOITO;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MAMMAL;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.RESEARCH;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(Theories.class)
public class LargeCarnivoreReportExcelFeatureTest extends EmbeddedDatabaseTest {

    @DataPoints
    public static final Set<Integer> REPORT_SPECIES = new ImmutableSet.Builder<Integer>()
            .addAll(LARGE_CARNIVORES)
            .add(OFFICIAL_CODE_OTTER)
            .build();

    @DataPoints
    public static final Set<HarvestPermitCategory> STOCK_MGMT_CATEGORIES = new ImmutableSet.Builder<HarvestPermitCategory>()
            .add(LARGE_CARNIVORE_BEAR)
            .add(LARGE_CARNIVORE_LYNX)
            .add(LARGE_CARNIVORE_LYNX_PORONHOITO)
            .add(LARGE_CARNIVORE_WOLF)
            .add(LARGE_CARNIVORE_WOLF_PORONHOITO)
            .build();

    @Resource
    private LargeCarnivoreReportExcelFeature feature;

    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
    private RiistakeskuksenAlue rka2;
    private Riistanhoitoyhdistys rhy2;

    private final Map<Integer, HarvestPermitApplication> derogationApplications = new HashMap<>();
    private final Map<Integer, HarvestPermitApplicationSpeciesAmount> derogationSpeciesAmounts = new HashMap<>();
    private final Map<Integer, PermitDecision> derogationDecisions = new HashMap<>();
    private final Map<Integer, HarvestPermit> derogationPermits = new HashMap<>();
    private final Map<Integer, HarvestPermitSpeciesAmount> derogationPermitSpeciesAmounts = new HashMap<>();

    private final List<HarvestPermitApplication> stockMgmtApplications = new ArrayList<>();
    private final Map<HarvestPermitApplication, HarvestPermitApplicationSpeciesAmount> stockMgmtSpeciesAmounts = new HashMap<>();
    private final Map<HarvestPermitApplication, PermitDecision> stockMgmtDecisions = new HashMap<>();
    private final Map<HarvestPermitApplication, HarvestPermit> stockMgmtPermits = new HashMap<>();
    private final Map<HarvestPermitApplication, HarvestPermitSpeciesAmount> stockMgmtPermitSpeciesAmounts = new HashMap<>();
    private final Map<HarvestPermitApplication, Integer> stockMgmtHarvestAmounts = new HashMap<>();
    private final Map<HarvestPermitApplication, Riistanhoitoyhdistys> stockMgmtRhys = new HashMap<>();

    private final Map<Integer, GameSpecies> gameSpecies = new HashMap<>();

    private final Map<Integer, HarvestPermitApplication> deportationApplications = new HashMap<>();
    private final Map<Integer, HarvestPermitApplicationSpeciesAmount> deportationSpeciesAmounts = new HashMap<>();
    private final Map<Integer, PermitDecision> deportationDecisions = new HashMap<>();
    private final Map<Integer, HarvestPermit> deportationPermits  = new HashMap<>();
    private final Map<Integer, HarvestPermitSpeciesAmount> deportationPermitSpeciesAmounts = new HashMap<>();

    private final Map<Integer, HarvestPermitApplication> researchApplications = new HashMap<>();
    private final Map<Integer, HarvestPermitApplicationSpeciesAmount> researchSpeciesAmounts = new HashMap<>();
    private final Map<Integer, PermitDecision> researchDecisions = new HashMap<>();
    private final Map<Integer, HarvestPermit> researchPermits  = new HashMap<>();
    private final Map<Integer, HarvestPermitSpeciesAmount> researchPermitSpeciesAmounts = new HashMap<>();

    private final Map<Integer, SrvaEvent> srvaEvents = new HashMap<>();

    private final Map<Integer, OtherwiseDeceased> otherwiseDeceasedMap = new HashMap<>();

    @Before
    public void setup() {
        rka = model().newRiistakeskuksenAlue();
        rhy = model().newRiistanhoitoyhdistys(rka);

        rka2 = model().newRiistakeskuksenAlue();
        rhy2 = model().newRiistanhoitoyhdistys(rka2);

        REPORT_SPECIES.forEach(speciesCode -> {
            final GameSpecies species = model().newGameSpecies(speciesCode);
            gameSpecies.put(speciesCode, species);
        });

        createDerogations();

        createStockMgmt();

        createDeportations();

        createResearch();

        createSrva();

        createOtherwiseDeceased();
    }

    private void createDerogations() {
        REPORT_SPECIES.forEach(speciesCode -> {
            final GameSpecies species = gameSpecies.get(speciesCode);

            final HarvestPermitApplication derogationApplication =
                    model().newHarvestPermitApplication(rhy, null, emptyList(), MAMMAL);
            derogationApplications.put(speciesCode, derogationApplication);

            final HarvestPermitApplicationSpeciesAmount derogationSpa =
                    model().newHarvestPermitApplicationSpeciesAmount(derogationApplication, species, 1.0f);
            derogationSpa.setBeginDate(new LocalDate(2021, 1, 1));
            derogationSpa.setEndDate(new LocalDate(2021, 1, 16));
            derogationSpeciesAmounts.put(speciesCode, derogationSpa);

            final PermitDecision derogationDecision = model().newPermitDecision(derogationApplication);
            derogationDecision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
            derogationDecision.setLockedDate(DateUtil.toDateTimeNullSafe(new LocalDate(2021, 1, 1)));
            derogationDecisions.put(speciesCode, derogationDecision);

            final HarvestPermit derogationPermit =
                    model().newHarvestPermit(rhy, permitNumber(), MAMMAL_DAMAGE_BASED, derogationDecision);
            derogationPermits.put(speciesCode, derogationPermit);

            final HarvestPermitSpeciesAmount derogationPermitSpa =
                    model().newHarvestPermitSpeciesAmount(derogationPermit, species, 1.0f);
            derogationPermitSpa.setBeginDate(new LocalDate(2021, 1, 1));
            derogationPermitSpa.setEndDate(new LocalDate(2021, 1, 7));
            derogationPermitSpa.setBeginDate2(new LocalDate(2021, 1, 9));
            derogationPermitSpa.setEndDate2(new LocalDate(2021, 1, 16));
            derogationPermitSpeciesAmounts.put(speciesCode, derogationPermitSpa);

            final Harvest derogationHarvest = model().newHarvest(derogationPermit, species);
            derogationHarvest.setAmount(1);
        });
    }

    private void createStockMgmt() {
        STOCK_MGMT_CATEGORIES.forEach(category -> {
            GameSpecies species = null;
            final String permitTypeCode = PermitTypeCode.getPermitTypeCode(category, 1);
            switch (category) {
                case LARGE_CARNIVORE_BEAR:
                    species = gameSpecies.get(OFFICIAL_CODE_BEAR);
                    break;
                case LARGE_CARNIVORE_LYNX:
                case LARGE_CARNIVORE_LYNX_PORONHOITO:
                    species = gameSpecies.get(OFFICIAL_CODE_LYNX);
                    break;
                case LARGE_CARNIVORE_WOLF:
                case LARGE_CARNIVORE_WOLF_PORONHOITO:
                    species = gameSpecies.get(OFFICIAL_CODE_WOLF);
                    break;
            }

            final HarvestPermitApplication stockMgmtApplication =
                    model().newHarvestPermitApplication(rhy, null, emptyList(), category);
            stockMgmtApplications.add(stockMgmtApplication);

            final HarvestPermitApplication stockMgmtApplication2 =
                    model().newHarvestPermitApplication(rhy2, null, emptyList(), category);
            stockMgmtApplications.add(stockMgmtApplication2);

            persistInNewTransaction();

            stockMgmtRhys.put(stockMgmtApplication, rhy);
            stockMgmtRhys.put(stockMgmtApplication2, rhy2);

            final HarvestPermitApplicationSpeciesAmount stockMgmtApplicationSpeciesAmount =
                    model().newHarvestPermitApplicationSpeciesAmount(stockMgmtApplication, species, 3.0f);
            stockMgmtApplicationSpeciesAmount.setBeginDate(new LocalDate(2021, 2, 1));
            stockMgmtApplicationSpeciesAmount.setEndDate(new LocalDate(2021, 2, 14));
            stockMgmtSpeciesAmounts.put(stockMgmtApplication, stockMgmtApplicationSpeciesAmount);

            final PermitDecision stockMgmtDecision = model().newPermitDecision(stockMgmtApplication);
            stockMgmtDecision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
            stockMgmtDecision.setLockedDate(DateUtil.toDateTimeNullSafe(new LocalDate(2021, 2, 1)));
            stockMgmtDecisions.put(stockMgmtApplication, stockMgmtDecision);

            final HarvestPermit stockMgmtPermit =
                    model().newHarvestPermit(rhy, permitNumber(), permitTypeCode, stockMgmtDecision);
            stockMgmtPermits.put(stockMgmtApplication, stockMgmtPermit);

            final HarvestPermitSpeciesAmount stockMgmtPermitSpeciesAmount =
                    model().newHarvestPermitSpeciesAmount(stockMgmtPermit, species, 3.0f);
            stockMgmtPermitSpeciesAmount.setBeginDate(new LocalDate(2021, 2, 1));
            stockMgmtPermitSpeciesAmount.setEndDate(new LocalDate(2021, 2, 6));
            stockMgmtPermitSpeciesAmount.setBeginDate2(new LocalDate(2021, 2, 8));
            stockMgmtPermitSpeciesAmount.setEndDate2(new LocalDate(2021, 2, 14));
            stockMgmtPermitSpeciesAmounts.put(stockMgmtApplication, stockMgmtPermitSpeciesAmount);

            final Harvest stockMgmtHarvest1 = model().newHarvest(stockMgmtPermit, species);
            stockMgmtHarvest1.setAmount(1);
            final Harvest stockMgmtHarvest2 = model().newHarvest(stockMgmtPermit, species);
            stockMgmtHarvest2.setAmount(2);
            stockMgmtHarvestAmounts.put(stockMgmtApplication, 3);

            final HarvestPermitApplicationSpeciesAmount stockMgmtApplicationSpeciesAmount2 =
                    model().newHarvestPermitApplicationSpeciesAmount(stockMgmtApplication2, species, 2.0f);
            stockMgmtApplicationSpeciesAmount2.setBeginDate(new LocalDate(2021, 3, 15));
            stockMgmtApplicationSpeciesAmount2.setEndDate(new LocalDate(2021, 3, 20));
            stockMgmtSpeciesAmounts.put(stockMgmtApplication2, stockMgmtApplicationSpeciesAmount2);

            final PermitDecision stockMgmtDecision2 = model().newPermitDecision(stockMgmtApplication2);
            stockMgmtDecision2.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
            stockMgmtDecision2.setLockedDate(DateUtil.toDateTimeNullSafe(new LocalDate(2021, 3, 15)));
            stockMgmtDecisions.put(stockMgmtApplication2, stockMgmtDecision2);

            final HarvestPermit stockMgmtPermit2 =
                    model().newHarvestPermit(rhy, permitNumber(), permitTypeCode, stockMgmtDecision2);
            stockMgmtPermits.put(stockMgmtApplication2, stockMgmtPermit2);

            final HarvestPermitSpeciesAmount stockMgmtPermitSpeciesAmount2 =
                    model().newHarvestPermitSpeciesAmount(stockMgmtPermit2, species, 2.0f);
            stockMgmtPermitSpeciesAmount2.setBeginDate(new LocalDate(2021, 3, 15));
            stockMgmtPermitSpeciesAmount2.setEndDate(new LocalDate(2021, 3, 20));
            stockMgmtPermitSpeciesAmounts.put(stockMgmtApplication2, stockMgmtPermitSpeciesAmount2);

            final Harvest stockMgmtHarvest3 = model().newHarvest(stockMgmtPermit2, species);
            stockMgmtHarvest3.setAmount(1);
            stockMgmtHarvestAmounts.put(stockMgmtApplication2, 1);
        });
    }

    private void createDeportations() {
        REPORT_SPECIES.forEach(speciesCode -> {
            final GameSpecies species = gameSpecies.get(speciesCode);

            final HarvestPermitApplication deportationApp =
                    model().newHarvestPermitApplication(rhy, null, emptyList(), DEPORTATION);
            deportationApplications.put(speciesCode, deportationApp);

            final HarvestPermitApplicationSpeciesAmount deportationSpa =
                    model().newHarvestPermitApplicationSpeciesAmount(deportationApp, species, 5.0f);
            deportationSpa.setBeginDate(new LocalDate(2021, 3, 1));
            deportationSpa.setEndDate(new LocalDate(2021, 3, 7));
            deportationSpeciesAmounts.put(speciesCode, deportationSpa);

            final PermitDecision deportationDecision = model().newPermitDecision(deportationApp);
            deportationDecision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
            deportationDecision.setLockedDate(DateUtil.toDateTimeNullSafe(new LocalDate(2021, 3, 1)));
            deportationDecisions.put(speciesCode, deportationDecision);

            final HarvestPermit deportationPermit =
                    model().newHarvestPermit(rhy, permitNumber(), PermitTypeCode.DEPORTATION, deportationDecision);
            deportationPermits.put(speciesCode, deportationPermit);

            final HarvestPermitSpeciesAmount deportationPermitSpa =
                    model().newHarvestPermitSpeciesAmount(deportationPermit, species, 5.0f);
            deportationPermitSpa.setBeginDate(new LocalDate(2021, 3, 1));
            deportationPermitSpa.setEndDate(new LocalDate(2021, 3, 7));
            deportationPermitSpeciesAmounts.put(speciesCode, deportationPermitSpa);
        });
    }

    private void createResearch() {
        REPORT_SPECIES.forEach(speciesCode -> {
            final GameSpecies species = gameSpecies.get(speciesCode);

            final HarvestPermitApplication researchApp =
                    model().newHarvestPermitApplication(rhy, null, emptyList(), RESEARCH);
            researchApplications.put(speciesCode, researchApp);

            final HarvestPermitApplicationSpeciesAmount researchSpa =
                    model().newHarvestPermitApplicationSpeciesAmount(researchApp, species, 5.0f);
            researchSpa.setBeginDate(new LocalDate(2021, 3, 1));
            researchSpa.setEndDate(new LocalDate(2021, 3, 7));
            researchSpeciesAmounts.put(speciesCode, researchSpa);

            final PermitDecision researchDecision = model().newPermitDecision(researchApp);
            researchDecision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
            researchDecision.setLockedDate(DateUtil.toDateTimeNullSafe(new LocalDate(2021, 3, 1)));
            researchDecisions.put(speciesCode, researchDecision);

            final HarvestPermit researchPermit =
                    model().newHarvestPermit(rhy, permitNumber(), PermitTypeCode.DEPORTATION, researchDecision);
            final Person researchPermitHolder = model().newPerson(rhy);
            researchPermitHolder.setFirstName("Permit");
            researchPermitHolder.setLastName("Holder1");
            researchPermit.setPermitHolder(PermitHolder.createHolderForPerson(researchPermitHolder));
            researchPermits.put(speciesCode, researchPermit);

            final HarvestPermitSpeciesAmount researchPermitSpa =
                    model().newHarvestPermitSpeciesAmount(researchPermit, species, 5.0f);
            researchPermitSpa.setBeginDate(new LocalDate(2021, 3, 1));
            researchPermitSpa.setEndDate(new LocalDate(2021, 3, 7));
            researchPermitSpeciesAmounts.put(speciesCode, researchPermitSpa);
        });
    }

    private void createSrva() {
        REPORT_SPECIES.forEach(speciesCode -> {
            final GameSpecies species = gameSpecies.get(speciesCode);

            final SrvaEvent event = model().newSrvaEvent(rhy);
            event.setSpecies(species);
            event.setEventName(some(SrvaEventNameEnum.class));
            event.setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(event.getEventName())));
            event.setPointOfTime(DateUtil.toDateTimeNullSafe(new LocalDate(2021, 3, 1)));
            srvaEvents.put(speciesCode, event);
        });
    }

    private void createOtherwiseDeceased() {
        REPORT_SPECIES.forEach(speciesCode -> {
            final GameSpecies species = gameSpecies.get(speciesCode);

            final OtherwiseDeceased otherwiseDeceased = model().newOtherwiseDeceased(
                    DateUtil.toDateTimeNullSafe(new LocalDate(2021, 1, 1)),
                    some(GameAge.class),
                    some(GameGender.class),
                    some(OtherwiseDeceasedCause.class),
                    some(OtherwiseDeceasedSource.class),
                    species,
                    rhy,
                    rka,
                    false);
            otherwiseDeceasedMap.put(speciesCode, otherwiseDeceased);
        });
    }

    @Theory
    public void testGetApplicationInfo_derogations(final int speciesCode) {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final List<LargeCarnivorePermitInfoDTO> derogations =
                        feature.getPermitInfo(MAMMAL, speciesCode, 2020);
                assertThat(derogations, hasSize(1));

                final LargeCarnivorePermitInfoDTO derogation = derogations.get(0);

                final HarvestPermitApplication expectedApplication = derogationApplications.get(speciesCode);
                assertThat(derogation.getApplicationNumber(), is(equalTo(expectedApplication.getApplicationNumber())));

                final HarvestPermit expectedPermit = derogationPermits.get(speciesCode);
                assertThat(derogation.getPermitNumber(), is(equalTo(expectedPermit.getPermitNumber())));

                final PermitDecision expectedDecision = derogationDecisions.get(speciesCode);
                assertThat(derogation.getDecisionType(), is(equalTo(expectedDecision.getDecisionType())));
                assertThat(derogation.getDecisionTime(), is(equalTo(expectedDecision.getLockedDate())));

                final HarvestPermitSpeciesAmount expectedPermitSpa =
                        derogationPermitSpeciesAmounts.get(speciesCode);
                assertThat(derogation.getBeginDate(), is(equalTo(expectedPermitSpa.getBeginDate())));
                assertThat(derogation.getEndDate(), is(equalTo(expectedPermitSpa.getEndDate())));
                assertThat(derogation.getBeginDate2(), is(equalTo(expectedPermitSpa.getBeginDate2())));
                assertThat(derogation.getEndDate2(), is(equalTo(expectedPermitSpa.getEndDate2())));

                final HarvestPermitApplicationSpeciesAmount expectedApplicationSpa =
                        derogationSpeciesAmounts.get(speciesCode);
                assertThat(derogation.getApplied(), is(equalTo(expectedApplicationSpa.getSpecimenAmount())));

                assertThat(derogation.getGranted(), is(equalTo(expectedPermitSpa.getSpecimenAmount())));

                assertThat(derogation.getHarvests(), is(equalTo(1)));

                assertThat(derogation.getRhy(), is(equalTo(LocalisedString.of(rhy.getNameFinnish(), rhy.getNameSwedish()))));

                assertThat(derogation.getRka(), is(equalTo(LocalisedString.of(rka.getNameFinnish(), rka.getNameSwedish()))));
                assertThat(derogation.isOnReindeerArea(), is(false));
            });
        });
    }

    @Theory
    public void testGetApplicationInfo_stockManagement(final HarvestPermitCategory category) {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                int speciesCode = 0;
                switch (category) {
                    case LARGE_CARNIVORE_BEAR:
                        speciesCode = OFFICIAL_CODE_BEAR;
                        break;
                    case LARGE_CARNIVORE_LYNX:
                    case LARGE_CARNIVORE_LYNX_PORONHOITO:
                        speciesCode = OFFICIAL_CODE_LYNX;
                        break;
                    case LARGE_CARNIVORE_WOLF:
                    case LARGE_CARNIVORE_WOLF_PORONHOITO:
                        speciesCode = OFFICIAL_CODE_WOLF;
                        break;
                }

                final List<LargeCarnivorePermitInfoDTO> stockMgmts =
                        feature.getPermitInfo(category, speciesCode, 2020);
                assertThat(stockMgmts, hasSize(2));

                stockMgmts.forEach(stockMgmtInfo -> {
                    final List<HarvestPermitApplication> expectedApplicationList = stockMgmtApplications.stream()
                            .filter(application -> application.getApplicationNumber().equals(stockMgmtInfo.getApplicationNumber()))
                            .collect(Collectors.toList());
                    assertThat(expectedApplicationList, hasSize(1));

                    final HarvestPermitApplication expectedApplication = expectedApplicationList.get(0);

                    assertThat(stockMgmtInfo.getApplicationNumber(), is(equalTo(expectedApplication.getApplicationNumber())));

                    final HarvestPermit expectedPermit = stockMgmtPermits.get(expectedApplication);
                    assertThat(stockMgmtInfo.getPermitNumber(), is(equalTo(expectedPermit.getPermitNumber())));

                    final PermitDecision expectedDecision = stockMgmtDecisions.get(expectedApplication);
                    assertThat(stockMgmtInfo.getDecisionType(), is(equalTo(expectedDecision.getDecisionType())));
                    assertThat(stockMgmtInfo.getDecisionTime(), is(equalTo(expectedDecision.getLockedDate())));

                    final HarvestPermitSpeciesAmount expectedPermitSpa =
                            stockMgmtPermitSpeciesAmounts.get(expectedApplication);
                    assertThat(stockMgmtInfo.getBeginDate(), is(equalTo(expectedPermitSpa.getBeginDate())));
                    assertThat(stockMgmtInfo.getEndDate(), is(equalTo(expectedPermitSpa.getEndDate())));
                    assertThat(stockMgmtInfo.getBeginDate2(), is(equalTo(expectedPermitSpa.getBeginDate2())));
                    assertThat(stockMgmtInfo.getEndDate2(), is(equalTo(expectedPermitSpa.getEndDate2())));

                    final HarvestPermitApplicationSpeciesAmount expectedApplicationSpa =
                            stockMgmtSpeciesAmounts.get(expectedApplication);
                    assertThat(stockMgmtInfo.getApplied(), is(equalTo(expectedApplicationSpa.getSpecimenAmount())));

                    assertThat(stockMgmtInfo.getGranted(), is(equalTo(expectedPermitSpa.getSpecimenAmount())));

                    assertThat(stockMgmtInfo.getHarvests(), is(equalTo(stockMgmtHarvestAmounts.get(expectedApplication))));

                    final Riistanhoitoyhdistys rhy = stockMgmtRhys.get(expectedApplication);
                    assertThat(stockMgmtInfo.getRhy(), is(equalTo(LocalisedString.of(rhy.getNameFinnish(), rhy.getNameSwedish()))));

                    final Organisation rka = rhy.getParentOrganisation();
                    assertThat(stockMgmtInfo.getRka(), is(equalTo(LocalisedString.of(rka.getNameFinnish(), rka.getNameSwedish()))));
                });
            });
        });
    }

    @Theory
    public void testGetApplicationInfo_deportations(final int speciesCode) {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final List<LargeCarnivorePermitInfoDTO> deportations =
                        feature.getPermitInfo(DEPORTATION, speciesCode, 2020);
                assertThat(deportations, hasSize(1));

                final LargeCarnivorePermitInfoDTO deporation = deportations.get(0);

                final HarvestPermitApplication expectedApplication = deportationApplications.get(speciesCode);
                assertThat(deporation.getApplicationNumber(), is(equalTo(expectedApplication.getApplicationNumber())));

                final HarvestPermit expectedPermit = deportationPermits.get(speciesCode);
                assertThat(deporation.getPermitNumber(), is(equalTo(expectedPermit.getPermitNumber())));

                final PermitDecision expectedDecision = deportationDecisions.get(speciesCode);
                assertThat(deporation.getDecisionType(), is(equalTo(expectedDecision.getDecisionType())));
                assertThat(deporation.getDecisionTime(), is(equalTo(expectedDecision.getLockedDate())));

                final HarvestPermitSpeciesAmount expectedPermitSpa =
                        deportationPermitSpeciesAmounts.get(speciesCode);
                assertThat(deporation.getBeginDate(), is(equalTo(expectedPermitSpa.getBeginDate())));
                assertThat(deporation.getEndDate(), is(equalTo(expectedPermitSpa.getEndDate())));
                assertThat(deporation.getBeginDate2(), is(equalTo(expectedPermitSpa.getBeginDate2())));
                assertThat(deporation.getEndDate2(), is(equalTo(expectedPermitSpa.getEndDate2())));

                final HarvestPermitApplicationSpeciesAmount expectedApplicationSpa =
                        deportationSpeciesAmounts.get(speciesCode);
                assertThat(deporation.getApplied(), is(equalTo(expectedApplicationSpa.getSpecimenAmount())));

                assertThat(deporation.getGranted(), is(equalTo(expectedPermitSpa.getSpecimenAmount())));

                assertThat(deporation.getRhy(), is(equalTo(LocalisedString.of(rhy.getNameFinnish(), rhy.getNameSwedish()))));

                assertThat(deporation.getRka(), is(equalTo(LocalisedString.of(rka.getNameFinnish(), rka.getNameSwedish()))));
                assertThat(deporation.isOnReindeerArea(), is(false));
            });
        });
    }

    @Theory
    public void testGetApplicationInfo_research(final int speciesCode) {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final List<LargeCarnivorePermitInfoDTO> researchList =
                        feature.getPermitInfo(RESEARCH, speciesCode, 2020);
                assertThat(researchList, hasSize(1));

                final LargeCarnivorePermitInfoDTO research = researchList.get(0);

                final HarvestPermitApplication expectedApplication = researchApplications.get(speciesCode);
                assertThat(research.getApplicationNumber(), is(equalTo(expectedApplication.getApplicationNumber())));

                final HarvestPermit expectedPermit = researchPermits.get(speciesCode);
                assertThat(research.getPermitNumber(), is(equalTo(expectedPermit.getPermitNumber())));

                final PermitDecision expectedDecision = researchDecisions.get(speciesCode);
                assertThat(research.getDecisionType(), is(equalTo(expectedDecision.getDecisionType())));
                assertThat(research.getDecisionTime(), is(equalTo(expectedDecision.getLockedDate())));

                final HarvestPermitSpeciesAmount expectedPermitSpa =
                        researchPermitSpeciesAmounts.get(speciesCode);
                assertThat(research.getBeginDate(), is(equalTo(expectedPermitSpa.getBeginDate())));
                assertThat(research.getEndDate(), is(equalTo(expectedPermitSpa.getEndDate())));
                assertThat(research.getBeginDate2(), is(equalTo(expectedPermitSpa.getBeginDate2())));
                assertThat(research.getEndDate2(), is(equalTo(expectedPermitSpa.getEndDate2())));

                final HarvestPermitApplicationSpeciesAmount expectedApplicationSpa =
                        researchSpeciesAmounts.get(speciesCode);
                assertThat(research.getApplied(), is(equalTo(expectedApplicationSpa.getSpecimenAmount())));

                assertThat(research.getGranted(), is(equalTo(expectedPermitSpa.getSpecimenAmount())));

                assertThat(research.getRhy(), is(equalTo(LocalisedString.of(rhy.getNameFinnish(), rhy.getNameSwedish()))));

                assertThat(research.getRka(), is(equalTo(LocalisedString.of(rka.getNameFinnish(), rka.getNameSwedish()))));
                assertThat(research.isOnReindeerArea(), is(false));
            });
        });
    }

    @Test
    public void testGetSrva() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final Map<Integer, List<LargeCarnivoreSrvaEventDTO>> events = feature.getSrva(2020);

                REPORT_SPECIES.forEach(speciesCode -> {
                    final List<LargeCarnivoreSrvaEventDTO> eventList = events.get(speciesCode);
                    assertThat(eventList, hasSize(1));

                    final LargeCarnivoreSrvaEventDTO event = eventList.get(0);
                    final SrvaEvent expectedEvent = srvaEvents.get(speciesCode);
                    final SrvaEventDTO receivedEvent = event.getEvent();
                    assertThat(receivedEvent.getId(), is(equalTo(expectedEvent.getId())));
                    assertThat(receivedEvent.getEventType(), is(equalTo(expectedEvent.getEventType())));
                });
            });
        });
    }

    @Test
    public void testGetOtherwiseDeceased() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final Map<Integer, List<LargeCarnivoreOtherwiseDeceasedDTO>> otherwiseDeceasedList = feature.getOtherwiseDeceased(2020);

                REPORT_SPECIES.forEach(speciesCode -> {
                    final List<LargeCarnivoreOtherwiseDeceasedDTO> deceasedDTOList = otherwiseDeceasedList.get(speciesCode);
                    assertThat(deceasedDTOList, hasSize(1));

                    final OtherwiseDeceasedDTO deceasedDTO = deceasedDTOList.get(0).getOtherwiseDeceased();
                    final OtherwiseDeceased expectedDeceased = otherwiseDeceasedMap.get(speciesCode);
                    assertThat(deceasedDTO.getId(), is(equalTo(expectedDeceased.getId())));
                    assertThat(deceasedDTO.getCause(), is(equalTo(expectedDeceased.getCause())));
                    assertThat(deceasedDTO.getSource(), is(equalTo(expectedDeceased.getSource())));
                });
            });
        });
    }

    @Test
    public void testNumberOfQueriesIsConstant() {
        IntStream.range(0, 100).forEach(i -> createDerogations());
        IntStream.range(0, 100).forEach(i -> createStockMgmt());
        IntStream.range(0, 100).forEach(i -> createDeportations());
        IntStream.range(0, 100).forEach(i -> createResearch());
        IntStream.range(0, 100).forEach(i -> createSrva());
        IntStream.range(0, 100).forEach(i -> createOtherwiseDeceased());
        assertMaxQueryCount(193, () -> feature.export(2020));
    }
}
