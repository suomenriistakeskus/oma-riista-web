package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;

import com.querydsl.spatial.*;



/**
 * SQRhyAnnualStatistics is a Querydsl query type for SQRhyAnnualStatistics
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRhyAnnualStatistics extends RelationalPathSpatial<SQRhyAnnualStatistics> {

    private static final long serialVersionUID = 1410168274;

    public static final SQRhyAnnualStatistics rhyAnnualStatistics = new SQRhyAnnualStatistics("rhy_annual_statistics");

    public final NumberPath<Integer> accidentPreventionTrainingEvents = createNumber("accidentPreventionTrainingEvents", Integer.class);

    public final NumberPath<Integer> accidentPreventionTrainingParticipants = createNumber("accidentPreventionTrainingParticipants", Integer.class);

    public final NumberPath<Integer> allBearAttempts = createNumber("allBearAttempts", Integer.class);

    public final NumberPath<Integer> allBowAttempts = createNumber("allBowAttempts", Integer.class);

    public final NumberPath<Integer> allMooseAttempts = createNumber("allMooseAttempts", Integer.class);

    public final NumberPath<Integer> allRoeDeerAttempts = createNumber("allRoeDeerAttempts", Integer.class);

    public final NumberPath<Integer> announcements = createNumber("announcements", Integer.class);

    public final NumberPath<Integer> bowTestEvents = createNumber("bowTestEvents", Integer.class);

    public final DateTimePath<java.sql.Timestamp> bowTestEventsLastOverridden = createDateTime("bowTestEventsLastOverridden", java.sql.Timestamp.class);

    public final NumberPath<Integer> carnivoreContactPersonTrainingEvents = createNumber("carnivoreContactPersonTrainingEvents", Integer.class);

    public final NumberPath<Integer> carnivoreContactPersonTrainingParticipants = createNumber("carnivoreContactPersonTrainingParticipants", Integer.class);

    public final NumberPath<Integer> carnivoreHuntingLeaderTrainingEvents = createNumber("carnivoreHuntingLeaderTrainingEvents", Integer.class);

    public final NumberPath<Integer> carnivoreHuntingLeaderTrainingParticipants = createNumber("carnivoreHuntingLeaderTrainingParticipants", Integer.class);

    public final NumberPath<Integer> carnivoreHuntingTrainingEvents = createNumber("carnivoreHuntingTrainingEvents", Integer.class);

    public final NumberPath<Integer> carnivoreHuntingTrainingParticipants = createNumber("carnivoreHuntingTrainingParticipants", Integer.class);

    public final NumberPath<Integer> collegeTrainingEvents = createNumber("collegeTrainingEvents", Integer.class);

    public final NumberPath<Integer> collegeTrainingParticipants = createNumber("collegeTrainingParticipants", Integer.class);

    public final DateTimePath<java.sql.Timestamp> communicationLastModified = createDateTime("communicationLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Integer> failedHunterExams = createNumber("failedHunterExams", Integer.class);

    public final NumberPath<Integer> firearmTestEvents = createNumber("firearmTestEvents", Integer.class);

    public final DateTimePath<java.sql.Timestamp> firearmTestEventsLastOverridden = createDateTime("firearmTestEventsLastOverridden", java.sql.Timestamp.class);

    public final NumberPath<Integer> gameCountingTrainingEvents = createNumber("gameCountingTrainingEvents", Integer.class);

    public final NumberPath<Integer> gameCountingTrainingParticipants = createNumber("gameCountingTrainingParticipants", Integer.class);

    public final NumberPath<Integer> gameDamageInspectors = createNumber("gameDamageInspectors", Integer.class);

    public final DateTimePath<java.sql.Timestamp> gameDamageLastModified = createDateTime("gameDamageLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> gameEnvironmentalCareTrainingEvents = createNumber("gameEnvironmentalCareTrainingEvents", Integer.class);

    public final NumberPath<Integer> gameEnvironmentalCareTrainingParticipants = createNumber("gameEnvironmentalCareTrainingParticipants", Integer.class);

    public final NumberPath<Integer> gamePopulationManagementTrainingEvents = createNumber("gamePopulationManagementTrainingEvents", Integer.class);

    public final NumberPath<Integer> gamePopulationManagementTrainingParticipants = createNumber("gamePopulationManagementTrainingParticipants", Integer.class);

    public final NumberPath<Integer> grantedRecreationalShootingCertificates = createNumber("grantedRecreationalShootingCertificates", Integer.class);

    public final NumberPath<Integer> harvestPermitApplicationPartners = createNumber("harvestPermitApplicationPartners", Integer.class);

    public final StringPath homePage = createString("homePage");

    public final NumberPath<Integer> hunterExamEvents = createNumber("hunterExamEvents", Integer.class);

    public final DateTimePath<java.sql.Timestamp> hunterExamEventsLastOverridden = createDateTime("hunterExamEventsLastOverridden", java.sql.Timestamp.class);

    public final NumberPath<Integer> hunterExamOfficials = createNumber("hunterExamOfficials", Integer.class);

    public final DateTimePath<java.sql.Timestamp> hunterExamsLastModified = createDateTime("hunterExamsLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> hunterExamTrainingEvents = createNumber("hunterExamTrainingEvents", Integer.class);

    public final DateTimePath<java.sql.Timestamp> hunterExamTrainingEventsLastOverridden = createDateTime("hunterExamTrainingEventsLastOverridden", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> hunterExamTrainingLastModified = createDateTime("hunterExamTrainingLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> hunterExamTrainingParticipants = createNumber("hunterExamTrainingParticipants", Integer.class);

    public final NumberPath<Integer> huntingControlCustomers = createNumber("huntingControlCustomers", Integer.class);

    public final NumberPath<Integer> huntingControlEvents = createNumber("huntingControlEvents", Integer.class);

    public final DateTimePath<java.sql.Timestamp> huntingControlLastModified = createDateTime("huntingControlLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> huntingControllers = createNumber("huntingControllers", Integer.class);

    public final StringPath iban = createString("iban");

    public final StringPath info = createString("info");

    public final NumberPath<Integer> interviews = createNumber("interviews", Integer.class);

    public final NumberPath<Integer> jhtGameDamageTrainingEvents = createNumber("jhtGameDamageTrainingEvents", Integer.class);

    public final NumberPath<Integer> jhtGameDamageTrainingParticipants = createNumber("jhtGameDamageTrainingParticipants", Integer.class);

    public final NumberPath<Integer> jhtHunterExamTrainingEvents = createNumber("jhtHunterExamTrainingEvents", Integer.class);

    public final NumberPath<Integer> jhtHunterExamTrainingParticipants = createNumber("jhtHunterExamTrainingParticipants", Integer.class);

    public final NumberPath<Integer> jhtHuntingControlTrainingEvents = createNumber("jhtHuntingControlTrainingEvents", Integer.class);

    public final NumberPath<Integer> jhtHuntingControlTrainingParticipants = createNumber("jhtHuntingControlTrainingParticipants", Integer.class);

    public final NumberPath<Integer> jhtShootingTestTrainingEvents = createNumber("jhtShootingTestTrainingEvents", Integer.class);

    public final NumberPath<Integer> jhtShootingTestTrainingParticipants = createNumber("jhtShootingTestTrainingParticipants", Integer.class);

    public final DateTimePath<java.sql.Timestamp> jhtTrainingsLastModified = createDateTime("jhtTrainingsLastModified", java.sql.Timestamp.class);

    public final NumberPath<java.math.BigDecimal> largeCarnivoreDamageInspectionExpenses = createNumber("largeCarnivoreDamageInspectionExpenses", java.math.BigDecimal.class);

    public final NumberPath<Integer> largeCarnivoreDamageInspectionLocations = createNumber("largeCarnivoreDamageInspectionLocations", Integer.class);

    public final DateTimePath<java.sql.Timestamp> lockedTime = createDateTime("lockedTime", java.sql.Timestamp.class);

    public final NumberPath<Integer> lukeFieldTriangles = createNumber("lukeFieldTriangles", Integer.class);

    public final DateTimePath<java.sql.Timestamp> lukeGameCalculationsLastModified = createDateTime("lukeGameCalculationsLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> lukeSummerGameTriangles = createNumber("lukeSummerGameTriangles", Integer.class);

    public final NumberPath<Integer> lukeWaterBirds = createNumber("lukeWaterBirds", Integer.class);

    public final NumberPath<Integer> lukeWinterGameTriangles = createNumber("lukeWinterGameTriangles", Integer.class);

    public final DateTimePath<java.sql.Timestamp> mhLastModified = createDateTime("mhLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> mhSmallGameSoldLicenses = createNumber("mhSmallGameSoldLicenses", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<java.math.BigDecimal> mooselikeDamageInspectionExpenses = createNumber("mooselikeDamageInspectionExpenses", java.math.BigDecimal.class);

    public final NumberPath<Integer> mooselikeDamageInspectionLocations = createNumber("mooselikeDamageInspectionLocations", Integer.class);

    public final NumberPath<Integer> mooselikeHuntingLeaderTrainingEvents = createNumber("mooselikeHuntingLeaderTrainingEvents", Integer.class);

    public final NumberPath<Integer> mooselikeHuntingLeaderTrainingParticipants = createNumber("mooselikeHuntingLeaderTrainingParticipants", Integer.class);

    public final NumberPath<Integer> mooselikeHuntingTrainingEvents = createNumber("mooselikeHuntingTrainingEvents", Integer.class);

    public final NumberPath<Integer> mooselikeHuntingTrainingParticipants = createNumber("mooselikeHuntingTrainingParticipants", Integer.class);

    public final NumberPath<Integer> mooseRanges = createNumber("mooseRanges", Integer.class);

    public final NumberPath<Integer> mutualAckShootingCertificates = createNumber("mutualAckShootingCertificates", Integer.class);

    public final NumberPath<Integer> omariistaAnnouncements = createNumber("omariistaAnnouncements", Integer.class);

    public final NumberPath<Integer> operationalLandAreaSize = createNumber("operationalLandAreaSize", Integer.class);

    public final DateTimePath<java.sql.Timestamp> otherAdminDataLastModified = createDateTime("otherAdminDataLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> otherGamekeepingTrainingEvents = createNumber("otherGamekeepingTrainingEvents", Integer.class);

    public final NumberPath<Integer> otherGamekeepingTrainingParticipants = createNumber("otherGamekeepingTrainingParticipants", Integer.class);

    public final DateTimePath<java.sql.Timestamp> otherHunterTrainingsLastModified = createDateTime("otherHunterTrainingsLastModified", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> otherHuntingRelatedLastModified = createDateTime("otherHuntingRelatedLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> otherShootingRanges = createNumber("otherShootingRanges", Integer.class);

    public final NumberPath<Integer> otherShootingTrainingEvents = createNumber("otherShootingTrainingEvents", Integer.class);

    public final NumberPath<Integer> otherShootingTrainingParticipants = createNumber("otherShootingTrainingParticipants", Integer.class);

    public final NumberPath<Integer> otherTrainingEvents = createNumber("otherTrainingEvents", Integer.class);

    public final DateTimePath<java.sql.Timestamp> otherTrainingLastModified = createDateTime("otherTrainingLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> otherTrainingParticipants = createNumber("otherTrainingParticipants", Integer.class);

    public final NumberPath<Integer> otherYouthTargetedTrainingEvents = createNumber("otherYouthTargetedTrainingEvents", Integer.class);

    public final NumberPath<Integer> otherYouthTargetedTrainingParticipants = createNumber("otherYouthTargetedTrainingParticipants", Integer.class);

    public final NumberPath<Integer> passedHunterExams = createNumber("passedHunterExams", Integer.class);

    public final NumberPath<Integer> proofOrders = createNumber("proofOrders", Integer.class);

    public final NumberPath<Integer> qualifiedBearAttempts = createNumber("qualifiedBearAttempts", Integer.class);

    public final NumberPath<Integer> qualifiedBowAttempts = createNumber("qualifiedBowAttempts", Integer.class);

    public final NumberPath<Integer> qualifiedMooseAttempts = createNumber("qualifiedMooseAttempts", Integer.class);

    public final NumberPath<Integer> qualifiedRoeDeerAttempts = createNumber("qualifiedRoeDeerAttempts", Integer.class);

    public final NumberPath<Long> rhyAnnualStatisticsId = createNumber("rhyAnnualStatisticsId", Long.class);

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final NumberPath<Integer> rhyMembers = createNumber("rhyMembers", Integer.class);

    public final NumberPath<Integer> rifleRanges = createNumber("rifleRanges", Integer.class);

    public final NumberPath<Integer> schoolTrainingEvents = createNumber("schoolTrainingEvents", Integer.class);

    public final NumberPath<Integer> schoolTrainingParticipants = createNumber("schoolTrainingParticipants", Integer.class);

    public final DateTimePath<java.sql.Timestamp> shootingRangesLastModified = createDateTime("shootingRangesLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> shootingTestOfficials = createNumber("shootingTestOfficials", Integer.class);

    public final DateTimePath<java.sql.Timestamp> shootingTestsLastModified = createDateTime("shootingTestsLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> shotgunRanges = createNumber("shotgunRanges", Integer.class);

    public final NumberPath<Integer> smallCarnivoreHuntingTrainingEvents = createNumber("smallCarnivoreHuntingTrainingEvents", Integer.class);

    public final NumberPath<Integer> smallCarnivoreHuntingTrainingParticipants = createNumber("smallCarnivoreHuntingTrainingParticipants", Integer.class);

    public final StringPath someInfo = createString("someInfo");

    public final NumberPath<Integer> srvaBearAccidents = createNumber("srvaBearAccidents", Integer.class);

    public final NumberPath<Integer> srvaBearDeportations = createNumber("srvaBearDeportations", Integer.class);

    public final NumberPath<Integer> srvaBearInjuries = createNumber("srvaBearInjuries", Integer.class);

    public final NumberPath<Integer> srvaFallowDeerAccidents = createNumber("srvaFallowDeerAccidents", Integer.class);

    public final NumberPath<Integer> srvaFallowDeerDeportations = createNumber("srvaFallowDeerDeportations", Integer.class);

    public final NumberPath<Integer> srvaFallowDeerInjuries = createNumber("srvaFallowDeerInjuries", Integer.class);

    public final NumberPath<Integer> srvaLynxAccidents = createNumber("srvaLynxAccidents", Integer.class);

    public final NumberPath<Integer> srvaLynxDeportations = createNumber("srvaLynxDeportations", Integer.class);

    public final NumberPath<Integer> srvaLynxInjuries = createNumber("srvaLynxInjuries", Integer.class);

    public final NumberPath<Integer> srvaMooseAccidents = createNumber("srvaMooseAccidents", Integer.class);

    public final NumberPath<Integer> srvaMooseDeportations = createNumber("srvaMooseDeportations", Integer.class);

    public final NumberPath<Integer> srvaMooseInjuries = createNumber("srvaMooseInjuries", Integer.class);

    public final NumberPath<Integer> srvaOtherAccidents = createNumber("srvaOtherAccidents", Integer.class);

    public final NumberPath<Integer> srvaParticipants = createNumber("srvaParticipants", Integer.class);

    public final NumberPath<Integer> srvaRailwayAccidents = createNumber("srvaRailwayAccidents", Integer.class);

    public final NumberPath<Integer> srvaRoeDeerAccidents = createNumber("srvaRoeDeerAccidents", Integer.class);

    public final NumberPath<Integer> srvaRoeDeerDeportations = createNumber("srvaRoeDeerDeportations", Integer.class);

    public final NumberPath<Integer> srvaRoeDeerInjuries = createNumber("srvaRoeDeerInjuries", Integer.class);

    public final NumberPath<Integer> srvaTotalWorkHours = createNumber("srvaTotalWorkHours", Integer.class);

    public final NumberPath<Integer> srvaTrafficAccidents = createNumber("srvaTrafficAccidents", Integer.class);

    public final NumberPath<Integer> srvaTrainingEvents = createNumber("srvaTrainingEvents", Integer.class);

    public final NumberPath<Integer> srvaTrainingParticipants = createNumber("srvaTrainingParticipants", Integer.class);

    public final NumberPath<Integer> srvaWhiteTailedDeerAccidents = createNumber("srvaWhiteTailedDeerAccidents", Integer.class);

    public final NumberPath<Integer> srvaWhiteTailedDeerDeportations = createNumber("srvaWhiteTailedDeerDeportations", Integer.class);

    public final NumberPath<Integer> srvaWhiteTailedDeerInjuries = createNumber("srvaWhiteTailedDeerInjuries", Integer.class);

    public final NumberPath<Integer> srvaWildBoarAccidents = createNumber("srvaWildBoarAccidents", Integer.class);

    public final NumberPath<Integer> srvaWildBoarDeportations = createNumber("srvaWildBoarDeportations", Integer.class);

    public final NumberPath<Integer> srvaWildBoarInjuries = createNumber("srvaWildBoarInjuries", Integer.class);

    public final NumberPath<Integer> srvaWildForestReindeerAccidents = createNumber("srvaWildForestReindeerAccidents", Integer.class);

    public final NumberPath<Integer> srvaWildForestReindeerDeportations = createNumber("srvaWildForestReindeerDeportations", Integer.class);

    public final NumberPath<Integer> srvaWildForestReindeerInjuries = createNumber("srvaWildForestReindeerInjuries", Integer.class);

    public final NumberPath<Integer> srvaWolfAccidents = createNumber("srvaWolfAccidents", Integer.class);

    public final NumberPath<Integer> srvaWolfDeportations = createNumber("srvaWolfDeportations", Integer.class);

    public final NumberPath<Integer> srvaWolfInjuries = createNumber("srvaWolfInjuries", Integer.class);

    public final NumberPath<Integer> srvaWolverineAccidents = createNumber("srvaWolverineAccidents", Integer.class);

    public final NumberPath<Integer> srvaWolverineDeportations = createNumber("srvaWolverineDeportations", Integer.class);

    public final NumberPath<Integer> srvaWolverineInjuries = createNumber("srvaWolverineInjuries", Integer.class);

    public final DateTimePath<java.sql.Timestamp> stateAidHunterTrainingsLastModified = createDateTime("stateAidHunterTrainingsLastModified", java.sql.Timestamp.class);

    public final NumberPath<Integer> trackerTrainingEvents = createNumber("trackerTrainingEvents", Integer.class);

    public final NumberPath<Integer> trackerTrainingParticipants = createNumber("trackerTrainingParticipants", Integer.class);

    public final NumberPath<Integer> wolfTerritoryWorkgroupLeads = createNumber("wolfTerritoryWorkgroupLeads", Integer.class);

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQRhyAnnualStatistics> rhyAnnualStatisticsPkey = createPrimaryKey(rhyAnnualStatisticsId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> rhyAnnualStatisticsRhyFk = createForeignKey(rhyId, "organisation_id");

    public SQRhyAnnualStatistics(String variable) {
        super(SQRhyAnnualStatistics.class, forVariable(variable), "public", "rhy_annual_statistics");
        addMetadata();
    }

    public SQRhyAnnualStatistics(String variable, String schema, String table) {
        super(SQRhyAnnualStatistics.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRhyAnnualStatistics(String variable, String schema) {
        super(SQRhyAnnualStatistics.class, forVariable(variable), schema, "rhy_annual_statistics");
        addMetadata();
    }

    public SQRhyAnnualStatistics(Path<? extends SQRhyAnnualStatistics> path) {
        super(path.getType(), path.getMetadata(), "public", "rhy_annual_statistics");
        addMetadata();
    }

    public SQRhyAnnualStatistics(PathMetadata metadata) {
        super(SQRhyAnnualStatistics.class, metadata, "public", "rhy_annual_statistics");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accidentPreventionTrainingEvents, ColumnMetadata.named("accident_prevention_training_events").withIndex(84).ofType(Types.INTEGER).withSize(10));
        addMetadata(accidentPreventionTrainingParticipants, ColumnMetadata.named("accident_prevention_training_participants").withIndex(85).ofType(Types.INTEGER).withSize(10));
        addMetadata(allBearAttempts, ColumnMetadata.named("all_bear_attempts").withIndex(28).ofType(Types.INTEGER).withSize(10));
        addMetadata(allBowAttempts, ColumnMetadata.named("all_bow_attempts").withIndex(32).ofType(Types.INTEGER).withSize(10));
        addMetadata(allMooseAttempts, ColumnMetadata.named("all_moose_attempts").withIndex(26).ofType(Types.INTEGER).withSize(10));
        addMetadata(allRoeDeerAttempts, ColumnMetadata.named("all_roe_deer_attempts").withIndex(30).ofType(Types.INTEGER).withSize(10));
        addMetadata(announcements, ColumnMetadata.named("announcements").withIndex(93).ofType(Types.INTEGER).withSize(10));
        addMetadata(bowTestEvents, ColumnMetadata.named("bow_test_events").withIndex(25).ofType(Types.INTEGER).withSize(10));
        addMetadata(bowTestEventsLastOverridden, ColumnMetadata.named("bow_test_events_last_overridden").withIndex(156).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(carnivoreContactPersonTrainingEvents, ColumnMetadata.named("carnivore_contact_person_training_events").withIndex(56).ofType(Types.INTEGER).withSize(10));
        addMetadata(carnivoreContactPersonTrainingParticipants, ColumnMetadata.named("carnivore_contact_person_training_participants").withIndex(57).ofType(Types.INTEGER).withSize(10));
        addMetadata(carnivoreHuntingLeaderTrainingEvents, ColumnMetadata.named("carnivore_hunting_leader_training_events").withIndex(52).ofType(Types.INTEGER).withSize(10));
        addMetadata(carnivoreHuntingLeaderTrainingParticipants, ColumnMetadata.named("carnivore_hunting_leader_training_participants").withIndex(53).ofType(Types.INTEGER).withSize(10));
        addMetadata(carnivoreHuntingTrainingEvents, ColumnMetadata.named("carnivore_hunting_training_events").withIndex(50).ofType(Types.INTEGER).withSize(10));
        addMetadata(carnivoreHuntingTrainingParticipants, ColumnMetadata.named("carnivore_hunting_training_participants").withIndex(51).ofType(Types.INTEGER).withSize(10));
        addMetadata(collegeTrainingEvents, ColumnMetadata.named("college_training_events").withIndex(78).ofType(Types.INTEGER).withSize(10));
        addMetadata(collegeTrainingParticipants, ColumnMetadata.named("college_training_participants").withIndex(79).ofType(Types.INTEGER).withSize(10));
        addMetadata(communicationLastModified, ColumnMetadata.named("communication_last_modified").withIndex(96).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(failedHunterExams, ColumnMetadata.named("failed_hunter_exams").withIndex(14).ofType(Types.INTEGER).withSize(10));
        addMetadata(firearmTestEvents, ColumnMetadata.named("firearm_test_events").withIndex(24).ofType(Types.INTEGER).withSize(10));
        addMetadata(firearmTestEventsLastOverridden, ColumnMetadata.named("firearm_test_events_last_overridden").withIndex(155).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gameCountingTrainingEvents, ColumnMetadata.named("game_counting_training_events").withIndex(63).ofType(Types.INTEGER).withSize(10));
        addMetadata(gameCountingTrainingParticipants, ColumnMetadata.named("game_counting_training_participants").withIndex(64).ofType(Types.INTEGER).withSize(10));
        addMetadata(gameDamageInspectors, ColumnMetadata.named("game_damage_inspectors").withIndex(22).ofType(Types.INTEGER).withSize(10));
        addMetadata(gameDamageLastModified, ColumnMetadata.named("game_damage_last_modified").withIndex(23).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gameEnvironmentalCareTrainingEvents, ColumnMetadata.named("game_environmental_care_training_events").withIndex(67).ofType(Types.INTEGER).withSize(10));
        addMetadata(gameEnvironmentalCareTrainingParticipants, ColumnMetadata.named("game_environmental_care_training_participants").withIndex(68).ofType(Types.INTEGER).withSize(10));
        addMetadata(gamePopulationManagementTrainingEvents, ColumnMetadata.named("game_population_management_training_events").withIndex(65).ofType(Types.INTEGER).withSize(10));
        addMetadata(gamePopulationManagementTrainingParticipants, ColumnMetadata.named("game_population_management_training_participants").withIndex(66).ofType(Types.INTEGER).withSize(10));
        addMetadata(grantedRecreationalShootingCertificates, ColumnMetadata.named("granted_recreational_shooting_certificates").withIndex(36).ofType(Types.INTEGER).withSize(10));
        addMetadata(harvestPermitApplicationPartners, ColumnMetadata.named("harvest_permit_application_partners").withIndex(90).ofType(Types.INTEGER).withSize(10));
        addMetadata(homePage, ColumnMetadata.named("home_page").withIndex(147).ofType(Types.VARCHAR).withSize(255));
        addMetadata(hunterExamEvents, ColumnMetadata.named("hunter_exam_events").withIndex(12).ofType(Types.INTEGER).withSize(10));
        addMetadata(hunterExamEventsLastOverridden, ColumnMetadata.named("hunter_exam_events_last_overridden").withIndex(154).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(hunterExamOfficials, ColumnMetadata.named("hunter_exam_officials").withIndex(15).ofType(Types.INTEGER).withSize(10));
        addMetadata(hunterExamsLastModified, ColumnMetadata.named("hunter_exams_last_modified").withIndex(16).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(hunterExamTrainingEvents, ColumnMetadata.named("hunter_exam_training_events").withIndex(87).ofType(Types.INTEGER).withSize(10));
        addMetadata(hunterExamTrainingEventsLastOverridden, ColumnMetadata.named("hunter_exam_training_events_last_overridden").withIndex(157).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(hunterExamTrainingLastModified, ColumnMetadata.named("hunter_exam_training_last_modified").withIndex(89).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(hunterExamTrainingParticipants, ColumnMetadata.named("hunter_exam_training_participants").withIndex(88).ofType(Types.INTEGER).withSize(10));
        addMetadata(huntingControlCustomers, ColumnMetadata.named("hunting_control_customers").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(huntingControlEvents, ColumnMetadata.named("hunting_control_events").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(huntingControlLastModified, ColumnMetadata.named("hunting_control_last_modified").withIndex(21).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(huntingControllers, ColumnMetadata.named("hunting_controllers").withIndex(20).ofType(Types.INTEGER).withSize(10));
        addMetadata(iban, ColumnMetadata.named("iban").withIndex(149).ofType(Types.CHAR).withSize(18));
        addMetadata(info, ColumnMetadata.named("info").withIndex(148).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(interviews, ColumnMetadata.named("interviews").withIndex(92).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtGameDamageTrainingEvents, ColumnMetadata.named("jht_game_damage_training_events").withIndex(43).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtGameDamageTrainingParticipants, ColumnMetadata.named("jht_game_damage_training_participants").withIndex(44).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtHunterExamTrainingEvents, ColumnMetadata.named("jht_hunter_exam_training_events").withIndex(41).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtHunterExamTrainingParticipants, ColumnMetadata.named("jht_hunter_exam_training_participants").withIndex(42).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtHuntingControlTrainingEvents, ColumnMetadata.named("jht_hunting_control_training_events").withIndex(45).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtHuntingControlTrainingParticipants, ColumnMetadata.named("jht_hunting_control_training_participants").withIndex(46).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtShootingTestTrainingEvents, ColumnMetadata.named("jht_shooting_test_training_events").withIndex(39).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtShootingTestTrainingParticipants, ColumnMetadata.named("jht_shooting_test_training_participants").withIndex(40).ofType(Types.INTEGER).withSize(10));
        addMetadata(jhtTrainingsLastModified, ColumnMetadata.named("jht_trainings_last_modified").withIndex(47).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(largeCarnivoreDamageInspectionExpenses, ColumnMetadata.named("large_carnivore_damage_inspection_expenses").withIndex(137).ofType(Types.NUMERIC).withSize(9).withDigits(2));
        addMetadata(largeCarnivoreDamageInspectionLocations, ColumnMetadata.named("large_carnivore_damage_inspection_locations").withIndex(136).ofType(Types.INTEGER).withSize(10));
        addMetadata(lockedTime, ColumnMetadata.named("locked_time").withIndex(11).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(lukeFieldTriangles, ColumnMetadata.named("luke_field_triangles").withIndex(140).ofType(Types.INTEGER).withSize(10));
        addMetadata(lukeGameCalculationsLastModified, ColumnMetadata.named("luke_game_calculations_last_modified").withIndex(142).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(lukeSummerGameTriangles, ColumnMetadata.named("luke_summer_game_triangles").withIndex(139).ofType(Types.INTEGER).withSize(10));
        addMetadata(lukeWaterBirds, ColumnMetadata.named("luke_water_birds").withIndex(141).ofType(Types.INTEGER).withSize(10));
        addMetadata(lukeWinterGameTriangles, ColumnMetadata.named("luke_winter_game_triangles").withIndex(138).ofType(Types.INTEGER).withSize(10));
        addMetadata(mhLastModified, ColumnMetadata.named("mh_last_modified").withIndex(145).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(mhSmallGameSoldLicenses, ColumnMetadata.named("mh_small_game_sold_licenses").withIndex(143).ofType(Types.INTEGER).withSize(10));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooselikeDamageInspectionExpenses, ColumnMetadata.named("mooselike_damage_inspection_expenses").withIndex(135).ofType(Types.NUMERIC).withSize(9).withDigits(2));
        addMetadata(mooselikeDamageInspectionLocations, ColumnMetadata.named("mooselike_damage_inspection_locations").withIndex(134).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeHuntingLeaderTrainingEvents, ColumnMetadata.named("mooselike_hunting_leader_training_events").withIndex(59).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeHuntingLeaderTrainingParticipants, ColumnMetadata.named("mooselike_hunting_leader_training_participants").withIndex(60).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeHuntingTrainingEvents, ColumnMetadata.named("mooselike_hunting_training_events").withIndex(48).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeHuntingTrainingParticipants, ColumnMetadata.named("mooselike_hunting_training_participants").withIndex(49).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooseRanges, ColumnMetadata.named("moose_ranges").withIndex(97).ofType(Types.INTEGER).withSize(10));
        addMetadata(mutualAckShootingCertificates, ColumnMetadata.named("mutual_ack_shooting_certificates").withIndex(37).ofType(Types.INTEGER).withSize(10));
        addMetadata(omariistaAnnouncements, ColumnMetadata.named("omariista_announcements").withIndex(94).ofType(Types.INTEGER).withSize(10));
        addMetadata(operationalLandAreaSize, ColumnMetadata.named("operational_land_area_size").withIndex(150).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherAdminDataLastModified, ColumnMetadata.named("other_admin_data_last_modified").withIndex(38).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(otherGamekeepingTrainingEvents, ColumnMetadata.named("other_gamekeeping_training_events").withIndex(69).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherGamekeepingTrainingParticipants, ColumnMetadata.named("other_gamekeeping_training_participants").withIndex(70).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherHunterTrainingsLastModified, ColumnMetadata.named("other_hunter_trainings_last_modified").withIndex(75).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(otherHuntingRelatedLastModified, ColumnMetadata.named("other_hunting_related_last_modified").withIndex(91).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(otherShootingRanges, ColumnMetadata.named("other_shooting_ranges").withIndex(100).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherShootingTrainingEvents, ColumnMetadata.named("other_shooting_training_events").withIndex(71).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherShootingTrainingParticipants, ColumnMetadata.named("other_shooting_training_participants").withIndex(72).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherTrainingEvents, ColumnMetadata.named("other_training_events").withIndex(82).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherTrainingLastModified, ColumnMetadata.named("other_training_last_modified").withIndex(86).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(otherTrainingParticipants, ColumnMetadata.named("other_training_participants").withIndex(83).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherYouthTargetedTrainingEvents, ColumnMetadata.named("other_youth_targeted_training_events").withIndex(80).ofType(Types.INTEGER).withSize(10));
        addMetadata(otherYouthTargetedTrainingParticipants, ColumnMetadata.named("other_youth_targeted_training_participants").withIndex(81).ofType(Types.INTEGER).withSize(10));
        addMetadata(passedHunterExams, ColumnMetadata.named("passed_hunter_exams").withIndex(13).ofType(Types.INTEGER).withSize(10));
        addMetadata(proofOrders, ColumnMetadata.named("proof_orders").withIndex(19).ofType(Types.INTEGER).withSize(10));
        addMetadata(qualifiedBearAttempts, ColumnMetadata.named("qualified_bear_attempts").withIndex(29).ofType(Types.INTEGER).withSize(10));
        addMetadata(qualifiedBowAttempts, ColumnMetadata.named("qualified_bow_attempts").withIndex(33).ofType(Types.INTEGER).withSize(10));
        addMetadata(qualifiedMooseAttempts, ColumnMetadata.named("qualified_moose_attempts").withIndex(27).ofType(Types.INTEGER).withSize(10));
        addMetadata(qualifiedRoeDeerAttempts, ColumnMetadata.named("qualified_roe_deer_attempts").withIndex(31).ofType(Types.INTEGER).withSize(10));
        addMetadata(rhyAnnualStatisticsId, ColumnMetadata.named("rhy_annual_statistics_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(rhyMembers, ColumnMetadata.named("rhy_members").withIndex(146).ofType(Types.INTEGER).withSize(10));
        addMetadata(rifleRanges, ColumnMetadata.named("rifle_ranges").withIndex(99).ofType(Types.INTEGER).withSize(10));
        addMetadata(schoolTrainingEvents, ColumnMetadata.named("school_training_events").withIndex(76).ofType(Types.INTEGER).withSize(10));
        addMetadata(schoolTrainingParticipants, ColumnMetadata.named("school_training_participants").withIndex(77).ofType(Types.INTEGER).withSize(10));
        addMetadata(shootingRangesLastModified, ColumnMetadata.named("shooting_ranges_last_modified").withIndex(101).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(shootingTestOfficials, ColumnMetadata.named("shooting_test_officials").withIndex(34).ofType(Types.INTEGER).withSize(10));
        addMetadata(shootingTestsLastModified, ColumnMetadata.named("shooting_tests_last_modified").withIndex(35).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(shotgunRanges, ColumnMetadata.named("shotgun_ranges").withIndex(98).ofType(Types.INTEGER).withSize(10));
        addMetadata(smallCarnivoreHuntingTrainingEvents, ColumnMetadata.named("small_carnivore_hunting_training_events").withIndex(61).ofType(Types.INTEGER).withSize(10));
        addMetadata(smallCarnivoreHuntingTrainingParticipants, ColumnMetadata.named("small_carnivore_hunting_training_participants").withIndex(62).ofType(Types.INTEGER).withSize(10));
        addMetadata(someInfo, ColumnMetadata.named("some_info").withIndex(95).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(srvaBearAccidents, ColumnMetadata.named("srva_bear_accidents").withIndex(123).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaBearDeportations, ColumnMetadata.named("srva_bear_deportations").withIndex(124).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaBearInjuries, ColumnMetadata.named("srva_bear_injuries").withIndex(125).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaFallowDeerAccidents, ColumnMetadata.named("srva_fallow_deer_accidents").withIndex(114).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaFallowDeerDeportations, ColumnMetadata.named("srva_fallow_deer_deportations").withIndex(115).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaFallowDeerInjuries, ColumnMetadata.named("srva_fallow_deer_injuries").withIndex(116).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaLynxAccidents, ColumnMetadata.named("srva_lynx_accidents").withIndex(120).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaLynxDeportations, ColumnMetadata.named("srva_lynx_deportations").withIndex(121).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaLynxInjuries, ColumnMetadata.named("srva_lynx_injuries").withIndex(122).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaMooseAccidents, ColumnMetadata.named("srva_moose_accidents").withIndex(102).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaMooseDeportations, ColumnMetadata.named("srva_moose_deportations").withIndex(103).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaMooseInjuries, ColumnMetadata.named("srva_moose_injuries").withIndex(104).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaOtherAccidents, ColumnMetadata.named("srva_other_accidents").withIndex(153).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaParticipants, ColumnMetadata.named("srva_participants").withIndex(133).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaRailwayAccidents, ColumnMetadata.named("srva_railway_accidents").withIndex(152).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaRoeDeerAccidents, ColumnMetadata.named("srva_roe_deer_accidents").withIndex(108).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaRoeDeerDeportations, ColumnMetadata.named("srva_roe_deer_deportations").withIndex(109).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaRoeDeerInjuries, ColumnMetadata.named("srva_roe_deer_injuries").withIndex(110).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaTotalWorkHours, ColumnMetadata.named("srva_total_work_hours").withIndex(132).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaTrafficAccidents, ColumnMetadata.named("srva_traffic_accidents").withIndex(151).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaTrainingEvents, ColumnMetadata.named("srva_training_events").withIndex(54).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaTrainingParticipants, ColumnMetadata.named("srva_training_participants").withIndex(55).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWhiteTailedDeerAccidents, ColumnMetadata.named("srva_white_tailed_deer_accidents").withIndex(105).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWhiteTailedDeerDeportations, ColumnMetadata.named("srva_white_tailed_deer_deportations").withIndex(106).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWhiteTailedDeerInjuries, ColumnMetadata.named("srva_white_tailed_deer_injuries").withIndex(107).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWildBoarAccidents, ColumnMetadata.named("srva_wild_boar_accidents").withIndex(117).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWildBoarDeportations, ColumnMetadata.named("srva_wild_boar_deportations").withIndex(118).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWildBoarInjuries, ColumnMetadata.named("srva_wild_boar_injuries").withIndex(119).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWildForestReindeerAccidents, ColumnMetadata.named("srva_wild_forest_reindeer_accidents").withIndex(111).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWildForestReindeerDeportations, ColumnMetadata.named("srva_wild_forest_reindeer_deportations").withIndex(112).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWildForestReindeerInjuries, ColumnMetadata.named("srva_wild_forest_reindeer_injuries").withIndex(113).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWolfAccidents, ColumnMetadata.named("srva_wolf_accidents").withIndex(126).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWolfDeportations, ColumnMetadata.named("srva_wolf_deportations").withIndex(127).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWolfInjuries, ColumnMetadata.named("srva_wolf_injuries").withIndex(128).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWolverineAccidents, ColumnMetadata.named("srva_wolverine_accidents").withIndex(129).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWolverineDeportations, ColumnMetadata.named("srva_wolverine_deportations").withIndex(130).ofType(Types.INTEGER).withSize(10));
        addMetadata(srvaWolverineInjuries, ColumnMetadata.named("srva_wolverine_injuries").withIndex(131).ofType(Types.INTEGER).withSize(10));
        addMetadata(stateAidHunterTrainingsLastModified, ColumnMetadata.named("state_aid_hunter_trainings_last_modified").withIndex(58).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(trackerTrainingEvents, ColumnMetadata.named("tracker_training_events").withIndex(73).ofType(Types.INTEGER).withSize(10));
        addMetadata(trackerTrainingParticipants, ColumnMetadata.named("tracker_training_participants").withIndex(74).ofType(Types.INTEGER).withSize(10));
        addMetadata(wolfTerritoryWorkgroupLeads, ColumnMetadata.named("wolf_territory_workgroup_leads").withIndex(144).ofType(Types.INTEGER).withSize(10));
        addMetadata(year, ColumnMetadata.named("year").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

