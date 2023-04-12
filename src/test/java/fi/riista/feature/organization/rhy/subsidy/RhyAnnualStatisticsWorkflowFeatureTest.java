package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.feature.organization.rhy.annualstats.statechange.IllegalRhyAnnualStatisticsStateTransitionException;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsWorkflowFeature;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.IN_PROGRESS;
import static org.junit.Assert.assertThrows;

public class RhyAnnualStatisticsWorkflowFeatureTest
        extends EmbeddedDatabaseTest
        implements RhyAnnualStatisticsTestDataPopulator, OrganisationFixtureMixin {

    @Resource
    private RhyAnnualStatisticsWorkflowFeature feature;

    @Test
    public void testSubmitForInspection_success() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                feature.submitForInspection(dto);
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroMooselikeHuntingLeaderTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateHunterTraining().setMooselikeHuntingLeaderTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroCarnivoreHuntingLeaderTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateHunterTraining().setCarnivoreHuntingLeaderTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroMooselikeHuntingTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);

            annualStatistics.getOrCreateHunterTraining().setMooselikeHuntingTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroCarnivoreHuntingTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateHunterTraining().setCarnivoreHuntingTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroSrvaTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateHunterTraining().setSrvaTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroCarnivoreContactPersonTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateHunterTraining().setCarnivoreContactPersonTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroAccidentPreventionTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateHunterTraining().setAccidentPreventionTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroSchoolTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateYouthTraining().setSchoolTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroCollegeTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateYouthTraining().setCollegeTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroOtherYouthTargetedTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateYouthTraining().setOtherYouthTargetedTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroGameDamageTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateJhtTraining().setGameDamageTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroHunterExamOfficialTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateJhtTraining().setHunterExamOfficialTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroHuntingControlTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateJhtTraining().setHuntingControlTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroShootingTestTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateJhtTraining().setShootingTestTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroSmallCarnivoreHuntingTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateOtherHunterTraining().setSmallCarnivoreHuntingTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroGameCountingTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateOtherHunterTraining().setGameCountingTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroGamePopulationManagementTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateOtherHunterTraining().setGamePopulationManagementTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroGameEnvironmentalCareTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateOtherHunterTraining().setGameEnvironmentalCareTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroOtherGamekeepingTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateOtherHunterTraining().setOtherGamekeepingTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroShootingTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateOtherHunterTraining().setShootingTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    @Test
    public void testSubmitForInspection_zeroTrackerTrainingParticipants() {
        final int year = 2020;
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);
            annualStatistics.getOrCreateOtherHunterTraining().setTrackerTrainingParticipants(0);
            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final IdRevisionDTO dto = new IdRevisionDTO();
                dto.setId(annualStatistics.getId());
                dto.setRev(annualStatistics.getConsistencyVersion());

                assertThrows(IllegalRhyAnnualStatisticsStateTransitionException.class, () -> feature.submitForInspection(dto));
            });
        });
    }

    private RhyAnnualStatistics populateStatistics(final Riistanhoitoyhdistys rhy, final int i, final int i2) {
        final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy, i);
        populateAllWithMatchingSubsidyTotalQuantities(statistics, i2);
        statistics.setState(IN_PROGRESS);
        return statistics;
    }

}
