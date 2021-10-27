package fi.riista.feature.organization.rhy.annualstats.export;

import com.google.common.collect.ImmutableMap;
import fi.riista.config.LocalizationConfig;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.AnnualShootingTestStatistics;
import fi.riista.feature.organization.rhy.annualstats.CommunicationStatistics;
import fi.riista.feature.organization.rhy.annualstats.GameDamageStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HuntingControlStatistics;
import fi.riista.feature.organization.rhy.annualstats.JHTTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.MetsahallitusStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHuntingRelatedStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherPublicAdminStatistics;
import fi.riista.feature.organization.rhy.annualstats.PublicEventStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyBasicInfo;
import fi.riista.feature.organization.rhy.annualstats.ShootingRangeStatistics;
import fi.riista.feature.organization.rhy.annualstats.SrvaEventStatistics;
import fi.riista.feature.organization.rhy.annualstats.SrvaSpeciesCountStatistics;
import fi.riista.feature.organization.rhy.annualstats.YouthTrainingStatistics;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.test.rules.SpringRuleConfigurer;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_UNKNOWN;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.BASIC_INFO;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.COMMUNICATION;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.GAME_DAMAGE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAMS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAM_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_TRAINING_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTING_CONTROL;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.JHT_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE_2018;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.MAIN_SUMMARY_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.METSAHALLITUS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.NON_SUBSIDIZABLE_HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.NON_SUBSIDIZABLE_JHT_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.NON_SUBSIDIZABLE_OTHER_HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.NON_SUBSIDIZABLE_TRAINING_SUMMARY;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.NON_SUBSIDIZABLE_YOUTH_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTER_TRAINING_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTING_RELATED;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTING_RELATED_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_PUBLIC_ADMIN_TASKS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_SUMMARY;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_TRAINING_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.PUBLIC_EVENTS_2018;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_TESTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_DEPORTATIONS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_INJURIES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SRVA_TOTALS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SUBSIDY_SUMMARY;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SUBSIDY_SUMMARY_2018;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.TRAINING_SUMMARY;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.TRAINING_SUMMARY_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.YOUTH_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.*;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_EVENTS;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@ContextConfiguration
public class AnnualStatisticGroupsFactoryTest extends SpringRuleConfigurer implements DefaultEntitySupplierProvider {

    private static final ImmutableMap<Integer, LocalisedString> SPECIES_CODE_TO_NAME = ImmutableMap
            .<Integer, LocalisedString>builder()
            .put(OFFICIAL_CODE_MOOSE, LocalisedString.of("Hirvi", "Älg"))
            .put(OFFICIAL_CODE_WHITE_TAILED_DEER, LocalisedString.of("Valkohäntäpeura", "Vitsvanshjort"))
            .put(OFFICIAL_CODE_ROE_DEER, LocalisedString.of("Metsäkauris", "Rådjur"))
            .put(OFFICIAL_CODE_WILD_FOREST_REINDEER, LocalisedString.of("Metsäpeura", "Skogsren"))
            .put(OFFICIAL_CODE_FALLOW_DEER, LocalisedString.of("Kuusipeura", "Dovhjort"))
            .put(OFFICIAL_CODE_WILD_BOAR, LocalisedString.of("Villisika", "Vildsvin"))
            .put(OFFICIAL_CODE_LYNX, LocalisedString.of("Ilves", "Lodjur"))
            .put(OFFICIAL_CODE_BEAR, LocalisedString.of("Karhu", "Björn"))
            .put(OFFICIAL_CODE_WOLF, LocalisedString.of("Susi", "Varg"))
            .put(OFFICIAL_CODE_WOLVERINE, LocalisedString.of("Ahma", "Järv"))
            .put(OFFICIAL_CODE_UNKNOWN, LocalisedString.of("Muu", "Övrig"))
            .build();

    @Configuration
    @Import(LocalizationConfig.class)
    static class Context {

        @Resource
        private MessageSource messageSource;

        @Bean
        public EnumLocaliser enumLocaliser() {
            return new EnumLocaliser(messageSource);
        }
    }

    @Resource
    private EnumLocaliser localiser;

    @Test
    public void testExtractedGroupsForYear2017() {
        final List<AnnualStatisticGroup> expectedGroups = asList(
                BASIC_INFO,
                MAIN_SUMMARY_2017,
                OTHER_SUMMARY,
                HUNTER_EXAMS,
                SHOOTING_TESTS,
                GAME_DAMAGE,
                HUNTING_CONTROL,
                OTHER_PUBLIC_ADMIN_TASKS,
                SRVA_TOTALS,
                SRVA_ACCIDENTS,
                SRVA_DEPORTATIONS,
                SRVA_INJURIES,
                TRAINING_SUMMARY_2017,
                HUNTER_EXAM_TRAINING,
                JHT_TRAINING,
                HUNTER_TRAINING_2017,
                OTHER_HUNTER_TRAINING_2017,
                OTHER_TRAINING_2017,
                OTHER_HUNTING_RELATED_2017,
                COMMUNICATION,
                SHOOTING_RANGES,
                LUKE_2017,
                METSAHALLITUS);

        assertEquals(expectedGroups, extractGroups(2017));
    }

    @Test
    public void testExtractedGroupsForYear2018() {
        final List<AnnualStatisticGroup> expectedGroups = asList(
                BASIC_INFO,
                SUBSIDY_SUMMARY_2018,
                OTHER_SUMMARY,
                HUNTER_EXAMS,
                SHOOTING_TESTS,
                GAME_DAMAGE,
                HUNTING_CONTROL,
                OTHER_PUBLIC_ADMIN_TASKS,
                SRVA_TOTALS,
                SRVA_ACCIDENTS,
                SRVA_DEPORTATIONS,
                SRVA_INJURIES,
                TRAINING_SUMMARY,
                HUNTER_EXAM_TRAINING,
                JHT_TRAINING,
                HUNTER_TRAINING,
                YOUTH_TRAINING,
                OTHER_HUNTER_TRAINING,
                PUBLIC_EVENTS_2018,
                OTHER_HUNTING_RELATED,
                COMMUNICATION,
                SHOOTING_RANGES,
                LUKE_2018,
                METSAHALLITUS);

        assertEquals(expectedGroups, extractGroups(2018));
    }

    @Test
    public void testExtractedGroupsForYear2019() {
        final List<AnnualStatisticGroup> expectedGroups = asList(
                BASIC_INFO,
                SUBSIDY_SUMMARY_2018,
                OTHER_SUMMARY,
                HUNTER_EXAMS,
                SHOOTING_TESTS,
                GAME_DAMAGE,
                HUNTING_CONTROL,
                OTHER_PUBLIC_ADMIN_TASKS,
                SRVA_TOTALS,
                SRVA_ACCIDENTS,
                SRVA_DEPORTATIONS,
                SRVA_INJURIES,
                TRAINING_SUMMARY,
                HUNTER_EXAM_TRAINING,
                JHT_TRAINING,
                HUNTER_TRAINING,
                YOUTH_TRAINING,
                OTHER_HUNTER_TRAINING,
                OTHER_HUNTING_RELATED,
                COMMUNICATION,
                SHOOTING_RANGES,
                LUKE_2018,
                METSAHALLITUS,
                AnnualStatisticGroup.PUBLIC_EVENTS);

        assertEquals(expectedGroups, extractGroups(2019));
    }

    @Test
    public void testExtractedGroupsForYearAfter2019() {
        final List<AnnualStatisticGroup> expectedGroups = asList(
                BASIC_INFO,
                SUBSIDY_SUMMARY,
                OTHER_SUMMARY,
                HUNTER_EXAMS,
                SHOOTING_TESTS,
                GAME_DAMAGE,
                HUNTING_CONTROL,
                OTHER_PUBLIC_ADMIN_TASKS,
                SRVA_TOTALS,
                SRVA_ACCIDENTS,
                SRVA_DEPORTATIONS,
                SRVA_INJURIES,
                TRAINING_SUMMARY,
                HUNTER_EXAM_TRAINING,
                JHT_TRAINING,
                HUNTER_TRAINING,
                YOUTH_TRAINING,
                OTHER_HUNTER_TRAINING,
                NON_SUBSIDIZABLE_TRAINING_SUMMARY,
                NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING,
                NON_SUBSIDIZABLE_JHT_TRAINING,
                NON_SUBSIDIZABLE_HUNTER_TRAINING,
                NON_SUBSIDIZABLE_YOUTH_TRAINING,
                NON_SUBSIDIZABLE_OTHER_HUNTER_TRAINING,
                OTHER_HUNTING_RELATED,
                COMMUNICATION,
                SHOOTING_RANGES,
                LUKE,
                METSAHALLITUS,
                AnnualStatisticGroup.PUBLIC_EVENTS);

        assertEquals(expectedGroups, extractGroups(2020));
    }

    @Test
    public void testExtractedItemsForYear2017() {
        final Riistanhoitoyhdistys rhy = getEntitySupplier().newRiistanhoitoyhdistys();
        final RhyAnnualStatistics stats = getEntitySupplier().newRhyAnnualStatistics(rhy, 2017);

        final List<Tuple2<LocalisedString, Either<Number, String>>> extractedItems = extractItems(stats);

        assertResult(getExpectedItems(stats), extractedItems);
        assertEquals(156, extractedItems.size());
    }

    @Test
    public void testExtractedItemsForYear2018() {
        final Riistanhoitoyhdistys rhy = getEntitySupplier().newRiistanhoitoyhdistys();
        final RhyAnnualStatistics stats = getEntitySupplier().newRhyAnnualStatistics(rhy, 2018);

        final List<Tuple2<LocalisedString, Either<Number, String>>> extractedItems = extractItems(stats);

        assertResult(getExpectedItems(stats), extractedItems);
        assertEquals(158, extractedItems.size());
    }

    @Test
    public void testExtractedItemsForYear2019() {
        final Riistanhoitoyhdistys rhy = getEntitySupplier().newRiistanhoitoyhdistys();
        final RhyAnnualStatistics stats = getEntitySupplier().newRhyAnnualStatistics(rhy, 2019);

        final List<Tuple2<LocalisedString, Either<Number, String>>> extractedItems = extractItems(stats);

        assertResult(getExpectedItems(stats), extractedItems);
        assertEquals(158, extractedItems.size());
    }

    @Test
    public void testExtractedItemsForYearAfter2019() {
        final Riistanhoitoyhdistys rhy = getEntitySupplier().newRiistanhoitoyhdistys();
        final RhyAnnualStatistics stats = getEntitySupplier().newRhyAnnualStatistics(rhy, 2020);

        final List<Tuple2<LocalisedString, Either<Number, String>>> extractedItems = extractItems(stats);

        assertResult(getExpectedItems(stats), extractedItems);
        assertEquals(207, extractedItems.size());
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> getExpectedItems(final RhyAnnualStatistics stats) {
        final int year = stats.getYear();

        final Stream.Builder<List<Tuple2<LocalisedString, Either<Number, String>>>> builder = Stream.builder();
        builder.add(listExpectedBasicInfoItems(stats.getOrCreateBasicInfo()))
                .add(listExpectedSubsidySummaryItems(stats))
                .add(listExpectedOtherSummaryItems(stats))
                .add(listExpectedHunterExamItems(stats.getOrCreateHunterExams()))
                .add(listExpectedShootingTestItems(stats.getOrCreateShootingTests()))
                .add(listExpectedGameDamageItems(stats.getOrCreateGameDamage()))
                .add(listExpectedHuntingControlItems(stats.getOrCreateHuntingControl()))
                .add(listExpectedOtherPublicAdminItems(stats.getOrCreateOtherPublicAdmin()))
                .add(listExpectedSrvaTotalItems(stats.getOrCreateSrva()))
                .add(listExpectedSrvaAccidentItems(stats.getOrCreateSrva().getAccident()))
                .add(listExpectedSrvaDeportationItems(stats.getOrCreateSrva().getDeportation()))
                .add(listExpectedSrvaInjuryItems(stats.getOrCreateSrva().getInjury()))
                .add(listExpectedTrainingSummaryItems(stats))
                .add(listExpectedHunterExamTrainingItems(stats.getOrCreateHunterExamTraining()))
                .add(listExpectedJhtTrainingItems(stats.getOrCreateJhtTraining()))
                .add(listExpectedHunterTrainingItems(stats.getOrCreateHunterTraining()))
                .add(listExpectedYouthTrainingItems(stats.getOrCreateYouthTraining()))
                .add(listExpectedOtherHunterTrainingItems(stats.getOrCreateOtherHunterTraining(), year));

        if (year > 2019) {
            builder.add(listExpectedNonSubsidizableTrainingSummaryItems(stats))
                    .add(listExpectedNonSubsidizableHunterExamTrainingItems(stats.getOrCreateHunterExamTraining()))
                    .add(listExpectedNonSubsidizableJhtTrainingItems(stats.getOrCreateJhtTraining()))
                    .add(listExpectedNonSubsidizableHunterTrainingItems(stats.getOrCreateHunterTraining()))
                    .add(listExpectedNonSubsidizableYouthTrainingItems(stats.getOrCreateYouthTraining()))
                    .add(listExpectedNonSubsidizableOtherHunterTrainingItems(stats.getOrCreateOtherHunterTraining(), year));
        }

        if (year < 2019) {
            builder.add(listExpectedPublicEventItems(stats.getOrCreatePublicEvents(), year));
        }

        builder.add(listExpectedOtherHuntingRelatedItems(stats.getOrCreateOtherHuntingRelated(), year))
                .add(listExpectedCommunicationItems(stats.getOrCreateCommunication()))
                .add(listExpectedShootingRangeItems(stats.getOrCreateShootingRanges()))
                .add(listExpectedLukeItems(stats.getOrCreateLuke(), year))
                .add(listExpectedMetsahallitusItems(stats.getOrCreateMetsahallitus()));

        if (year >= 2019) {
            builder.add(listExpectedPublicEventItems(stats.getOrCreatePublicEvents(), year));
        }

        return builder.build().flatMap(List::stream).collect(toList());
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> extractItems(final RhyAnnualStatistics statistics) {
        final Riistanhoitoyhdistys rhy = statistics.getRhy();
        final OrganisationNameDTO rhyDTO = new OrganisationNameDTO();
        rhyDTO.setId(51L);
        rhyDTO.setOfficialCode(rhy.getOfficialCode());
        rhyDTO.setNameFI(rhy.getNameFinnish());
        rhyDTO.setNameSV(rhy.getNameSwedish());

        final Organisation rka = rhy.getRiistakeskuksenAlue();
        final OrganisationNameDTO rkaDTO = new OrganisationNameDTO();
        rkaDTO.setId(50L);
        rkaDTO.setOfficialCode(rka.getOfficialCode());
        rkaDTO.setNameFI(rka.getNameFinnish());
        rkaDTO.setNameSV(rka.getNameSwedish());

        final AnnualStatisticsExportDTO exportedStats = AnnualStatisticsExportDTO.create(rhyDTO, rkaDTO, statistics);

        return extractGroups(statistics.getYear())
                .stream()
                .flatMap(group -> group.getItems().stream())
                .map(item -> {
                    final LocalisedString title = localiser.getLocalisedString(item);
                    final Either<Number, String> value = item.extractValue(exportedStats);

                    return Tuple.of(title, value);
                })
                .collect(toList());
    }

    private static List<AnnualStatisticGroup> extractGroups(final int year) {
        return AnnualStatisticGroupsFactory.getAllGroups(year);
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedBasicInfoItems(final RhyBasicInfo basicInfo) {
        return asList(
                text(IBAN, basicInfo.getIbanAsFormattedString()),
                number(OPERATIONAL_LAND_AREA_SIZE, basicInfo.getOperationalLandAreaSize()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedSubsidySummaryItems(final RhyAnnualStatistics stats) {
        final int year = stats.getYear();

        final RhyBasicInfo basicInfo = stats.getOrCreateBasicInfo();
        final HuntingControlStatistics huntingControl = stats.getOrCreateHuntingControl();
        final HunterExamTrainingStatistics hunterExamTraining = stats.getOrCreateHunterExamTraining();
        final OtherHuntingRelatedStatistics ohr = stats.getOrCreateOtherHuntingRelated();
        final MetsahallitusStatistics mh = stats.getOrCreateMetsahallitus();

        if (year < 2018) {
            return asList(
                    number(RHY_MEMBERS, basicInfo.getRhyMembers()),
                    number(HUNTER_EXAM_TRAINING_EVENTS, hunterExamTraining.getHunterExamTrainingEvents()),
                    number(HUNTER_TRAINING_EVENTS_2017, 67 + 69 + 71 + 73 + 75 + 77 + 79),
                    number(STUDENT_TRAINING_EVENTS_2017, 81 + 83),
                    number(HUNTING_CONTROL_EVENTS, huntingControl.getHuntingControlEvents()),
                    number(SUM_OF_LUKE_CALCULATIONS_2018, 113 + 114 + 115 + 116),
                    number(HARVEST_PERMIT_APPLICATION_PARTNERS, ohr.getHarvestPermitApplicationPartners()),
                    number(WOLF_TERRITORY_WORKGROUP_LEADS_2017, ohr.getWolfTerritoryWorkgroups()),
                    number(SRVA_ALL_EVENTS, 528),
                    number(SRVA_ALL_MOOSELIKE_EVENTS_2017, 195),
                    number(SRVA_ALL_LARGE_CARNIVORE_EVENTS_2017, 222),
                    number(SRVA_ALL_WILD_BOAR_EVENTS_2017, 6 + 16 + 26),
                    number(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, mh.getSmallGameLicensesSoldByMetsahallitus()));
        }

        if (year < 2020) {
            return asList(
                    number(RHY_MEMBERS, basicInfo.getRhyMembers()),
                    number(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, hunterExamTraining.getHunterExamTrainingEvents()),
                    number(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, 1410),
                    number(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, 81 + 83 + 85),
                    number(HUNTING_CONTROL_EVENTS, huntingControl.getHuntingControlEvents()),
                    number(SUM_OF_LUKE_CALCULATIONS_2018, 113 + 114 + 115 + 116 + 117),
                    number(LUKE_CARNIVORE_CONTACT_PERSONS, stats.getOrCreateLuke().getCarnivoreContactPersons()),
                    number(MOOSELIKE_TAXATION_PLANNING_EVENTS, ohr.getMooselikeTaxationPlanningEvents()),
                    number(WOLF_TERRITORY_WORKGROUPS, ohr.getWolfTerritoryWorkgroups()),
                    number(SRVA_ALL_MOOSELIKE_EVENTS, 195),
                    number(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, mh.getSmallGameLicensesSoldByMetsahallitus()));
        }

        return asList(
                number(RHY_MEMBERS, basicInfo.getRhyMembers()),
                number(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, hunterExamTraining.getHunterExamTrainingEvents()),
                number(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, 1410),
                number(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, 81 + 83 + 85),
                number(HUNTING_CONTROL_EVENTS, huntingControl.getHuntingControlEvents()),
                number(SUM_OF_LUKE_CALCULATIONS, 113 + 114 + 115 + 116 + 117 + 119),
                number(TOTAL_LUKE_CARNIVORE_PERSONS, stats.getOrCreateLuke().sumOfCarnivorePersons()),
                number(MOOSELIKE_TAXATION_PLANNING_EVENTS, ohr.getMooselikeTaxationPlanningEvents()),
                number(WOLF_TERRITORY_WORKGROUPS, ohr.getWolfTerritoryWorkgroups()),
                number(SRVA_ALL_MOOSELIKE_EVENTS, 195),
                number(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, mh.getSmallGameLicensesSoldByMetsahallitus()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedOtherSummaryItems(final RhyAnnualStatistics stats) {
        final AnnualShootingTestStatistics shootingTests = stats.getOrCreateShootingTests();
        final Integer firearmEvents = shootingTests.getFirearmTestEvents();
        final Integer bowEvents = shootingTests.getBowTestEvents();

        final GameDamageStatistics gameDamage = stats.getOrCreateGameDamage();
        final Integer mooselikeDamageLocations = gameDamage.getMooselikeDamageInspectionLocations();
        final Integer carnivoreDamageLocations = gameDamage.getLargeCarnivoreDamageInspectionLocations();

        final OtherPublicAdminStatistics opa = stats.getOrCreateOtherPublicAdmin();

        return asList(
                number(HUNTER_EXAM_EVENTS, stats.getOrCreateHunterExams().getHunterExamEvents()),
                number(ALL_SHOOTING_TEST_EVENTS, nullableIntSum(firearmEvents, bowEvents)),
                number(HUNTING_CONTROL_EVENTS, stats.getOrCreateHuntingControl().getHuntingControlEvents()),
                number(GAME_DAMAGE_LOCATIONS_TOTAL, nullableIntSum(mooselikeDamageLocations, carnivoreDamageLocations)),
                number(GRANTED_RECREATIONAL_SHOOTING_CERTIFICATES, opa.getGrantedRecreationalShootingCertificates()),
                number(MUTUAL_ACK_SHOOTING_CERTIFICATES, opa.getMutualAckShootingCertificates()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedHunterExamItems(final HunterExamStatistics stats) {
        final Integer passedExams = stats.getPassedHunterExams();
        final Integer failedExams = stats.getFailedHunterExams();

        return asList(
                number(HUNTER_EXAM_EVENTS, stats.getHunterExamEvents()),
                number(ALL_HUNTER_EXAM_ATTEMPTS, nullableIntSum(passedExams, failedExams)),
                number(PASSED_HUNTER_EXAM_ATTEMPTS, passedExams),
                number(FAILED_HUNTER_EXAM_ATTEMPTS, failedExams),
                number(ASSIGNED_HUNTER_EXAM_OFFICIALS, stats.getHunterExamOfficials()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedShootingTestItems(final AnnualShootingTestStatistics stats) {
        final Integer firearmEvents = stats.getFirearmTestEvents();
        final Integer bowEvents = stats.getBowTestEvents();

        return asList(
                number(ALL_SHOOTING_TEST_EVENTS, nullableIntSum(firearmEvents, bowEvents)),
                number(FIREARM_TEST_EVENTS, firearmEvents),
                number(BOW_TEST_EVENTS, bowEvents),
                number(ALL_SHOOTING_TEST_ATTEMPTS, 40 + 42 + 44 + 46),
                number(ALL_ROE_DEER_ATTEMPTS, stats.getAllRoeDeerAttempts()),
                number(QUALIFIED_ROE_DEER_ATTEMPTS, stats.getQualifiedRoeDeerAttempts()),
                number(ALL_MOOSE_ATTEMPTS, stats.getAllMooseAttempts()),
                number(QUALIFIED_MOOSE_ATTEMPTS, stats.getQualifiedMooseAttempts()),
                number(ALL_BEAR_ATTEMPTS, stats.getAllBearAttempts()),
                number(QUALIFIED_BEAR_ATTEMPTS, stats.getQualifiedBearAttempts()),
                number(ALL_BOW_ATTEMPTS, stats.getAllBowAttempts()),
                number(QUALIFIED_BOW_ATTEMPTS, stats.getQualifiedBowAttempts()),
                number(SHOOTING_TEST_OFFICIALS, stats.getShootingTestOfficials()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedGameDamageItems(final GameDamageStatistics stats) {
        return asList(
                number(GAME_DAMAGE_LOCATIONS_MOOSELIKE, stats.getMooselikeDamageInspectionLocations()),
                number(GAME_DAMAGE_LOCATIONS_LARGE_CARNIVORE, stats.getLargeCarnivoreDamageInspectionLocations()),
                number(GAME_DAMAGE_LOCATIONS_TOTAL, 48 + 49),
                number(GAME_DAMAGE_INSPECTORS, stats.getGameDamageInspectors()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedHuntingControlItems(final HuntingControlStatistics stats) {
        return asList(
                number(HUNTING_CONTROL_EVENTS, stats.getHuntingControlEvents()),
                number(HUNTING_CONTROL_CUSTOMERS, stats.getHuntingControlCustomers()),
                number(HUNTING_CONTROL_PROOF_ORDERS, stats.getProofOrders()),
                number(HUNTING_CONTROLLERS, stats.getHuntingControllers()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedOtherPublicAdminItems(final OtherPublicAdminStatistics stats) {
        return asList(
                number(GRANTED_RECREATIONAL_SHOOTING_CERTIFICATES, stats.getGrantedRecreationalShootingCertificates()),
                number(MUTUAL_ACK_SHOOTING_CERTIFICATES, stats.getMutualAckShootingCertificates()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedSrvaTotalItems(final SrvaEventStatistics srva) {
        return asList(
                number(SRVA_ALL_EVENTS, 528),
                number(SRVA_TOTAL_WORK_HOURS, srva.getTotalSrvaWorkHours()),
                number(SRVA_TOTAL_PARTICIPANTS, srva.getSrvaParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedSrvaAccidentItems(final SrvaSpeciesCountStatistics accident) {
        final int mooselikes = 1 + 2 + 3 + 4 + 5;
        final int largeCarnivores = 7 + 8 + 9 + 10;
        final int wildBoars = accident.getWildBoars();
        final int otherSpecies = accident.getOtherSpecies();

        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>(15);
        list.add(number(SRVA_ALL_ACCIDENTS, mooselikes + largeCarnivores + wildBoars + otherSpecies));
        list.add(number(SRVA_TRAFFIC_ACCIDENTS, mooselikes));
        list.add(number(SRVA_RAILWAY_ACCIDENTS, largeCarnivores));
        list.add(number(SRVA_OTHER_ACCIDENTS, wildBoars + otherSpecies));
        list.addAll(listSrvaSpeciesCounts(accident));
        return list;
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedSrvaDeportationItems(final SrvaSpeciesCountStatistics deportation) {
        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>(12);
        list.add(number(SRVA_ALL_DEPORTATIONS, 11 + 12 + 13 + 14 + 15 + 16 + 17 + 18 + 19 + 20 + 21));
        list.addAll(listSrvaSpeciesCounts(deportation));
        return list;
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedSrvaInjuryItems(final SrvaSpeciesCountStatistics injury) {
        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>(12);
        list.add(number(SRVA_ALL_INJURIES, 21 + 22 + 23 + 24 + 25 + 26 + 27 + 28 + 29 + 30 + 31));
        list.addAll(listSrvaSpeciesCounts(injury));
        return list;
    }

    private static List<Tuple2<LocalisedString, Either<Number, String>>> listSrvaSpeciesCounts(final SrvaSpeciesCountStatistics speciesCounts) {
        return asList(
                speciesCount(OFFICIAL_CODE_MOOSE, speciesCounts.getMooses()),
                speciesCount(OFFICIAL_CODE_WHITE_TAILED_DEER, speciesCounts.getWhiteTailedDeers()),
                speciesCount(OFFICIAL_CODE_ROE_DEER, speciesCounts.getRoeDeers()),
                speciesCount(OFFICIAL_CODE_WILD_FOREST_REINDEER, speciesCounts.getWildForestReindeers()),
                speciesCount(OFFICIAL_CODE_FALLOW_DEER, speciesCounts.getFallowDeers()),
                speciesCount(OFFICIAL_CODE_WILD_BOAR, speciesCounts.getWildBoars()),
                speciesCount(OFFICIAL_CODE_LYNX, speciesCounts.getLynxes()),
                speciesCount(OFFICIAL_CODE_BEAR, speciesCounts.getBears()),
                speciesCount(OFFICIAL_CODE_WOLF, speciesCounts.getWolves()),
                speciesCount(OFFICIAL_CODE_WOLVERINE, speciesCounts.getWolverines()),
                speciesCount(OFFICIAL_CODE_UNKNOWN, speciesCounts.getOtherSpecies()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedTrainingSummaryItems(final RhyAnnualStatistics stats) {
        final int year = stats.getYear();

        int allTrainingEvents = 1716;
        int allTrainingParticipants = 1738;

        if (year < 2018) {
            final PublicEventStatistics publicEvents = stats.getOrCreatePublicEvents();
            allTrainingEvents += F.coalesceAsInt(publicEvents.getPublicEvents(), 0);
            allTrainingParticipants += F.coalesceAsInt(publicEvents.getPublicEventParticipants(), 0);

            return asList(
                    number(ALL_TRAINING_EVENTS_2017, allTrainingEvents),
                    number(ALL_TRAINING_PARTICIPANTS_2017, allTrainingParticipants));
        }

        return asList(
                number(SUBSIDIZABLE_TRAINING_EVENTS, allTrainingEvents),
                number(SUBSIDIZABLE_TRAINING_PARTICIPANTS, allTrainingParticipants));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedNonSubsidizableTrainingSummaryItems(final RhyAnnualStatistics stats) {
        final int year = stats.getYear();

        //Twice the number of subsidizable events
        int allTrainingEvents = 2 * 1716;
        int allTrainingParticipants = 2 * 1738;

        if (year < 2020) {
            return emptyList();
        }

        return asList(
                number(NON_SUBSIDIZABLE_TRAINING_EVENTS, allTrainingEvents),
                number(NON_SUBSIDIZABLE_TRAINING_PARTICIPANTS, allTrainingParticipants));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedHunterExamTrainingItems(final HunterExamTrainingStatistics stats) {
        return asList(
                number(HUNTER_EXAM_TRAINING_EVENTS, stats.getHunterExamTrainingEvents()),
                number(HUNTER_EXAM_TRAINING_PARTICIPANTS, stats.getHunterExamTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedNonSubsidizableHunterExamTrainingItems(final HunterExamTrainingStatistics stats) {
        return asList(
                number(NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, stats.getNonSubsidizableHunterExamTrainingEvents()),
                number(NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING_PARTICIPANTS, stats.getNonSubsidizableHunterExamTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedJhtTrainingItems(final JHTTrainingStatistics stats) {
        return asList(
                number(SHOOTING_TEST_TRAINING_EVENTS, stats.getShootingTestTrainingEvents()),
                number(SHOOTING_TEST_TRAINING_PARTICIPANTS, stats.getShootingTestTrainingParticipants()),
                number(HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS, stats.getHunterExamOfficialTrainingEvents()),
                number(HUNTER_EXAM_OFFICIAL_TRAINING_PARTICIPANTS, stats.getHunterExamOfficialTrainingParticipants()),
                number(GAME_DAMAGE_TRAINING_EVENTS, stats.getGameDamageTrainingEvents()),
                number(GAME_DAMAGE_TRAINING_PARTICIPANTS, stats.getGameDamageTrainingParticipants()),
                number(HUNTING_CONTROL_TRAINING_EVENTS, stats.getHuntingControlTrainingEvents()),
                number(HUNTING_CONTROL_TRAINING_PARTICIPANTS, stats.getHuntingControlTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedNonSubsidizableJhtTrainingItems(final JHTTrainingStatistics stats) {
        return asList(
                number(NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_EVENTS, stats.getNonSubsidizableShootingTestTrainingEvents()),
                number(NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_PARTICIPANTS, stats.getNonSubsidizableShootingTestTrainingParticipants()),
                number(NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS, stats.getNonSubsidizableHunterExamOfficialTrainingEvents()),
                number(NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_PARTICIPANTS, stats.getNonSubsidizableHunterExamOfficialTrainingParticipants()),
                number(NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_EVENTS, stats.getNonSubsidizableGameDamageTrainingEvents()),
                number(NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_PARTICIPANTS, stats.getNonSubsidizableGameDamageTrainingParticipants()),
                number(NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_EVENTS, stats.getNonSubsidizableHuntingControlTrainingEvents()),
                number(NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_PARTICIPANTS, stats.getNonSubsidizableHuntingControlTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedHunterTrainingItems(final HunterTrainingStatistics stats) {
        return asList(
                number(MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS, stats.getMooselikeHuntingLeaderTrainingEvents()),
                number(MOOSELIKE_HUNTING_LEADER_TRAINING_PARTICIPANTS, stats.getMooselikeHuntingLeaderTrainingParticipants()),
                number(CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS, stats.getCarnivoreHuntingLeaderTrainingEvents()),
                number(CARNIVORE_HUNTING_LEADER_TRAINING_PARTICIPANTS, stats.getCarnivoreHuntingLeaderTrainingParticipants()),
                number(MOOSELIKE_HUNTING_TRAINING_EVENTS, stats.getMooselikeHuntingTrainingEvents()),
                number(MOOSELIKE_HUNTING_TRAINING_PARTICIPANTS, stats.getMooselikeHuntingTrainingParticipants()),
                number(CARNIVORE_HUNTING_TRAINING_EVENTS, stats.getCarnivoreHuntingTrainingEvents()),
                number(CARNIVORE_HUNTING_TRAINING_PARTICIPANTS, stats.getCarnivoreHuntingTrainingParticipants()),
                number(SRVA_TRAINING_EVENTS, stats.getSrvaTrainingEvents()),
                number(SRVA_TRAINING_PARTICIPANTS, stats.getSrvaTrainingParticipants()),
                number(CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS, stats.getCarnivoreContactPersonTrainingEvents()),
                number(CARNIVORE_CONTACT_PERSON_TRAINING_PARTICIPANTS, stats.getCarnivoreContactPersonTrainingParticipants()),
                number(ACCIDENT_PREVENTION_TRAINING_EVENTS, stats.getAccidentPreventionTrainingEvents()),
                number(ACCIDENT_PREVENTION_TRAINING_PARTICIPANTS, stats.getAccidentPreventionTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedNonSubsidizableHunterTrainingItems(final HunterTrainingStatistics stats) {
        return asList(
                number(NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS, stats.getNonSubsidizableMooselikeHuntingLeaderTrainingEvents()),
                number(NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_PARTICIPANTS, stats.getNonSubsidizableMooselikeHuntingLeaderTrainingParticipants()),
                number(NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS, stats.getNonSubsidizableCarnivoreHuntingLeaderTrainingEvents()),
                number(NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_PARTICIPANTS, stats.getNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants()),
                number(NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_EVENTS, stats.getNonSubsidizableMooselikeHuntingTrainingEvents()),
                number(NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_PARTICIPANTS, stats.getNonSubsidizableMooselikeHuntingTrainingParticipants()),
                number(NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_EVENTS, stats.getNonSubsidizableCarnivoreHuntingTrainingEvents()),
                number(NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS, stats.getNonSubsidizableCarnivoreHuntingTrainingParticipants()),
                number(NON_SUBSIDIZABLE_SRVA_TRAINING_EVENTS, stats.getNonSubsidizableSrvaTrainingEvents()),
                number(NON_SUBSIDIZABLE_SRVA_TRAINING_PARTICIPANTS, stats.getNonSubsidizableSrvaTrainingParticipants()),
                number(NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS, stats.getNonSubsidizableCarnivoreContactPersonTrainingEvents()),
                number(NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_PARTICIPANTS, stats.getNonSubsidizableCarnivoreContactPersonTrainingParticipants()),
                number(NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_EVENTS, stats.getNonSubsidizableAccidentPreventionTrainingEvents()),
                number(NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_PARTICIPANTS, stats.getNonSubsidizableAccidentPreventionTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedYouthTrainingItems(final YouthTrainingStatistics stats) {
        return asList(
                number(SCHOOL_TRAINING_EVENTS, stats.getSchoolTrainingEvents()),
                number(SCHOOL_TRAINING_PARTICIPANTS, stats.getSchoolTrainingParticipants()),
                number(COLLEGE_TRAINING_EVENTS, stats.getCollegeTrainingEvents()),
                number(COLLEGE_TRAINING_PARTICIPANTS, stats.getCollegeTrainingParticipants()),
                number(OTHER_YOUTH_TARGETED_TRAINING_EVENTS, stats.getOtherYouthTargetedTrainingEvents()),
                number(OTHER_YOUTH_TARGETED_TRAINING_PARTICIPANTS, stats.getOtherYouthTargetedTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedNonSubsidizableYouthTrainingItems(final YouthTrainingStatistics stats) {
        return asList(
                number(NON_SUBSIDIZABLE_SCHOOL_TRAINING_EVENTS, stats.getNonSubsidizableSchoolTrainingEvents()),
                number(NON_SUBSIDIZABLE_SCHOOL_TRAINING_PARTICIPANTS, stats.getNonSubsidizableSchoolTrainingParticipants()),
                number(NON_SUBSIDIZABLE_COLLEGE_TRAINING_EVENTS, stats.getNonSubsidizableCollegeTrainingEvents()),
                number(NON_SUBSIDIZABLE_COLLEGE_TRAINING_PARTICIPANTS, stats.getNonSubsidizableCollegeTrainingParticipants()),
                number(NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_EVENTS, stats.getNonSubsidizableOtherYouthTargetedTrainingEvents()),
                number(NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_PARTICIPANTS, stats.getNonSubsidizableOtherYouthTargetedTrainingParticipants()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedOtherHunterTrainingItems(final OtherHunterTrainingStatistics stats,
                                                                                                       final int year) {

        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>();
        list.add(number(SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS, stats.getSmallCarnivoreHuntingTrainingEvents()));
        list.add(number(SMALL_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS, stats.getSmallCarnivoreHuntingTrainingParticipants()));
        list.add(number(GAME_COUNTING_TRAINING_EVENTS, stats.getGameCountingTrainingEvents()));
        list.add(number(GAME_COUNTING_TRAINING_PARTICIPANTS, stats.getGameCountingTrainingParticipants()));
        list.add(number(GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS, stats.getGamePopulationManagementTrainingEvents()));
        list.add(number(GAME_POPULATION_MANAGEMENT_TRAINING_PARTICIPANTS, stats.getGamePopulationManagementTrainingParticipants()));
        list.add(number(GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS, stats.getGameEnvironmentalCareTrainingEvents()));
        list.add(number(GAME_ENVIRONMENTAL_CARE_TRAINING_PARTICIPANTS, stats.getGameEnvironmentalCareTrainingParticipants()));
        list.add(number(OTHER_GAMEKEEPING_TRAINING_EVENTS, stats.getOtherGamekeepingTrainingEvents()));
        list.add(number(OTHER_GAMEKEEPING_TRAINING_PARTICIPANTS, stats.getOtherGamekeepingTrainingParticipants()));

        if (year < 2018) {
            list.add(number(OTHER_SHOOTING_TRAINING_EVENTS_2017, stats.getShootingTrainingEvents()));
            list.add(number(OTHER_SHOOTING_TRAINING_PARTICIPANTS_2017, stats.getShootingTrainingParticipants()));

        } else {
            list.add(number(SHOOTING_TRAINING_EVENTS, stats.getShootingTrainingEvents()));
            list.add(number(SHOOTING_TRAINING_PARTICIPANTS, stats.getShootingTrainingParticipants()));
        }

        list.add(number(TRACKER_TRAINING_EVENTS, stats.getTrackerTrainingEvents()));
        list.add(number(TRACKER_TRAINING_PARTICIPANTS, stats.getTrackerTrainingParticipants()));

        return list;
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedNonSubsidizableOtherHunterTrainingItems(final OtherHunterTrainingStatistics stats,
                                                                                                                      final int year) {

        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>();
        list.add(number(NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS, stats.getNonSubsidizableSmallCarnivoreHuntingTrainingEvents()));
        list.add(number(NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS, stats.getNonSubsidizableSmallCarnivoreHuntingTrainingParticipants()));
        list.add(number(NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_EVENTS, stats.getNonSubsidizableGameCountingTrainingEvents()));
        list.add(number(NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_PARTICIPANTS, stats.getNonSubsidizableGameCountingTrainingParticipants()));
        list.add(number(NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS, stats.getNonSubsidizableGamePopulationManagementTrainingEvents()));
        list.add(number(NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_PARTICIPANTS, stats.getNonSubsidizableGamePopulationManagementTrainingParticipants()));
        list.add(number(NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS, stats.getNonSubsidizableGameEnvironmentalCareTrainingEvents()));
        list.add(number(NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_PARTICIPANTS, stats.getNonSubsidizableGameEnvironmentalCareTrainingParticipants()));
        list.add(number(NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_EVENTS, stats.getNonSubsidizableOtherGamekeepingTrainingEvents()));
        list.add(number(NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_PARTICIPANTS, stats.getNonSubsidizableOtherGamekeepingTrainingParticipants()));

        if (year >= 2018) {
            list.add(number(NON_SUBSIDIZABLE_SHOOTING_TRAINING_EVENTS, stats.getNonSubsidizableShootingTrainingEvents()));
            list.add(number(NON_SUBSIDIZABLE_SHOOTING_TRAINING_PARTICIPANTS, stats.getNonSubsidizableShootingTrainingParticipants()));
        }

        list.add(number(NON_SUBSIDIZABLE_TRACKER_TRAINING_EVENTS, stats.getNonSubsidizableTrackerTrainingEvents()));
        list.add(number(NON_SUBSIDIZABLE_TRACKER_TRAINING_PARTICIPANTS, stats.getNonSubsidizableTrackerTrainingParticipants()));

        return list;
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedPublicEventItems(final PublicEventStatistics stats,
                                                                                               final int year) {

        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>();

        if (year < 2018) {
            list.add(number(OTHER_TRAINING_EVENTS_2017, stats.getPublicEvents()));
            list.add(number(OTHER_TRAINING_PARTICIPANTS_2017, stats.getPublicEventParticipants()));

        } else {
            list.add(number(PUBLIC_EVENTS, stats.getPublicEvents()));
            list.add(number(PUBLIC_EVENT_PARTICIPANTS, stats.getPublicEventParticipants()));
        }
        return list;
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedOtherHuntingRelatedItems(final OtherHuntingRelatedStatistics stats,
                                                                                                       final int year) {

        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>();
        list.add(number(HARVEST_PERMIT_APPLICATION_PARTNERS, stats.getHarvestPermitApplicationPartners()));

        if (year < 2018) {
            list.add(number(WOLF_TERRITORY_WORKGROUP_LEADS_2017, stats.getWolfTerritoryWorkgroups()));

        } else {
            list.add(number(MOOSELIKE_TAXATION_PLANNING_EVENTS, stats.getMooselikeTaxationPlanningEvents()));
            list.add(number(WOLF_TERRITORY_WORKGROUPS, stats.getWolfTerritoryWorkgroups()));
        }

        return list;
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedCommunicationItems(final CommunicationStatistics stats) {
        return asList(
                number(INTERVIEWS, stats.getInterviews()),
                number(ANNOUNCEMENTS, stats.getAnnouncements()),
                number(OMARIISTA_ANNOUNCEMENTS, stats.getOmariistaAnnouncements()),
                text(WWW, stats.getHomePage()),
                text(SOME_CHANNELS, stats.getSomeInfo()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedShootingRangeItems(final ShootingRangeStatistics stats) {
        return asList(
                number(MOOSE_RANGES, stats.getMooseRanges()),
                number(SHOTGUN_RANGES, stats.getShotgunRanges()),
                number(RIFLE_RANGES, stats.getRifleRanges()),
                number(OTHER_SHOOTING_RANGES, stats.getOtherShootingRanges()));
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedLukeItems(final LukeStatistics stats,
                                                                                        final int year) {

        final List<Tuple2<LocalisedString, Either<Number, String>>> list = new ArrayList<>();

        list.add(number(TOTAL_GAME_TRIANGLES, stats.sumOfWinterAndSummerGameTriangles()));
        list.add(number(WINTER_GAME_TRIANGLES, stats.getWinterGameTriangles()));
        list.add(number(SUMMER_GAME_TRIANGLES, stats.getSummerGameTriangles()));
        list.add(number(FIELD_TRIANGLES, stats.getFieldTriangles()));

        final Integer waterBirdCouples = stats.getWaterBirdCouples();

        if (year < 2018) {
            list.add(number(WATER_BIRD_CALCULATION_LOCATIONS_2017, waterBirdCouples));

        } else if (year < 2020) {
            final Integer waterBirdBroods = stats.getWaterBirdBroods();

            list.add(number(TOTAL_WATER_BIRD_CALCULATION_LOCATIONS, nullableIntSum(waterBirdBroods, waterBirdCouples)));
            list.add(number(WATER_BIRD_BROOD_CALCULATION_LOCATIONS, waterBirdBroods));
            list.add(number(WATER_BIRD_COUPLE_CALCULATION_LOCATIONS, waterBirdCouples));
            list.add(number(LUKE_CARNIVORE_CONTACT_PERSONS, stats.getCarnivoreContactPersons()));
        } else {
            final Integer waterBirdBroods = stats.getWaterBirdBroods();

            list.add(number(TOTAL_WATER_BIRD_CALCULATION_LOCATIONS, nullableIntSum(waterBirdBroods, waterBirdCouples)));
            list.add(number(WATER_BIRD_BROOD_CALCULATION_LOCATIONS, waterBirdBroods));
            list.add(number(WATER_BIRD_COUPLE_CALCULATION_LOCATIONS, waterBirdCouples));
            list.add(number(NORTHERN_LAPLAND_WILLOW_GROUSE_LINES, stats.getNorthernLaplandWillowGrouseLines()));

            final Integer carnivoreContacts = stats.getCarnivoreContactPersons();
            final Integer carnivoreDnaCollectors = stats.getCarnivoreDnaCollectors();

            list.add(number(TOTAL_LUKE_CARNIVORE_PERSONS, nullableIntSum(carnivoreContacts, carnivoreDnaCollectors)));
            list.add(number(LUKE_CARNIVORE_CONTACT_PERSONS, carnivoreContacts));
            list.add(number(LUKE_CARNIVORE_DNA_COLLECTORS, carnivoreDnaCollectors));
        }

        return list;
    }

    private List<Tuple2<LocalisedString, Either<Number, String>>> listExpectedMetsahallitusItems(final MetsahallitusStatistics stats) {
        return asList(
                number(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, stats.getSmallGameLicensesSoldByMetsahallitus()));
    }

    private static Tuple2<LocalisedString, Either<Number, String>> number(final LocalisedString statisticItemName,
                                                                          final Integer expectedNumber) {
        return Tuple.of(statisticItemName, Either.left(expectedNumber));
    }

    private Tuple2<LocalisedString, Either<Number, String>> number(final AnnualStatisticItem statisticItem,
                                                                   final Integer expectedNumber) {

        return number(localiser.getLocalisedString(statisticItem), expectedNumber);
    }

    private static Tuple2<LocalisedString, Either<Number, String>> speciesCount(final Integer speciesCode,
                                                                                final Integer expectedCount) {
        return number(SPECIES_CODE_TO_NAME.get(speciesCode), expectedCount);
    }

    private Tuple2<LocalisedString, Either<Number, String>> text(final AnnualStatisticItem statisticItem,
                                                                 final String expectedText) {

        return Tuple.of(localiser.getLocalisedString(statisticItem), Either.right(expectedText));
    }

    private static void assertResult(final List<Tuple2<LocalisedString, Either<Number, String>>> expected,
                                     final List<Tuple2<LocalisedString, Either<Number, String>>> actual) {

        final int numExpected = expected.size();
        final int numActual = actual.size();
        final int numCommon = Math.min(numExpected, numActual);

        for (int i = 0; i < numCommon; i++) {
            assertEquals("Elements differ at index " + i + ", ", expected.get(i), actual.get(i));
        }

        if (numActual < numExpected) {
            final String missing = expected
                    .stream()
                    .skip(numActual)
                    .map(Object::toString)
                    .collect(joining("\n"));

            fail("Missing items:\n" + missing);

        } else if (numActual > numExpected) {
            final String missing = actual
                    .stream()
                    .skip(numExpected)
                    .map(Object::toString)
                    .collect(joining("\n"));

            fail("Found unexpected items:\n" + missing);
        }
    }
}
