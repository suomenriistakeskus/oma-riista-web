package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.util.LocalisedEnum;
import io.vavr.control.Either;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countAllTrainingEvents2017;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countAllTrainingParticipants2017;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countHunterTrainingEvents2017;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countLargeCarnivores2017;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countNonSubsidizableTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countNonSubsidizableTrainingParticipants;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countStudentTrainingEvents2017;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableOtherTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableStudentAndYouthTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableTrainingParticipants;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countWildBoars2017;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

public enum AnnualStatisticItem implements LocalisedEnum {

    // BASIC INFO

    IBAN(asText(i -> i.getBasicInfo().getIbanAsFormattedString())),
    OPERATIONAL_LAND_AREA_SIZE(asNumber(i -> i.getBasicInfo().getOperationalLandAreaSize())),

    // STATE-AID SUMMARY

    RHY_MEMBERS(asNumber(i -> i.getBasicInfo().getRhyMembers())),
    SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS(asNumber(i -> i.getHunterExamTraining().getHunterExamTrainingEvents())),

    SUBSIDIZABLE_OTHER_TRAINING_EVENTS(asNumber(i -> {
        return countSubsidizableOtherTrainingEvents(
                i.getJhtTraining(),
                i.getHunterTraining(),
                i.getOtherHunterTraining());
    })),

    SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS(asNumber(i -> {
        return countSubsidizableStudentAndYouthTrainingEvents(i.getYouthTraining());
    })),

    // Overridden by SUBSIDIZABLE_OTHER_TRAINING_EVENTS
    HUNTER_TRAINING_EVENTS_2017(asNumber(i -> {
        return countHunterTrainingEvents2017(i.getHunterTraining());
    })),

    // Overridden by SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS
    STUDENT_TRAINING_EVENTS_2017(asNumber(i -> {
        return countStudentTrainingEvents2017(i.getYouthTraining());
    })),

    SUM_OF_LUKE_CALCULATIONS(asNumber(i -> i.getLuke().sumOfAllLukeCalculations())),
    SUM_OF_LUKE_CALCULATIONS_2018(asNumber(i -> i.getLuke().sumOfAllLukeCalculations2018())),
    SRVA_ALL_MOOSELIKE_EVENTS(asNumber(i -> i.getSrva().countMooselikes())),

    // Overridden by SRVA_ALL_MOOSELIKE_EVENTS
    SRVA_ALL_MOOSELIKE_EVENTS_2017(asNumber(i -> i.getSrva().countMooselikes())),
    // Removed 2018
    SRVA_ALL_LARGE_CARNIVORE_EVENTS_2017(asNumber(i -> countLargeCarnivores2017(i.getSrva()))),
    // Removed 2018
    SRVA_ALL_WILD_BOAR_EVENTS_2017(asNumber(i -> countWildBoars2017(i.getSrva()))),

    // HUNTER EXAMS

    HUNTER_EXAM_EVENTS(asNumber(i -> i.getHunterExams().getHunterExamEvents())),
    ALL_HUNTER_EXAM_ATTEMPTS(asNumber(i -> i.getHunterExams().countAllAttempts())),
    PASSED_HUNTER_EXAM_ATTEMPTS(asNumber(i -> i.getHunterExams().getPassedHunterExams())),
    FAILED_HUNTER_EXAM_ATTEMPTS(asNumber(i -> i.getHunterExams().getFailedHunterExams())),
    ASSIGNED_HUNTER_EXAM_OFFICIALS(asNumber(i -> i.getHunterExams().getHunterExamOfficials())),

    // SHOOTING TESTS

    ALL_SHOOTING_TEST_EVENTS(asNumber(i -> i.getShootingTests().countAllShootingTestEvents())),
    FIREARM_TEST_EVENTS(asNumber(i -> i.getShootingTests().getFirearmTestEvents())),
    BOW_TEST_EVENTS(asNumber(i -> i.getShootingTests().getBowTestEvents())),
    ALL_SHOOTING_TEST_ATTEMPTS(asNumber(i -> i.getShootingTests().countAllShootingTestAttempts())),
    ALL_ROE_DEER_ATTEMPTS(asNumber(i -> i.getShootingTests().getAllRoeDeerAttempts())),
    QUALIFIED_ROE_DEER_ATTEMPTS(asNumber(i -> i.getShootingTests().getQualifiedRoeDeerAttempts())),
    ALL_MOOSE_ATTEMPTS(asNumber(i -> i.getShootingTests().getAllMooseAttempts())),
    QUALIFIED_MOOSE_ATTEMPTS(asNumber(i -> i.getShootingTests().getQualifiedMooseAttempts())),
    ALL_BEAR_ATTEMPTS(asNumber(i -> i.getShootingTests().getAllBearAttempts())),
    QUALIFIED_BEAR_ATTEMPTS(asNumber(i -> i.getShootingTests().getQualifiedBearAttempts())),
    ALL_BOW_ATTEMPTS(asNumber(i -> i.getShootingTests().getAllBowAttempts())),
    QUALIFIED_BOW_ATTEMPTS(asNumber(i -> i.getShootingTests().getQualifiedBowAttempts())),
    SHOOTING_TEST_OFFICIALS(asNumber(i -> i.getShootingTests().getShootingTestOfficials())),

    // GAME DAMAGE

    GAME_DAMAGE_LOCATIONS_MOOSELIKE(asNumber(i -> i.getGameDamage().getMooselikeDamageInspectionLocations())),
    //GAME_DAMAGE_EXPENSES_MOOSELIKE(asNumber(i -> i.getGameDamage().getMooselikeDamageInspectionExpenses())),
    GAME_DAMAGE_LOCATIONS_LARGE_CARNIVORE(asNumber(i -> i.getGameDamage().getLargeCarnivoreDamageInspectionLocations())),
    //GAME_DAMAGE_EXPENSES_LARGE_CARNIVORE(asNumber(i -> i.getGameDamage().getLargeCarnivoreDamageInspectionExpenses())),
    GAME_DAMAGE_LOCATIONS_TOTAL(asNumber(i -> i.getGameDamage().getTotalDamageInspectionLocations())),
    //GAME_DAMAGE_EXPENSES_TOTAL(asNumber(i -> i.getGameDamage().getTotalDamageInspectionExpenses())),
    GAME_DAMAGE_INSPECTORS(asNumber(i -> i.getGameDamage().getGameDamageInspectors())),

    // HUNTING CONTROL

    HUNTING_CONTROL_EVENTS(asNumber(i -> i.getHuntingControl().getHuntingControlEvents())),
    HUNTING_CONTROL_CUSTOMERS(asNumber(i -> i.getHuntingControl().getHuntingControlCustomers())),
    HUNTING_CONTROL_PROOF_ORDERS(asNumber(i -> i.getHuntingControl().getProofOrders())),
    HUNTING_CONTROLLERS(asNumber(i -> i.getHuntingControl().getHuntingControllers())),

    // OTHER PUBLIC ADMIN

    GRANTED_RECREATIONAL_SHOOTING_CERTIFICATES(asNumber(i -> i.getOtherPublicAdmin().getGrantedRecreationalShootingCertificates())),
    MUTUAL_ACK_SHOOTING_CERTIFICATES(asNumber(i -> i.getOtherPublicAdmin().getMutualAckShootingCertificates())),

    // SRVA TOTALS

    SRVA_ALL_EVENTS(asNumber(i -> i.getSrva().countAllSrvaEvents())),
    SRVA_TOTAL_WORK_HOURS(asNumber(i -> i.getSrva().getTotalSrvaWorkHours())),
    SRVA_TOTAL_PARTICIPANTS(asNumber(i -> i.getSrva().getSrvaParticipants())),

    // SRVA ACCIDENT

    SRVA_ALL_ACCIDENTS(asNumber(i -> i.getSrva().getAccident().countAll())),
    SRVA_TRAFFIC_ACCIDENTS(asNumber(i -> i.getSrva().getTrafficAccidents())),
    SRVA_RAILWAY_ACCIDENTS(asNumber(i -> i.getSrva().getRailwayAccidents())),
    SRVA_OTHER_ACCIDENTS(asNumber(i -> i.getSrva().getOtherAccidents())),
    SRVA_ACCIDENTS_MOOSE(asNumber(i -> i.getSrva().getAccident().getMooses())),
    SRVA_ACCIDENTS_WHITE_TAILED_DEER(asNumber(i -> i.getSrva().getAccident().getWhiteTailedDeers())),
    SRVA_ACCIDENTS_ROE_DEER(asNumber(i -> i.getSrva().getAccident().getRoeDeers())),
    SRVA_ACCIDENTS_WILD_FOREST_REINDEER(asNumber(i -> i.getSrva().getAccident().getWildForestReindeers())),
    SRVA_ACCIDENTS_FALLOW_DEER(asNumber(i -> i.getSrva().getAccident().getFallowDeers())),
    SRVA_ACCIDENTS_WILD_BOAR(asNumber(i -> i.getSrva().getAccident().getWildBoars())),
    SRVA_ACCIDENTS_LYNX(asNumber(i -> i.getSrva().getAccident().getLynxes())),
    SRVA_ACCIDENTS_BEAR(asNumber(i -> i.getSrva().getAccident().getBears())),
    SRVA_ACCIDENTS_WOLF(asNumber(i -> i.getSrva().getAccident().getWolves())),
    SRVA_ACCIDENTS_WOLVERINE(asNumber(i -> i.getSrva().getAccident().getWolverines())),
    SRVA_ACCIDENTS_OTHER_SPECIES(asNumber(i -> i.getSrva().getAccident().getOtherSpecies())),

    // SRVA DEPORTATION

    SRVA_ALL_DEPORTATIONS(asNumber(i -> i.getSrva().getDeportation().countAll())),
    SRVA_DEPORTATIONS_MOOSE(asNumber(i -> i.getSrva().getDeportation().getMooses())),
    SRVA_DEPORTATIONS_WHITE_TAILED_DEER(asNumber(i -> i.getSrva().getDeportation().getWhiteTailedDeers())),
    SRVA_DEPORTATIONS_ROE_DEER(asNumber(i -> i.getSrva().getDeportation().getRoeDeers())),
    SRVA_DEPORTATIONS_WILD_FOREST_REINDEER(asNumber(i -> i.getSrva().getDeportation().getWildForestReindeers())),
    SRVA_DEPORTATIONS_FALLOW_DEER(asNumber(i -> i.getSrva().getDeportation().getFallowDeers())),
    SRVA_DEPORTATIONS_WILD_BOAR(asNumber(i -> i.getSrva().getDeportation().getWildBoars())),
    SRVA_DEPORTATIONS_LYNX(asNumber(i -> i.getSrva().getDeportation().getLynxes())),
    SRVA_DEPORTATIONS_BEAR(asNumber(i -> i.getSrva().getDeportation().getBears())),
    SRVA_DEPORTATIONS_WOLF(asNumber(i -> i.getSrva().getDeportation().getWolves())),
    SRVA_DEPORTATIONS_WOLVERINE(asNumber(i -> i.getSrva().getDeportation().getWolverines())),
    SRVA_DEPORTATIONS_OTHER_SPECIES(asNumber(i -> i.getSrva().getDeportation().getOtherSpecies())),

    // SRVA INJURY

    SRVA_ALL_INJURIES(asNumber(i -> i.getSrva().getInjury().countAll())),
    SRVA_INJURIES_MOOSE(asNumber(i -> i.getSrva().getInjury().getMooses())),
    SRVA_INJURIES_WHITE_TAILED_DEER(asNumber(i -> i.getSrva().getInjury().getWhiteTailedDeers())),
    SRVA_INJURIES_ROE_DEER(asNumber(i -> i.getSrva().getInjury().getRoeDeers())),
    SRVA_INJURIES_WILD_FOREST_REINDEER(asNumber(i -> i.getSrva().getInjury().getWildForestReindeers())),
    SRVA_INJURIES_FALLOW_DEER(asNumber(i -> i.getSrva().getInjury().getFallowDeers())),
    SRVA_INJURIES_WILD_BOAR(asNumber(i -> i.getSrva().getInjury().getWildBoars())),
    SRVA_INJURIES_LYNX(asNumber(i -> i.getSrva().getInjury().getLynxes())),
    SRVA_INJURIES_BEAR(asNumber(i -> i.getSrva().getInjury().getBears())),
    SRVA_INJURIES_WOLF(asNumber(i -> i.getSrva().getInjury().getWolves())),
    SRVA_INJURIES_WOLVERINE(asNumber(i -> i.getSrva().getInjury().getWolverines())),
    SRVA_INJURIES_OTHER_SPECIES(asNumber(i -> i.getSrva().getInjury().getOtherSpecies())),

    // TRAINING SUMMARY

    ALL_TRAINING_EVENTS(asNumber(i -> {
        return nullableIntSum(
                countSubsidizableTrainingEvents(
                        i.getHunterExamTraining(),
                        i.getJhtTraining(),
                        i.getHunterTraining(),
                        i.getYouthTraining(),
                        i.getOtherHunterTraining()),
                countNonSubsidizableTrainingEvents(
                        i.getHunterExamTraining(),
                        i.getJhtTraining(),
                        i.getHunterTraining(),
                        i.getYouthTraining(),
                        i.getOtherHunterTraining()));
    })),

    ALL_TRAINING_PARTICIPANTS(asNumber(i -> {
        return nullableIntSum(
                countSubsidizableTrainingParticipants(
                        i.getHunterExamTraining(),
                        i.getJhtTraining(),
                        i.getHunterTraining(),
                        i.getYouthTraining(),
                        i.getOtherHunterTraining()),
                countNonSubsidizableTrainingParticipants(
                        i.getHunterExamTraining(),
                        i.getJhtTraining(),
                        i.getHunterTraining(),
                        i.getYouthTraining(),
                        i.getOtherHunterTraining()
                ));
    })),

    SUBSIDIZABLE_TRAINING_EVENTS(asNumber(i -> {
        return countSubsidizableTrainingEvents(
                i.getHunterExamTraining(),
                i.getJhtTraining(),
                i.getHunterTraining(),
                i.getYouthTraining(),
                i.getOtherHunterTraining());
    })),

    SUBSIDIZABLE_TRAINING_PARTICIPANTS(asNumber(i -> {
        return countSubsidizableTrainingParticipants(
                i.getHunterExamTraining(),
                i.getJhtTraining(),
                i.getHunterTraining(),
                i.getYouthTraining(),
                i.getOtherHunterTraining());
    })),

    NON_SUBSIDIZABLE_TRAINING_EVENTS(asNumber(i -> {
        return countNonSubsidizableTrainingEvents(
                i.getHunterExamTraining(),
                i.getJhtTraining(),
                i.getHunterTraining(),
                i.getYouthTraining(),
                i.getOtherHunterTraining());
    })),

    NON_SUBSIDIZABLE_TRAINING_PARTICIPANTS(asNumber(i -> {
        return countNonSubsidizableTrainingParticipants(
                i.getHunterExamTraining(),
                i.getJhtTraining(),
                i.getHunterTraining(),
                i.getYouthTraining(),
                i.getOtherHunterTraining());
    })),

    // Overridden by ALL_TRAINING_EVENTS
    ALL_TRAINING_EVENTS_2017(asNumber(i -> {
        return countAllTrainingEvents2017(
                i.getHunterExamTraining(),
                i.getJhtTraining(),
                i.getHunterTraining(),
                i.getYouthTraining(),
                i.getOtherHunterTraining(),
                i.getPublicEvents());
    })),
    // Overridden by ALL_TRAINING_PARTICIPANTS
    ALL_TRAINING_PARTICIPANTS_2017(asNumber(i -> {
        return countAllTrainingParticipants2017(
                i.getHunterExamTraining(),
                i.getJhtTraining(),
                i.getHunterTraining(),
                i.getYouthTraining(),
                i.getOtherHunterTraining(),
                i.getPublicEvents());
    })),

    // HUNTER EXAM TRAINING EVENTS

    HUNTER_EXAM_TRAINING_EVENTS(asNumber(i -> i.getHunterExamTraining().getHunterExamTrainingEvents())),
    NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS(asNumber(i -> i.getHunterExamTraining().getNonSubsidizableHunterExamTrainingEvents())),
    HUNTER_EXAM_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterExamTraining().getHunterExamTrainingParticipants())),
    NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterExamTraining().getNonSubsidizableHunterExamTrainingParticipants())),

    // JHT TRAINING EVENTS

    SHOOTING_TEST_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getShootingTestTrainingEvents())),
    SHOOTING_TEST_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getShootingTestTrainingParticipants())),
    HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getHunterExamOfficialTrainingEvents())),
    HUNTER_EXAM_OFFICIAL_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getHunterExamOfficialTrainingParticipants())),
    GAME_DAMAGE_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getGameDamageTrainingEvents())),
    GAME_DAMAGE_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getGameDamageTrainingParticipants())),
    HUNTING_CONTROL_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getHuntingControlTrainingEvents())),
    HUNTING_CONTROL_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getHuntingControlTrainingParticipants())),

    NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableShootingTestTrainingEvents())),
    NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableShootingTestTrainingParticipants())),
    NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableHunterExamOfficialTrainingEvents())),
    NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableHunterExamOfficialTrainingParticipants())),
    NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableGameDamageTrainingEvents())),
    NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableGameDamageTrainingParticipants())),
    NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_EVENTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableHuntingControlTrainingEvents())),
    NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_PARTICIPANTS(asNumber(i -> i.getJhtTraining().getNonSubsidizableHuntingControlTrainingParticipants())),

    // HUNTER TRAINING EVENTS

    MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getMooselikeHuntingLeaderTrainingEvents())),
    MOOSELIKE_HUNTING_LEADER_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getMooselikeHuntingLeaderTrainingParticipants())),
    CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getCarnivoreHuntingLeaderTrainingEvents())),
    CARNIVORE_HUNTING_LEADER_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getCarnivoreHuntingLeaderTrainingParticipants())),
    MOOSELIKE_HUNTING_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getMooselikeHuntingTrainingEvents())),
    MOOSELIKE_HUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getMooselikeHuntingTrainingParticipants())),
    CARNIVORE_HUNTING_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getCarnivoreHuntingTrainingEvents())),
    CARNIVORE_HUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getCarnivoreHuntingTrainingParticipants())),
    SRVA_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getSrvaTrainingEvents())),
    SRVA_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getSrvaTrainingParticipants())),
    CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getCarnivoreContactPersonTrainingEvents())),
    CARNIVORE_CONTACT_PERSON_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getCarnivoreContactPersonTrainingParticipants())),
    ACCIDENT_PREVENTION_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getAccidentPreventionTrainingEvents())),
    ACCIDENT_PREVENTION_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getAccidentPreventionTrainingParticipants())),

    NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableMooselikeHuntingLeaderTrainingEvents())),
    NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableMooselikeHuntingLeaderTrainingParticipants())),
    NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableCarnivoreHuntingLeaderTrainingEvents())),
    NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants())),
    NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableMooselikeHuntingTrainingEvents())),
    NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableMooselikeHuntingTrainingParticipants())),
    NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableCarnivoreHuntingTrainingEvents())),
    NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableCarnivoreHuntingTrainingParticipants())),
    NON_SUBSIDIZABLE_SRVA_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableSrvaTrainingEvents())),
    NON_SUBSIDIZABLE_SRVA_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableSrvaTrainingParticipants())),
    NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableCarnivoreContactPersonTrainingEvents())),
    NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableCarnivoreContactPersonTrainingParticipants())),
    NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_EVENTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableAccidentPreventionTrainingEvents())),
    NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_PARTICIPANTS(asNumber(i -> i.getHunterTraining().getNonSubsidizableAccidentPreventionTrainingParticipants())),

    // YOUTH TRAINING EVENTS

    SCHOOL_TRAINING_EVENTS(asNumber(i -> i.getYouthTraining().getSchoolTrainingEvents())),
    SCHOOL_TRAINING_PARTICIPANTS(asNumber(i -> i.getYouthTraining().getSchoolTrainingParticipants())),
    COLLEGE_TRAINING_EVENTS(asNumber(i -> i.getYouthTraining().getCollegeTrainingEvents())),
    COLLEGE_TRAINING_PARTICIPANTS(asNumber(i -> i.getYouthTraining().getCollegeTrainingParticipants())),
    OTHER_YOUTH_TARGETED_TRAINING_EVENTS(asNumber(i -> i.getYouthTraining().getOtherYouthTargetedTrainingEvents())),
    OTHER_YOUTH_TARGETED_TRAINING_PARTICIPANTS(asNumber(i -> i.getYouthTraining().getOtherYouthTargetedTrainingParticipants())),

    NON_SUBSIDIZABLE_SCHOOL_TRAINING_EVENTS(asNumber(i -> i.getYouthTraining().getNonSubsidizableSchoolTrainingEvents())),
    NON_SUBSIDIZABLE_SCHOOL_TRAINING_PARTICIPANTS(asNumber(i -> i.getYouthTraining().getNonSubsidizableSchoolTrainingParticipants())),
    NON_SUBSIDIZABLE_COLLEGE_TRAINING_EVENTS(asNumber(i -> i.getYouthTraining().getNonSubsidizableCollegeTrainingEvents())),
    NON_SUBSIDIZABLE_COLLEGE_TRAINING_PARTICIPANTS(asNumber(i -> i.getYouthTraining().getNonSubsidizableCollegeTrainingParticipants())),
    NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_EVENTS(asNumber(i -> i.getYouthTraining().getNonSubsidizableOtherYouthTargetedTrainingEvents())),
    NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_PARTICIPANTS(asNumber(i -> i.getYouthTraining().getNonSubsidizableOtherYouthTargetedTrainingParticipants())),

    // OTHER HUNTER TRAINING EVENTS

    SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getSmallCarnivoreHuntingTrainingEvents())),
    SMALL_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getSmallCarnivoreHuntingTrainingParticipants())),
    GAME_COUNTING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getGameCountingTrainingEvents())),
    GAME_COUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getGameCountingTrainingParticipants())),
    GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getGamePopulationManagementTrainingEvents())),
    GAME_POPULATION_MANAGEMENT_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getGamePopulationManagementTrainingParticipants())),
    GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getGameEnvironmentalCareTrainingEvents())),
    GAME_ENVIRONMENTAL_CARE_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getGameEnvironmentalCareTrainingParticipants())),
    OTHER_GAMEKEEPING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getOtherGamekeepingTrainingEvents())),
    OTHER_GAMEKEEPING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getOtherGamekeepingTrainingParticipants())),
    SHOOTING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getShootingTrainingEvents())),
    SHOOTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getShootingTrainingParticipants())),

    NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableSmallCarnivoreHuntingTrainingEvents())),
    NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableSmallCarnivoreHuntingTrainingParticipants())),
    NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableGameCountingTrainingEvents())),
    NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableGameCountingTrainingParticipants())),
    NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableGamePopulationManagementTrainingEvents())),
    NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableGamePopulationManagementTrainingParticipants())),
    NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableGameEnvironmentalCareTrainingEvents())),
    NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableGameEnvironmentalCareTrainingParticipants())),
    NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableOtherGamekeepingTrainingEvents())),
    NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableOtherGamekeepingTrainingParticipants())),
    NON_SUBSIDIZABLE_SHOOTING_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableShootingTrainingEvents())),
    NON_SUBSIDIZABLE_SHOOTING_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableShootingTrainingParticipants())),

    // Overridden by SHOOTING_TRAINING_EVENTS
    OTHER_SHOOTING_TRAINING_EVENTS_2017(asNumber(i -> i.getOtherHunterTraining().getShootingTrainingEvents())),
    // Overridden by SHOOTING_TRAINING_PARTICIPANTS
    OTHER_SHOOTING_TRAINING_PARTICIPANTS_2017(asNumber(i -> i.getOtherHunterTraining().getShootingTrainingParticipants())),

    TRACKER_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getTrackerTrainingEvents())),
    TRACKER_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getTrackerTrainingParticipants())),

    NON_SUBSIDIZABLE_TRACKER_TRAINING_EVENTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableTrackerTrainingEvents())),
    NON_SUBSIDIZABLE_TRACKER_TRAINING_PARTICIPANTS(asNumber(i -> i.getOtherHunterTraining().getNonSubsidizableTrackerTrainingParticipants())),

    // PUBLIC EVENTS

    PUBLIC_EVENTS(asNumber(i -> i.getPublicEvents().getPublicEvents())),
    PUBLIC_EVENT_PARTICIPANTS(asNumber(i -> i.getPublicEvents().getPublicEventParticipants())),

    PUBLIC_EVENTS_2018(asNumber(i -> i.getPublicEvents().getPublicEvents())),
    PUBLIC_EVENT_PARTICIPANTS_2018(asNumber(i -> i.getPublicEvents().getPublicEventParticipants())),

    // Overridden by PUBLIC_EVENTS
    OTHER_TRAINING_EVENTS_2017(asNumber(i -> i.getPublicEvents().getPublicEvents())),
    // Overridden by PUBLIC_EVENT_PARTICIPANTS 
    OTHER_TRAINING_PARTICIPANTS_2017(asNumber(i -> i.getPublicEvents().getPublicEventParticipants())),

    // OTHER HUNTING RELATED

    HARVEST_PERMIT_APPLICATION_PARTNERS(asNumber(i -> i.getOtherHuntingRelated().getHarvestPermitApplicationPartners())),
    MOOSELIKE_TAXATION_PLANNING_EVENTS(asNumber(i -> i.getOtherHuntingRelated().getMooselikeTaxationPlanningEvents())),

    WOLF_TERRITORY_WORKGROUPS(asNumber(i -> i.getOtherHuntingRelated().getWolfTerritoryWorkgroups())),
    // Overridden by WOLF_TERRITORY_WORKGROUPS
    WOLF_TERRITORY_WORKGROUP_LEADS_2017(asNumber(i -> i.getOtherHuntingRelated().getWolfTerritoryWorkgroups())),

    // COMMUNICATION

    INTERVIEWS(asNumber(i -> i.getCommunication().getInterviews())),
    ANNOUNCEMENTS(asNumber(i -> i.getCommunication().getAnnouncements())),
    OMARIISTA_ANNOUNCEMENTS(asNumber(i -> i.getCommunication().getOmariistaAnnouncements())),
    WWW(asText(i -> i.getCommunication().getHomePage())),
    SOME_CHANNELS(asText(i -> i.getCommunication().getSomeInfo())),
    //COMMUNICATION_FREE_TEXT(asText(i -> i.getCommunication().getInfo())),

    // SHOOTING RANGES

    MOOSE_RANGES(asNumber(i -> i.getShootingRanges().getMooseRanges())),
    SHOTGUN_RANGES(asNumber(i -> i.getShootingRanges().getShotgunRanges())),
    RIFLE_RANGES(asNumber(i -> i.getShootingRanges().getRifleRanges())),
    OTHER_SHOOTING_RANGES(asNumber(i -> i.getShootingRanges().getOtherShootingRanges())),

    // LUKE

    TOTAL_GAME_TRIANGLES(asNumber(i -> i.getLuke().sumOfWinterAndSummerGameTriangles())),
    WINTER_GAME_TRIANGLES(asNumber(i -> i.getLuke().getWinterGameTriangles())),
    SUMMER_GAME_TRIANGLES(asNumber(i -> i.getLuke().getSummerGameTriangles())),
    FIELD_TRIANGLES(asNumber(i -> i.getLuke().getFieldTriangles())),

    WATER_BIRD_BROOD_CALCULATION_LOCATIONS(asNumber(i -> i.getLuke().getWaterBirdBroods())),
    WATER_BIRD_COUPLE_CALCULATION_LOCATIONS(asNumber(i -> i.getLuke().getWaterBirdCouples())),
    // Overridden by WATER_BIRD_COUPLE_CALCULATION_LOCATIONS
    WATER_BIRD_CALCULATION_LOCATIONS_2017(asNumber(i -> i.getLuke().getWaterBirdCouples())),

    TOTAL_WATER_BIRD_CALCULATION_LOCATIONS(asNumber(i -> i.getLuke().sumOfWaterBirdCalculationLocations())),

    NORTHERN_LAPLAND_WILLOW_GROUSE_LINES(asNumber(i -> i.getLuke().getNorthernLaplandWillowGrouseLines())),

    LUKE_CARNIVORE_CONTACT_PERSONS(asNumber(i -> i.getLuke().getCarnivoreContactPersons())),
    LUKE_CARNIVORE_DNA_COLLECTORS(asNumber(i -> i.getLuke().getCarnivoreDnaCollectors())),
    TOTAL_LUKE_CARNIVORE_PERSONS(asNumber(i -> i.getLuke().sumOfCarnivorePersons())),

    // METSÄHALLITUS

    SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS(asNumber(i -> i.getMetsahallitus().getSmallGameLicensesSoldByMetsahallitus()));

    private final Function<AnnualStatisticsExportDTO, Either<Number, String>> valueExtractor;

    private static Function<AnnualStatisticsExportDTO, Either<Number, String>> asNumber(
            final Function<AnnualStatisticsExportDTO, Number> numberExtractor) {

        return numberExtractor.andThen(Either::left);
    }

    private static Function<AnnualStatisticsExportDTO, Either<Number, String>> asText(
            final Function<AnnualStatisticsExportDTO, String> textExtractor) {

        return textExtractor.andThen(Either::right);
    }

    AnnualStatisticItem(final Function<AnnualStatisticsExportDTO, Either<Number, String>> valueExtractor) {
        this.valueExtractor = requireNonNull(valueExtractor);
    }

    @Nonnull
    public Either<Number, String> extractValue(final AnnualStatisticsExportDTO dto) {
        return valueExtractor.apply(dto);
    }

    @Nullable
    public Integer extractInteger(final AnnualStatisticsExportDTO statistics) {
        final Number number = extractValue(statistics)
                .swap()
                .getOrElseThrow(() -> new IllegalArgumentException("Expected number value for statistic item"));

        if (number == null) {
            return null;
        }

        final int integer = number.intValue();

        if (integer < 0) {
            throw new IllegalArgumentException("Negative number not allowed: " + integer);
        }

        return Integer.valueOf(integer);
    }
}
