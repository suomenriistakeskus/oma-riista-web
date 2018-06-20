package fi.riista.feature.organization.rhy.annualstats.export;

import com.google.common.collect.Streams;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.organization.rhy.annualstats.CommunicationStatistics;
import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyBasicInfo;
import fi.riista.feature.organization.rhy.annualstats.ShootingRangeStatistics;
import fi.riista.feature.organization.rhy.annualstats.SrvaEventStatistics;
import fi.riista.feature.organization.rhy.annualstats.SrvaSpeciesCountStatistics;
import fi.riista.util.LocalisedString;
import io.vavr.Function1;
import io.vavr.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ACCIDENT_PREVENTION_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ACCIDENT_PREVENTION_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_BEAR_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_BOW_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_HUNTER_EXAM_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_MOOSE_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_ROE_DEER_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_SHOOTING_TEST_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_SHOOTING_TEST_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ALL_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ANNOUNCEMENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.ASSIGNED_HUNTER_EXAM_OFFICIALS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.BOW_TEST_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.CARNIVORE_CONTACT_PERSON_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.CARNIVORE_HUNTING_LEADER_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.CARNIVORE_HUNTING_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.COLLEGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.COLLEGE_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.FAILED_HUNTER_EXAM_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.FIELD_TRIANGLES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.FIREARM_TEST_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_COUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_COUNTING_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_DAMAGE_INSPECTORS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_DAMAGE_LOCATIONS_LARGE_CARNIVORE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_DAMAGE_LOCATIONS_MOOSELIKE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_DAMAGE_LOCATIONS_TOTAL;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_DAMAGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_DAMAGE_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_ENVIRONMENTAL_CARE_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GAME_POPULATION_MANAGEMENT_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.GRANTED_RECREATIONAL_SHOOTING_CERTIFICATES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HARVEST_PERMIT_APPLICATION_PARTNERS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTER_EXAM_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTER_EXAM_OFFICIAL_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTER_EXAM_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTER_EXAM_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTING_CONTROLLERS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTING_CONTROL_CUSTOMERS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTING_CONTROL_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTING_CONTROL_PROOF_ORDERS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTING_CONTROL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.HUNTING_CONTROL_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.IBAN;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.INTERVIEWS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.MOOSELIKE_HUNTING_LEADER_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.MOOSELIKE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.MOOSELIKE_HUNTING_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.MOOSE_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.MUTUAL_ACK_SHOOTING_CERTIFICATES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OMARIISTA_ANNOUNCEMENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OPERATIONAL_LAND_AREA_SIZE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_GAMEKEEPING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_GAMEKEEPING_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_SHOOTING_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_SHOOTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_SHOOTING_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_YOUTH_TARGETED_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.OTHER_YOUTH_TARGETED_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.PASSED_HUNTER_EXAM_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.QUALIFIED_BEAR_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.QUALIFIED_BOW_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.QUALIFIED_MOOSE_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.QUALIFIED_ROE_DEER_ATTEMPTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.RHY_MEMBERS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.RIFLE_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SCHOOL_AND_COLLEGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SCHOOL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SCHOOL_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SHOOTING_TEST_OFFICIALS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SHOOTING_TEST_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SHOOTING_TEST_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SHOTGUN_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SMALL_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SOME_CHANNELS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_ALL_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_ALL_DEPORTATIONS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_ALL_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_ALL_INJURIES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_ALL_LARGE_CARNIVORE_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_ALL_MOOSELIKE_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_ALL_WILD_BOAR_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_OTHER_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_RAILWAY_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_TOTAL_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_TOTAL_WORK_HOURS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_TRAFFIC_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SRVA_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.STATE_AID_HUNTER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SUMMER_GAME_TRIANGLES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.SUM_OF_LUKE_CALCULATIONS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.TOTAL_GAME_TRIANGLES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.TRACKER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.TRACKER_TRAINING_PARTICIPANTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.WATER_BIRD_CALCULATION_LOCATIONS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.WINTER_GAME_TRIANGLES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.WOLF_TERRITORY_WORKGROUP_LEADS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItemId.WWW;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

@Component
public class AnnualStatisticItemGroupFactory {

    private final GameSpeciesService gameSpeciesService;
    private final EnumLocaliser localiser;

    private final Function1<Boolean, AnnualStatisticItemGroup> BASIC_INFO =
            Function1.of(this::createBasicInfoGroup).memoized();

    private final Lazy<AnnualStatisticItemGroup> STATE_AID_SUMMARY = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.STATE_AID_SUMMARY)
                .addNumberItem(RHY_MEMBERS, item -> item.getBasicInfo().getRhyMembers())
                .addNumberItem(HUNTER_EXAM_TRAINING_EVENTS, item -> item.getHunterExamTraining().getHunterExamTrainingEvents())
                .addNumberItem(STATE_AID_HUNTER_TRAINING_EVENTS, item -> item.getStateAidTraining().countStateAidHunterTrainingEvents())
                .addNumberItem(SCHOOL_AND_COLLEGE_TRAINING_EVENTS, item -> item.getStateAidTraining().countSchoolAndCollegeTrainingEvents())
                .addNumberItem(HUNTING_CONTROL_EVENTS, item -> item.getHuntingControl().getHuntingControlEvents())
                .addNumberItem(SUM_OF_LUKE_CALCULATIONS, item -> item.getLuke().sumOfAllLukeCalculations())
                .addNumberItem(HARVEST_PERMIT_APPLICATION_PARTNERS, item -> item.getOtherHuntingRelated().getHarvestPermitApplicationPartners())
                .addNumberItem(WOLF_TERRITORY_WORKGROUP_LEADS, item -> item.getOtherHuntingRelated().getWolfTerritoryWorkgroupLeads())
                .addNumberItem(SRVA_ALL_EVENTS, item -> item.getSrva().countAllSrvaEvents())
                .addNumberItem(SRVA_ALL_MOOSELIKE_EVENTS, item -> item.getSrva().countMooselikes())
                .addNumberItem(SRVA_ALL_LARGE_CARNIVORE_EVENTS, item -> item.getSrva().countLargeCarnivores())
                .addNumberItem(SRVA_ALL_WILD_BOAR_EVENTS, item -> item.getSrva().countWildBoars())
                .addNumberItem(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, item -> item.getMetsahallitus().getSmallGameLicensesSoldByMetsahallitus())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> OTHER_SUMMARY = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.OTHER_SUMMARY)
                .addNumberItem(HUNTER_EXAM_EVENTS, item -> item.getHunterExams().getHunterExamEvents())
                .addNumberItem(ALL_SHOOTING_TEST_EVENTS, item -> item.getShootingTests().countAllShootingTestEvents())
                .addNumberItem(HUNTING_CONTROL_EVENTS, item -> item.getHuntingControl().getHuntingControlEvents())
                .addNumberItem(GAME_DAMAGE_LOCATIONS_TOTAL, item -> item.getGameDamage().getTotalDamageInspectionLocations())
                .addNumberItem(GRANTED_RECREATIONAL_SHOOTING_CERTIFICATES, item -> item.getOtherPublicAdmin().getGrantedRecreationalShootingCertificates())
                .addNumberItem(MUTUAL_ACK_SHOOTING_CERTIFICATES, item -> item.getOtherPublicAdmin().getMutualAckShootingCertificates())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> HUNTER_EXAM = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.HUNTER_EXAMS, AnnualStatisticsExportItemDTO::getHunterExams)
                .addNumberItem(HUNTER_EXAM_EVENTS, grp -> grp.getHunterExamEvents())
                .addNumberItem(ALL_HUNTER_EXAM_ATTEMPTS, grp -> grp.countAllAttempts())
                .addNumberItem(PASSED_HUNTER_EXAM_ATTEMPTS, grp -> grp.getPassedHunterExams())
                .addNumberItem(FAILED_HUNTER_EXAM_ATTEMPTS, grp -> grp.getFailedHunterExams())
                .addNumberItem(ASSIGNED_HUNTER_EXAM_OFFICIALS, grp -> grp.getHunterExamOfficials())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> SHOOTING_TEST = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.SHOOTING_TESTS, AnnualStatisticsExportItemDTO::getShootingTests)
                .addNumberItem(ALL_SHOOTING_TEST_EVENTS, grp -> grp.countAllShootingTestEvents())
                .addNumberItem(FIREARM_TEST_EVENTS, grp -> grp.getFirearmTestEvents())
                .addNumberItem(BOW_TEST_EVENTS, grp -> grp.getBowTestEvents())
                .addNumberItem(ALL_SHOOTING_TEST_ATTEMPTS, grp -> grp.countAllShootingTestAttempts())
                .addNumberItem(ALL_ROE_DEER_ATTEMPTS, grp -> grp.getAllRoeDeerAttempts())
                .addNumberItem(QUALIFIED_ROE_DEER_ATTEMPTS, grp -> grp.getQualifiedRoeDeerAttempts())
                .addNumberItem(ALL_MOOSE_ATTEMPTS, grp -> grp.getAllMooseAttempts())
                .addNumberItem(QUALIFIED_MOOSE_ATTEMPTS, grp -> grp.getQualifiedMooseAttempts())
                .addNumberItem(ALL_BEAR_ATTEMPTS, grp -> grp.getAllBearAttempts())
                .addNumberItem(QUALIFIED_BEAR_ATTEMPTS, grp -> grp.getQualifiedBearAttempts())
                .addNumberItem(ALL_BOW_ATTEMPTS, grp -> grp.getAllBowAttempts())
                .addNumberItem(QUALIFIED_BOW_ATTEMPTS, grp -> grp.getQualifiedBowAttempts())
                .addNumberItem(SHOOTING_TEST_OFFICIALS, grp -> grp.getShootingTestOfficials())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> GAME_DAMAGE = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.GAME_DAMAGE, AnnualStatisticsExportItemDTO::getGameDamage)
                .addNumberItem(GAME_DAMAGE_LOCATIONS_MOOSELIKE, grp -> grp.getMooselikeDamageInspectionLocations())
                //.addNumberItem(GAME_DAMAGE_EXPENSES_MOOSELIKE, grp -> grp.getMooselikeDamageInspectionExpenses())
                .addNumberItem(GAME_DAMAGE_LOCATIONS_LARGE_CARNIVORE, grp -> grp.getLargeCarnivoreDamageInspectionLocations())
                //.addNumberItem(GAME_DAMAGE_EXPENSES_LARGE_CARNIVORE, grp -> grp.getLargeCarnivoreDamageInspectionExpenses())
                .addNumberItem(GAME_DAMAGE_LOCATIONS_TOTAL, grp -> grp.getTotalDamageInspectionLocations())
                //.addNumberItem(GAME_DAMAGE_EXPENSES_TOTAL, grp -> grp.getTotalDamageInspectionExpenses())
                .addNumberItem(GAME_DAMAGE_INSPECTORS, grp -> grp.getGameDamageInspectors())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> HUNTING_CONTROL = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.HUNTING_CONTROL, AnnualStatisticsExportItemDTO::getHuntingControl)
                .addNumberItem(HUNTING_CONTROL_EVENTS, grp -> grp.getHuntingControlEvents())
                .addNumberItem(HUNTING_CONTROL_CUSTOMERS, grp -> grp.getHuntingControlCustomers())
                .addNumberItem(HUNTING_CONTROL_PROOF_ORDERS, grp -> grp.getProofOrders())
                .addNumberItem(HUNTING_CONTROLLERS, grp -> grp.getHuntingControllers())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> OTHER_PUBLIC_ADMIN_TASKS = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.OTHER_PUBLIC_ADMIN_TASKS, AnnualStatisticsExportItemDTO::getOtherPublicAdmin)
                .addNumberItem(GRANTED_RECREATIONAL_SHOOTING_CERTIFICATES, grp -> grp.getGrantedRecreationalShootingCertificates())
                .addNumberItem(MUTUAL_ACK_SHOOTING_CERTIFICATES, grp -> grp.getMutualAckShootingCertificates())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> SRVA_TOTALS = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.SRVA_TOTALS, AnnualStatisticsExportItemDTO::getSrva)
                .addNumberItem(SRVA_ALL_EVENTS, grp -> grp.countAllSrvaEvents())
                .addNumberItem(SRVA_TOTAL_WORK_HOURS, grp -> grp.getTotalSrvaWorkHours())
                .addNumberItem(SRVA_TOTAL_PARTICIPANTS, grp -> grp.getSrvaParticipants())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> TRAINING_SUMMARY = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.TRAINING_SUMMARY)
                .addNumberItem(ALL_TRAINING_EVENTS, AnnualStatisticsExportItemDTO::getAllTrainingEvents)
                .addNumberItem(ALL_TRAINING_PARTICIPANTS, AnnualStatisticsExportItemDTO::getAllTrainingParticipants)
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> HUNTER_EXAM_TRAINING = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.HUNTER_EXAM_TRAINING, AnnualStatisticsExportItemDTO::getHunterExamTraining)
                .addNumberItem(HUNTER_EXAM_TRAINING_EVENTS, grp -> grp.getHunterExamTrainingEvents())
                .addNumberItem(HUNTER_EXAM_TRAINING_PARTICIPANTS, grp -> grp.getHunterExamTrainingParticipants())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> JHT_TRAINING = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.JHT_TRAINING, AnnualStatisticsExportItemDTO::getJhtTraining)
                .addNumberItem(SHOOTING_TEST_TRAINING_EVENTS, grp -> grp.getShootingTestTrainingEvents())
                .addNumberItem(SHOOTING_TEST_TRAINING_PARTICIPANTS, grp -> grp.getShootingTestTrainingParticipants())
                .addNumberItem(HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS, grp -> grp.getHunterExamOfficialTrainingEvents())
                .addNumberItem(HUNTER_EXAM_OFFICIAL_TRAINING_PARTICIPANTS, grp -> grp.getHunterExamOfficialTrainingParticipants())
                .addNumberItem(GAME_DAMAGE_TRAINING_EVENTS, grp -> grp.getGameDamageTrainingEvents())
                .addNumberItem(GAME_DAMAGE_TRAINING_PARTICIPANTS, grp -> grp.getGameDamageTrainingParticipants())
                .addNumberItem(HUNTING_CONTROL_TRAINING_EVENTS, grp -> grp.getHuntingControlTrainingEvents())
                .addNumberItem(HUNTING_CONTROL_TRAINING_PARTICIPANTS, grp -> grp.getHuntingControlTrainingParticipants())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> STATE_AID_TRAINING = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.STATE_AID_TRAINING, AnnualStatisticsExportItemDTO::getStateAidTraining)
                .addNumberItem(MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS, grp -> grp.getMooselikeHuntingLeaderTrainingEvents())
                .addNumberItem(MOOSELIKE_HUNTING_LEADER_TRAINING_PARTICIPANTS, grp -> grp.getMooselikeHuntingLeaderTrainingParticipants())
                .addNumberItem(CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS, grp -> grp.getCarnivoreHuntingLeaderTrainingEvents())
                .addNumberItem(CARNIVORE_HUNTING_LEADER_TRAINING_PARTICIPANTS, grp -> grp.getCarnivoreHuntingLeaderTrainingParticipants())
                .addNumberItem(MOOSELIKE_HUNTING_TRAINING_EVENTS, grp -> grp.getMooselikeHuntingTrainingEvents())
                .addNumberItem(MOOSELIKE_HUNTING_TRAINING_PARTICIPANTS, grp -> grp.getMooselikeHuntingTrainingParticipants())
                .addNumberItem(CARNIVORE_HUNTING_TRAINING_EVENTS, grp -> grp.getCarnivoreHuntingTrainingEvents())
                .addNumberItem(CARNIVORE_HUNTING_TRAINING_PARTICIPANTS, grp -> grp.getCarnivoreContactPersonTrainingParticipants())
                .addNumberItem(SRVA_TRAINING_EVENTS, grp -> grp.getSrvaTrainingEvents())
                .addNumberItem(SRVA_TRAINING_PARTICIPANTS, grp -> grp.getSrvaTrainingParticipants())
                .addNumberItem(CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS, grp -> grp.getCarnivoreContactPersonTrainingEvents())
                .addNumberItem(CARNIVORE_CONTACT_PERSON_TRAINING_PARTICIPANTS, grp -> grp.getCarnivoreContactPersonTrainingParticipants())
                .addNumberItem(ACCIDENT_PREVENTION_TRAINING_EVENTS, grp -> grp.getAccidentPreventionTrainingEvents())
                .addNumberItem(ACCIDENT_PREVENTION_TRAINING_PARTICIPANTS, grp -> grp.getAccidentPreventionTrainingParticipants())
                .addNumberItem(SCHOOL_TRAINING_EVENTS, grp -> grp.getSchoolTrainingEvents())
                .addNumberItem(SCHOOL_TRAINING_PARTICIPANTS, grp -> grp.getSchoolTrainingParticipants())
                .addNumberItem(COLLEGE_TRAINING_EVENTS, grp -> grp.getCollegeTrainingEvents())
                .addNumberItem(COLLEGE_TRAINING_PARTICIPANTS, grp -> grp.getCollegeTrainingParticipants())
                .addNumberItem(OTHER_YOUTH_TARGETED_TRAINING_EVENTS, grp -> grp.getOtherYouthTargetedTrainingEvents())
                .addNumberItem(OTHER_YOUTH_TARGETED_TRAINING_PARTICIPANTS, grp -> grp.getOtherYouthTargetedTrainingParticipants())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> OTHER_HUNTER_TRAINING = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.OTHER_HUNTER_TRAINING, AnnualStatisticsExportItemDTO::getOtherHunterTraining)
                .addNumberItem(SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS, grp -> grp.getSmallCarnivoreHuntingTrainingEvents())
                .addNumberItem(SMALL_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS, grp -> grp.getSmallCarnivoreHuntingTrainingParticipants())
                .addNumberItem(GAME_COUNTING_TRAINING_EVENTS, grp -> grp.getGameCountingTrainingEvents())
                .addNumberItem(GAME_COUNTING_TRAINING_PARTICIPANTS, grp -> grp.getGameCountingTrainingParticipants())
                .addNumberItem(GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS, grp -> grp.getGamePopulationManagementTrainingEvents())
                .addNumberItem(GAME_POPULATION_MANAGEMENT_TRAINING_PARTICIPANTS, grp -> grp.getGamePopulationManagementTrainingParticipants())
                .addNumberItem(GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS, grp -> grp.getGameEnvironmentalCareTrainingEvents())
                .addNumberItem(GAME_ENVIRONMENTAL_CARE_TRAINING_PARTICIPANTS, grp -> grp.getGameEnvironmentalCareTrainingParticipants())
                .addNumberItem(OTHER_GAMEKEEPING_TRAINING_EVENTS, grp -> grp.getOtherGamekeepingTrainingEvents())
                .addNumberItem(OTHER_GAMEKEEPING_TRAINING_PARTICIPANTS, grp -> grp.getOtherGamekeepingTrainingParticipants())
                .addNumberItem(OTHER_SHOOTING_TRAINING_EVENTS, grp -> grp.getOtherShootingTrainingEvents())
                .addNumberItem(OTHER_SHOOTING_TRAINING_PARTICIPANTS, grp -> grp.getOtherShootingTrainingParticipants())
                .addNumberItem(TRACKER_TRAINING_EVENTS, grp -> grp.getTrackerTrainingEvents())
                .addNumberItem(TRACKER_TRAINING_PARTICIPANTS, grp -> grp.getTrackerTrainingParticipants())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> OTHER_TRAINING = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.OTHER_TRAINING, AnnualStatisticsExportItemDTO::getOtherTraining)
                .addNumberItem(OTHER_TRAINING_EVENTS, grp -> grp.getOtherTrainingEvents())
                .addNumberItem(OTHER_TRAINING_PARTICIPANTS, grp -> grp.getOtherTrainingParticipants())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> OTHER_HUNTING_RELATED = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.OTHER_HUNTING_RELATED, AnnualStatisticsExportItemDTO::getOtherHuntingRelated)
                .addNumberItem(HARVEST_PERMIT_APPLICATION_PARTNERS, grp -> grp.getHarvestPermitApplicationPartners())
                .addNumberItem(WOLF_TERRITORY_WORKGROUP_LEADS, grp -> grp.getWolfTerritoryWorkgroupLeads())
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> COMMUNICATION = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.COMMUNICATION, AnnualStatisticsExportItemDTO::getCommunication)
                .addNumberItem(INTERVIEWS, CommunicationStatistics::getInterviews)
                .addNumberItem(ANNOUNCEMENTS, CommunicationStatistics::getAnnouncements)
                .addNumberItem(OMARIISTA_ANNOUNCEMENTS, CommunicationStatistics::getOmariistaAnnouncements)
                .addTextItem(WWW, CommunicationStatistics::getHomePage)
                .addTextItem(SOME_CHANNELS, CommunicationStatistics::getSomeInfo)
                //.addTextItem(COMMUNICATION_FREE_TEXT, CommunicationStatistics::getInfo)
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> SHOOTING_RANGES = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.SHOOTING_RANGES, AnnualStatisticsExportItemDTO::getShootingRanges)
                .addNumberItem(MOOSE_RANGES, ShootingRangeStatistics::getMooseRanges)
                .addNumberItem(SHOTGUN_RANGES, ShootingRangeStatistics::getShotgunRanges)
                .addNumberItem(RIFLE_RANGES, ShootingRangeStatistics::getRifleRanges)
                .addNumberItem(OTHER_SHOOTING_RANGES, ShootingRangeStatistics::getOtherShootingRanges)
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> LUKE = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.LUKE, AnnualStatisticsExportItemDTO::getLuke)
                .addNumberItem(TOTAL_GAME_TRIANGLES, LukeStatistics::sumOfWinterAndSummerGameTriangles)
                .addNumberItem(WINTER_GAME_TRIANGLES, LukeStatistics::getWinterGameTriangles)
                .addNumberItem(SUMMER_GAME_TRIANGLES, LukeStatistics::getSummerGameTriangles)
                .addNumberItem(FIELD_TRIANGLES, LukeStatistics::getFieldTriangles)
                .addNumberItem(WATER_BIRD_CALCULATION_LOCATIONS, LukeStatistics::getWaterBirds)
                .build();
    });

    private final Lazy<AnnualStatisticItemGroup> METSAHALLITUS = Lazy.of(() -> {
        return newBuilder(AnnualStatisticItemGroupId.METSAHALLITUS, AnnualStatisticsExportItemDTO::getMetsahallitus)
                .addNumberItem(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, grp -> grp.getSmallGameLicensesSoldByMetsahallitus())
                .build();
    });

    @Autowired
    public AnnualStatisticItemGroupFactory(@Nonnull final GameSpeciesService gameSpeciesService,
                                           @Nonnull final EnumLocaliser localiser) {

        this.gameSpeciesService = requireNonNull(gameSpeciesService);
        this.localiser = requireNonNull(localiser);
    }

    private EnumLocaliser getLocaliser() {
        return localiser;
    }

    private AnnualStatisticItemGroup.Builder<AnnualStatisticsExportItemDTO> newBuilder(@Nonnull final AnnualStatisticItemGroupId groupId) {
        return newBuilder(groupId, identity());
    }

    private <T> AnnualStatisticItemGroup.Builder<T> newBuilder(@Nonnull final AnnualStatisticItemGroupId groupId,
                                                               @Nonnull final Function<? super AnnualStatisticsExportItemDTO, T> dataExtractor) {

        return new AnnualStatisticItemGroup.Builder<>(groupId, dataExtractor, getLocaliser());
    }

    public List<AnnualStatisticItemGroup> getAllGroups(final boolean includeIban) {
        final Map<Integer, LocalisedString> speciesLocalisations = gameSpeciesService.getNameIndex();

        final Stream<AnnualStatisticItemGroup> preSrva = Stream.of(getBasicInfoGroup(includeIban),
                getStateAidSummaryGroup(), getOtherSummaryGroup(), getHunterExamGroup(), getShootingTestGroup(),
                getGameDamageGroup(), getHuntingControlGroup(), getOtherPublicAdminTasksGroup());

        final Stream<AnnualStatisticItemGroup> srva = Stream.of(getSrvaTotalsGroup(),
                createSrvaAccidentGroup(speciesLocalisations), createSrvaDeportationGroup(speciesLocalisations),
                createSrvaInjuryGroup(speciesLocalisations));

        final Stream<AnnualStatisticItemGroup> postSrva = Stream.of(getTrainingSummaryGroup(),
                getHunterExamTrainingGroup(), getJhtTrainingGroup(), getStateAidTrainingGroup(),
                getOtherHunterTrainingGroup(), getOtherTrainingGroup(), getOtherHuntingRelatedGroup(),
                getCommunicationGroup(), getShootingRangeGroup(), getLukeGroup(), getMetsahallitusGroup());

        return Streams.concat(preSrva, srva, postSrva).collect(toList());
    }

    public AnnualStatisticItemGroup getBasicInfoGroup(final boolean includeIban) {
        return BASIC_INFO.apply(includeIban);
    }

    public AnnualStatisticItemGroup getStateAidSummaryGroup() {
        return STATE_AID_SUMMARY.get();
    }

    public AnnualStatisticItemGroup getOtherSummaryGroup() {
        return OTHER_SUMMARY.get();
    }

    public AnnualStatisticItemGroup getHunterExamGroup() {
        return HUNTER_EXAM.get();
    }

    public AnnualStatisticItemGroup getShootingTestGroup() {
        return SHOOTING_TEST.get();
    }

    public AnnualStatisticItemGroup getGameDamageGroup() {
        return GAME_DAMAGE.get();
    }

    public AnnualStatisticItemGroup getHuntingControlGroup() {
        return HUNTING_CONTROL.get();
    }

    public AnnualStatisticItemGroup getOtherPublicAdminTasksGroup() {
        return OTHER_PUBLIC_ADMIN_TASKS.get();
    }

    public AnnualStatisticItemGroup getSrvaTotalsGroup() {
        return SRVA_TOTALS.get();
    }

    public AnnualStatisticItemGroup getSrvaAccidentGroup() {
        return createSrvaAccidentGroup(gameSpeciesService.getNameIndex());
    }

    public AnnualStatisticItemGroup getSrvaDeportationGroup() {
        return createSrvaDeportationGroup(gameSpeciesService.getNameIndex());
    }

    public AnnualStatisticItemGroup getSrvaInjuryGroup() {
        return createSrvaInjuryGroup(gameSpeciesService.getNameIndex());
    }

    public AnnualStatisticItemGroup getTrainingSummaryGroup() {
        return TRAINING_SUMMARY.get();
    }

    public AnnualStatisticItemGroup getHunterExamTrainingGroup() {
        return HUNTER_EXAM_TRAINING.get();
    }

    public AnnualStatisticItemGroup getJhtTrainingGroup() {
        return JHT_TRAINING.get();
    }

    public AnnualStatisticItemGroup getStateAidTrainingGroup() {
        return STATE_AID_TRAINING.get();
    }

    public AnnualStatisticItemGroup getOtherHunterTrainingGroup() {
        return OTHER_HUNTER_TRAINING.get();
    }

    public AnnualStatisticItemGroup getOtherTrainingGroup() {
        return OTHER_TRAINING.get();
    }

    public AnnualStatisticItemGroup getOtherHuntingRelatedGroup() {
        return OTHER_HUNTING_RELATED.get();
    }

    public AnnualStatisticItemGroup getCommunicationGroup() {
        return COMMUNICATION.get();
    }

    public AnnualStatisticItemGroup getShootingRangeGroup() {
        return SHOOTING_RANGES.get();
    }

    public AnnualStatisticItemGroup getLukeGroup() {
        return LUKE.get();
    }

    public AnnualStatisticItemGroup getMetsahallitusGroup() {
        return METSAHALLITUS.get();
    }

    private AnnualStatisticItemGroup createBasicInfoGroup(final boolean includeIban) {
        final AnnualStatisticItemGroup.Builder<RhyBasicInfo> builder =
                newBuilder(AnnualStatisticItemGroupId.BASIC_INFO, AnnualStatisticsExportItemDTO::getBasicInfo);

        if (includeIban) {
            builder.addTextItem(IBAN, RhyBasicInfo::getIbanAsFormattedString);
        }

        return builder.addNumberItem(OPERATIONAL_LAND_AREA_SIZE, RhyBasicInfo::getOperationalLandAreaSize).build();
    }

    private AnnualStatisticItemGroup createSrvaAccidentGroup(final Map<Integer, LocalisedString> speciesLocalisations) {
        final AnnualStatisticItemGroup.Builder<SrvaEventStatistics> builder =
                newBuilder(AnnualStatisticItemGroupId.SRVA_ACCIDENTS, item -> item.getSrva())
                        .addNumberItem(SRVA_ALL_ACCIDENTS, srva -> srva.getAccident().countAll())
                        .addNumberItem(SRVA_TRAFFIC_ACCIDENTS, srva -> srva.getTrafficAccidents())
                        .addNumberItem(SRVA_RAILWAY_ACCIDENTS, srva -> srva.getRailwayAccidents())
                        .addNumberItem(SRVA_OTHER_ACCIDENTS, srva -> srva.getOtherAccidents());

        return appendSpeciesCounts(builder, SrvaEventStatistics::getAccident, speciesLocalisations).build();
    }

    private AnnualStatisticItemGroup createSrvaDeportationGroup(final Map<Integer, LocalisedString> speciesLocalisations) {
        final AnnualStatisticItemGroup.Builder<SrvaSpeciesCountStatistics> builder =
                newBuilder(AnnualStatisticItemGroupId.SRVA_DEPORTATIONS, item -> item.getSrva().getDeportation())
                        .addNumberItem(SRVA_ALL_DEPORTATIONS, SrvaSpeciesCountStatistics::countAll);

        return appendSpeciesCounts(builder, identity(), speciesLocalisations).build();
    }

    private AnnualStatisticItemGroup createSrvaInjuryGroup(final Map<Integer, LocalisedString> speciesLocalisations) {
        final AnnualStatisticItemGroup.Builder<SrvaSpeciesCountStatistics> builder =
                newBuilder(AnnualStatisticItemGroupId.SRVA_INJURIES, item -> item.getSrva().getInjury())
                        .addNumberItem(SRVA_ALL_INJURIES, SrvaSpeciesCountStatistics::countAll);

        return appendSpeciesCounts(builder, identity(), speciesLocalisations).build();
    }

    private static <T> AnnualStatisticItemGroup.Builder<T> appendSpeciesCounts(final AnnualStatisticItemGroup.Builder<T> builder,
                                                                               final Function<? super T, SrvaSpeciesCountStatistics> extractor,
                                                                               final Map<Integer, LocalisedString> speciesLocalisations) {

        return builder
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_MOOSE), extractor.andThen(s -> s.getMooses()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_WHITE_TAILED_DEER), extractor.andThen(s -> s.getWhiteTailedDeers()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_ROE_DEER), extractor.andThen(s -> s.getRoeDeers()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_WILD_FOREST_REINDEER), extractor.andThen(s -> s.getWildForestReindeers()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_FALLOW_DEER), extractor.andThen(s -> s.getFallowDeers()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_WILD_BOAR), extractor.andThen(s -> s.getWildBoars()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_LYNX), extractor.andThen(s -> s.getLynxes()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_BEAR), extractor.andThen(s -> s.getBears()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_WOLF), extractor.andThen(s -> s.getWolves()))
                .addNumberItem(speciesLocalisations.get(OFFICIAL_CODE_WOLVERINE), extractor.andThen(s -> s.getWolverines()));
    }
}
