package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;

import static fi.riista.util.F.mapNullable;
import static java.util.Objects.requireNonNull;

public interface RhyAnnualStatisticsTestDataPopulator extends ValueGeneratorMixin {

    default void populate(@Nonnull final RhyAnnualStatistics statistics) {
        requireNonNull(statistics);

        final int year = statistics.getYear();

        final RhyBasicInfo basicInfo = statistics.getOrCreateBasicInfo();
        basicInfo.setIban(iban());
        basicInfo.setOperationalLandAreaSize(12345);
        basicInfo.setRhyMembers(9876);

        final SrvaEventStatistics srva = statistics.getOrCreateSrva();

        final SrvaSpeciesCountStatistics accident = srva.getAccident();
        accident.setMooses(1);
        accident.setWhiteTailedDeers(2);
        accident.setRoeDeers(3);
        accident.setWildForestReindeers(4);
        accident.setFallowDeers(5);
        accident.setWildBoars(6);
        accident.setLynxes(7);
        accident.setBears(8);
        accident.setWolves(9);
        accident.setWolverines(10);
        accident.setOtherSpecies(11);

        srva.setTrafficAccidents(1 + 2 + 3 + 4 + 5); // mooselikes
        srva.setRailwayAccidents(7 + 8 + 9 + 10); // large carnivores
        srva.setOtherAccidents(6 + 11); // wild boars + other species

        final SrvaSpeciesCountStatistics deportation = srva.getDeportation();
        deportation.setMooses(11);
        deportation.setWhiteTailedDeers(12);
        deportation.setRoeDeers(13);
        deportation.setWildForestReindeers(14);
        deportation.setFallowDeers(15);
        deportation.setWildBoars(16);
        deportation.setLynxes(17);
        deportation.setBears(18);
        deportation.setWolves(19);
        deportation.setWolverines(20);
        deportation.setOtherSpecies(21);

        final SrvaSpeciesCountStatistics injury = srva.getInjury();
        injury.setMooses(21);
        injury.setWhiteTailedDeers(22);
        injury.setRoeDeers(23);
        injury.setWildForestReindeers(24);
        injury.setFallowDeers(25);
        injury.setWildBoars(26);
        injury.setLynxes(27);
        injury.setBears(28);
        injury.setWolves(29);
        injury.setWolverines(30);
        injury.setOtherSpecies(31);

        srva.setTotalSrvaWorkHours(31);
        srva.setSrvaParticipants(32);

        final HunterExamStatistics hunterExams = statistics.getOrCreateHunterExams();
        hunterExams.setHunterExamEvents(33);
        hunterExams.setPassedHunterExams(34);
        hunterExams.setFailedHunterExams(35);
        hunterExams.setHunterExamOfficials(36);

        final AnnualShootingTestStatistics shootingTests = statistics.getOrCreateShootingTests();
        shootingTests.setFirearmTestEvents(37);
        shootingTests.setBowTestEvents(38);
        shootingTests.setQualifiedMooseAttempts(39);
        shootingTests.setAllMooseAttempts(40);
        shootingTests.setQualifiedBearAttempts(41);
        shootingTests.setAllBearAttempts(42);
        shootingTests.setQualifiedRoeDeerAttempts(43);
        shootingTests.setAllRoeDeerAttempts(44);
        shootingTests.setQualifiedBowAttempts(45);
        shootingTests.setAllBowAttempts(46);
        shootingTests.setShootingTestOfficials(47);

        final GameDamageStatistics gameDamage = statistics.getOrCreateGameDamage();
        gameDamage.setMooselikeDamageInspectionLocations(48);
        gameDamage.setLargeCarnivoreDamageInspectionLocations(49);
        gameDamage.setGameDamageInspectors(50);

        final HuntingControlStatistics huntingControl = statistics.getOrCreateHuntingControl();
        huntingControl.setHuntingControlEvents(51);
        huntingControl.setHuntingControlCustomers(52);
        huntingControl.setProofOrders(53);
        huntingControl.setHuntingControllers(54);

        final OtherPublicAdminStatistics otherPublicAdmin = statistics.getOrCreateOtherPublicAdmin();
        otherPublicAdmin.setGrantedRecreationalShootingCertificates(55);
        otherPublicAdmin.setMutualAckShootingCertificates(56);

        final HunterExamTrainingStatistics hunterExamTraining = statistics.getOrCreateHunterExamTraining();
        hunterExamTraining.setHunterExamTrainingEvents(57);
        hunterExamTraining.setNonSubsidizableHunterExamTrainingEvents(2*57);
        hunterExamTraining.setHunterExamTrainingParticipants(58);
        hunterExamTraining.setNonSubsidizableHunterExamTrainingParticipants(2*58);

        final JHTTrainingStatistics jhtTraining = statistics.getOrCreateJhtTraining();
        jhtTraining.setShootingTestTrainingEvents(59);
        jhtTraining.setNonSubsidizableShootingTestTrainingEvents(2*59);
        jhtTraining.setShootingTestTrainingParticipants(60);
        jhtTraining.setNonSubsidizableShootingTestTrainingParticipants(2*60);
        jhtTraining.setHunterExamOfficialTrainingEvents(61);
        jhtTraining.setNonSubsidizableHunterExamOfficialTrainingEvents(2*61);
        jhtTraining.setHunterExamOfficialTrainingParticipants(62);
        jhtTraining.setNonSubsidizableHunterExamOfficialTrainingParticipants(2*62);
        jhtTraining.setGameDamageTrainingEvents(63);
        jhtTraining.setNonSubsidizableGameDamageTrainingEvents(2*63);
        jhtTraining.setGameDamageTrainingParticipants(64);
        jhtTraining.setNonSubsidizableGameDamageTrainingParticipants(2*64);
        jhtTraining.setHuntingControlTrainingEvents(65);
        jhtTraining.setNonSubsidizableHuntingControlTrainingEvents(2*65);
        jhtTraining.setHuntingControlTrainingParticipants(66);
        jhtTraining.setNonSubsidizableHuntingControlTrainingParticipants(2*66);

        final HunterTrainingStatistics hunterTraining = statistics.getOrCreateHunterTraining();
        hunterTraining.setMooselikeHuntingLeaderTrainingEvents(67);
        hunterTraining.setNonSubsidizableMooselikeHuntingLeaderTrainingEvents(2*67);
        hunterTraining.setMooselikeHuntingLeaderTrainingParticipants(68);
        hunterTraining.setNonSubsidizableMooselikeHuntingLeaderTrainingParticipants(2*68);
        hunterTraining.setCarnivoreHuntingLeaderTrainingEvents(69);
        hunterTraining.setNonSubsidizableCarnivoreHuntingLeaderTrainingEvents(2*69);
        hunterTraining.setCarnivoreHuntingLeaderTrainingParticipants(70);
        hunterTraining.setNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants(2*70);
        hunterTraining.setMooselikeHuntingTrainingEvents(71);
        hunterTraining.setNonSubsidizableMooselikeHuntingTrainingEvents(2*71);
        hunterTraining.setMooselikeHuntingTrainingParticipants(72);
        hunterTraining.setNonSubsidizableMooselikeHuntingTrainingParticipants(2*72);
        hunterTraining.setCarnivoreHuntingTrainingEvents(73);
        hunterTraining.setNonSubsidizableCarnivoreHuntingTrainingEvents(2*73);
        hunterTraining.setCarnivoreHuntingTrainingParticipants(74);
        hunterTraining.setNonSubsidizableCarnivoreHuntingTrainingParticipants(2*74);
        hunterTraining.setSrvaTrainingEvents(75);
        hunterTraining.setNonSubsidizableSrvaTrainingEvents(2*75);
        hunterTraining.setSrvaTrainingParticipants(76);
        hunterTraining.setNonSubsidizableSrvaTrainingParticipants(2*76);
        hunterTraining.setCarnivoreContactPersonTrainingEvents(77);
        hunterTraining.setNonSubsidizableCarnivoreContactPersonTrainingEvents(2*77);
        hunterTraining.setCarnivoreContactPersonTrainingParticipants(78);
        hunterTraining.setNonSubsidizableCarnivoreContactPersonTrainingParticipants(2*78);
        hunterTraining.setAccidentPreventionTrainingEvents(79);
        hunterTraining.setNonSubsidizableAccidentPreventionTrainingEvents(2*79);
        hunterTraining.setAccidentPreventionTrainingParticipants(80);
        hunterTraining.setNonSubsidizableAccidentPreventionTrainingParticipants(2*80);

        final YouthTrainingStatistics youthTraining = statistics.getOrCreateYouthTraining();
        youthTraining.setSchoolTrainingEvents(81);
        youthTraining.setNonSubsidizableSchoolTrainingEvents(2*81);
        youthTraining.setSchoolTrainingParticipants(82);
        youthTraining.setNonSubsidizableSchoolTrainingParticipants(2*82);
        youthTraining.setCollegeTrainingEvents(83);
        youthTraining.setNonSubsidizableCollegeTrainingEvents(2*83);
        youthTraining.setCollegeTrainingParticipants(84);
        youthTraining.setNonSubsidizableCollegeTrainingParticipants(2*84);
        youthTraining.setOtherYouthTargetedTrainingEvents(85);
        youthTraining.setNonSubsidizableOtherYouthTargetedTrainingEvents(2*85);
        youthTraining.setOtherYouthTargetedTrainingParticipants(86);
        youthTraining.setNonSubsidizableOtherYouthTargetedTrainingParticipants(2*86);

        final OtherHunterTrainingStatistics otherHunterTraining = statistics.getOrCreateOtherHunterTraining();
        otherHunterTraining.setSmallCarnivoreHuntingTrainingEvents(87);
        otherHunterTraining.setNonSubsidizableSmallCarnivoreHuntingTrainingEvents(2*87);
        otherHunterTraining.setSmallCarnivoreHuntingTrainingParticipants(88);
        otherHunterTraining.setNonSubsidizableSmallCarnivoreHuntingTrainingParticipants(2*88);
        otherHunterTraining.setGameCountingTrainingEvents(89);
        otherHunterTraining.setNonSubsidizableGameCountingTrainingEvents(2*89);
        otherHunterTraining.setGameCountingTrainingParticipants(90);
        otherHunterTraining.setNonSubsidizableGameCountingTrainingParticipants(2*90);
        otherHunterTraining.setGamePopulationManagementTrainingEvents(91);
        otherHunterTraining.setNonSubsidizableGamePopulationManagementTrainingEvents(2*91);
        otherHunterTraining.setGamePopulationManagementTrainingParticipants(92);
        otherHunterTraining.setNonSubsidizableGamePopulationManagementTrainingParticipants(2*92);
        otherHunterTraining.setGameEnvironmentalCareTrainingEvents(93);
        otherHunterTraining.setNonSubsidizableGameEnvironmentalCareTrainingEvents(2*93);
        otherHunterTraining.setGameEnvironmentalCareTrainingParticipants(94);
        otherHunterTraining.setNonSubsidizableGameEnvironmentalCareTrainingParticipants(2*94);
        otherHunterTraining.setOtherGamekeepingTrainingEvents(95);
        otherHunterTraining.setNonSubsidizableOtherGamekeepingTrainingEvents(2*95);
        otherHunterTraining.setOtherGamekeepingTrainingParticipants(96);
        otherHunterTraining.setNonSubsidizableOtherGamekeepingTrainingParticipants(2*96);
        otherHunterTraining.setShootingTrainingEvents(97);
        otherHunterTraining.setNonSubsidizableShootingTrainingEvents(2*97);
        otherHunterTraining.setShootingTrainingParticipants(98);
        otherHunterTraining.setNonSubsidizableShootingTrainingParticipants(2*98);
        otherHunterTraining.setTrackerTrainingEvents(99);
        otherHunterTraining.setNonSubsidizableTrackerTrainingEvents(2*99);
        otherHunterTraining.setTrackerTrainingParticipants(100);
        otherHunterTraining.setNonSubsidizableTrackerTrainingParticipants(2*100);

        final PublicEventStatistics publicEvents = statistics.getOrCreatePublicEvents();
        publicEvents.setPublicEvents(101);
        publicEvents.setPublicEventParticipants(102);

        final OtherHuntingRelatedStatistics otherHuntingRelated = statistics.getOrCreateOtherHuntingRelated();
        otherHuntingRelated.setHarvestPermitApplicationPartners(103);
        if (year >= 2018) {
            otherHuntingRelated.setMooselikeTaxationPlanningEvents(104);
        }
        otherHuntingRelated.setWolfTerritoryWorkgroups(105);

        final CommunicationStatistics communication = statistics.getOrCreateCommunication();
        communication.setInterviews(106);
        communication.setAnnouncements(107);
        communication.setOmariistaAnnouncements(108);
        communication.setHomePage("homepage");
        communication.setSomeInfo("social media");

        final ShootingRangeStatistics shootingRanges = statistics.getOrCreateShootingRanges();
        shootingRanges.setMooseRanges(109);
        shootingRanges.setShotgunRanges(110);
        shootingRanges.setRifleRanges(111);
        shootingRanges.setOtherShootingRanges(112);

        final LukeStatistics luke = statistics.getOrCreateLuke();
        luke.setWinterGameTriangles(113);
        luke.setSummerGameTriangles(114);
        luke.setFieldTriangles(115);
        luke.setWaterBirdCouples(116);
        if (year >= 2018) {
            luke.setWaterBirdBroods(117);
            luke.setCarnivoreContactPersons(118);
        }
        if (year >= 2019) {
            luke.setNorthernLaplandWillowGrouseLines(119);
            luke.setCarnivoreDnaCollectors(120);
        }

        final MetsahallitusStatistics metsahallitus = statistics.getOrCreateMetsahallitus();
        metsahallitus.setSmallGameLicensesSoldByMetsahallitus(121);
    }

    default void populateWithMatchingSubsidyTotalQuantities(@Nonnull final RhyAnnualStatistics statistics,
                                                            final Integer i) {

        populateWithMatchingSubsidyTotalQuantities(statistics, i, i, i, i, i, i, i, i, i, i, i);
    }

    default void populateAllWithMatchingSubsidyTotalQuantities(@Nonnull final RhyAnnualStatistics statistics,
                                                               final Integer i) {

        populateAllWithMatchingSubsidyTotalQuantities(statistics, i, i, i, i, i, i, i, i, i, i, i);
    }

    default void populateWithMatchingSubsidyTotalQuantities(@Nonnull final RhyAnnualStatistics statistics,
                                                            final Integer rhyMembers,
                                                            final Integer hunterExamTrainingEvents,
                                                            final Integer otherTrainingEvents,
                                                            final Integer studentAndYouthTrainingEvents,
                                                            final Integer huntingControlEvents,
                                                            final Integer sumOfLukeCalculations,
                                                            final Integer lukeCarnivoreContactPersons,
                                                            final Integer mooselikeTaxationPlanningEvents,
                                                            final Integer wolfTerritoryWorkgroups,
                                                            final Integer srvaMooselikeEvents,
                                                            final Integer soldMhLicenses) {

        final HunterTrainingStatistics hunterTraining = new HunterTrainingStatistics();
        hunterTraining.setMooselikeHuntingTrainingEvents(otherTrainingEvents);
        hunterTraining.setNonSubsidizableMooselikeHuntingTrainingEvents(mapNullable(otherTrainingEvents, i->i*2));

        final YouthTrainingStatistics youthTraining = new YouthTrainingStatistics();
        youthTraining.setSchoolTrainingEvents(studentAndYouthTrainingEvents);
        youthTraining.setNonSubsidizableSchoolTrainingEvents(mapNullable(studentAndYouthTrainingEvents, i->i*2));

        final SrvaEventStatistics srva = new SrvaEventStatistics();
        srva.getAccident().setMooses(srvaMooselikeEvents);

        final LukeStatistics luke = new LukeStatistics();
        luke.setWinterGameTriangles(sumOfLukeCalculations);
        luke.setCarnivoreContactPersons(lukeCarnivoreContactPersons);

        statistics.getOrCreateBasicInfo().setRhyMembers(rhyMembers);
        statistics.getOrCreateHunterExamTraining().setHunterExamTrainingEvents(hunterExamTrainingEvents);
        statistics.setJhtTraining(new JHTTrainingStatistics());
        statistics.setHunterTraining(hunterTraining);
        statistics.setYouthTraining(youthTraining);
        statistics.setOtherHunterTraining(new OtherHunterTrainingStatistics());
        statistics.getOrCreateHuntingControl().setHuntingControlEvents(huntingControlEvents);
        statistics.setLuke(luke);
        statistics.getOrCreateOtherHuntingRelated().setMooselikeTaxationPlanningEvents(mooselikeTaxationPlanningEvents);
        statistics.getOrCreateOtherHuntingRelated().setWolfTerritoryWorkgroups(wolfTerritoryWorkgroups);
        statistics.setSrva(srva);
        statistics.getOrCreateMetsahallitus().setSmallGameLicensesSoldByMetsahallitus(soldMhLicenses);

    }

    default void populateAllWithMatchingSubsidyTotalQuantities(@Nonnull final RhyAnnualStatistics statistics,
                                                               final Integer rhyMembers,
                                                               final Integer hunterExamTrainingEvents,
                                                               final Integer hunterTrainingEvents,
                                                               final Integer studentAndYouthTrainingEvents,
                                                               final Integer huntingControlEvents,
                                                               final Integer lukeCalculations,
                                                               final Integer lukeCarnivoreContactPersons,
                                                               final Integer mooselikeTaxationPlanningEvents,
                                                               final Integer wolfTerritoryWorkgroups,
                                                               final Integer srvaMooselikeEvents,
                                                               final Integer soldMhLicenses) {
        statistics.getOrCreateBasicInfo().setRhyMembers(rhyMembers);

        final HunterTrainingStatistics hunterTraining = statistics.getOrCreateHunterTraining();
        hunterTraining.setMooselikeHuntingTrainingEvents(hunterTrainingEvents);
        hunterTraining.setNonSubsidizableMooselikeHuntingTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        hunterTraining.setMooselikeHuntingLeaderTrainingParticipants(0);
        hunterTraining.setCarnivoreHuntingLeaderTrainingEvents(hunterTrainingEvents);
        hunterTraining.setNonSubsidizableCarnivoreHuntingLeaderTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        hunterTraining.setCarnivoreHuntingLeaderTrainingParticipants(0);
        hunterTraining.setMooselikeHuntingTrainingParticipants(0);
        hunterTraining.setCarnivoreHuntingTrainingEvents(hunterTrainingEvents);
        hunterTraining.setNonSubsidizableCarnivoreHuntingTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        hunterTraining.setCarnivoreHuntingTrainingParticipants(0);
        hunterTraining.setSrvaTrainingEvents(hunterTrainingEvents);
        hunterTraining.setSrvaTrainingParticipants(0);
        hunterTraining.setCarnivoreContactPersonTrainingEvents(hunterTrainingEvents);
        hunterTraining.setNonSubsidizableCarnivoreContactPersonTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        hunterTraining.setCarnivoreContactPersonTrainingParticipants(0);
        hunterTraining.setAccidentPreventionTrainingEvents(hunterTrainingEvents);
        hunterTraining.setAccidentPreventionTrainingParticipants(0);

        final YouthTrainingStatistics youthTraining = statistics.getOrCreateYouthTraining();
        youthTraining.setSchoolTrainingEvents(studentAndYouthTrainingEvents);
        youthTraining.setNonSubsidizableSchoolTrainingEvents(mapNullable(studentAndYouthTrainingEvents, i->i*2));
        youthTraining.setSchoolTrainingParticipants(0);
        youthTraining.setCollegeTrainingEvents(studentAndYouthTrainingEvents);
        youthTraining.setNonSubsidizableCollegeTrainingEvents(mapNullable(studentAndYouthTrainingEvents, i->i*2));
        youthTraining.setCollegeTrainingParticipants(0);
        youthTraining.setOtherYouthTargetedTrainingEvents(studentAndYouthTrainingEvents);
        youthTraining.setNonSubsidizableOtherYouthTargetedTrainingEvents(mapNullable(studentAndYouthTrainingEvents, i->i*2));
        youthTraining.setOtherYouthTargetedTrainingParticipants(0);

        final SrvaEventStatistics srva = statistics.getOrCreateSrva();
        srva.getAccident().setMooses(srvaMooselikeEvents);
        srva.getAccident().setWhiteTailedDeers(srvaMooselikeEvents);
        srva.getAccident().setRoeDeers(srvaMooselikeEvents);
        srva.getAccident().setFallowDeers(srvaMooselikeEvents);
        srva.getDeportation().setMooses(srvaMooselikeEvents);
        srva.getDeportation().setWhiteTailedDeers(srvaMooselikeEvents);
        srva.getDeportation().setRoeDeers(srvaMooselikeEvents);
        srva.getDeportation().setFallowDeers(srvaMooselikeEvents);
        srva.getInjury().setMooses(srvaMooselikeEvents);
        srva.getInjury().setWhiteTailedDeers(srvaMooselikeEvents);
        srva.getInjury().setRoeDeers(srvaMooselikeEvents);
        srva.getInjury().setFallowDeers(srvaMooselikeEvents);

        final LukeStatistics luke = statistics.getOrCreateLuke();
        luke.setWinterGameTriangles(lukeCalculations);
        luke.setCarnivoreContactPersons(lukeCarnivoreContactPersons);
        luke.setCarnivoreDnaCollectors(lukeCarnivoreContactPersons);
        luke.setSummerGameTriangles(lukeCalculations);
        luke.setFieldTriangles(lukeCalculations);
        luke.setWaterBirdBroods(lukeCalculations);
        luke.setWaterBirdCouples(lukeCalculations);
        luke.setNorthernLaplandWillowGrouseLines(lukeCalculations);

        statistics.getOrCreateHunterExamTraining().setHunterExamTrainingEvents(hunterExamTrainingEvents);

        final JHTTrainingStatistics jhtTraining = statistics.getOrCreateJhtTraining();
        jhtTraining.setGameDamageTrainingEvents(hunterTrainingEvents);
        jhtTraining.setNonSubsidizableGameDamageTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        jhtTraining.setGameDamageTrainingParticipants(0);
        jhtTraining.setHunterExamOfficialTrainingEvents(hunterTrainingEvents);
        jhtTraining.setNonSubsidizableHunterExamOfficialTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        jhtTraining.setHunterExamOfficialTrainingParticipants(0);
        jhtTraining.setHuntingControlTrainingEvents(hunterTrainingEvents);
        jhtTraining.setNonSubsidizableHuntingControlTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        jhtTraining.setHuntingControlTrainingParticipants(0);
        jhtTraining.setShootingTestTrainingEvents(hunterTrainingEvents);
        jhtTraining.setNonSubsidizableShootingTestTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        jhtTraining.setShootingTestTrainingParticipants(0);

        final OtherHunterTrainingStatistics otherHunterTraining = statistics.getOrCreateOtherHunterTraining();
        otherHunterTraining.setSmallCarnivoreHuntingTrainingEvents(hunterTrainingEvents);
        otherHunterTraining.setNonSubsidizableSmallCarnivoreHuntingTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        otherHunterTraining.setSmallCarnivoreHuntingTrainingParticipants(0);
        otherHunterTraining.setGameCountingTrainingEvents(hunterTrainingEvents);
        otherHunterTraining.setNonSubsidizableGameCountingTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        otherHunterTraining.setGameCountingTrainingParticipants(0);
        otherHunterTraining.setGamePopulationManagementTrainingEvents(hunterTrainingEvents);
        otherHunterTraining.setNonSubsidizableGamePopulationManagementTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        otherHunterTraining.setGamePopulationManagementTrainingParticipants(0);
        otherHunterTraining.setGameEnvironmentalCareTrainingEvents(hunterTrainingEvents);
        otherHunterTraining.setNonSubsidizableGameEnvironmentalCareTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        otherHunterTraining.setGameEnvironmentalCareTrainingParticipants(0);
        otherHunterTraining.setOtherGamekeepingTrainingEvents(hunterTrainingEvents);
        otherHunterTraining.setNonSubsidizableOtherGamekeepingTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        otherHunterTraining.setOtherGamekeepingTrainingParticipants(0);
        otherHunterTraining.setShootingTrainingEvents(hunterTrainingEvents);
        otherHunterTraining.setNonSubsidizableShootingTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        otherHunterTraining.setShootingTrainingParticipants(0);
        otherHunterTraining.setTrackerTrainingEvents(hunterTrainingEvents);
        otherHunterTraining.setNonSubsidizableTrackerTrainingEvents(mapNullable(hunterTrainingEvents, i->i*2));
        otherHunterTraining.setTrackerTrainingParticipants(0);

        statistics.getOrCreateHuntingControl().setHuntingControlEvents(huntingControlEvents);
        statistics.getOrCreateOtherHuntingRelated().setMooselikeTaxationPlanningEvents(mooselikeTaxationPlanningEvents);
        statistics.getOrCreateOtherHuntingRelated().setWolfTerritoryWorkgroups(wolfTerritoryWorkgroups);
        statistics.getOrCreateMetsahallitus().setSmallGameLicensesSoldByMetsahallitus(soldMhLicenses);
    }
}
