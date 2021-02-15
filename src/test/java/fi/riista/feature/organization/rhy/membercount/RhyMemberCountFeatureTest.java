package fi.riista.feature.organization.rhy.membercount;


import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.IntStream;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.currentYear;
import static fi.riista.util.DateUtil.toDateNullSafe;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class RhyMemberCountFeatureTest extends EmbeddedDatabaseTest implements OrganisationFixtureMixin {

    @Resource
    private RhyMemberCountFeature feature;

    @Resource
    private RhyAnnualStatisticsRepository statisticsRepository;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testUpdateMemberCounts_beginOfJanuary() {
        final int currentYear = currentYear();
        final LocalDate mockedDate = new LocalDate(currentYear, 1, 16);
        MockTimeProvider.mockTime(toDateNullSafe(mockedDate).getTime());

        withRhy(rhy -> {
            createMembersFor(rhy, 5);

            onSavedAndAuthenticated(createNewAdmin(), feature::updateMemberCounts);

            runInTransaction(() -> {
                final List<RhyAnnualStatistics> all = statisticsRepository.findAll();
                assertThat(all, hasSize(1));

                final RhyAnnualStatistics statistics = all.get(0);
                assertThat(statistics.getRhy(), equalTo(rhy));
                assertThat(statistics.getYear(), equalTo(currentYear - 1));
                assertThat(statistics.getOrCreateBasicInfo().getRhyMembers(), equalTo(5));
            });
        });

    }

    @Test
    public void testUpdateMemberCounts_afterBeginOfJanuary() {
        final LocalDate mockedDate = new LocalDate(currentYear(), 1, 17);
        MockTimeProvider.mockTime(toDateNullSafe(mockedDate).getTime());

        withRhy(rhy -> {
            createMembersFor(rhy, 5);
            onSavedAndAuthenticated(createNewAdmin(), feature::updateMemberCounts);

            runInTransaction(() -> {
                final List<RhyAnnualStatistics> all = statisticsRepository.findAll();
                assertThat(all, is(empty()));
            });
        });
    }

    @Test
    public void testUpdateMemberCounts_updatesMemberCountToZero() {
        final int currentYear = currentYear();
        final LocalDate mockedDate = new LocalDate(currentYear, 1, 15);
        MockTimeProvider.mockTime(toDateNullSafe(mockedDate).getTime());

        withRhy(rhy -> {
            final int statisticsYear = currentYear - 1;
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy, statisticsYear);
            statistics.getOrCreateBasicInfo().setRhyMembers(50);

            onSavedAndAuthenticated(createNewAdmin(), feature::updateMemberCounts);

            runInTransaction(() -> {
                final List<RhyAnnualStatistics> all = statisticsRepository.findAll();
                assertThat(all, hasSize(1));

                final RhyAnnualStatistics updatedStatistics = all.get(0);
                assertThat(updatedStatistics.getRhy(), equalTo(rhy));
                assertThat(updatedStatistics.getYear(), equalTo(statisticsYear));
                assertThat(updatedStatistics.getOrCreateBasicInfo().getRhyMembers(), equalTo(0));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdateMemberCounts_unauthorizedUser() {
        onSavedAndAuthenticated(createNewUser(), feature::updateMemberCounts);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdateMemberCounts_unauthorizedModerator() {
        onSavedAndAuthenticated(createNewModerator(), feature::updateMemberCounts);
    }

    private void createMembersFor(final Riistanhoitoyhdistys rhy, final int memberCount) {
        IntStream.range(0, memberCount).forEach(notUsed -> createMember(rhy));
    }

    private void createMember(final Riistanhoitoyhdistys rhy) {
        final int currentYear = currentYear();
        final Person person = model().newPerson(rhy);
        person.setRhyMembershipForStatistics(rhy);
        person.setHuntingPaymentOneYear(currentYear - 1);
        person.setHuntingPaymentOneDay(today().minusMonths(5));
    }

}
